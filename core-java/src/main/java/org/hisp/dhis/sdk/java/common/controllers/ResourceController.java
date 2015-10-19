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

package org.hisp.dhis.sdk.java.common.controllers;

import org.hisp.dhis.java.sdk.models.common.base.IdentifiableObject;
import org.hisp.dhis.sdk.java.common.persistence.IDbOperation;
import org.hisp.dhis.sdk.java.common.persistence.IIdentifiableObjectStore;
import org.hisp.dhis.sdk.java.common.persistence.ITransactionManager;
import org.hisp.dhis.sdk.java.common.preferences.ILastUpdatedPreferences;
import org.hisp.dhis.sdk.java.common.preferences.ResourceType;
import org.joda.time.DateTime;

import java.util.*;


public abstract class ResourceController<T extends IdentifiableObject> implements IDataController<T> {
    private final ITransactionManager transactionManager;
    private final ILastUpdatedPreferences lastUpdatedPreferences;

    public ResourceController(ITransactionManager transactionManager,
                              ILastUpdatedPreferences lastUpdatedPreferences) {
        this.transactionManager = transactionManager;
        this.lastUpdatedPreferences = lastUpdatedPreferences;
    }

    public void saveResourceDataFromServer(ResourceType resourceType, IIdentifiableObjectStore<T> store,
                                           List<T> updatedItems, List<T> persistedItems, DateTime serverDateTime) {
        saveResourceDataFromServer(resourceType, null, store, updatedItems, persistedItems, serverDateTime);
    }

    public void saveResourceDataFromServer(ResourceType resourceType, String extraIdentifier, IIdentifiableObjectStore<T> store,
                                           List<T> updatedItems, List<T> persistedItems, DateTime serverDateTime) {
        Queue<IDbOperation> operations = new LinkedList<>();
        operations.addAll(transactionManager.createOperations(store, persistedItems, updatedItems));
        transactionManager.transact(operations);
        lastUpdatedPreferences.save(resourceType, serverDateTime, extraIdentifier);
    }

    public Map<String, String> getBasicQueryMap() {
        final Map<String, String> map = new HashMap<>();
        map.put("fields", "id");
        return map;
    }

    public Map<String, String> getAllFieldsQueryMap(DateTime lastUpdated) {
        final Map<String, String> map = new HashMap<>();
        map.put("fields", "[:all]");
        if (lastUpdated != null) {
            map.put("filter", "lastUpdated:gt:" + lastUpdated.toString());
        }
        return map;
    }
}
