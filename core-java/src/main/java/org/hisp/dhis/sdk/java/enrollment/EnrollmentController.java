/*
 * Copyright (c) 2015, University of Oslo
 *
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * Neither the name of the HISP project nor the names of its contributors may
 * be used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.hisp.dhis.sdk.java.enrollment;

import org.hisp.dhis.java.sdk.models.common.faileditem.FailedItemType;
import org.hisp.dhis.java.sdk.models.common.importsummary.ImportSummary;
import org.hisp.dhis.java.sdk.models.common.state.Action;
import org.hisp.dhis.java.sdk.models.enrollment.Enrollment;
import org.hisp.dhis.java.sdk.models.event.Event;
import org.hisp.dhis.java.sdk.models.trackedentity.TrackedEntityInstance;
import org.hisp.dhis.sdk.java.common.IFailedItemStore;
import org.hisp.dhis.sdk.java.common.IStateStore;
import org.hisp.dhis.sdk.java.common.controllers.PushableDataController;
import org.hisp.dhis.sdk.java.common.network.ApiException;
import org.hisp.dhis.sdk.java.common.persistence.DbOperation;
import org.hisp.dhis.sdk.java.common.persistence.IDbOperation;
import org.hisp.dhis.sdk.java.common.persistence.IStore;
import org.hisp.dhis.sdk.java.common.persistence.ITransactionManager;
import org.hisp.dhis.sdk.java.common.preferences.ILastUpdatedPreferences;
import org.hisp.dhis.sdk.java.common.preferences.ResourceType;
import org.hisp.dhis.sdk.java.event.IEventController;
import org.hisp.dhis.sdk.java.event.IEventStore;
import org.hisp.dhis.sdk.java.systeminfo.ISystemInfoApiClient;
import org.joda.time.DateTime;

import java.util.*;

public final class EnrollmentController extends PushableDataController implements IEnrollmentController {
    private final static String ENROLLMENTS = "enrollments";

    private final IEnrollmentApiClient enrollmentApiClient;
    private final ISystemInfoApiClient systemInfoApiClient;

    private final ILastUpdatedPreferences lastUpdatedPreferences;
    private final ITransactionManager transactionManager;

    private final IEventController eventController;
    private final IEnrollmentStore enrollmentStore;
    private final IEventStore eventStore;
    private final IStateStore stateStore;
    private final IFailedItemStore failedItemStore;

    public EnrollmentController(IEnrollmentApiClient apiClient, ISystemInfoApiClient systemInfoApiClient, ILastUpdatedPreferences preferences,
                                ITransactionManager transactionManager, IEventController eventController, IEnrollmentStore enrollmentStore,
                                IEventStore eventStore, IStateStore stateStore, IFailedItemStore failedItemStore) {
        this.enrollmentApiClient = apiClient;
        this.systemInfoApiClient = systemInfoApiClient;
        this.lastUpdatedPreferences = preferences;
        this.transactionManager = transactionManager;
        this.eventController = eventController;
        this.enrollmentStore = enrollmentStore;
        this.eventStore = eventStore;
        this.stateStore = stateStore;
        this.failedItemStore = failedItemStore;
    }

    private List<Enrollment> getEnrollmentsDataFromServer(TrackedEntityInstance trackedEntityInstance) throws ApiException {
        if (trackedEntityInstance == null) {
            return new ArrayList<>();
        }

        DateTime lastUpdated = lastUpdatedPreferences
                .get(ResourceType.ENROLLMENTS, trackedEntityInstance.getTrackedEntityInstanceUid());
        DateTime serverDateTime = systemInfoApiClient.getSystemInfo().getServerDate();

        List<Enrollment> allExistingEnrollments = enrollmentApiClient
                .getBasicEnrollments(trackedEntityInstance.getTrackedEntityInstanceUid(), null);
        List<Enrollment> updatedEnrollments = enrollmentApiClient
                .getFullEnrollments(trackedEntityInstance.getTrackedEntityInstanceUid(), lastUpdated);

        List<Enrollment> existingUpdatedAndPersistedEnrollments =
                merge(allExistingEnrollments, updatedEnrollments, enrollmentStore.query(trackedEntityInstance));
        for (Enrollment enrollment : existingUpdatedAndPersistedEnrollments) {
            enrollment.setTrackedEntityInstance(trackedEntityInstance);
        }

        saveResourceDataFromServer(ResourceType.ENROLLMENTS,
                trackedEntityInstance.getTrackedEntityInstanceUid(), enrollmentStore,
                existingUpdatedAndPersistedEnrollments, enrollmentStore.query(trackedEntityInstance), serverDateTime);

        for (Enrollment enrollment : existingUpdatedAndPersistedEnrollments) {
            try {
                eventController.getEventsDataFromServer(enrollment);
            } catch (ApiException e) {
                // can't throw this exception up because we want to
                // continue loading enrollments.. todo: let the user know?
                e.printStackTrace();
            }
        }
        return existingUpdatedAndPersistedEnrollments;
    }

    private Enrollment getEnrollmentDataFromServer(String uid, boolean getEvents) throws ApiException {
        DateTime lastUpdated = lastUpdatedPreferences.get(ResourceType.ENROLLMENT, uid);
        DateTime serverDateTime = systemInfoApiClient.getSystemInfo().getServerDate();

        Enrollment updatedEnrollment = enrollmentApiClient.getFullEnrollment(uid, lastUpdated);
        //todo: if the updatedEnrollment is deleted on the server, delete it also locally
        //todo: be sure to check if the enrollment has ever been on the server, or if it is still pending first time registration sync

        Enrollment persistedEnrollment = enrollmentStore.query(uid);
        if (updatedEnrollment.getEnrollmentUid() == null) {
            //either the uid provided was invalid, or the enrollment has not been updated since lastUpdated
            return persistedEnrollment;
        }
        if (persistedEnrollment != null) {
            updatedEnrollment.setId(persistedEnrollment.getId());
            if (updatedEnrollment.getLastUpdated().isAfter(persistedEnrollment.getLastUpdated())) {
                DbOperation.with(enrollmentStore).update(updatedEnrollment).execute();
            }
        } else {
            DbOperation.with(enrollmentStore).insert(updatedEnrollment).execute();
        }
        lastUpdatedPreferences.save(ResourceType.ENROLLMENT, serverDateTime, uid);
        if (getEvents) {
            eventController.getEventsDataFromServer(updatedEnrollment);
        }
        return updatedEnrollment;
    }

    private void saveResourceDataFromServer(ResourceType resourceType, String extraIdentifier,
                                            IStore<Enrollment> store, List<Enrollment> updatedItems,
                                            List<Enrollment> persistedItems, DateTime serverDateTime) {
        Queue<IDbOperation> operations = new LinkedList<>();
        operations.addAll(createOperations(store, persistedItems, updatedItems));
        transactionManager.transact(operations);
        lastUpdatedPreferences.save(resourceType, serverDateTime, extraIdentifier);
    }

    /**
     * This utility method allows to determine which type of operation to apply to
     * each BaseIdentifiableObject$Flow depending on TimeStamp.
     *
     * @param oldModels List of models from local storage.
     * @param newModels List of models of distance instance of DHIS.
     */
    private List<DbOperation> createOperations(IStore<Enrollment> store, List<Enrollment> oldModels,
                                               List<Enrollment> newModels) {
        List<DbOperation> ops = new ArrayList<>();

        Map<String, Enrollment> newModelsMap = toMap(newModels);
        Map<String, Enrollment> oldModelsMap = toMap(oldModels);

        // As we will go through map of persisted items, we will try to update existing data.
        // Also, during each iteration we will remove old model key from list of new models.
        // As the result, the list of remaining items in newModelsMap,
        // will contain only those items which were not inserted before.
        for (String oldModelKey : oldModelsMap.keySet()) {
            Enrollment newModel = newModelsMap.get(oldModelKey);
            Enrollment oldModel = oldModelsMap.get(oldModelKey);

            // if there is no particular model with given uid in list of
            // actual (up to date) items, it means it was removed on the server side
            if (newModel == null) {
                Action action = stateStore.queryActionForModel(oldModel);
                if (!Action.TO_POST.equals(action) && !Action.TO_UPDATE.equals(action)) {
                    ops.add(DbOperation.with(store)
                            .delete(oldModel));
                }

                // in case if there is no new model object,
                // we can jump to next iteration.
                continue;
            }

            // if the last updated field in up to date model is after the same
            // field in persisted model, it means we need to update it.
            if (newModel.getLastUpdated().isAfter(oldModel.getLastUpdated())) {
                // note, we need to pass database primary id to updated model
                // in order to avoid creation of new object.
                newModel.setId(oldModel.getId());
                ops.add(DbOperation.with(store)
                        .update(newModel));
            }

            // as we have processed given old (persisted) model,
            // we can remove it from map of new models.
            newModelsMap.remove(oldModelKey);
        }

        // Inserting new items.
        for (String newModelKey : newModelsMap.keySet()) {
            Enrollment item = newModelsMap.get(newModelKey);
            ops.add(DbOperation.with(enrollmentStore)
                    .insert(item));
        }

        return ops;
    }

    /**
     * Returns a list of items taken from updatedItems and persistedItems, based on the items in
     * the passed existingItems List. Items that are not present in existingItems will not be
     * included.
     *
     * @param existingItems
     * @param updatedItems
     * @param persistedItems
     * @return
     */
    private List<Enrollment> merge(List<Enrollment> existingItems,
                                   List<Enrollment> updatedItems,
                                   List<Enrollment> persistedItems) {
        Map<String, Enrollment> updatedItemsMap = toMap(updatedItems);
        Map<String, Enrollment> persistedItemsMap = toMap(persistedItems);
        Map<String, Enrollment> existingItemsMap = new HashMap<>();

        if (existingItems == null || existingItems.isEmpty()) {
            return new ArrayList<>(existingItemsMap.values());
        }

        for (Enrollment existingItem : existingItems) {
            String id = existingItem.getEnrollmentUid();
            Enrollment updatedItem = updatedItemsMap.get(id);
            Enrollment persistedItem = persistedItemsMap.get(id);

            if (updatedItem != null) {
                if (persistedItem != null) {
                    updatedItem.setId(persistedItem.getId());
                }
                existingItemsMap.put(id, updatedItem);
                continue;
            }

            if (persistedItem != null) {
                existingItemsMap.put(id, persistedItem);
            }
        }

        return new ArrayList<>(existingItemsMap.values());
    }

    private Map<String, Enrollment> toMap(Collection<Enrollment> objects) {
        Map<String, Enrollment> map = new HashMap<>();
        if (objects != null && objects.size() > 0) {
            for (Enrollment object : objects) {
                if (object.getEnrollmentUid() != null) {
                    map.put(object.getEnrollmentUid(), object);
                }
            }
        }
        return map;
    }

    private void sendEnrollmentChanges(boolean sendEvents) throws ApiException {
        List<Enrollment> enrollments = getLocallyChangedEnrollments();
        sendEnrollmentChanges(enrollments, sendEvents);
    }

    private List<Enrollment> getLocallyChangedEnrollments() {
        List<Enrollment> toPost = stateStore.filterModelsByAction(Enrollment.class, Action.TO_POST);
        List<Enrollment> toPut = stateStore.filterModelsByAction(Enrollment.class, Action.TO_UPDATE);
        List<Enrollment> enrollments = new ArrayList<>();
        enrollments.addAll(toPost);
        enrollments.addAll(toPut);
        return enrollments;
    }

    @Override
    public void sendEnrollmentChanges(List<Enrollment> enrollments, boolean sendEvents) throws ApiException {
        if (enrollments == null || enrollments.isEmpty()) {
            return;
        }

        Map<Long, Action> actionMap = stateStore
                .queryActionsForModel(Enrollment.class);


        for (int i = 0; i < enrollments.size(); i++) {/* workaround for not attempting to upload enrollments with local tei reference*/
            Enrollment enrollment = enrollments.get(i);
            Action trackedEntityInstanceAction = null;
            TrackedEntityInstance trackedEntityInstance = enrollment.getTrackedEntityInstance();
            if (trackedEntityInstance == null) {
                trackedEntityInstanceAction = stateStore.queryActionForModel(trackedEntityInstance);
            }

            //we avoid trying to send enrollments whose trackedEntityInstances that have not yet been posted to server
            if (Action.TO_POST.equals(trackedEntityInstanceAction)) {
                enrollments.remove(i);
                i--;
            }
        }
        for (Enrollment enrollment : enrollments) {
            sendEnrollmentChanges(enrollment, actionMap.get(enrollment.getId()), sendEvents);
        }
    }

    private void sendEnrollmentChanges(Enrollment enrollment, Action action, boolean sendEvents) throws ApiException {
        if (enrollment == null) {
            return;
        }
        Action trackedEntityInstanceAction = null;
        TrackedEntityInstance trackedEntityInstance = enrollment.getTrackedEntityInstance();
        if (trackedEntityInstance == null) {
            trackedEntityInstanceAction = stateStore.queryActionForModel(trackedEntityInstance);
        }

        //we avoid trying to send enrollments whose trackedEntityInstances that have not yet been posted to server
        if (Action.TO_POST.equals(trackedEntityInstanceAction)) {
            return;
        }

        if (Action.TO_POST.equals(action)) {
            postEnrollment(enrollment);
        } else {
            putEnrollment(enrollment);
        }
        if (sendEvents) {
            List<Event> events = eventStore.query(enrollment);
            eventController.sendEventChanges(events);
        }
    }

    private void postEnrollment(Enrollment enrollment) throws ApiException {
        try {
            ImportSummary importSummary = enrollmentApiClient.postEnrollment(enrollment);
            handleImportSummary(importSummary, failedItemStore, FailedItemType.ENROLLMENT, enrollment.getId());

            if (ImportSummary.Status.SUCCESS.equals(importSummary.getStatus()) ||
                    ImportSummary.Status.OK.equals(importSummary.getStatus())) {

                stateStore.saveActionForModel(enrollment, Action.SYNCED);
                enrollmentStore.save(enrollment);
                clearFailedItem(FailedItemType.ENROLLMENT, failedItemStore, enrollment.getId());
                UpdateEnrollmentTimestamp(enrollment);
            }
        } catch (ApiException apiException) {
            handleEnrollmentSendException(apiException, failedItemStore, enrollment);
        }
    }

    private void putEnrollment(Enrollment enrollment) throws ApiException {
        try {
            ImportSummary importSummary = enrollmentApiClient.putEnrollment(enrollment);
            handleImportSummary(importSummary, failedItemStore, FailedItemType.ENROLLMENT, enrollment.getId());

            if (ImportSummary.Status.SUCCESS.equals(importSummary.getStatus()) ||
                    ImportSummary.Status.OK.equals(importSummary.getStatus())) {

                stateStore.saveActionForModel(enrollment, Action.SYNCED);
                enrollmentStore.save(enrollment);
                clearFailedItem(FailedItemType.ENROLLMENT, failedItemStore, enrollment.getId());
                UpdateEnrollmentTimestamp(enrollment);
            }
        } catch (ApiException apiException) {
            handleEnrollmentSendException(apiException, failedItemStore, enrollment);
        }
    }

    private void UpdateEnrollmentTimestamp(Enrollment enrollment) throws ApiException {
        try {
            Enrollment updatedEnrollment = enrollmentApiClient
                    .getBasicEnrollment(enrollment.getEnrollmentUid(), null);

            // merging updated timestamp to local enrollment model
            enrollment.setCreated(updatedEnrollment.getCreated());
            enrollment.setLastUpdated(updatedEnrollment.getLastUpdated());
            enrollmentStore.save(enrollment);
        } catch (ApiException apiException) {
            // NetworkUtils.handleApiException(apiException);
        }
    }

    @Override
    public void sync() throws ApiException {
    }

    @Override
    public List<Enrollment> sync(TrackedEntityInstance trackedEntityInstance) throws ApiException {
        return getEnrollmentsDataFromServer(trackedEntityInstance);
    }

    @Override
    public Enrollment sync(String uid, boolean getEvents) throws ApiException {
        return getEnrollmentDataFromServer(uid, getEvents);
    }
}
