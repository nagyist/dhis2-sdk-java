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

package org.hisp.dhis.java.sdk.relationship;

import org.hisp.dhis.java.sdk.common.controllers.IDataController;
import org.hisp.dhis.java.sdk.common.persistence.IIdentifiableObjectStore;
import org.hisp.dhis.java.sdk.common.preferences.ILastUpdatedPreferences;
import org.hisp.dhis.java.sdk.common.preferences.ResourceType;
import org.hisp.dhis.java.sdk.models.relationship.RelationshipType;
import org.hisp.dhis.java.sdk.systeminfo.ISystemInfoApiClient;
import org.hisp.dhis.java.sdk.common.network.ApiException;
import org.hisp.dhis.java.sdk.common.persistence.IDbOperation;
import org.hisp.dhis.java.sdk.common.persistence.ITransactionManager;
import org.hisp.dhis.java.sdk.utils.IModelUtils;
import org.joda.time.DateTime;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public final class RelationshipTypeController implements IDataController<RelationshipType> {
    private final ITransactionManager transactionManager;
    private final ILastUpdatedPreferences lastUpdatedPreferences;
    private final IRelationshipTypeApiClient relationshipTypeApiClient;
    private final ISystemInfoApiClient systemInfoApiClient;
    private final IIdentifiableObjectStore<RelationshipType> mRelationshipTypeStore;
    private final IModelUtils modelUtils;

    public RelationshipTypeController(IRelationshipTypeApiClient relationshipApiClient,
                                      ITransactionManager transactionManager,
                                      IIdentifiableObjectStore<RelationshipType> mRelationshipTypeStore,
                                      ILastUpdatedPreferences lastUpdatedPreferences,
                                      ISystemInfoApiClient systemInfoApiClient, IModelUtils modelUtils) {
        this.relationshipTypeApiClient = relationshipApiClient;
        this.transactionManager = transactionManager;
        this.mRelationshipTypeStore = mRelationshipTypeStore;
        this.lastUpdatedPreferences = lastUpdatedPreferences;
        this.systemInfoApiClient = systemInfoApiClient;
        this.modelUtils = modelUtils;
    }

    private void getRelationshipTypesDataFromServer() throws ApiException {
        ResourceType resource = ResourceType.RELATIONSHIP_TYPES;
        DateTime serverTime = systemInfoApiClient.getSystemInfo().getServerDate();
        DateTime lastUpdated = lastUpdatedPreferences.get(resource);

        //fetching id and name for all items on server. This is needed in case something is
        // deleted on the server and we want to reflect that locally
        List<RelationshipType> allRelationshipTypes = relationshipTypeApiClient.getBasicRelationshipTypes(null);


        //fetch all updated relationshiptypes
        List<RelationshipType> updatedRelationshipTypes = relationshipTypeApiClient.getFullRelationshipTypes(lastUpdated);

        //merging updated items with persisted items, and removing ones not present in server.
        List<RelationshipType> existingPersistedAndUpdatedRelationshipTypes =
                modelUtils.merge(allRelationshipTypes, updatedRelationshipTypes, mRelationshipTypeStore.
                        queryAll());

        Queue<IDbOperation> operations = new LinkedList<>();
        operations.addAll(transactionManager.createOperations(mRelationshipTypeStore,
                existingPersistedAndUpdatedRelationshipTypes, mRelationshipTypeStore.queryAll()));

        transactionManager.transact(operations);
        lastUpdatedPreferences.save(resource, serverTime, null);
    }

    @Override
    public void sync() throws ApiException {
        getRelationshipTypesDataFromServer();
    }
}