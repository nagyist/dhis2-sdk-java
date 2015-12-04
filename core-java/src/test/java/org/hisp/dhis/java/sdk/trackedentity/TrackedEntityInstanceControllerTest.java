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
package org.hisp.dhis.java.sdk.trackedentity;

import org.hisp.dhis.java.sdk.common.IFailedItemStore;
import org.hisp.dhis.java.sdk.common.IStateStore;
import org.hisp.dhis.java.sdk.common.network.ApiException;
import org.hisp.dhis.java.sdk.common.network.Response;
import org.hisp.dhis.java.sdk.common.persistence.ITransactionManager;
import org.hisp.dhis.java.sdk.common.preferences.ILastUpdatedPreferences;
import org.hisp.dhis.java.sdk.enrollment.IEnrollmentController;
import org.hisp.dhis.java.sdk.enrollment.IEnrollmentStore;
import org.hisp.dhis.java.sdk.models.common.faileditem.FailedItemType;
import org.hisp.dhis.java.sdk.models.common.importsummary.ImportSummary;
import org.hisp.dhis.java.sdk.models.common.state.Action;
import org.hisp.dhis.java.sdk.models.enrollment.Enrollment;
import org.hisp.dhis.java.sdk.models.trackedentity.TrackedEntityInstance;
import org.hisp.dhis.java.sdk.relationship.IRelationshipStore;
import org.hisp.dhis.java.sdk.systeminfo.ISystemInfoApiClient;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.*;
import static org.powermock.api.mockito.PowerMockito.doNothing;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.verifyStatic;


public class TrackedEntityInstanceControllerTest {

    private ITrackedEntityInstanceApiClient trackedEntityInstanceApiClient;
    private IStateStore stateStore;
    private ITrackedEntityInstanceStore trackedEntityInstanceStore;
    private TrackedEntityInstanceController trackedEntityInstanceController;
    private ISystemInfoApiClient systemInfoApiClient;
    private ILastUpdatedPreferences lastUpdatedPreferences;
    private ITransactionManager transactionManager;
    private IEnrollmentController enrollmentController;
    private IFailedItemStore failedItemStore;
    private IRelationshipStore relationshipStore;
    private ITrackedEntityAttributeValueStore trackedEntityAttributeValueStore;
    private IEnrollmentStore enrollmentStore;

    private static final long TRACKED_ENTITY_INSTANCE_ID = 43;
    TrackedEntityInstance trackedEntityInstance = new TrackedEntityInstance();

    @Before
    public void setUp() {
        trackedEntityInstanceApiClient = mock(ITrackedEntityInstanceApiClient.class);
        stateStore = mock(IStateStore.class);
        trackedEntityInstanceStore = mock(ITrackedEntityInstanceStore.class);
        systemInfoApiClient = mock(ISystemInfoApiClient.class);
        lastUpdatedPreferences = mock(ILastUpdatedPreferences.class);
        transactionManager = mock(ITransactionManager.class);
        enrollmentController = mock(IEnrollmentController.class);
        failedItemStore = mock(IFailedItemStore.class);
        relationshipStore = mock(IRelationshipStore.class);
        trackedEntityAttributeValueStore = mock(ITrackedEntityAttributeValueStore.class);
        enrollmentStore = mock(IEnrollmentStore.class);
        trackedEntityInstanceController = spy(new TrackedEntityInstanceController(trackedEntityInstanceApiClient,
                systemInfoApiClient, trackedEntityInstanceStore, lastUpdatedPreferences, transactionManager,
                enrollmentController, stateStore, failedItemStore, relationshipStore, trackedEntityAttributeValueStore,
                enrollmentStore));

        trackedEntityInstance.setId(TRACKED_ENTITY_INSTANCE_ID);
    }

    @Test
    public void testPutTrackedEntityInstanceWithSuccessImportSummary() {
        ImportSummary importSummary = new ImportSummary();
        when(trackedEntityInstanceApiClient.putTrackedEntityInstance(trackedEntityInstance)).thenReturn(importSummary);
        doNothing().when(trackedEntityInstanceController).handleImportSummary(trackedEntityInstance, importSummary);
        trackedEntityInstanceController.putTrackedEntityInstance(trackedEntityInstance);

        verify(trackedEntityInstanceApiClient, times(1)).putTrackedEntityInstance(trackedEntityInstance);
        verify(trackedEntityInstanceController, times(1)).handleImportSummary(trackedEntityInstance, importSummary);
    }

