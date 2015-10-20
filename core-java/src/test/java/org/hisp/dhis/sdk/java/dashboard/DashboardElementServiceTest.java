package org.hisp.dhis.sdk.java.dashboard;

import org.hisp.dhis.java.sdk.models.common.state.Action;
import org.hisp.dhis.java.sdk.models.dashboard.DashboardElement;
import org.hisp.dhis.java.sdk.models.dashboard.DashboardItem;
import org.hisp.dhis.sdk.java.common.IStateStore;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class DashboardElementServiceTest {
    private static final int DASHBOARD_ELEMENTS_COUNT_BY_ACTION = 64;

    private DashboardItem dashboardItem;
    private DashboardElement dashboardElementSynced;
    private DashboardElement dashboardElementToPost;
    private DashboardElement dashboardElementToUpdate;
    private DashboardElement dashboardElementToDelete;

    private List<DashboardElement> noneRemovedDashboardElements;
    private Map<DashboardElement, Action> dashboardElementActionMap;
    private Map<Long, Action> actionMap;

    private IStateStore stateStore;
    private IDashboardItemService dashboardItemService;
    private IDashboardElementStore dashboardElementStore;

    /* the only method which is spied here is count() */
    private IDashboardElementService dashboardElementServiceSpy;
    private IDashboardElementService dashboardElementService;

    @Before
    public void setUp() {
        dashboardItem = mock(DashboardItem.class);
        dashboardElementSynced = mock(DashboardElement.class);
        dashboardElementToPost = mock(DashboardElement.class);
        dashboardElementToUpdate = mock(DashboardElement.class);
        dashboardElementToDelete = mock(DashboardElement.class);

        when(dashboardElementSynced.getDashboardItem()).thenReturn(dashboardItem);
        when(dashboardElementToDelete.getDashboardItem()).thenReturn(dashboardItem);

        /* Mocking state store */
        stateStore = mock(IStateStore.class);
        when(stateStore.queryActionForModel(dashboardElementSynced)).thenReturn(Action.SYNCED);
        when(stateStore.queryActionForModel(dashboardElementToPost)).thenReturn(Action.TO_POST);
        when(stateStore.queryActionForModel(dashboardElementToUpdate)).thenReturn(Action.TO_UPDATE);
        when(stateStore.queryActionForModel(dashboardElementToDelete)).thenReturn(Action.TO_DELETE);
        when(stateStore.saveActionForModel(any(DashboardElement.class), any(Action.class))).thenReturn(true);

        dashboardElementActionMap = new HashMap<>();
        actionMap = new HashMap<>();
        for (long i = 0; i < DASHBOARD_ELEMENTS_COUNT_BY_ACTION; i++) {
            DashboardElement dashboardElement = mock(DashboardElement.class);
            when(dashboardElement.getId()).thenReturn(i);

            Action action;
            if (i % 2 == 0) {
                action = Action.TO_DELETE;
            } else {
                if (i % 3 == 0) {
                    action = Action.TO_POST;
                } else {
                    action = Action.SYNCED;
                }
            }

            actionMap.put(i, action);
            dashboardElementActionMap.put(dashboardElement, action);
        }

        noneRemovedDashboardElements = new ArrayList<>();
        when(stateStore.filterModelsByAction(DashboardElement.class,
                Action.TO_DELETE)).thenReturn(noneRemovedDashboardElements);
        when(stateStore.queryActionsForModel(DashboardElement.class)).thenReturn(actionMap);

        dashboardElementStore = mock(IDashboardElementStore.class);
        when(dashboardElementStore.delete(any(DashboardElement.class))).thenReturn(true);
        when(dashboardElementStore.queryByDashboardItem(dashboardItem))
                .thenReturn(new ArrayList<>(dashboardElementActionMap.keySet()));

        dashboardItemService = mock(IDashboardItemService.class);
        when(dashboardItemService.remove(any(DashboardItem.class))).thenReturn(true);

        dashboardElementService = new DashboardElementService(stateStore,
                dashboardElementStore, dashboardItemService);
        dashboardElementServiceSpy = spy(new DashboardElementService(stateStore,
                dashboardElementStore, dashboardItemService));

        doReturn(1).when(dashboardElementServiceSpy).count(any(DashboardItem.class));
        // when(dashboardElementServiceSpy.count(any(DashboardItem.class))).thenReturn(1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRemoveNullDashboardElement() {
        dashboardElementServiceSpy.remove(null);
    }

    @Test
    public void testRemoveNoneExistingDashboardElement() {
        assertFalse(dashboardElementServiceSpy.remove(mock(DashboardElement.class)));
    }

    @Test
    public void testRemoveDashboardElementSynced() {
        assertTrue(dashboardElementServiceSpy.remove(dashboardElementSynced));
        verify(stateStore).saveActionForModel(dashboardElementSynced, Action.TO_DELETE);
    }

    @Test
    public void testRemoveDashboardElementToUpdate() {
        assertTrue(dashboardElementServiceSpy.remove(dashboardElementToUpdate));
        verify(stateStore).saveActionForModel(dashboardElementToUpdate, Action.TO_DELETE);
    }

    @Test
    public void testRemoveDashboardElementToPost() {
        assertTrue(dashboardElementServiceSpy.remove(dashboardElementToPost));
        verify(stateStore, never()).saveActionForModel(any(DashboardElement.class), any(Action.class));
        verify(dashboardElementStore).delete(dashboardElementToPost);
    }

    @Test
    public void testRemoveDashboardElementToDelete() {
        assertFalse(dashboardElementServiceSpy.remove(dashboardElementToDelete));
        verify(stateStore, never()).saveActionForModel(any(DashboardElement.class), any(Action.class));

        /* if element was already marker as removed, dashboard item service should not be called */
        verify(dashboardItemService, never()).remove(dashboardItem);
    }

    @Test
    public void testRemoveLastDashboardElement() {
        assertTrue(dashboardElementServiceSpy.remove(dashboardElementSynced));
        verify(dashboardItemService).remove(dashboardItem);
    }

    @Test
    public void testListDashboardElements() {
        List<DashboardElement> dashboardElements = dashboardElementServiceSpy.list();
        assertEquals(dashboardElements, noneRemovedDashboardElements);
        verify(stateStore).filterModelsByAction(DashboardElement.class, Action.TO_DELETE);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testListDashboardElementsByNullDashboardItem() {
        assertNull(dashboardElementServiceSpy.list(null));
    }

    @Test
    public void testListDashboardElementsByDashboardItem() {
        List<DashboardElement> dashboardElements = dashboardElementServiceSpy.list(dashboardItem);
        verify(dashboardElementStore).queryByDashboardItem(dashboardItem);
        verify(stateStore).queryActionsForModel(DashboardElement.class);

        for (DashboardElement dashboardElement : dashboardElements) {
            assertNotEquals(Action.TO_DELETE, dashboardElementActionMap.get(dashboardElement));
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCountDashboardElementsByNullDashboardItem() {
        dashboardElementService.count(null);
    }

    @Test
    public void testCountDashboardElements() {
        int dashboardElementCount = dashboardElementService.count(dashboardItem);
        assertEquals(noneRemovedDashboardElements.size(), dashboardElementCount);
        verify(stateStore).filterModelsByAction(DashboardElement.class, Action.TO_DELETE);
    }
}
