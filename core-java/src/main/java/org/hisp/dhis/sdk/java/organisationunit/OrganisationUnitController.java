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

package org.hisp.dhis.sdk.java.organisationunit;

import org.hisp.dhis.sdk.java.common.network.ApiException;
import org.hisp.dhis.sdk.java.common.persistence.DbOperation;
import org.hisp.dhis.sdk.java.common.persistence.IDbOperation;
import org.hisp.dhis.java.sdk.models.organisationunit.OrganisationUnit;
import org.hisp.dhis.sdk.java.common.persistence.ITransactionManager;
import org.hisp.dhis.sdk.java.common.preferences.ILastUpdatedPreferences;
import org.hisp.dhis.sdk.java.common.preferences.ResourceType;
import org.hisp.dhis.sdk.java.systeminfo.ISystemInfoApiClient;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.hisp.dhis.java.sdk.models.common.base.BaseIdentifiableObject.toMap;

public final class OrganisationUnitController implements IOrganisationUnitController {

    private final static String ORGANISATIONUNITS = "organisationUnits";

    private final IOrganisationUnitStore mOrganisationUnitStore;
    private final ISystemInfoApiClient systemInfoApiClient;
    private final IOrganisationUnitApiClient organisationUnitApiClient;
    private final ILastUpdatedPreferences lastUpdatedPreferences;
    private final ITransactionManager transactionManager;

    public OrganisationUnitController(IOrganisationUnitStore mOrganisationUnitStore, ISystemInfoApiClient systemInfoApiClient,
                                      IOrganisationUnitApiClient organisationUnitApiClient,
                                      ILastUpdatedPreferences lastUpdatedPreferences, ITransactionManager transactionManager) {
        this.mOrganisationUnitStore = mOrganisationUnitStore;
        this.systemInfoApiClient = systemInfoApiClient;
        this.organisationUnitApiClient = organisationUnitApiClient;
        this.lastUpdatedPreferences = lastUpdatedPreferences;
        this.transactionManager = transactionManager;
    }

    private void getOrganisationUnitsFromServer(List<OrganisationUnit> organisationUnits) {
        DateTime serverTime = systemInfoApiClient.getSystemInfo().getServerDate();
        DateTime lastUpdated = lastUpdatedPreferences.get(ResourceType.ORGANISATION_UNITS);
        List<String> organisationUnitIds = new ArrayList<>();
        for(OrganisationUnit organisationUnit : organisationUnits) {
            organisationUnitIds.add(organisationUnit.getUId());
        }
        List<OrganisationUnit> updatedOrganisationUnits = organisationUnitApiClient.getFullOrganisationUnits(organisationUnitIds);
        List<OrganisationUnit> persistedOrganisationUnits = mOrganisationUnitStore.queryAll();
        Map<String, OrganisationUnit> persistedOrganisationUnitsMap = toMap(persistedOrganisationUnits);
        Map<String, OrganisationUnit> updatedOrganisationUnitsMap = toMap(updatedOrganisationUnits);
        for(OrganisationUnit persistedOrganisationUnit : persistedOrganisationUnits) {
            OrganisationUnit updatedOrganisationUnit = updatedOrganisationUnitsMap.get(persistedOrganisationUnit.getUId());
            if(updatedOrganisationUnit != null) {
                updatedOrganisationUnit.setId(persistedOrganisationUnit.getId());
            }
        }
        List<IDbOperation> operations = new ArrayList<>();
        for(OrganisationUnit updatedOrganisationUnit : updatedOrganisationUnits) {
            if(persistedOrganisationUnitsMap.containsValue(updatedOrganisationUnit.getUId())) {
                operations.add(DbOperation.with(mOrganisationUnitStore).update(updatedOrganisationUnit));
            } else {
                operations.add(DbOperation.with(mOrganisationUnitStore).insert(updatedOrganisationUnit));
            }
        }
        transactionManager.transact(operations);
        lastUpdatedPreferences.save(ResourceType.ORGANISATION_UNITS, serverTime);
    }

    private void getOrganisationUnitsFromServer() {
        getOrganisationUnitsFromServer(mOrganisationUnitStore.queryAll());
    }

    @Override
    public void sync() throws ApiException {
        getOrganisationUnitsFromServer();
    }

    @Override
    public void sync(Collection<String> organisationUnitIds) {

    }

    public void sync(List<OrganisationUnit> organisationUnits) throws ApiException {
        getOrganisationUnitsFromServer(organisationUnits);
    }
}