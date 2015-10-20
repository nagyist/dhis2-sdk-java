package org.hisp.dhis.sdk.java.dashboard;

import org.hisp.dhis.java.sdk.models.common.state.Action;
import org.hisp.dhis.java.sdk.models.dashboard.DashboardElement;
import org.hisp.dhis.java.sdk.models.dashboard.DashboardItem;
import org.hisp.dhis.sdk.java.common.IStateStore;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

public class DashboardElementServiceTest {
    private DashboardItem dashboardItem;
    private DashboardElement dashboardElementSynced;
    private DashboardElement dashboardElementToPost;
    private DashboardElement dashboardElementToUpdate;
    private DashboardElement dashboardElementToDelete;

    private IStateStore stateStore;
    private IDashboardItemService dashboardItemService;
    private IDashboardElementStore dashboardElementStore;

    private IDashboardElementService dashboardElementService;

    @Before
    public void setUp() {
        dashboardItem = mock(DashboardItem.class);
        dashboardElementSynced = mock(DashboardElement.class);
        dashboardElementToPost = mock(DashboardElement.class);
        dashboardElementToUpdate = mock(DashboardElement.class);
        dashboardElementToDelete = mock(DashboardElement.class);

        when(dashboardElementSynced.getDashboardItem()).thenReturn(dashboardItem);

        stateStore = mock(IStateStore.class);
        dashboardElementStore = mock(IDashboardElementStore.class);
        dashboardItemService = mock(IDashboardItemService.class);

        when(stateStore.queryActionForModel(dashboardElementSynced)).thenReturn(Action.SYNCED);
        when(stateStore.queryActionForModel(dashboardElementToPost)).thenReturn(Action.TO_POST);
        when(stateStore.queryActionForModel(dashboardElementToUpdate)).thenReturn(Action.TO_UPDATE);
        when(stateStore.queryActionForModel(dashboardElementToDelete)).thenReturn(Action.TO_DELETE);
        when(stateStore.saveActionForModel(any(DashboardElement.class), any(Action.class))).thenReturn(true);

        when(dashboardElementStore.delete(any(DashboardElement.class))).thenReturn(true);

        when(dashboardItemService.remove(any(DashboardItem.class))).thenReturn(true);

        dashboardElementService = spy(new DashboardElementService2(stateStore, dashboardElementStore, dashboardItemService));
        when(dashboardElementService.count(any(DashboardItem.class))).thenReturn(1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRemoveNullDashboardElement() {
        dashboardElementService.remove(null);
    }

    @Test
    public void testRemoveNoneExistingDashboardElement() {
        assertFalse(dashboardElementService.remove(mock(DashboardElement.class)));
    }

    @Test
    public void testRemoveDashboardElementSynced() {
        assertTrue(dashboardElementService.remove(dashboardElementSynced));
        verify(stateStore).saveActionForModel(dashboardElementSynced, Action.TO_DELETE);
    }

    @Test
    public void testRemoveDashboardElementToUpdate() {
        assertTrue(dashboardElementService.remove(dashboardElementToUpdate));
        verify(stateStore).saveActionForModel(dashboardElementToUpdate, Action.TO_DELETE);
    }

    @Test
    public void testRemoveDashboardElementToPost() {
        assertTrue(dashboardElementService.remove(dashboardElementToPost));
        verify(stateStore, never()).saveActionForModel(any(DashboardElement.class), any(Action.class));
        verify(dashboardElementStore).delete(dashboardElementToPost);
    }

    @Test
    public void testRemoveDashboardElementToDelete() {
        assertFalse(dashboardElementService.remove(dashboardElementToDelete));
        verify(stateStore, never()).saveActionForModel(any(DashboardElement.class), any(Action.class));
    }

    @Test
    public void testRemoveLastDashboardElement() {
        assertTrue(dashboardElementService.remove(dashboardElementSynced));
        verify(dashboardItemService).remove(dashboardItem);
    }
}
