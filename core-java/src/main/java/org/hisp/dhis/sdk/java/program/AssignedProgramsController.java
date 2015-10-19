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

import org.hisp.dhis.java.sdk.models.organisationunit.OrganisationUnit;
import org.hisp.dhis.java.sdk.models.program.IAssignedProgramApiClient;
import org.hisp.dhis.java.sdk.models.program.Program;
import org.hisp.dhis.sdk.java.common.controllers.ResourceController;
import org.hisp.dhis.sdk.java.common.network.ApiException;
import org.hisp.dhis.sdk.java.common.persistence.ITransactionManager;
import org.hisp.dhis.sdk.java.common.preferences.ILastUpdatedPreferences;
import org.hisp.dhis.sdk.java.common.preferences.ResourceType;
import org.hisp.dhis.sdk.java.organisationunit.IOrganisationUnitController;
import org.hisp.dhis.sdk.java.organisationunit.IOrganisationUnitStore;
import org.hisp.dhis.sdk.java.systeminfo.ISystemInfoApiClient;
import org.joda.time.DateTime;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.hisp.dhis.java.sdk.models.common.base.BaseIdentifiableObject.getUids;

public final class AssignedProgramsController extends ResourceController<Program> {
    private final IProgramController programController;
    private final IOrganisationUnitController organisationUnitController;

    private final IOrganisationUnitStore organisationUnitStore;
    private final IProgramStore programStore;

    private final ILastUpdatedPreferences lastUpdatedPreferences;
    private final ISystemInfoApiClient systemInfoApiClient;
    private final IAssignedProgramApiClient assignedProgramApiClient;


    public AssignedProgramsController(IProgramController programController,
                                      IOrganisationUnitController organisationUnitController,
                                      IOrganisationUnitStore organisationUnitStore,
                                      IProgramStore programStore,
                                      ITransactionManager transactionManager,
                                      ILastUpdatedPreferences lastUpdatedPreferences,
                                      ISystemInfoApiClient systemInfoApiClient,
                                      IAssignedProgramApiClient assignedProgramApiClient) {
        super(transactionManager, lastUpdatedPreferences);
        this.programController = programController;
        this.organisationUnitController = organisationUnitController;
        this.organisationUnitStore = organisationUnitStore;
        this.programStore = programStore;

        this.lastUpdatedPreferences = lastUpdatedPreferences;
        this.systemInfoApiClient = systemInfoApiClient;
        this.assignedProgramApiClient = assignedProgramApiClient;
    }

    @Override
    public void sync() throws ApiException {
        getAssignedProgramsDataFromServer();
    }

    private void getAssignedProgramsDataFromServer() throws ApiException {
        DateTime serverTime = systemInfoApiClient.getSystemInfo().getServerDate();
        Map<OrganisationUnit, Set<Program>> assignedPrograms = assignedProgramApiClient.getAssignedPrograms();

        Set<String> organisationUnitsToLoad = getUids(assignedPrograms.keySet());
        Set<String> programsToLoad = new HashSet<>();
        for (OrganisationUnit organisationUnit : assignedPrograms.keySet()) {
            programsToLoad.addAll(getUids(assignedPrograms.get(organisationUnit)));
        }

        //Load the programs and organisation units from server with full data
        organisationUnitController.sync(organisationUnitsToLoad);
        programController.sync(programsToLoad);

        Map<Program, Set<OrganisationUnit>> programToUnits = reverseRelationship(assignedPrograms);
        for (Program program : reverseRelationship(assignedPrograms).keySet()) {
            Set<OrganisationUnit> units = programToUnits.get(program);

            programStore.assign(program, units);
        }

        lastUpdatedPreferences.save(ResourceType.ASSIGNED_PROGRAMS, serverTime);
    }

    private Map<Program, Set<OrganisationUnit>> reverseRelationship(Map<OrganisationUnit, Set<Program>> assignedOrganisations) {
        Map<Program, Set<OrganisationUnit>> programToOrganisationUnitsMap = new HashMap<>();
        for (OrganisationUnit unit : assignedOrganisations.keySet()) {
            Set<Program> assignedUnitPrograms = assignedOrganisations.get(unit);

            if (assignedUnitPrograms == null) {
                continue;
            }

            for (Program program : assignedUnitPrograms) {
                if (!programToOrganisationUnitsMap.containsKey(program)) {
                    programToOrganisationUnitsMap.put(program, new HashSet<OrganisationUnit>());
                }

                programToOrganisationUnitsMap.get(program).add(unit);
            }
        }

        return programToOrganisationUnitsMap;
    }
}