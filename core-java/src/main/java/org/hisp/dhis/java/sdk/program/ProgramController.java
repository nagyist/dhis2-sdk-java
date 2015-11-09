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

package org.hisp.dhis.java.sdk.program;

import org.hisp.dhis.java.sdk.common.controllers.IDataController;
import org.hisp.dhis.java.sdk.common.persistence.DbOperation;
import org.hisp.dhis.java.sdk.common.preferences.ResourceType;
import org.hisp.dhis.java.sdk.models.program.Program;
import org.hisp.dhis.java.sdk.models.program.ProgramIndicator;
import org.hisp.dhis.java.sdk.models.program.ProgramStage;
import org.hisp.dhis.java.sdk.models.program.ProgramStageDataElement;
import org.hisp.dhis.java.sdk.models.program.ProgramStageSection;
import org.hisp.dhis.java.sdk.models.program.ProgramTrackedEntityAttribute;
import org.hisp.dhis.java.sdk.systeminfo.ISystemInfoApiClient;
import org.hisp.dhis.java.sdk.common.network.ApiException;
import org.hisp.dhis.java.sdk.common.persistence.IDbOperation;
import org.hisp.dhis.java.sdk.common.persistence.ITransactionManager;
import org.hisp.dhis.java.sdk.common.preferences.ILastUpdatedPreferences;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class ProgramController implements IProgramController, IDataController<Program> {
    private final IProgramStore mProgramStore;
    private final IProgramApiClient programApiClient;
    private final ITransactionManager transactionManager;
    private final ILastUpdatedPreferences lastUpdatedPreferences;
    private final ISystemInfoApiClient systemInfoApiClient;
    private final IProgramIndicatorStore mProgramIndicatorsStore;
    private final IProgramStageDataElementStore mProgramStageDataElementStore;
    private final IProgramTrackedEntityAttributeStore mProgramTrackedEntityAttributeStore;
    private final IProgramStageStore mProgramStageStore;
    private final IProgramStageSectionStore mProgramStageSectionStore;

    public ProgramController(IProgramApiClient programApiClient,
                             ITransactionManager transactionManager,
                             ILastUpdatedPreferences lastUpdatedPreferences,
                             ISystemInfoApiClient systemInfoApiClient, IProgramStore mProgramStore,
                             IProgramIndicatorStore mProgramIndicatorsStore,
                             IProgramStageDataElementStore mProgramStageDataElementStore,
                             IProgramTrackedEntityAttributeStore mProgramTrackedEntityAttributeStore,
                             IProgramStageStore mProgramStageStore,
                             IProgramStageSectionStore mProgramStageSectionStore) {
        this.programApiClient = programApiClient;
        this.transactionManager = transactionManager;
        this.lastUpdatedPreferences = lastUpdatedPreferences;
        this.systemInfoApiClient = systemInfoApiClient;
        this.mProgramStore = mProgramStore;
        this.mProgramIndicatorsStore = mProgramIndicatorsStore;
        this.mProgramStageDataElementStore = mProgramStageDataElementStore;
        this.mProgramTrackedEntityAttributeStore = mProgramTrackedEntityAttributeStore;
        this.mProgramStageStore = mProgramStageStore;
        this.mProgramStageSectionStore = mProgramStageSectionStore;
    }


    private void getProgramsDataFromServer() throws ApiException {
        ResourceType resource = ResourceType.PROGRAMS;
        DateTime serverTime = systemInfoApiClient.getSystemInfo().getServerDate();
        DateTime lastUpdated = lastUpdatedPreferences.get(resource);

        List<Program> allProgramsOnServer = programApiClient.getBasicPrograms(null);

        List<Program> updatedPrograms = programApiClient.getFullPrograms(lastUpdated);

        List<IDbOperation> operations = new ArrayList<>();
        for (Program program : updatedPrograms) {
            operations.addAll(generateUpdateProgramDbOperations(program));
        }
        //deleting programs that are stored on device, but are removed on server
        Map<String, Program> programsOnServerMap = new HashMap<>();
        for (Program program : allProgramsOnServer) {
            programsOnServerMap.put(program.getUId(), program);
        }
        for (Program persistedProgram : mProgramStore.queryAll()) {
            if (!programsOnServerMap.containsKey(persistedProgram.getUId())) {
                operations.addAll(genereDeleteProgramDbOperations(persistedProgram, true));
            }
        }

        transactionManager.transact(operations);
        lastUpdatedPreferences.save(resource, serverTime);
    }

    private void getProgramsDataFromServer(Collection<String> programUidsToLoad) throws ApiException {
        List<Program> allProgramsOnServer = programApiClient.getBasicPrograms(null);

        Map<String, Program> programsOnServerMap = new HashMap<>();
        for (Program program : allProgramsOnServer) {
            programsOnServerMap.put(program.getUId(), program);
        }
        //verifying that the programs we want to load exist on the server
        List<String> existingProgramUidsToLoad = new ArrayList<>();
        for (String programUidToLoad : programUidsToLoad) {
            if (programsOnServerMap.containsKey(programUidToLoad)) {
                existingProgramUidsToLoad.add(programUidToLoad);
            }
        }
        for (String programUidToLoad : existingProgramUidsToLoad) {
            getProgramDataFromServer(programUidToLoad);
        }
        //deleting previously loaded programs that are removed from server
        List<Program> persistedPrograms = mProgramStore.queryAll();
        for (Program persistedProgram : persistedPrograms) {
            if (!existingProgramUidsToLoad.contains(persistedProgram.getUId())) {
                genereDeleteProgramDbOperations(persistedProgram, true);
            }
        }
    }

    private void getProgramDataFromServer(String uid) throws ApiException {
        ResourceType resource = ResourceType.PROGRAM;
        DateTime serverTime = systemInfoApiClient.getSystemInfo().getServerDate();
        DateTime lastUpdated = lastUpdatedPreferences.get(resource);

        // program with content.
        Program updatedProgram = programApiClient.getFullProgram(uid, lastUpdated);
        if (updatedProgram.getUId() == null) {
            return;
        }
        List<IDbOperation> operations = generateUpdateProgramDbOperations(updatedProgram);
        transactionManager.transact(operations);
        lastUpdatedPreferences.save(resource, serverTime);
    }

    private List<IDbOperation> generateUpdateProgramDbOperations(Program updatedProgram) {
        List<IDbOperation> operations = new ArrayList<>();
        /* In case something has been deleted on the server for this program, we delete everything
         * related exclusively to the program and store it again cause it's easier, it's not that
         * big, and  it rarely happens. We delete the following:
         * ProgramTrackedEntityAttribute
         * ProgramStageDataElement (not the actual DataElement, we can simply update that)
         * ProgramStages
         * ProgramStageSections
         * ProgramIndicators
         */
        Program persistedProgram = mProgramStore.queryByUid(updatedProgram.getUId());
        if (persistedProgram != null) {
            updatedProgram.setId(persistedProgram.getId());
            operations.addAll(genereDeleteProgramDbOperations(persistedProgram, false));
        }
        operations.add(DbOperation.with(mProgramStore).save(updatedProgram));

        int sortOrder = 0;
        for (ProgramTrackedEntityAttribute ptea : updatedProgram.getProgramTrackedEntityAttributes()) {
            ptea.setProgram(updatedProgram.getUId());
            ptea.setSortOrder(sortOrder);
            operations.add(DbOperation.with(mProgramTrackedEntityAttributeStore).save(ptea));
            sortOrder++;
        }

        for (ProgramStage programStage : updatedProgram.getProgramStages()) {
            operations.add(DbOperation.with(mProgramStageStore).save(programStage));
            if (programStage.getProgramStageSections() != null && !programStage.getProgramStageSections().isEmpty()) {
                // due to the way the WebAPI lists programStageSections we have to manually
                // set id of programStageSection in programStageDataElements to be able to
                // access it later when loading from local db
                for (ProgramStageSection programStageSection : programStage.getProgramStageSections()) {
                    operations.add(DbOperation.with(mProgramStageSectionStore).save(programStageSection));
                    for (ProgramStageDataElement programStageDataElement : programStageSection.getProgramStageDataElements()) {
                        programStageDataElement.setProgramStageSection(programStageSection.getUId());
                        operations.add(DbOperation.with(mProgramStageDataElementStore).save(programStageDataElement));
                    }
                    for (ProgramIndicator programIndicator : programStageSection.getProgramIndicators()) {
                        operations.add(DbOperation.with(mProgramIndicatorsStore).save(programIndicator));
                    }
                }
            }
        }
        return operations;
    }

    private List<IDbOperation> genereDeleteProgramDbOperations(Program persistedProgram, boolean deleteProgram) {
        List<IDbOperation> operations = new ArrayList<>();
        for (ProgramTrackedEntityAttribute ptea : persistedProgram.getProgramTrackedEntityAttributes()) {
            operations.add(DbOperation.with(mProgramTrackedEntityAttributeStore).delete(ptea));
        }
        for (ProgramStage programStage : persistedProgram.getProgramStages()) {
            for (ProgramStageDataElement psde : programStage.getProgramStageDataElements()) {
                operations.add(DbOperation.with(mProgramStageDataElementStore).delete(psde));
            }
            for (ProgramStageSection programStageSection : programStage.getProgramStageSections()) {
                operations.add(DbOperation.with(mProgramStageSectionStore).delete(programStageSection));
            }
            operations.add(DbOperation.with(mProgramStageStore).delete(programStage));
        }
        for (ProgramIndicator programIndicator : mProgramIndicatorsStore.query(persistedProgram)) {
            operations.add(DbOperation.with(mProgramIndicatorsStore).delete(programIndicator));
        }
        if (deleteProgram) {
            operations.add(DbOperation.with(mProgramStore).delete(persistedProgram));
        }
        return operations;
    }

    private Map<String, String> getFullQueryMap(DateTime lastUpdated) {
        final Map<String, String> QUERY_MAP_FULL = new HashMap<>();
        QUERY_MAP_FULL.put("fields",
                "*,programStages[*,!dataEntryForm,program[id],programIndicators[*]," +
                        "programStageSections[*,programStageDataElements[*,programStage[id]," +
                        "dataElement[*,optionSet[id]]],programIndicators[*]],programStageDataElements" +
                        "[*,programStage[id],dataElement[*,optionSet[id]]]],programTrackedEntityAttributes" +
                        "[*,trackedEntityAttribute[*]],!organisationUnits)");
        if (lastUpdated != null) {
            QUERY_MAP_FULL.put("filter", "lastUpdated:gt:" + lastUpdated.toString());
        }
        return QUERY_MAP_FULL;
    }

    @Override
    public void sync() throws ApiException {
        getProgramsDataFromServer();
    }

    @Override
    public void sync(Collection<String> programUids) {
        getProgramsDataFromServer(programUids);
    }

}