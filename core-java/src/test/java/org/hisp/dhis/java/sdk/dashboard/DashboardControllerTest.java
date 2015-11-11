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

package org.hisp.dhis.java.sdk.dashboard;

import org.hisp.dhis.java.sdk.common.IStateStore;
import org.hisp.dhis.java.sdk.common.preferences.ILastUpdatedPreferences;
import org.hisp.dhis.java.sdk.models.common.state.Action;
import org.hisp.dhis.java.sdk.models.dashboard.Dashboard;
import org.hisp.dhis.java.sdk.models.dashboard.DashboardContent;
import org.hisp.dhis.java.sdk.models.dashboard.DashboardElement;
import org.hisp.dhis.java.sdk.models.dashboard.DashboardItem;
import org.hisp.dhis.java.sdk.utils.ModelUtils;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class DashboardControllerTest {

    @Captor
    private ArgumentCaptor<List<?>> dashboardCaptor;

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
        MockitoAnnotations.initMocks(this);

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

        dashboard.setId(1L);
        dashboardElement.setId(1L);

        dashboardItem.setId(1L);
        dashboardItem.setType(DashboardContent.TYPE_CHART);

        dashboardItem.setDashboard(dashboard);
        dashboardElement.setDashboardItem(dashboardItem);
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

    @Test
    @SuppressWarnings("unchecked")
    public void testUpdateShouldQueryDashboardsFromStorage() {
        when(stateStoreMock.queryModelsWithActions(Dashboard.class, Action.SYNCED,
                Action.TO_UPDATE, Action.TO_DELETE)).thenReturn(Arrays.asList(dashboard));

        dashboardController.update();

        verify(modelUtilsMock, atLeastOnce()).merge(anyListOf(Dashboard.class),
                anyListOf(Dashboard.class), (List<Dashboard>) dashboardCaptor.capture());

        List<Dashboard> dashboardList = (List<Dashboard>) dashboardCaptor.getAllValues().get(0);

        assertNotNull(dashboardList);
        assertFalse(dashboardList.isEmpty());
        assertEquals(dashboardList.get(0), dashboard);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testUpdateShouldQueryRelatedDashboardItemsFromStorage() {
        when(stateStoreMock.queryModelsWithActions(Dashboard.class, Action.SYNCED,
                Action.TO_UPDATE, Action.TO_DELETE)).thenReturn(Arrays.asList(dashboard));
        when(stateStoreMock.queryModelsWithActions(DashboardItem.class, Action.SYNCED,
                Action.TO_UPDATE, Action.TO_DELETE)).thenReturn(Arrays.asList(dashboardItem));

        dashboardController.update();

        verify(modelUtilsMock, atLeastOnce()).merge(anyListOf(Dashboard.class),
                anyListOf(Dashboard.class), (List<Dashboard>) dashboardCaptor.capture());

        List<Dashboard> captureDashboards = (List<Dashboard>) dashboardCaptor.getAllValues().get(0);
        dashboardCaptor.getAllValues().get(0);

        Dashboard dashboard = captureDashboards.get(0);

        assertNotNull(dashboard.getDashboardItems());
        assertFalse(dashboard.getDashboardItems().isEmpty());
        assertEquals(dashboard.getDashboardItems().get(0), dashboardItem);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testUpdateShouldQueryRelatedDashboardElementsFromStorage() {
        when(stateStoreMock.queryModelsWithActions(Dashboard.class, Action.SYNCED,
                Action.TO_UPDATE, Action.TO_DELETE)).thenReturn(Arrays.asList(dashboard));
        when(stateStoreMock.queryModelsWithActions(DashboardItem.class, Action.SYNCED,
                Action.TO_UPDATE, Action.TO_DELETE)).thenReturn(Arrays.asList(dashboardItem));
        when(stateStoreMock.queryModelsWithActions(DashboardElement.class, Action.SYNCED,
                Action.TO_UPDATE, Action.TO_DELETE)).thenReturn(Arrays.asList(dashboardElement));

        dashboardController.update();

        verify(modelUtilsMock, atLeastOnce()).merge(anyListOf(Dashboard.class),
                anyListOf(Dashboard.class), (List<Dashboard>) dashboardCaptor.capture());

        List<Dashboard> dashboardList = (List<Dashboard>) dashboardCaptor.getAllValues().get(0);
        Dashboard dashboard = dashboardList.get(0);

        List<DashboardItem> dashboardItems = dashboard.getDashboardItems();
        DashboardItem dashboardItem = dashboardItems.get(0);

        assertNotNull(dashboardItem.getDashboardElements());
        assertFalse(dashboardItem.getDashboardElements().isEmpty());
        assertEquals(dashboardItem.getDashboardElements().get(0), dashboardElement);
    }

    @Test
    public void testUpdateShouldUpdateDashboardItemsShape() {
        when(dashboardApiClientMock.getBasicDashboardItems(any(DateTime.class)))
                .thenReturn(Arrays.asList(dashboardItem));
        when(stateStoreMock.queryModelsWithActions(DashboardItem.class,
                Action.SYNCED, Action.TO_UPDATE, Action.TO_DELETE)).thenReturn(Arrays.asList(dashboardItem));

        dashboardController.update();
    }
}
