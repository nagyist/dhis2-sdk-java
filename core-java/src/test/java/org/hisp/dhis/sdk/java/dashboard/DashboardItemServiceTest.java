package org.hisp.dhis.sdk.java.dashboard;

import org.hisp.dhis.java.sdk.models.common.state.Action;
import org.hisp.dhis.java.sdk.models.dashboard.Dashboard;
import org.hisp.dhis.java.sdk.models.dashboard.DashboardItem;
import org.hisp.dhis.sdk.java.common.IStateStore;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class DashboardItemServiceTest {
    private Dashboard dashboardMock;
    private DashboardItem dashboardItemMock;

    private IDashboardStore dashboardStoreMock;
    private IDashboardItemStore dashboardItemStoreMock;
    private IDashboardElementStore dashboardElementStoreMock;
    private IStateStore stateStoreMock;

    private IDashboardItemService dashboardItemService;

    @Before
    public void setUp() {
        dashboardMock = mock(Dashboard.class);
        dashboardItemMock = mock(DashboardItem.class);

        dashboardStoreMock = mock(IDashboardStore.class);
        dashboardItemStoreMock = mock(IDashboardItemStore.class);
        dashboardElementStoreMock = mock(IDashboardElementStore.class);
        stateStoreMock = mock(IStateStore.class);

        dashboardItemService = new DashboardItemService2(dashboardItemStoreMock, stateStoreMock);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testListDashboardItemsShouldThrowExceptionOnNullArgument() {
        dashboardItemService.list(null);
    }

    @Test
    public void testListDashboardItemsForDashboard() {
        DashboardItem dashboardItemSynced = mock(DashboardItem.class);
        DashboardItem dashboardItemToPost = mock(DashboardItem.class);
        DashboardItem dashboardItemToUpdate = mock(DashboardItem.class);
        DashboardItem dashboardItemToDelete = mock(DashboardItem.class);

        List<DashboardItem> dashboardItemList = Arrays.asList(dashboardItemSynced,
                dashboardItemToPost, dashboardItemToUpdate, dashboardItemToDelete);

        when(dashboardItemSynced.getId()).thenReturn(1L);
        when(dashboardItemToPost.getId()).thenReturn(2L);
        when(dashboardItemToUpdate.getId()).thenReturn(3L);
        when(dashboardItemToDelete.getId()).thenReturn(4L);

        Map<Long, Action> actionMap = new HashMap<>();
        actionMap.put(1L, Action.SYNCED);
        actionMap.put(2L, Action.TO_POST);
        actionMap.put(3L, Action.TO_UPDATE);
        actionMap.put(4L, Action.TO_DELETE);

        when(dashboardItemStoreMock.queryByDashboard(dashboardMock)).thenReturn(dashboardItemList);
        when(stateStoreMock.queryActionsForModel(DashboardItem.class)).thenReturn(actionMap);

        List<DashboardItem> dashboardItems = dashboardItemService.list(dashboardMock);

        verify(dashboardItemStoreMock, times(1)).queryByDashboard(dashboardMock);
        verify(stateStoreMock, times(1)).queryActionsForModel(DashboardItem.class);
        assertFalse(dashboardItems.contains(dashboardItemToDelete));
    }

    @Test
    public void testListDashboardItems() {
        dashboardItemService.list();

        verify(stateStoreMock, times(1)).queryModelsWithActions(DashboardItem.class,
                Action.SYNCED, Action.TO_POST, Action.TO_UPDATE);
    }

    @Test
    public void testGetDashboardItemByIdShouldReturnNullForRemovedItem() {
        when(dashboardItemStoreMock.queryById(anyInt())).thenReturn(dashboardItemMock);
        when(stateStoreMock.queryActionForModel(dashboardItemMock)).thenReturn(Action.TO_DELETE);

        DashboardItem dashboardItem = dashboardItemService.get(12);
        assertNull(dashboardItem);
        verify(dashboardItemStoreMock, times(1)).queryById(12);
        verify(stateStoreMock, times(1)).queryActionForModel(dashboardItemMock);
    }


    @Test
    public void testGetDashboardItemByIdShouldReturnItem() {
        when(dashboardItemStoreMock.queryById(anyInt())).thenReturn(dashboardItemMock);
        when(stateStoreMock.queryActionForModel(dashboardItemMock)).thenReturn(Action.TO_POST);

        DashboardItem dashboardItem = dashboardItemService.get(12);
        assertEquals(dashboardItem, dashboardItemMock);
        verify(dashboardItemStoreMock, times(1)).queryById(12);
        verify(stateStoreMock, times(1)).queryActionForModel(dashboardItemMock);
    }

    @Test
    public void testGetDashboardItemByUidShouldReturnNotRemovedItem() {

    }

    @Test
    public void testRemoveNullDashboardItem() {

    }
}
