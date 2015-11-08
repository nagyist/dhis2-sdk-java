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

public class DashboardItemServiceTest {
    private Dashboard dashboard;
    private DashboardItem dashboardItem;
    private DashboardElement dashboardElement;

    private IDashboardItemStore dashboardItemStoreMock;
    private IDashboardElementService dashboardElementServiceMock;
    private IStateStore stateStoreMock;

    private IDashboardItemService dashboardItemService;

    @Before
    public void setUp() {
        dashboard = new Dashboard();
        dashboardItem = new DashboardItem();
        dashboardElement = new DashboardElement();

        dashboardItemStoreMock = mock(IDashboardItemStore.class);
        dashboardElementServiceMock = mock(IDashboardElementService.class);
        stateStoreMock = mock(IStateStore.class);

        dashboardItemService = new DashboardItemService(dashboardItemStoreMock, stateStoreMock,
                dashboardElementServiceMock);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testListDashboardItemsShouldThrowExceptionOnNullArgument() {
        dashboardItemService.list(null);
    }

    @Test
    public void testListDashboardItemsForDashboard() {
        DashboardItem dashboardItemSynced = new DashboardItem();
        DashboardItem dashboardItemToPost = new DashboardItem();
        DashboardItem dashboardItemToUpdate = new DashboardItem();
        DashboardItem dashboardItemToDelete = new DashboardItem();

        List<DashboardItem> dashboardItemList = Arrays.asList(dashboardItemSynced,
                dashboardItemToPost, dashboardItemToUpdate, dashboardItemToDelete);

        dashboardItemSynced.setId(1L);
        dashboardItemToPost.setId(2L);
        dashboardItemToUpdate.setId(3L);
        dashboardItemToDelete.setId(4L);

        Map<Long, Action> actionMap = new HashMap<>();
        actionMap.put(1L, Action.SYNCED);
        actionMap.put(2L, Action.TO_POST);
        actionMap.put(3L, Action.TO_UPDATE);
        actionMap.put(4L, Action.TO_DELETE);

        when(dashboardItemStoreMock.queryByDashboard(dashboard)).thenReturn(dashboardItemList);
        when(stateStoreMock.queryActionsForModel(DashboardItem.class)).thenReturn(actionMap);

        List<DashboardItem> dashboardItems = dashboardItemService.list(dashboard);

        verify(dashboardItemStoreMock, times(1)).queryByDashboard(dashboard);
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
    public void testGetDashboardItemByIdForItemWithStateToDelete() {
        when(dashboardItemStoreMock.queryById(anyInt())).thenReturn(dashboardItem);
        when(stateStoreMock.queryActionForModel(dashboardItem)).thenReturn(Action.TO_DELETE);

        DashboardItem dashboardItem = dashboardItemService.get(12);
        assertNull(dashboardItem);
        verify(dashboardItemStoreMock, times(1)).queryById(12);
        verify(stateStoreMock, times(1)).queryActionForModel(this.dashboardItem);
    }

    @Test
    public void testGetDashboardItemByIdForItemWithStateToPost() {
        when(dashboardItemStoreMock.queryById(anyInt())).thenReturn(dashboardItem);
        when(stateStoreMock.queryActionForModel(dashboardItem)).thenReturn(Action.TO_POST);

        DashboardItem dashboardItemToPostResult = dashboardItemService.get(75L);

        assertEquals(dashboardItemToPostResult, dashboardItem);
        verify(dashboardItemStoreMock, times(1)).queryById(75L);
        verify(stateStoreMock, times(1)).queryActionForModel(dashboardItem);
    }

    @Test
    public void testGetDashboardItemByIdForItemWithStateToUpdate() {
        when(dashboardItemStoreMock.queryById(anyInt())).thenReturn(dashboardItem);
        when(stateStoreMock.queryActionForModel(dashboardItem)).thenReturn(Action.TO_UPDATE);

        DashboardItem dashboardItemToUpdateResult = dashboardItemService.get(66L);

        assertEquals(dashboardItemToUpdateResult, dashboardItem);
        verify(dashboardItemStoreMock, times(1)).queryById(66L);
        verify(stateStoreMock, times(1)).queryActionForModel(dashboardItem);
    }

    @Test
    public void testGetDashboardItemByIdWithStateSynced() {
        when(dashboardItemStoreMock.queryById(anyInt())).thenReturn(dashboardItem);
        when(stateStoreMock.queryActionForModel(dashboardItem)).thenReturn(Action.SYNCED);

        DashboardItem dashboardItemSyncedResult = dashboardItemService.get(85L);

        assertEquals(dashboardItemSyncedResult, dashboardItem);
        verify(dashboardItemStoreMock, times(1)).queryById(85L);
        verify(stateStoreMock, times(1)).queryActionForModel(dashboardItem);
    }

    @Test
    public void testGetDashboardItemByUidWithStateToDelete() {
        when(dashboardItemStoreMock.queryByUid(anyString())).thenReturn(dashboardItem);
        when(stateStoreMock.queryActionForModel(dashboardItem)).thenReturn(Action.TO_DELETE);

        DashboardItem dashboardItem = dashboardItemService.get("dasdfgdd");

        assertNull(dashboardItem);
        verify(dashboardItemStoreMock, times(1)).queryByUid("dasdfgdd");
        verify(stateStoreMock, times(1)).queryActionForModel(this.dashboardItem);
    }

    @Test
    public void testGetDashboardItemByUidWithStateSynced() {
        when(dashboardItemStoreMock.queryByUid(anyString())).thenReturn(dashboardItem);
        when(stateStoreMock.queryActionForModel(dashboardItem)).thenReturn(Action.SYNCED);

        DashboardItem dashboardItemSyncedResult = dashboardItemService.get("gadgdfg");

        assertEquals(dashboardItemSyncedResult, dashboardItem);
        verify(dashboardItemStoreMock, times(1)).queryByUid("gadgdfg");
        verify(stateStoreMock, times(1)).queryActionForModel(dashboardItem);
    }

    @Test
    public void testGetDashboardItemByUidWithStateToPost() {
        when(dashboardItemStoreMock.queryByUid(anyString())).thenReturn(dashboardItem);
        when(stateStoreMock.queryActionForModel(dashboardItem)).thenReturn(Action.TO_POST);

        DashboardItem dashboardItemToPostResult = dashboardItemService.get("sdfds4gdgad");

        assertEquals(dashboardItemToPostResult, dashboardItem);
        verify(dashboardItemStoreMock, times(1)).queryByUid("sdfds4gdgad");
        verify(stateStoreMock, times(1)).queryActionForModel(dashboardItem);
    }

    @Test
    public void testGetDashboardItemByUidWithStateToUpdate() {
        when(dashboardItemStoreMock.queryByUid(anyString())).thenReturn(dashboardItem);
        when(stateStoreMock.queryActionForModel(dashboardItem)).thenReturn(Action.TO_UPDATE);

        DashboardItem dashboardItemToUpdateResult = dashboardItemService.get("sdfgfsd234");

        assertEquals(dashboardItemToUpdateResult, dashboardItem);
        verify(dashboardItemStoreMock, times(1)).queryByUid("sdfgfsd234");
        verify(stateStoreMock, times(1)).queryActionForModel(dashboardItem);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRemoveNullDashboardItem() {
        dashboardItemService.remove(null);
    }

    @Test
    public void testRemoveNotExistingDashboardItem() {
        when(stateStoreMock.queryActionForModel(dashboardItem)).thenReturn(null);

        boolean status = dashboardItemService.remove(dashboardItem);

        assertFalse(status);
        verify(stateStoreMock, times(1)).queryActionForModel(dashboardItem);
    }

    @Test
    public void testRemoveDashboardItemWithStateToPost() {
        when(stateStoreMock.queryActionForModel(dashboardItem)).thenReturn(Action.TO_POST);
        when(dashboardItemStoreMock.delete(dashboardItem)).thenReturn(true);

        boolean status = dashboardItemService.remove(dashboardItem);

        assertTrue(status);
        verify(stateStoreMock, times(1)).queryActionForModel(dashboardItem);
        verify(dashboardItemStoreMock, times(1)).delete(dashboardItem);
    }

    @Test
    public void testRemoveDashboardItemWWithStateToUpdate() {
        when(stateStoreMock.queryActionForModel(dashboardItem)).thenReturn(Action.TO_UPDATE);
        when(stateStoreMock.saveActionForModel(dashboardItem, Action.TO_DELETE)).thenReturn(true);

        boolean status = dashboardItemService.remove(dashboardItem);

        assertTrue(status);
        verify(stateStoreMock, times(1)).queryActionForModel(dashboardItem);
        verify(stateStoreMock, times(1)).saveActionForModel(dashboardItem, Action.TO_DELETE);
    }

    @Test
    public void testRemoveDashboardItemWithStateSynced() {
        when(stateStoreMock.queryActionForModel(dashboardItem)).thenReturn(Action.SYNCED);
        when(stateStoreMock.saveActionForModel(dashboardItem, Action.TO_DELETE)).thenReturn(true);

        boolean status = dashboardItemService.remove(dashboardItem);

        assertTrue(status);
        verify(stateStoreMock, times(1)).queryActionForModel(dashboardItem);
        verify(stateStoreMock, times(1)).saveActionForModel(dashboardItem, Action.TO_DELETE);
    }


    @Test
    public void testRemoveDashboardItemWithStateToDelete() {
        when(stateStoreMock.queryActionForModel(dashboardItem)).thenReturn(Action.TO_DELETE);

        boolean status = dashboardItemService.remove(dashboardItem);

        assertFalse(status);
        verify(stateStoreMock, times(1)).queryActionForModel(dashboardItem);
        verify(stateStoreMock, never()).saveActionForModel(dashboardItem, Action.TO_DELETE);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateDashboardItemWithNullDashboardShouldThrow() {
        dashboardItemService.create(null, DashboardContent.TYPE_CHART);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateDashboardItemWithNullTypeShouldThrow() {
        dashboardItemService.create(dashboard, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateDashboardItemWithUnsupportedTypeShouldThrow() {
        dashboardItemService.create(dashboard, "SomeType");
    }

    @Test
    public void testCreateDashboardItem() {
        DashboardItem dashboardItem = dashboardItemService.create(dashboard, DashboardContent.TYPE_CHART);

        assertNotNull(dashboardItem.getUId());
        assertNotNull(dashboardItem.getCreated());
        assertNotNull(dashboardItem.getLastUpdated());
        assertNotNull(dashboardItem.getName());
        assertNotNull(dashboardItem.getDisplayName());
        assertNotNull(dashboardItem.getAccess());

        assertTrue(DashboardItem.SHAPE_DOUBLE_WIDTH.equals(dashboardItem.getShape()) ||
                DashboardItem.SHAPE_FULL_WIDTH.equals(dashboardItem.getShape()) ||
                DashboardItem.SHAPE_NORMAL.equals(dashboardItem.getShape()));

        assertEquals(dashboardItem.getType(), DashboardContent.TYPE_CHART);
        assertEquals(dashboardItem.getDashboard(), dashboard);
    }

    @Test
    public void testCreateDashboardItemWithDifferentTypes() {
        dashboardItemService.create(dashboard, DashboardContent.TYPE_CHART);
        dashboardItemService.create(dashboard, DashboardContent.TYPE_EVENT_CHART);
        dashboardItemService.create(dashboard, DashboardContent.TYPE_MAP);
        dashboardItemService.create(dashboard, DashboardContent.TYPE_REPORT_TABLE);
        dashboardItemService.create(dashboard, DashboardContent.TYPE_USERS);
        dashboardItemService.create(dashboard, DashboardContent.TYPE_REPORTS);
        dashboardItemService.create(dashboard, DashboardContent.TYPE_EVENT_REPORT);
        dashboardItemService.create(dashboard, DashboardContent.TYPE_RESOURCES);
        dashboardItemService.create(dashboard, DashboardContent.TYPE_MESSAGES);
    }


    @Test(expected = IllegalArgumentException.class)
    public void testCountItemsByNullDashboardItem() {
        dashboardItemService.countElements(null);
    }

    @Test
    public void testCountItems() {
        when(dashboardElementServiceMock.list(dashboardItem)).thenReturn(
                Arrays.asList(dashboardElement, dashboardElement, dashboardElement));

        int dashboardElementCount = dashboardItemService.countElements(dashboardItem);

        assertEquals(dashboardElementCount, 3);
        verify(dashboardElementServiceMock, times(1)).list(dashboardItem);
    }
}