    @Test
    public void testPutTrackedEntityInstanceWithThrownApiException() {
        Response response = new Response("", 200, "", new ArrayList<>(), null);
        ApiException apiException = ApiException.httpError("", response);
        when(trackedEntityInstanceApiClient.putTrackedEntityInstance(trackedEntityInstance)).
                thenThrow(apiException);
        doNothing().when(trackedEntityInstanceController).handleTrackedEntityInstanceSendException
                (apiException, failedItemStore, trackedEntityInstance);

        trackedEntityInstanceController.putTrackedEntityInstance(trackedEntityInstance);

        verify(trackedEntityInstanceController, times(1)).handleTrackedEntityInstanceSendException
                (apiException, failedItemStore, trackedEntityInstance);
    }

    @Test
    public void testPostTrackedEntityInstanceWithSuccessImportSummary() {
        ImportSummary importSummary = new ImportSummary();
        when(trackedEntityInstanceApiClient.postTrackedEntityInstance(trackedEntityInstance)).thenReturn(importSummary);
        doNothing().when(trackedEntityInstanceController).handleImportSummary(trackedEntityInstance, importSummary);
        trackedEntityInstanceController.postTrackedEntityInstance(trackedEntityInstance);

        verify(trackedEntityInstanceApiClient, times(1)).postTrackedEntityInstance(trackedEntityInstance);
        verify(trackedEntityInstanceController, times(1)).handleImportSummary(trackedEntityInstance, importSummary);
    }

    @Test
    public void testPostTrackedEntityInstanceWithThrownApiException() {
        Response response = new Response("", 200, "", new ArrayList<>(), null);
        ApiException apiException = ApiException.httpError("", response);
        when(trackedEntityInstanceApiClient.postTrackedEntityInstance(trackedEntityInstance)).
                thenThrow(apiException);
        doNothing().when(trackedEntityInstanceController).handleTrackedEntityInstanceSendException
                (apiException, failedItemStore, trackedEntityInstance);

        trackedEntityInstanceController.postTrackedEntityInstance(trackedEntityInstance);

        verify(trackedEntityInstanceController, times(1)).handleTrackedEntityInstanceSendException
                (apiException, failedItemStore, trackedEntityInstance);
    }

    @Test
    public void testHandleImportSummaryWithSuccessImportSummary() {
        ImportSummary importSummary = new ImportSummary();
        importSummary.setStatus(ImportSummary.Status.SUCCESS);
        doNothing().when(trackedEntityInstanceController).updateTrackedEntityInstanceTimestamp(trackedEntityInstance);

        trackedEntityInstanceController.handleImportSummary(trackedEntityInstance, importSummary);

        verify(stateStore, times(1)).saveActionForModel(trackedEntityInstance, Action.SYNCED);
        verify(trackedEntityInstanceController, times(1)).updateTrackedEntityInstanceTimestamp(trackedEntityInstance);
        verify(trackedEntityInstanceController, times(1)).clearFailedItem(FailedItemType.TRACKED_ENTITY_INSTANCE,
                failedItemStore, TRACKED_ENTITY_INSTANCE_ID);
    }

    @Test
    public void testHandleImportSummaryWithOkImportSummary() {
        ImportSummary importSummary = new ImportSummary();
        importSummary.setStatus(ImportSummary.Status.OK);
        doNothing().when(trackedEntityInstanceController).updateTrackedEntityInstanceTimestamp(trackedEntityInstance);
        doNothing().when(trackedEntityInstanceController).clearFailedItem(FailedItemType.TRACKED_ENTITY_INSTANCE,
                failedItemStore, TRACKED_ENTITY_INSTANCE_ID);

        trackedEntityInstanceController.handleImportSummary(trackedEntityInstance, importSummary);

        verify(stateStore, times(1)).saveActionForModel(trackedEntityInstance, Action.SYNCED);
        verify(trackedEntityInstanceController, times(1)).updateTrackedEntityInstanceTimestamp(trackedEntityInstance);
        verify(trackedEntityInstanceController, times(1)).clearFailedItem(FailedItemType.TRACKED_ENTITY_INSTANCE,
                failedItemStore, TRACKED_ENTITY_INSTANCE_ID);
    }

