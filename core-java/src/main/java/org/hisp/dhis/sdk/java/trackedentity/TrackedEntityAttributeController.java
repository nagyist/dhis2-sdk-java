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

package org.hisp.dhis.sdk.java.trackedentity;

import org.hisp.dhis.java.sdk.models.trackedentity.TrackedEntityAttribute;
import org.hisp.dhis.sdk.java.common.controllers.ResourceController;
import org.hisp.dhis.sdk.java.common.network.ApiException;
import org.hisp.dhis.sdk.java.common.persistence.IIdentifiableObjectStore;
import org.hisp.dhis.sdk.java.common.preferences.ILastUpdatedPreferences;
import org.hisp.dhis.sdk.java.common.preferences.ResourceType;
import org.hisp.dhis.sdk.java.systeminfo.ISystemInfoApiClient;
import org.joda.time.DateTime;

import java.util.List;

import static org.hisp.dhis.java.sdk.models.common.base.BaseIdentifiableObject.merge;

public final class TrackedEntityAttributeController extends ResourceController<TrackedEntityAttribute> {

    private final static String TRACKED_ENTITY_ATTRIBUTES = "trackedEntityAttributes";
    private final IIdentifiableObjectStore<TrackedEntityAttribute> mTrackedEntityAttributeStore;
    private final ITrackedEntityApiClient trackedEntityApiClient;
    private final ILastUpdatedPreferences lastUpdatedPreferences;
    private final ISystemInfoApiClient systemInfoApiClient;

    public TrackedEntityAttributeController(IIdentifiableObjectStore<TrackedEntityAttribute> mTrackedEntityAttributeStore,
                                            ITrackedEntityApiClient trackedEntityApiClient,
                                            ILastUpdatedPreferences lastUpdatedPreferences,
                                            ISystemInfoApiClient systemInfoApiClient) {
        this.trackedEntityApiClient = trackedEntityApiClient;
        this.mTrackedEntityAttributeStore = mTrackedEntityAttributeStore;
        this.lastUpdatedPreferences = lastUpdatedPreferences;
        this.systemInfoApiClient = systemInfoApiClient;
    }

    private void getProgramRulesDataFromServer() throws ApiException {
        ResourceType resource = ResourceType.TRACKED_ENTITY_ATTRIBUTES;
        DateTime serverTime = systemInfoApiClient.getSystemInfo().getServerDate();
        DateTime lastUpdated = lastUpdatedPreferences.get(resource);

        //fetching id and name for all items on server. This is needed in case something is
        // deleted on the server and we want to reflect that locally
        List<TrackedEntityAttribute> allTrackedEntityAttributes =
                trackedEntityApiClient.getBasicTrackedEntityAttributes(null);

        //fetch all updated items
        List<TrackedEntityAttribute> updatedTrackedEntityAttributes =
                trackedEntityApiClient.getFullTrackedEntityAttributes(lastUpdated);

        //merging updated items with persisted items, and removing ones not present in server.
        List<TrackedEntityAttribute> existingPersistedAndUpdatedTrackedEntityAttributes =
                merge(allTrackedEntityAttributes, updatedTrackedEntityAttributes,
                        mTrackedEntityAttributeStore.queryAll());

        saveResourceDataFromServer(resource, mTrackedEntityAttributeStore,
                existingPersistedAndUpdatedTrackedEntityAttributes, mTrackedEntityAttributeStore.queryAll(),
                serverTime);
    }

    @Override
    public void sync() throws ApiException {
        getProgramRulesDataFromServer();
    }
}