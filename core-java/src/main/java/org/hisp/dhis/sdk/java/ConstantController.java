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

package org.hisp.dhis.sdk.java;

import org.hisp.dhis.java.sdk.models.constant.Constant;
import org.hisp.dhis.sdk.java.common.controllers.ResourceController;
import org.hisp.dhis.sdk.java.common.network.ApiException;
import org.hisp.dhis.sdk.java.common.persistence.IIdentifiableObjectStore;
import org.hisp.dhis.sdk.java.common.preferences.ILastUpdatedPreferences;
import org.hisp.dhis.sdk.java.common.preferences.ResourceType;
import org.hisp.dhis.sdk.java.systeminfo.ISystemInfoApiClient;
import org.joda.time.DateTime;

import java.util.List;

import static org.hisp.dhis.java.sdk.models.common.base.BaseIdentifiableObject.merge;

public final class ConstantController extends ResourceController<Constant> {

    private final static String CONSTANTS = "constants";
    private final IConstantApiClient constantApiClient;
    private final ISystemInfoApiClient systemInfoApiClient;
    private final ILastUpdatedPreferences lastUpdatedPreferences;
    private final IIdentifiableObjectStore<Constant> constantStore;

    public ConstantController(IConstantApiClient constantApiClient,
                              ISystemInfoApiClient systemInfoApiClient,
                              ILastUpdatedPreferences lastUpdatedPreferences,
                              IIdentifiableObjectStore<Constant> constantStore) {
        this.constantApiClient = constantApiClient;
        this.systemInfoApiClient = systemInfoApiClient;
        this.lastUpdatedPreferences = lastUpdatedPreferences;
        this.constantStore = constantStore;
    }

    private void getConstantsDataFromServer() throws ApiException {
        ResourceType resource = ResourceType.CONSTANTS;
        DateTime serverTime = systemInfoApiClient.getSystemInfo().getServerDate();
        DateTime lastUpdated = lastUpdatedPreferences.get(resource);

        //fetching id and name for all items on server. This is needed in case something is
        // deleted on the server and we want to reflect that locally
        List<Constant> allConstants = constantApiClient.getBasicConstants(null);

        //fetch all updated items
        List<Constant> updatedConstants = constantApiClient.getFullConstants(lastUpdated);

        //merging updated items with persisted items, and removing ones not present in server.
        List<Constant> existingPersistedAndUpdatedConstants =
                merge(allConstants, updatedConstants, constantStore.
                        queryAll());
        saveResourceDataFromServer(resource, constantStore,
                existingPersistedAndUpdatedConstants, constantStore.queryAll(),
                serverTime);
    }

    @Override
    public void sync() throws ApiException {
        getConstantsDataFromServer();
    }
}