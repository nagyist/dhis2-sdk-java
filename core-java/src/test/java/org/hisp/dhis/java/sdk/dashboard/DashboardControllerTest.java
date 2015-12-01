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
import org.hisp.dhis.java.sdk.common.persistence.ITransactionManager;
import org.hisp.dhis.java.sdk.common.preferences.ILastUpdatedPreferences;
import org.hisp.dhis.java.sdk.common.preferences.ResourceType;
import org.hisp.dhis.java.sdk.models.common.state.Action;
import org.hisp.dhis.java.sdk.models.dashboard.Dashboard;
import org.hisp.dhis.java.sdk.models.dashboard.DashboardContent;
import org.hisp.dhis.java.sdk.models.dashboard.DashboardElement;
import org.hisp.dhis.java.sdk.models.dashboard.DashboardItem;
import org.hisp.dhis.java.sdk.utils.ModelUtils;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class DashboardControllerTest {
    private IStateStore stateStoreMock;
    private IDashboardStore dashboardStoreMock;
    private IDashboardApiClient dashboardApiClientMock;

    private ModelUtils modelUtilsMock;
    private ILastUpdatedPreferences lastUpdatedPreferencesMock;
    private ITransactionManager transactionManagerMock;

    private Dashboard dashboard;
    private DashboardItem dashboardItem;
    private DashboardElement dashboardElement;

    private IDashboardController dashboardController;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        stateStoreMock = mock(IStateStore.class);
        dashboardStoreMock = mock(IDashboardStore.class);
        dashboardApiClientMock = mock(IDashboardApiClient.class);

        modelUtilsMock = mock(ModelUtils.class);
        lastUpdatedPreferencesMock = mock(ILastUpdatedPreferences.class);
        transactionManagerMock = mock(ITransactionManager.class);

        dashboard = new Dashboard();
        dashboardItem = new DashboardItem();
        dashboardElement = new DashboardElement();

        dashboard.setId(1L);
        dashboardElement.setId(1L);

        dashboardItem.setId(1L);
        dashboardItem.setType(DashboardContent.TYPE_CHART);

        dashboardItem.setDashboard(dashboard);
        dashboardElement.setDashboardItem(dashboardItem);

        dashboardController = spy(new DashboardController2(
                stateStoreMock,
                dashboardStoreMock,
                dashboardApiClientMock,
                modelUtilsMock,
                lastUpdatedPreferencesMock,
                transactionManagerMock));
    }

    @Test
    public void syncShouldCallBothUpdateAndSync() {
        dashboardController.sync();

        verify(dashboardController, times(1)).update();
        verify(dashboardController, times(1)).send();
    }

    @Test
    public void updateShouldCallUpdateMethodsOnController() {
        dashboardController.update();

        verify(dashboardController, times(1)).updateDashboards(any(DateTime.class));
        verify(dashboardController, times(1)).updateDashboardItems(any(DateTime.class));
        /* verify(dashboardController, times(1)).updateDashboardElements(anyListOf(
                DashboardItem.class), any(DateTime.class)); */
    }

    @Test
    public void updateDashboardsShouldCallApiClientWithLastUpdatedField() {
        DateTime lastUpdated = DateTime.now();

        when(lastUpdatedPreferencesMock.get(ResourceType.DASHBOARDS)).thenReturn(lastUpdated);

        dashboardController.updateDashboards(lastUpdated);

        verify(dashboardApiClientMock, times(1)).getDashboardUids(lastUpdated);
        verify(dashboardApiClientMock, times(1)).getDashboards(lastUpdated);
    }

    @Test
    public void updateDashboardsShouldQueryStore() {
        DateTime lastUpdated = DateTime.now();

        when(stateStoreMock.queryModelsWithActions(Dashboard.class,
                Action.SYNCED, Action.TO_UPDATE, Action.TO_DELETE)).thenReturn(Arrays.asList(dashboard));

        dashboardController.updateDashboards(lastUpdated);

        verify(stateStoreMock, times(1)).queryModelsWithActions(Dashboard.class,
                Action.SYNCED, Action.TO_UPDATE, Action.TO_DELETE);
    }

    @Test
    public void updateDashboardsShouldMergeUpdatedWithPersistedData() {
        DateTime lastUpdated = DateTime.now();

        List<Dashboard> actualDashboards = Arrays.asList(dashboard);
        List<Dashboard> updatedDashboards = Arrays.asList(dashboard);
        List<Dashboard> persistedDashboards = Arrays.asList(dashboard);

        when(dashboardApiClientMock.getDashboardUids(any(DateTime.class))).thenReturn(actualDashboards);
        when(dashboardApiClientMock.getDashboards(any(DateTime.class))).thenReturn(updatedDashboards);
        when(stateStoreMock.queryModelsWithActions(Dashboard.class, Action.SYNCED, Action.TO_UPDATE,
                Action.TO_DELETE)).thenReturn(persistedDashboards);

        dashboardController.updateDashboards(lastUpdated);

        verify(modelUtilsMock, times(1)).merge(actualDashboards, updatedDashboards, persistedDashboards);
    }

    @Test
    public void updateDashboardItemsShouldCallApiClient() {
        DateTime lastUpdated = DateTime.now();

        List<DashboardItem> actualDashboardItems = Arrays.asList(dashboardItem);
        List<DashboardItem> updatedDashboardItems = Arrays.asList(dashboardItem);
        List<DashboardItem> persistedDashboardItems = Arrays.asList(dashboardItem);

        when(dashboardApiClientMock.getBaseDashboardItems(lastUpdated)).thenReturn(actualDashboardItems);
        when(dashboardApiClientMock.getDashboardItems(lastUpdated)).thenReturn(updatedDashboardItems);
        when(stateStoreMock.queryModelsWithActions(DashboardItem.class, Action.SYNCED,
                Action.TO_UPDATE, Action.TO_DELETE)).thenReturn(persistedDashboardItems);

        dashboardController.updateDashboardItems(lastUpdated);

        verify(dashboardApiClientMock, times(1)).getBaseDashboardItems(lastUpdated);
        verify(dashboardApiClientMock, times(1)).getDashboardItems(lastUpdated);
        verify(stateStoreMock, times(1)).queryModelsWithActions(DashboardItem.class,
                Action.SYNCED, Action.TO_UPDATE, Action.TO_DELETE);
    }

    /* @Test
    public void testUpdateShouldPullNewDashboardsFromServer() {
        dashboardController.update();

        verify(dashboardApiClientMock, times(1)).getDashboardUids(null);
        verify(dashboardApiClientMock, times(1)).getDashboards(null);
    }

    @Test
    public void testUpdateShouldQuerySyncedDashboardsFromStorage() {
        dashboardController.update();

        verify(stateStoreMock, times(1)).queryModelsWithActions(Dashboard.class,
                Action.SYNCED, Action.TO_UPDATE, Action.TO_DELETE);
        verify(stateStoreMock, times(2)).queryModelsWithActions(DashboardItem.class,
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
    public void testUpdateShouldGetBasicFieldsForUpdatedDashboardItems() {
        List<Dashboard> dashboards = new ArrayList<>();
        List<DashboardItem> dashboardItems = new ArrayList<>();
        List<DashboardElement> dashboardElements = new ArrayList<>();

        dashboardElements.add(dashboardElement);
        dashboardItems.add(dashboardItem);
        dashboards.add(dashboard);

        dashboard.setDashboardItems(dashboardItems);
        dashboardItem.setDashboardElements(dashboardElements);

        when(modelUtilsMock.merge(anyListOf(Dashboard.class), anyListOf(Dashboard.class),
                anyListOf(Dashboard.class))).thenReturn(dashboards);

        dashboardController.update();

        verify(dashboardApiClientMock, times(1)).getBaseDashboardItems(any(DateTime.class));
        verify(stateStoreMock, atLeastOnce()).queryModelsWithActions(DashboardItem.class,
                Action.SYNCED, Action.TO_UPDATE, Action.TO_DELETE);
    }

    @Test
    public void testUpdateShouldCheckForLocalItemsAndSetId() {

    }

    @Test
    public void testUpdateShouldUpdateShapeOfDashboardItem() {

    } */
}
