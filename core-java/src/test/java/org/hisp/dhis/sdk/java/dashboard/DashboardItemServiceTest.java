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

        dashboardItemService = new DashboardItemService(dashboardItemStoreMock, stateStoreMock);
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
        DashboardItem dashboardItemSynced = mock(DashboardItem.class);
        DashboardItem dashboardItemToPost = mock(DashboardItem.class);
        DashboardItem dashboardItemToUpdate = mock(DashboardItem.class);

        when(dashboardItemStoreMock.queryById(1L)).thenReturn(dashboardItemSynced);
        when(dashboardItemStoreMock.queryById(2L)).thenReturn(dashboardItemToPost);
        when(dashboardItemStoreMock.queryById(3L)).thenReturn(dashboardItemToUpdate);

        when(stateStoreMock.queryActionForModel(dashboardItemSynced)).thenReturn(Action.SYNCED);
        when(stateStoreMock.queryActionForModel(dashboardItemToPost)).thenReturn(Action.TO_POST);
        when(stateStoreMock.queryActionForModel(dashboardItemToUpdate)).thenReturn(Action.TO_UPDATE);

        DashboardItem dashboardItemSyncedResult = dashboardItemService.get(1L);
        DashboardItem dashboardItemToPostResult = dashboardItemService.get(2L);
        DashboardItem dashboardItemToUpdateResult = dashboardItemService.get(3L);

        assertEquals(dashboardItemSyncedResult, dashboardItemSynced);
        assertEquals(dashboardItemToPostResult, dashboardItemToPost);
        assertEquals(dashboardItemToUpdateResult, dashboardItemToUpdate);

        verify(dashboardItemStoreMock, times(1)).queryById(1L);
        verify(dashboardItemStoreMock, times(1)).queryById(2L);
        verify(dashboardItemStoreMock, times(1)).queryById(3L);

        verify(stateStoreMock, times(1)).queryActionForModel(dashboardItemSynced);
        verify(stateStoreMock, times(1)).queryActionForModel(dashboardItemToPost);
        verify(stateStoreMock, times(1)).queryActionForModel(dashboardItemToUpdate);
    }

    @Test
    public void testGetDashboardItemByUidShouldReturnNullForRemovedItem() {
        when(dashboardItemStoreMock.queryByUid(anyString())).thenReturn(dashboardItemMock);
        when(stateStoreMock.queryActionForModel(dashboardItemMock)).thenReturn(Action.TO_DELETE);

        DashboardItem dashboardItem = dashboardItemService.get("dasdfgdd");

        assertNull(dashboardItem);
        verify(dashboardItemStoreMock, times(1)).queryByUid("dasdfgdd");
        verify(stateStoreMock, times(1)).queryActionForModel(dashboardItemMock);
    }


    @Test
    public void testGetDashboardItemByUidShouldReturnItem() {
        DashboardItem dashboardItemSynced = mock(DashboardItem.class);
        DashboardItem dashboardItemToPost = mock(DashboardItem.class);
        DashboardItem dashboardItemToUpdate = mock(DashboardItem.class);

        when(dashboardItemStoreMock.queryByUid("gadgdfg")).thenReturn(dashboardItemSynced);
        when(dashboardItemStoreMock.queryByUid("sdfds4gdgad")).thenReturn(dashboardItemToPost);
        when(dashboardItemStoreMock.queryByUid("sdfgfsd234")).thenReturn(dashboardItemToUpdate);

        when(stateStoreMock.queryActionForModel(dashboardItemSynced)).thenReturn(Action.SYNCED);
        when(stateStoreMock.queryActionForModel(dashboardItemToPost)).thenReturn(Action.TO_POST);
        when(stateStoreMock.queryActionForModel(dashboardItemToUpdate)).thenReturn(Action.TO_UPDATE);

        DashboardItem dashboardItemSyncedResult = dashboardItemService.get("gadgdfg");
        DashboardItem dashboardItemToPostResult = dashboardItemService.get("sdfds4gdgad");
        DashboardItem dashboardItemToUpdateResult = dashboardItemService.get("sdfgfsd234");

        assertEquals(dashboardItemSyncedResult, dashboardItemSynced);
        assertEquals(dashboardItemToPostResult, dashboardItemToPost);
        assertEquals(dashboardItemToUpdateResult, dashboardItemToUpdate);

        verify(dashboardItemStoreMock, times(1)).queryByUid("gadgdfg");
        verify(dashboardItemStoreMock, times(1)).queryByUid("sdfds4gdgad");
        verify(dashboardItemStoreMock, times(1)).queryByUid("sdfgfsd234");

        verify(stateStoreMock, times(1)).queryActionForModel(dashboardItemSynced);
        verify(stateStoreMock, times(1)).queryActionForModel(dashboardItemToPost);
        verify(stateStoreMock, times(1)).queryActionForModel(dashboardItemToUpdate);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRemoveNullDashboardItem() {
        dashboardItemService.remove(null);
    }

    @Test
    public void testRemoveNotExistingDashboardItem() {
        when(stateStoreMock.queryActionForModel(dashboardItemMock)).thenReturn(null);

        boolean status = dashboardItemService.remove(dashboardItemMock);

        assertFalse(status);
        verify(stateStoreMock, times(1)).queryActionForModel(dashboardItemMock);
    }

    @Test
    public void testRemoveDashboardItemWhichShouldBePostedToServer() {
        when(stateStoreMock.queryActionForModel(dashboardItemMock)).thenReturn(Action.TO_POST);
        when(dashboardItemStoreMock.delete(dashboardItemMock)).thenReturn(true);

        boolean status = dashboardItemService.remove(dashboardItemMock);

        assertTrue(status);
        verify(stateStoreMock, times(1)).queryActionForModel(dashboardItemMock);
        verify(dashboardItemStoreMock, times(1)).delete(dashboardItemMock);
    }

    @Test
    public void testRemoveDashboardItemWhichShouldBePutToServer() {
        when(stateStoreMock.queryActionForModel(dashboardItemMock)).thenReturn(Action.TO_UPDATE);
        when(stateStoreMock.saveActionForModel(dashboardItemMock, Action.TO_DELETE)).thenReturn(true);

        boolean status = dashboardItemService.remove(dashboardItemMock);

        assertTrue(status);
        verify(stateStoreMock, times(1)).queryActionForModel(dashboardItemMock);
        verify(stateStoreMock, times(1)).saveActionForModel(dashboardItemMock, Action.TO_DELETE);
    }

    @Test
    public void testRemoveSyncedDashboardItem() {
        when(stateStoreMock.queryActionForModel(dashboardItemMock)).thenReturn(Action.SYNCED);
        when(stateStoreMock.saveActionForModel(dashboardItemMock, Action.TO_DELETE)).thenReturn(true);

        boolean status = dashboardItemService.remove(dashboardItemMock);

        assertTrue(status);
        verify(stateStoreMock, times(1)).queryActionForModel(dashboardItemMock);
        verify(stateStoreMock, times(1)).saveActionForModel(dashboardItemMock, Action.TO_DELETE);
    }


    @Test
    public void testRemoveDashboardItemWhichIsAlreadyRemoved() {
        when(stateStoreMock.queryActionForModel(dashboardItemMock)).thenReturn(Action.TO_DELETE);

        boolean status = dashboardItemService.remove(dashboardItemMock);

        assertFalse(status);
        verify(stateStoreMock, times(1)).queryActionForModel(dashboardItemMock);
        verify(stateStoreMock, never()).saveActionForModel(dashboardItemMock, Action.TO_DELETE);
    }
}
