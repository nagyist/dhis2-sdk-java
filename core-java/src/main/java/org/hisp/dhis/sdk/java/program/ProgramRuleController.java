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

package org.hisp.dhis.sdk.java.program;

import org.hisp.dhis.java.sdk.models.program.IProgramRuleApiClient;
import org.hisp.dhis.java.sdk.models.program.ProgramRule;
import org.hisp.dhis.sdk.java.common.controllers.IDataController;
import org.hisp.dhis.sdk.java.common.network.ApiException;
import org.hisp.dhis.sdk.java.common.persistence.IDbOperation;
import org.hisp.dhis.sdk.java.common.persistence.IIdentifiableObjectStore;
import org.hisp.dhis.sdk.java.common.persistence.ITransactionManager;
import org.hisp.dhis.sdk.java.common.preferences.ILastUpdatedPreferences;
import org.hisp.dhis.sdk.java.common.preferences.ResourceType;
import org.hisp.dhis.sdk.java.systeminfo.ISystemInfoApiClient;
import org.hisp.dhis.sdk.java.utils.IIdentifialModelUtils;
import org.joda.time.DateTime;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public final class ProgramRuleController implements IDataController<ProgramRule> {
    private final ITransactionManager transactionManager;
    private final IIdentifiableObjectStore<ProgramRule> mProgramRuleStore;
    private final ILastUpdatedPreferences lastUpdatedPreferences;
    private final ISystemInfoApiClient systemInfoApiClient;
    private final IProgramRuleApiClient programRuleApiClient;
    private final IIdentifialModelUtils modelUtils;

    public ProgramRuleController(ITransactionManager transactionManager,
                                 ILastUpdatedPreferences lastUpdatedPreferences,
                                 IIdentifiableObjectStore<ProgramRule> mProgramRuleStore,
                                 ISystemInfoApiClient systemInfoApiClient,
                                 IProgramRuleApiClient programRuleApiClient, IIdentifialModelUtils modelUtils) {
        this.transactionManager = transactionManager;
        this.lastUpdatedPreferences = lastUpdatedPreferences;
        this.mProgramRuleStore = mProgramRuleStore;
        this.systemInfoApiClient = systemInfoApiClient;
        this.programRuleApiClient = programRuleApiClient;
        this.modelUtils = modelUtils;
    }

    private void getProgramRulesDataFromServer() throws ApiException {
        ResourceType resource = ResourceType.PROGRAM_RULES;
        DateTime serverTime = systemInfoApiClient.getSystemInfo().getServerDate();
        DateTime lastUpdated = lastUpdatedPreferences.get(resource);

        // fetching id and name for all items on server. This is needed in case something is
        // deleted on the server and we want to reflect that locally
        List<ProgramRule> allProgramRules = programRuleApiClient.getBasicProgramRules(null);

        // fetch all updated items
        List<ProgramRule> updatedProgramRules = programRuleApiClient.getFullProgramRules(lastUpdated);

        // merging updated items with persisted items, and removing ones not present in server.
        List<ProgramRule> existingPersistedAndUpdatedProgramRules =
                modelUtils.merge(allProgramRules, updatedProgramRules, mProgramRuleStore.queryAll());

        Queue<IDbOperation> operations = new LinkedList<>();
        operations.addAll(transactionManager.createOperations(mProgramRuleStore, existingPersistedAndUpdatedProgramRules, mProgramRuleStore.queryAll()));

        transactionManager.transact(operations);
        lastUpdatedPreferences.save(resource, serverTime);
    }

    @Override
    public void sync() throws ApiException {
        getProgramRulesDataFromServer();
    }
}