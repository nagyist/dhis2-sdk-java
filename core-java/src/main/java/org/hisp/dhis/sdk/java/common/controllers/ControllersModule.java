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

import org.hisp.dhis.sdk.java.common.network.INetworkModule;
import org.hisp.dhis.sdk.java.common.persistence.IPersistenceModule;
import org.hisp.dhis.sdk.java.common.preferences.IPreferencesModule;
import org.hisp.dhis.sdk.java.dashboard.DashboardController;
import org.hisp.dhis.java.sdk.models.dashboard.Dashboard;

import static org.hisp.dhis.sdk.java.utils.Preconditions.isNull;

public class ControllersModule implements IControllersModule {
    private final IDataController<Dashboard> dashboardController;

    public ControllersModule(INetworkModule networkModule,
                             IPersistenceModule persistenceModule,
                             IPreferencesModule preferencesModule) {
        isNull(networkModule, "networkModule must not be null");
        isNull(persistenceModule, "persistenceModule must not be null");
        isNull(preferencesModule, "preferencesModule must not be null");

        dashboardController = new DashboardController(
                persistenceModule.getDashboardStore(),
                persistenceModule.getDashboardItemStore(),
                persistenceModule.getDashboardElementStore(),
                persistenceModule.getDashboardContentStore(),
                persistenceModule.getStateStore(),
                networkModule.getDashboardApiClient(),
                networkModule.getSystemInfoApiClient(),
                preferencesModule.getLastUpdatedPreferences(),
                persistenceModule.getTransactionManager());
    }

    @Override
    public IDataController<Dashboard> getDashboardController() {
        return dashboardController;
    }
}
