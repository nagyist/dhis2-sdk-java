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

package org.hisp.dhis.sdk.java.dashboard;

import org.hisp.dhis.java.sdk.models.common.state.Action;
import org.hisp.dhis.java.sdk.models.dashboard.Dashboard;
import org.hisp.dhis.java.sdk.models.dashboard.DashboardElement;
import org.hisp.dhis.java.sdk.models.dashboard.DashboardItem;
import org.hisp.dhis.sdk.java.common.IStateStore;
import org.hisp.dhis.sdk.java.common.preferences.ILastUpdatedPreferences;
import org.hisp.dhis.sdk.java.utils.ModelUtils;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

import static org.mockito.Mockito.*;

public class DashboardControllerTest {
    private IDashboardApiClient dashboardApiClientMock;
    private IDashboardStore dashboardStoreMock;
    private IStateStore stateStoreMock;

    private ILastUpdatedPreferences lastUpdatedPreferencesMock;
    private ModelUtils modelUtilsMock;

    private Dashboard dashboard;
    private DashboardItem dashboardItem;
    private DashboardElement dashboardElement;

    private IDashboardController dashboardController;

    @Before
    public void setUp() {
        lastUpdatedPreferencesMock = mock(ILastUpdatedPreferences.class);
        dashboardApiClientMock = mock(IDashboardApiClient.class);
        dashboardStoreMock = mock(IDashboardStore.class);
        stateStoreMock = mock(IStateStore.class);
        modelUtilsMock = mock(ModelUtils.class);

        dashboardController = new DashboardController2(
                lastUpdatedPreferencesMock,
                dashboardApiClientMock,
                dashboardStoreMock,
                stateStoreMock,
                modelUtilsMock);

        dashboard = new Dashboard();
        dashboardItem = new DashboardItem();
        dashboardElement = new DashboardElement();

        Arrays.asList(dashboardItem, dashboardItem, dashboardItem);
        Arrays.asList(dashboard, dashboard);
    }

    @Test
    public void testUpdateDashboardsShouldGetUpdatedModelsAndMergeItWithPersistedData() {
        /* List<Dashboard> basicDashboards = Arrays.asList(dashboard, dashboard, dashboard, dashboard);
        List<Dashboard> fullDashboards = Arrays.asList(dashboard, dashboard);

        List<Dashboard> persistedDashboards = Arrays.asList(dashboard, dashboard, dashboard);
        List<DashboardItem> persistedDashboardItems = Arrays.asList(dashboardItem,
                dashboardItem, dashboardItem);
        List<DashboardElement> persistedDashboardElements = Arrays.asList(dashboardElement,
                dashboardElement, dashboardElement);

        when(dashboardApiClientMock.getBasicDashboards(null)).thenReturn(basicDashboards);
        when(dashboardApiClientMock.getFullDashboards(null)).thenReturn(fullDashboards);

        when(stateStoreMock.queryModelsWithActions(Dashboard.class,
                Action.SYNCED, Action.TO_UPDATE, Action.TO_DELETE)).thenReturn(persistedDashboards);
        when(stateStoreMock.queryModelsWithActions(DashboardItem.class,
                Action.SYNCED, Action.TO_UPDATE, Action.TO_DELETE)).thenReturn(persistedDashboardItems);
        when(stateStoreMock.queryModelsWithActions(DashboardElement.class,
                Action.SYNCED, Action.TO_UPDATE, Action.TO_DELETE)).thenReturn(persistedDashboardElements);

        dashboardController.update();

        verify(lastUpdatedPreferencesMock, times(1)).get(ResourceType.DASHBOARDS);
        verify(dashboardApiClientMock, times(1)).getBasicDashboards(null);
        verify(dashboardApiClientMock, times(1)).getFullDashboards(null);
        verify(stateStoreMock, times(1)).queryModelsWithActions(
                Dashboard.class, Action.SYNCED, Action.TO_UPDATE, Action.TO_DELETE);
        verify(modelUtilsMock, times(1)).merge(basicDashboards, fullDashboards, persistedDashboards); */
    }

    @Test
    public void testUpdateShouldPullNewDashboardsFromServer() {
        dashboardController.update();

        verify(dashboardApiClientMock, times(1)).getBasicDashboards(null);
        verify(dashboardApiClientMock, times(1)).getFullDashboards(null);
    }

    @Test
    public void testUpdateShouldQuerySyncedDashboardsFromStorage() {
        dashboardController.update();

        verify(stateStoreMock, times(1)).queryModelsWithActions(Dashboard.class,
                Action.SYNCED, Action.TO_UPDATE, Action.TO_DELETE);
        verify(stateStoreMock, times(1)).queryModelsWithActions(DashboardItem.class,
                Action.SYNCED, Action.TO_UPDATE, Action.TO_DELETE);
        verify(stateStoreMock, times(1)).queryModelsWithActions(DashboardElement.class,
                Action.SYNCED, Action.TO_UPDATE, Action.TO_DELETE);
    }
}
