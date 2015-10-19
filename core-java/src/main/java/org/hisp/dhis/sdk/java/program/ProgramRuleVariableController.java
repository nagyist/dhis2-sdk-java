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

import org.hisp.dhis.java.sdk.models.program.ProgramRuleVariable;
import org.hisp.dhis.sdk.java.common.controllers.IDataController;
import org.hisp.dhis.sdk.java.common.network.ApiException;
import org.hisp.dhis.sdk.java.common.persistence.IDbOperation;
import org.hisp.dhis.sdk.java.common.persistence.IIdentifiableObjectStore;
import org.hisp.dhis.sdk.java.common.persistence.ITransactionManager;
import org.hisp.dhis.sdk.java.common.preferences.ILastUpdatedPreferences;
import org.hisp.dhis.sdk.java.common.preferences.ResourceType;
import org.hisp.dhis.sdk.java.systeminfo.ISystemInfoApiClient;
import org.joda.time.DateTime;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import static org.hisp.dhis.java.sdk.models.common.base.BaseIdentifiableObject.merge;

public final class ProgramRuleVariableController implements IDataController<ProgramRuleVariable> {

    private final static String PROGRAMRULEVARIABLES = "programRuleVariables";
    private final IProgramRuleVariableApiClient programRuleActionApiClient;
    private final ITransactionManager transactionManager;
    private final ILastUpdatedPreferences lastUpdatedPreferences;
    private final ISystemInfoApiClient systemInfoApiClient;
    private final IIdentifiableObjectStore<ProgramRuleVariable> mProgramRuleVariableStore;

    public ProgramRuleVariableController(IProgramRuleVariableApiClient programRuleActionApiClient,
                                         ITransactionManager transactionManager,
                                         ILastUpdatedPreferences lastUpdatedPreferences,
                                         ISystemInfoApiClient systemInfoApiClient, IIdentifiableObjectStore<ProgramRuleVariable> mProgramRuleVariableStore) {
        this.programRuleActionApiClient = programRuleActionApiClient;
        this.transactionManager = transactionManager;
        this.lastUpdatedPreferences = lastUpdatedPreferences;
        this.systemInfoApiClient = systemInfoApiClient;
        this.mProgramRuleVariableStore = mProgramRuleVariableStore;
    }

    private void getProgramRuleVariablesDataFromServer() throws ApiException {
        ResourceType resource = ResourceType.PROGRAM_RULE_VARIABLES;
        DateTime serverTime = systemInfoApiClient.getSystemInfo().getServerDate();
        DateTime lastUpdated = lastUpdatedPreferences.get(resource);

        //fetching id and name for all items on server. This is needed in case something is
        // deleted on the server and we want to reflect that locally
        List<ProgramRuleVariable> allProgramRuleVariables = programRuleActionApiClient.getBasicProgramRuleVariables(null);

        //fetch all updated items
        List<ProgramRuleVariable> updatedProgramRuleVariables = programRuleActionApiClient.getFullProgramRuleVariables(lastUpdated);
        //merging updated items with persisted items, and removing ones not present in server.
        List<ProgramRuleVariable> existingPersistedAndUpdatedProgramRuleVariables =
                merge(allProgramRuleVariables, updatedProgramRuleVariables, mProgramRuleVariableStore.
                        queryAll());

        Queue<IDbOperation> operations = new LinkedList<>();
        operations.addAll(transactionManager.createOperations(mProgramRuleVariableStore, existingPersistedAndUpdatedProgramRuleVariables, mProgramRuleVariableStore.queryAll()));

        transactionManager.transact(operations);
        lastUpdatedPreferences.save(resource, serverTime, null);

    }

    @Override
    public void sync() throws ApiException {
        getProgramRuleVariablesDataFromServer();
    }
}