    @Test
    public void testHandleImportSummaryWithErrorImportSummary() {
        ImportSummary importSummary = new ImportSummary();
        importSummary.setStatus(ImportSummary.Status.ERROR);
        doNothing().when(trackedEntityInstanceController).handleImportSummaryWithError(importSummary, failedItemStore,
                FailedItemType.TRACKED_ENTITY_INSTANCE, TRACKED_ENTITY_INSTANCE_ID);

        trackedEntityInstanceController.handleImportSummary(trackedEntityInstance, importSummary);

        verify(trackedEntityInstanceController, times(1)).handleImportSummaryWithError(importSummary, failedItemStore,
                FailedItemType.TRACKED_ENTITY_INSTANCE, TRACKED_ENTITY_INSTANCE_ID);
    }

    @Test
    public void testUpdateTrackedEntityInstanceTimestamp() {
        TrackedEntityInstance updatedTrackedEntityInstance = new TrackedEntityInstance();
        DateTime created = new DateTime();
        DateTime lastUpdated = new DateTime();
        updatedTrackedEntityInstance.setCreated(created);
        updatedTrackedEntityInstance.setLastUpdated(lastUpdated);
        when(trackedEntityInstanceApiClient.getFullTrackedEntityInstance
                (trackedEntityInstance.getTrackedEntityInstanceUid(), null)).thenReturn(updatedTrackedEntityInstance);

        trackedEntityInstanceController.updateTrackedEntityInstanceTimestamp(trackedEntityInstance);

        verify(trackedEntityInstanceApiClient, times(1)).getFullTrackedEntityInstance(trackedEntityInstance.getTrackedEntityInstanceUid(), null);
        assertEquals(created, trackedEntityInstance.getCreated());
        assertEquals(lastUpdated, trackedEntityInstance.getLastUpdated());
        verify(trackedEntityInstanceStore, times(1)).save(trackedEntityInstance);
    }

    @Test
    public void testUpdateTrackedEntityInstanceTimestampWithApiException() {
        Response response = new Response("", 200, "", new ArrayList<>(), null);
        ApiException apiException = ApiException.httpError("", response);
        when(trackedEntityInstanceApiClient.getFullTrackedEntityInstance(trackedEntityInstance.
                getTrackedEntityInstanceUid(), null)).thenThrow(apiException);
        trackedEntityInstanceController.updateTrackedEntityInstanceTimestamp(trackedEntityInstance);
        assertNotNull(trackedEntityInstance.getLastUpdated());
        verify(trackedEntityInstanceStore).save(trackedEntityInstance);
    }

    @Test
    public void testSendTrackedEntityInstanceChangesToPost() {
        Action action = Action.TO_POST;
        doNothing().when(trackedEntityInstanceController).postTrackedEntityInstance(trackedEntityInstance);
        trackedEntityInstanceController.sendTrackedEntityInstanceChanges(trackedEntityInstance, action, true);
        verify(trackedEntityInstanceController).postTrackedEntityInstance(trackedEntityInstance);
    }

    @Test
    public void testSendTrackedEntityInstanceChangesToUpdate() {
        Action action = Action.TO_UPDATE;
        doNothing().when(trackedEntityInstanceController).putTrackedEntityInstance(trackedEntityInstance);
        trackedEntityInstanceController.sendTrackedEntityInstanceChanges(trackedEntityInstance, action, true);
        verify(trackedEntityInstanceController).putTrackedEntityInstance(trackedEntityInstance);
    }

