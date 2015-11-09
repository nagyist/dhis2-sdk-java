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

package org.hisp.dhis.java.sdk.optionset;


import org.hisp.dhis.java.sdk.common.controllers.IDataController;
import org.hisp.dhis.java.sdk.common.persistence.IIdentifiableObjectStore;
import org.hisp.dhis.java.sdk.common.preferences.ResourceType;
import org.hisp.dhis.java.sdk.models.optionset.Option;
import org.hisp.dhis.java.sdk.models.optionset.OptionSet;
import org.hisp.dhis.java.sdk.systeminfo.ISystemInfoApiClient;
import org.hisp.dhis.java.sdk.common.network.ApiException;
import org.hisp.dhis.java.sdk.common.persistence.IDbOperation;
import org.hisp.dhis.java.sdk.common.persistence.ITransactionManager;
import org.hisp.dhis.java.sdk.common.preferences.ILastUpdatedPreferences;
import org.hisp.dhis.java.sdk.utils.IModelUtils;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

public final class OptionSetController implements IDataController<OptionSet> {
    private final IOptionSetApiClient optionSetApiClient;
    private final ISystemInfoApiClient systemInfoApiClient;
    private final ILastUpdatedPreferences lastUpdatedPreferences;
    private final ITransactionManager transactionManager;

    private final IOptionStore mOptionStore;
    private final IIdentifiableObjectStore<OptionSet> mOptionSetStore;
    private final IModelUtils modelUtils;


    public OptionSetController(IOptionSetApiClient optionSetApiClient, IOptionStore mOptionStore,
                               IIdentifiableObjectStore<OptionSet> mOptionSetStore,
                               ISystemInfoApiClient systemInfoApiClient,
                               ILastUpdatedPreferences lastUpdatedPreferences,
                               ITransactionManager transactionManager, IModelUtils modelUtils) {
        this.transactionManager = transactionManager;
        this.lastUpdatedPreferences = lastUpdatedPreferences;
        this.optionSetApiClient = optionSetApiClient;
        this.mOptionStore = mOptionStore;
        this.mOptionSetStore = mOptionSetStore;
        this.systemInfoApiClient = systemInfoApiClient;
        this.modelUtils = modelUtils;
    }

    private void getOptionSetDataFromServer() throws ApiException {
        ResourceType resource = ResourceType.OPTION_SETS;
        DateTime serverTime = systemInfoApiClient.getSystemInfo().getServerDate();
        DateTime lastUpdated = lastUpdatedPreferences.get(resource);
        List<OptionSet> allOptionSets = optionSetApiClient.getBasicOptionSets(null);
        List<OptionSet> updatedOptionSets = optionSetApiClient.getFullOptionSets(lastUpdated);
        linkOptionsWithOptionSets(updatedOptionSets);
        List<OptionSet> existingPersistedAndUpdatedOptionSets =
                modelUtils.merge(allOptionSets, updatedOptionSets, mOptionSetStore.queryAll());

        List<IDbOperation> operations = new ArrayList<>();
        List<OptionSet> persistedOptionSets = mOptionSetStore.queryAll();
        if (existingPersistedAndUpdatedOptionSets != null && !existingPersistedAndUpdatedOptionSets.isEmpty()) {
            for (OptionSet optionSet : existingPersistedAndUpdatedOptionSets) {
                if (optionSet == null || optionSet.getOptions() == null) {
                    continue;
                }
                OptionSet persistedOptionSet = mOptionSetStore.queryByUid(optionSet.getUId());
                List<Option> persistedOptions;
                if (persistedOptionSet != null) {
                    persistedOptions = persistedOptionSet.getOptions();
                } else {
                    persistedOptions = new ArrayList<>();
                }
                operations.addAll(transactionManager.createOperations(mOptionStore, persistedOptions, optionSet.getOptions()));
            }
        }
        operations.addAll(transactionManager.createOperations(mOptionSetStore, persistedOptionSets, existingPersistedAndUpdatedOptionSets));

//        DbUtils.applyBatch(operations);
        transactionManager.transact(operations);
        lastUpdatedPreferences.save(ResourceType.OPTION_SETS, serverTime);
    }

    private void linkOptionsWithOptionSets(List<OptionSet> optionSets) {
        // Building option to optionset relationship.
        if (optionSets != null && !optionSets.isEmpty()) {
            for (OptionSet optionSet : optionSets) {
                if (optionSet == null || optionSet.getOptions() == null) {
                    continue;
                }
                int sortOrder = 0;
                for (Option option : optionSet.getOptions()) {
                    option.setUId(optionSet.getUId() + option.getCode());//options don't have uid, but uid is used in createOperations
                    option.setLastUpdated(new DateTime());//same with these dates
                    option.setCreated(new DateTime());
                    option.setOptionSet(optionSet.getUId());
                    option.setSortOrder(sortOrder);
                    sortOrder++;
                }
            }
        }
    }

    @Override
    public void sync() throws ApiException {
        getOptionSetDataFromServer();
    }
}