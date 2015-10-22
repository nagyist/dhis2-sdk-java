package org.hisp.dhis.sdk.java.dashboard;

import org.hisp.dhis.java.sdk.models.common.state.Action;
import org.hisp.dhis.java.sdk.models.dashboard.DashboardContent;
import org.hisp.dhis.java.sdk.models.dashboard.DashboardElement;
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

public class DashboardElementServiceTest {
    private DashboardItem dashboardItemMock;
    private DashboardElement dashboardElementMock;

    private IStateStore stateStoreMock;
    private IDashboardItemService dashboardItemServiceMock;
    private IDashboardElementStore dashboardElementStoreMock;

    private IDashboardElementService dashboardElementService;

    @Before
    public void setUp() {
        dashboardItemMock = mock(DashboardItem.class);
        dashboardElementMock = mock(DashboardElement.class);

        /* Mocking state store */
        stateStoreMock = mock(IStateStore.class);
        when(stateStoreMock.saveActionForModel(any(DashboardElement.class), any(Action.class))).thenReturn(true);

        dashboardElementStoreMock = mock(IDashboardElementStore.class);
        when(dashboardElementStoreMock.delete(any(DashboardElement.class))).thenReturn(true);

        dashboardItemServiceMock = mock(IDashboardItemService.class);
        when(dashboardItemServiceMock.remove(any(DashboardItem.class))).thenReturn(true);

        dashboardElementService = spy(new DashboardElementService(stateStoreMock,
                dashboardElementStoreMock, dashboardItemServiceMock));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRemoveNullDashboardElement() {
        dashboardElementService.remove(null);
    }

    @Test
    public void testRemoveNoneExistingDashboardElement() {
        doReturn(16).when(dashboardElementService).count(any(DashboardItem.class));
        when(stateStoreMock.queryActionForModel(dashboardElementMock)).thenReturn(null);

        boolean status = dashboardElementService.remove(dashboardElementMock);

        assertFalse(status);
    }

    @Test
    public void testRemoveDashboardElementSynced() {
        doReturn(16).when(dashboardElementService).count(any(DashboardItem.class));
        when(stateStoreMock.queryActionForModel(dashboardElementMock)).thenReturn(Action.SYNCED);

        boolean status = dashboardElementService.remove(dashboardElementMock);

        assertTrue(status);
        verify(stateStoreMock, times(1)).saveActionForModel(dashboardElementMock, Action.TO_DELETE);
    }

    @Test
    public void testRemoveDashboardElementToUpdate() {
        doReturn(16).when(dashboardElementService).count(any(DashboardItem.class));
        when(stateStoreMock.queryActionForModel(dashboardElementMock)).thenReturn(Action.TO_UPDATE);

        boolean status = dashboardElementService.remove(dashboardElementMock);

        assertTrue(status);
        verify(stateStoreMock).saveActionForModel(dashboardElementMock, Action.TO_DELETE);
    }

    @Test
    public void testRemoveDashboardElementToPost() {
        doReturn(16).when(dashboardElementService).count(any(DashboardItem.class));
        when(stateStoreMock.queryActionForModel(dashboardElementMock)).thenReturn(Action.TO_POST);

        boolean status = dashboardElementService.remove(dashboardElementMock);

        assertTrue(status);
        verify(stateStoreMock, never()).saveActionForModel(any(DashboardContent.class), any(Action.class));
        verify(dashboardElementStoreMock, times(1)).delete(dashboardElementMock);
    }

    @Test
    public void testRemoveDashboardElementToDelete() {
        doReturn(16).when(dashboardElementService).count(any(DashboardItem.class));
        when(stateStoreMock.queryActionForModel(dashboardElementMock)).thenReturn(Action.TO_DELETE);

        boolean status = dashboardElementService.remove(dashboardElementMock);

        assertFalse(status);
        verify(stateStoreMock, never()).saveActionForModel(any(DashboardElement.class), any(Action.class));
    }

    @Test
    public void testRemoveLastDashboardElement() {
        doReturn(1).when(dashboardElementService).count(any(DashboardItem.class));
        when(stateStoreMock.queryActionForModel(dashboardElementMock)).thenReturn(Action.SYNCED);
        when(dashboardElementMock.getDashboardItem()).thenReturn(dashboardItemMock);

        boolean status = dashboardElementService.remove(dashboardElementMock);

        assertTrue(status);
        verify(dashboardItemServiceMock, times(1)).remove(dashboardItemMock);
    }

    @Test
    public void testListDashboardElements() {
        dashboardElementService.list();

        verify(stateStoreMock, times(1)).queryModelsWithActions(DashboardElement.class,
                Action.SYNCED, Action.TO_POST, Action.TO_UPDATE);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testListDashboardElementsByNullDashboardItem() {
        assertNull(dashboardElementService.list(null));
    }

    @Test
    public void testListDashboardElementsByDashboardItem() {
        DashboardElement dashboardElementSynced = mock(DashboardElement.class);
        DashboardElement dashboardElementToUpdate = mock(DashboardElement.class);
        DashboardElement dashboardElementToDelete = mock(DashboardElement.class);
        DashboardElement dashboardElementToPost = mock(DashboardElement.class);

        List<DashboardElement> dashboardElementMockList = Arrays.asList(dashboardElementSynced,
                dashboardElementToUpdate, dashboardElementToDelete, dashboardElementToPost);

        when(dashboardElementSynced.getId()).thenReturn(1L);
        when(dashboardElementToUpdate.getId()).thenReturn(2L);
        when(dashboardElementToDelete.getId()).thenReturn(3L);
        when(dashboardElementToPost.getId()).thenReturn(4L);

        Map<Long, Action> actionMap = new HashMap<>();
        actionMap.put(dashboardElementSynced.getId(), Action.SYNCED);
        actionMap.put(dashboardElementToUpdate.getId(), Action.TO_UPDATE);
        actionMap.put(dashboardElementToDelete.getId(), Action.TO_DELETE);
        actionMap.put(dashboardElementToPost.getId(), Action.TO_POST);

        when(stateStoreMock.queryActionsForModel(DashboardElement.class)).thenReturn(actionMap);
        when(dashboardElementStoreMock.queryByDashboardItem(dashboardItemMock)).thenReturn(dashboardElementMockList);

        List<DashboardElement> dashboardElements = dashboardElementService.list(dashboardItemMock);

        verify(dashboardElementStoreMock, times(1)).queryByDashboardItem(dashboardItemMock);
        verify(stateStoreMock, times(1)).queryActionsForModel(DashboardElement.class);
        assertFalse(dashboardElements.contains(dashboardElementToDelete));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCountDashboardElementsByNullDashboardItem() {
        dashboardElementService.count(null);
    }

    @Test
    public void testCountDashboardElements() {
        List<DashboardElement> dashboardElementsMockList = Arrays.asList(dashboardElementMock,
                dashboardElementMock, dashboardElementMock);
        when(dashboardElementStoreMock.queryByDashboardItem(dashboardItemMock))
                .thenReturn(dashboardElementsMockList);

        int dashboardElementCount = dashboardElementService.count(dashboardItemMock);

        assertEquals(dashboardElementsMockList.size(), dashboardElementCount);
        verify(dashboardElementStoreMock, times(1)).queryByDashboardItem(dashboardItemMock);
        verify(stateStoreMock, times(1)).queryActionsForModel(DashboardElement.class);
    }
}