    @Test
    public void testSendTrackedEntityInstanceChangesWithEnrollments() {
        Enrollment enrollment1 = new Enrollment();
        Enrollment enrollment2 = new Enrollment();
        List<Enrollment> enrollmentList = new ArrayList<>();
        enrollmentList.add(enrollment1);
        enrollmentList.add(enrollment2);
        when(enrollmentStore.query(trackedEntityInstance)).thenReturn(enrollmentList);
        doNothing().when(trackedEntityInstanceController).putTrackedEntityInstance(trackedEntityInstance);

        trackedEntityInstanceController.sendTrackedEntityInstanceChanges(trackedEntityInstance, null, true);

        verify(enrollmentController).sendEnrollmentChanges(enrollmentList);
    }

    @Test
    public void testSendTrackedEntityInstanceChangesWithoutEnrollments() {
        Enrollment enrollment1 = new Enrollment();
        Enrollment enrollment2 = new Enrollment();
        List<Enrollment> enrollmentList = new ArrayList<>();
        enrollmentList.add(enrollment1);
        enrollmentList.add(enrollment2);
        when(enrollmentStore.query(trackedEntityInstance)).thenReturn(enrollmentList);
        doNothing().when(trackedEntityInstanceController).putTrackedEntityInstance(trackedEntityInstance);

        trackedEntityInstanceController.sendTrackedEntityInstanceChanges(trackedEntityInstance, null, false);

        verify(enrollmentController, never()).sendEnrollmentChanges(enrollmentList);
    }

    @Test
    public void testSendTrackedEntityInstanceChangesListWithEnrollments() {
        TrackedEntityInstance trackedEntityInstance1 = new TrackedEntityInstance();
        trackedEntityInstance1.setId(1);
        TrackedEntityInstance trackedEntityInstance2 = new TrackedEntityInstance();
        trackedEntityInstance2.setId(2);
        List<TrackedEntityInstance> trackedEntityInstanceList = new ArrayList<>();
        trackedEntityInstanceList.add(trackedEntityInstance1);
        trackedEntityInstanceList.add(trackedEntityInstance2);

        Map<Long, Action> actionMap = new HashMap<>();
        actionMap.put(new Long(1), Action.TO_POST);
        actionMap.put(new Long(2), Action.TO_POST);

        when(stateStore.queryActionsForModel(TrackedEntityInstance.class)).thenReturn(actionMap);
        doNothing().when(trackedEntityInstanceController).sendTrackedEntityInstanceChanges(any(), any(), anyBoolean());

        trackedEntityInstanceController.sendTrackedEntityInstancesChanges(trackedEntityInstanceList, true);

        verify(trackedEntityInstanceController).sendTrackedEntityInstanceChanges(trackedEntityInstance1, Action.TO_POST, true);
        verify(trackedEntityInstanceController).sendTrackedEntityInstanceChanges(trackedEntityInstance2, Action.TO_POST, true);
    }

    @Test
    public void testSendTrackedEntityInstanceChangesListWithoutEnrollments() {
        TrackedEntityInstance trackedEntityInstance1 = new TrackedEntityInstance();
        trackedEntityInstance1.setId(1);
        TrackedEntityInstance trackedEntityInstance2 = new TrackedEntityInstance();
        trackedEntityInstance2.setId(2);
        List<TrackedEntityInstance> trackedEntityInstanceList = new ArrayList<>();
        trackedEntityInstanceList.add(trackedEntityInstance1);
        trackedEntityInstanceList.add(trackedEntityInstance2);

        Map<Long, Action> actionMap = new HashMap<>();
        actionMap.put(new Long(1), Action.TO_POST);
        actionMap.put(new Long(2), Action.TO_POST);

        when(stateStore.queryActionsForModel(TrackedEntityInstance.class)).thenReturn(actionMap);
        doNothing().when(trackedEntityInstanceController).sendTrackedEntityInstanceChanges(any(), any(), anyBoolean());

        trackedEntityInstanceController.sendTrackedEntityInstancesChanges(trackedEntityInstanceList, false);

        verify(trackedEntityInstanceController).sendTrackedEntityInstanceChanges(trackedEntityInstance1, Action.TO_POST, false);
        verify(trackedEntityInstanceController).sendTrackedEntityInstanceChanges(trackedEntityInstance2, Action.TO_POST, false);
    }

}
