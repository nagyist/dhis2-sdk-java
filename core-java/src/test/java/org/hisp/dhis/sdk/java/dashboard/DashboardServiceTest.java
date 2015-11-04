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

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class DashboardServiceTest {
    private Dashboard dashboardMock;
    private DashboardItem dashboardItemMock;
    private DashboardContent dashboardContentMock;

    private IDashboardStore dashboardStoreMock;
    private IDashboardItemStore dashboardItemStoreMock;
    private IDashboardElementStore dashboardElementStoreMock;
    private IStateStore stateStoreMock;

    private IDashboardItemService dashboardItemServiceMock;
    private IDashboardElementService dashboardElementServiceMock;

    private IDashboardService dashboardService;

    @Before
    public void setUp() {
        dashboardMock = mock(Dashboard.class);
        dashboardItemMock = mock(DashboardItem.class);
        dashboardContentMock = mock(DashboardContent.class);

        dashboardStoreMock = mock(IDashboardStore.class);
        dashboardItemStoreMock = mock(IDashboardItemStore.class);
        dashboardElementStoreMock = mock(IDashboardElementStore.class);

        dashboardItemServiceMock = mock(IDashboardItemService.class);
        dashboardElementServiceMock = mock(IDashboardElementService.class);

        stateStoreMock = mock(IStateStore.class);

        dashboardService = spy(new DashboardService(dashboardStoreMock, dashboardItemStoreMock,
                dashboardElementStoreMock, stateStoreMock, dashboardItemServiceMock, dashboardElementServiceMock));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRemoveNullDashboard() {
        dashboardService.remove(null);
    }

    @Test
    public void testRemoveDashboardWhichDoesNotExist() {
        when(stateStoreMock.queryActionForModel(any(Dashboard.class))).thenReturn(null);

        boolean status = dashboardService.remove(dashboardMock);

        assertFalse(status);
        verify(stateStoreMock, never()).saveActionForModel(dashboardMock, Action.TO_DELETE);
    }

    @Test
    public void testRemoveDashboardWithStateToPost() {
        when(dashboardStoreMock.delete(dashboardMock)).thenReturn(true);
        when(stateStoreMock.queryActionForModel(dashboardMock)).thenReturn(Action.TO_POST);

        boolean status = dashboardService.remove(dashboardMock);

        assertTrue(status);
        verify(stateStoreMock, times(1)).queryActionForModel(dashboardMock);
        verify(stateStoreMock, never()).saveActionForModel(dashboardMock, Action.TO_DELETE);
        verify(dashboardStoreMock, times(1)).delete(dashboardMock);
    }

    @Test
    public void testRemoveDashboardWithStateToUpdate() {
        when(stateStoreMock.queryActionForModel(dashboardMock)).thenReturn(Action.TO_UPDATE);
        when(stateStoreMock.saveActionForModel(dashboardMock, Action.TO_DELETE)).thenReturn(true);

        boolean status = dashboardService.remove(dashboardMock);

        assertTrue(status);
        verify(stateStoreMock, times(1)).queryActionForModel(dashboardMock);
        verify(stateStoreMock, times(1)).saveActionForModel(dashboardMock, Action.TO_DELETE);
    }

    @Test
    public void testRemoveDashboardWithStateSynced() {
        when(stateStoreMock.queryActionForModel(dashboardMock)).thenReturn(Action.SYNCED);
        when(stateStoreMock.saveActionForModel(dashboardMock, Action.TO_DELETE)).thenReturn(true);

        boolean status = dashboardService.remove(dashboardMock);

        assertTrue(status);
        verify(stateStoreMock, times(1)).queryActionForModel(dashboardMock);
        verify(stateStoreMock, times(1)).saveActionForModel(dashboardMock, Action.TO_DELETE);
    }


    @Test
    public void testRemoveDashboardWithStateToDelete() {
        when(stateStoreMock.queryActionForModel(dashboardMock)).thenReturn(Action.TO_DELETE);
        when(stateStoreMock.saveActionForModel(dashboardMock, Action.TO_DELETE)).thenReturn(true);

        boolean status = dashboardService.remove(dashboardMock);

        assertFalse(status);
        verify(stateStoreMock, times(1)).queryActionForModel(dashboardMock);
        verify(stateStoreMock, never()).saveActionForModel(dashboardMock, Action.TO_DELETE);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSaveNullDashboard() {
        dashboardService.save(null);
    }

    @Test
    public void testSaveDashboardWithoutState() {
        when(stateStoreMock.queryActionForModel(dashboardMock)).thenReturn(null);
        when(stateStoreMock.saveActionForModel(dashboardMock, Action.TO_POST)).thenReturn(true);
        when(dashboardStoreMock.queryById(anyInt())).thenReturn(null);
        when(dashboardStoreMock.save(dashboardMock)).thenReturn(true);

        boolean status = dashboardService.save(dashboardMock);

        assertTrue(status);
        verify(stateStoreMock, times(1)).queryActionForModel(dashboardMock);
        verify(stateStoreMock, times(1)).saveActionForModel(dashboardMock, Action.TO_POST);
        verify(dashboardStoreMock, times(1)).save(dashboardMock);
    }

    @Test
    public void testSaveDashboardWithStateToPost() {
        when(stateStoreMock.queryActionForModel(dashboardMock)).thenReturn(Action.TO_POST);
        when(dashboardStoreMock.queryById(anyInt())).thenReturn(dashboardMock);
        when(dashboardStoreMock.save(dashboardMock)).thenReturn(true);

        boolean status = dashboardService.save(dashboardMock);

        assertTrue(status);
        verify(stateStoreMock, times(1)).queryActionForModel(dashboardMock);
        verify(dashboardStoreMock, times(1)).save(dashboardMock);
    }

    @Test
    public void testSaveDashboardWithStateToUpdate() {
        when(stateStoreMock.queryActionForModel(dashboardMock)).thenReturn(Action.TO_UPDATE);
        when(dashboardStoreMock.queryById(anyInt())).thenReturn(dashboardMock);
        when(dashboardStoreMock.save(dashboardMock)).thenReturn(true);

        boolean status = dashboardService.save(dashboardMock);

        assertTrue(status);
        verify(stateStoreMock, times(1)).queryActionForModel(dashboardMock);
        verify(dashboardStoreMock, times(1)).save(dashboardMock);
    }

    @Test
    public void testSaveDashboardWithStateSynced() {
        when(stateStoreMock.queryActionForModel(dashboardMock)).thenReturn(Action.SYNCED);
        when(stateStoreMock.saveActionForModel(dashboardMock, Action.TO_UPDATE)).thenReturn(true);
        when(dashboardStoreMock.queryById(anyInt())).thenReturn(dashboardMock);
        when(dashboardStoreMock.save(dashboardMock)).thenReturn(true);

        boolean status = dashboardService.save(dashboardMock);

        assertTrue(status);
        verify(stateStoreMock, times(1)).queryActionForModel(dashboardMock);
        verify(stateStoreMock, times(1)).saveActionForModel(dashboardMock, Action.TO_UPDATE);
        verify(dashboardStoreMock, times(1)).save(dashboardMock);
    }

    @Test
    public void testSaveDashboardWithStateToDelete() {
        when(stateStoreMock.queryActionForModel(dashboardMock)).thenReturn(Action.TO_DELETE);
        when(dashboardStoreMock.queryById(anyInt())).thenReturn(dashboardMock);

        boolean status = dashboardService.save(dashboardMock);

        assertFalse(status);
        verify(stateStoreMock, times(1)).queryActionForModel(dashboardMock);
    }

    @Test
    public void testSaveDashboardWithStoreFailingToSaveIt() {
        when(stateStoreMock.queryActionForModel(dashboardMock)).thenReturn(Action.SYNCED);
        when(stateStoreMock.saveActionForModel(dashboardMock, Action.TO_UPDATE)).thenReturn(true);
        when(dashboardStoreMock.queryById(anyInt())).thenReturn(dashboardMock);
        when(dashboardStoreMock.save(dashboardMock)).thenReturn(false);

        boolean status = dashboardService.save(dashboardMock);

        assertFalse(status);
        verify(stateStoreMock, times(1)).queryActionForModel(dashboardMock);
        verify(dashboardStoreMock, times(1)).save(dashboardMock);
        verify(stateStoreMock, never()).saveActionForModel(dashboardMock, Action.TO_UPDATE);
    }

    @Test
    public void testSaveNewDashboardWithStoreFailingToSaveIt() {
        when(stateStoreMock.queryActionForModel(dashboardMock)).thenReturn(null);
        when(stateStoreMock.saveActionForModel(dashboardMock, Action.TO_POST)).thenReturn(true);
        when(dashboardStoreMock.queryById(anyInt())).thenReturn(dashboardMock);
        when(dashboardStoreMock.save(dashboardMock)).thenReturn(false);

        boolean status = dashboardService.save(dashboardMock);

        assertFalse(status);
        verify(stateStoreMock, times(1)).queryActionForModel(dashboardMock);
        verify(dashboardStoreMock, times(1)).save(dashboardMock);
        verify(stateStoreMock, never()).saveActionForModel(dashboardMock, Action.TO_POST);
    }

    @Test
    public void testGetDashboardByIdWithStateSynced() {
        when(dashboardStoreMock.queryById(anyInt())).thenReturn(dashboardMock);
        when(stateStoreMock.queryActionForModel(dashboardMock)).thenReturn(Action.SYNCED);

        Dashboard dashboard = dashboardService.get(12);

        assertSame(dashboard, dashboardMock);
        verify(dashboardStoreMock, times(1)).queryById(12);
        verify(stateStoreMock, times(1)).queryActionForModel(dashboardMock);
    }


    @Test
    public void testGetDashboardByIdWithStateToPost() {
        when(dashboardStoreMock.queryById(anyInt())).thenReturn(dashboardMock);
        when(stateStoreMock.queryActionForModel(dashboardMock)).thenReturn(Action.TO_POST);

        Dashboard dashboard = dashboardService.get(12);

        assertSame(dashboard, dashboardMock);
        verify(dashboardStoreMock, times(1)).queryById(12);
        verify(stateStoreMock, times(1)).queryActionForModel(dashboardMock);
    }


    @Test
    public void testGetDashboardByIdWithStateToUpdate() {
        when(dashboardStoreMock.queryById(anyInt())).thenReturn(dashboardMock);
        when(stateStoreMock.queryActionForModel(dashboardMock)).thenReturn(Action.TO_UPDATE);

        Dashboard dashboard = dashboardService.get(12);

        assertSame(dashboard, dashboardMock);
        verify(dashboardStoreMock, times(1)).queryById(12);
        verify(stateStoreMock, times(1)).queryActionForModel(dashboardMock);
    }


    @Test
    public void testGetDashboardByIdWithStateToDelete() {
        when(dashboardStoreMock.queryById(anyInt())).thenReturn(dashboardMock);
        when(stateStoreMock.queryActionForModel(dashboardMock)).thenReturn(Action.TO_DELETE);

        Dashboard dashboard = dashboardService.get(12);

        assertNull(dashboard);
        verify(dashboardStoreMock, times(1)).queryById(12);
        verify(stateStoreMock, times(1)).queryActionForModel(dashboardMock);
    }


    @Test
    public void testGetDashboardByUidWithStateSynced() {
        when(dashboardStoreMock.queryByUid(anyString())).thenReturn(dashboardMock);
        when(stateStoreMock.queryActionForModel(dashboardMock)).thenReturn(Action.SYNCED);

        Dashboard dashboard = dashboardService.get("asfkj234");

        assertSame(dashboard, dashboardMock);
        verify(dashboardStoreMock, times(1)).queryByUid("asfkj234");
        verify(stateStoreMock, times(1)).queryActionForModel(dashboardMock);
    }


    @Test
    public void testGetDashboardByUidWithStateToPost() {
        when(dashboardStoreMock.queryByUid(anyString())).thenReturn(dashboardMock);
        when(stateStoreMock.queryActionForModel(dashboardMock)).thenReturn(Action.TO_POST);

        Dashboard dashboard = dashboardService.get("asfkj234");

        assertSame(dashboard, dashboardMock);
        verify(dashboardStoreMock, times(1)).queryByUid("asfkj234");
        verify(stateStoreMock, times(1)).queryActionForModel(dashboardMock);
    }


    @Test
    public void testGetDashboardByUidWithStateToUpdate() {
        when(dashboardStoreMock.queryByUid(anyString())).thenReturn(dashboardMock);
        when(stateStoreMock.queryActionForModel(dashboardMock)).thenReturn(Action.TO_UPDATE);

        Dashboard dashboard = dashboardService.get("asfkj234");

        assertSame(dashboard, dashboardMock);
        verify(dashboardStoreMock, times(1)).queryByUid("asfkj234");
        verify(stateStoreMock, times(1)).queryActionForModel(dashboardMock);
    }


    @Test
    public void testGetDashboardByUidWithStateToDelete() {
        when(dashboardStoreMock.queryByUid(anyString())).thenReturn(dashboardMock);
        when(stateStoreMock.queryActionForModel(dashboardMock)).thenReturn(Action.TO_DELETE);

        Dashboard dashboard = dashboardService.get("asfkj234");

        assertNull(dashboard);
        verify(dashboardStoreMock, times(1)).queryByUid("asfkj234");
        verify(stateStoreMock, times(1)).queryActionForModel(dashboardMock);
    }

    @Test
    public void testListDashboards() {
        dashboardService.list();

        verify(stateStoreMock, times(1)).queryModelsWithActions(Dashboard.class,
                Action.SYNCED, Action.TO_POST, Action.TO_UPDATE);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCountDashboardItemsForNullDashboard() {
        dashboardService.countItems(null);
    }

    @Test
    public void testCountItemsOnEmptyDashboard() {
        when(dashboardItemServiceMock.list(dashboardMock)).thenReturn(null);

        int itemCount = dashboardService.countItems(dashboardMock);

        assertEquals(itemCount, 0);
        verify(dashboardItemServiceMock, times(1)).list(dashboardMock);
    }

    @Test
    public void testCountItemsOnNoneEmptyDashboard() {
        when(dashboardItemServiceMock.list(dashboardMock)).thenReturn(Arrays.asList(
                dashboardItemMock, dashboardItemMock, dashboardItemMock));

        int itemCount = dashboardService.countItems(dashboardMock);

        assertEquals(itemCount, 3);
        verify(dashboardItemServiceMock, times(1)).list(dashboardMock);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateDashboardWithNullName() {
        dashboardService.create(null);
    }

    @Test
    public void testCreateDashboard() {
        Dashboard dashboard = dashboardService.create("FancyDashboard");

        assertNotNull(dashboard.getUId());
        assertNotNull(dashboard.getAccess());
        assertNotNull(dashboard.getCreated());
        assertNotNull(dashboard.getLastUpdated());
        assertEquals(dashboard.getCreated(), dashboard.getLastUpdated());
        assertEquals(dashboard.getName(), "FancyDashboard");
        assertEquals(dashboard.getDisplayName(), "FancyDashboard");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddContentToNullDashboard() {
        dashboardService.addContent(null, dashboardContentMock);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddNullContentToDashboard() {
        dashboardService.addContent(dashboardMock, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddContentWithUnsupportedType() {
        when(dashboardContentMock.getType()).thenReturn("UnknownType");

        dashboardService.addContent(dashboardMock, dashboardContentMock);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddContentWithNullTypeToDashboard() {
        when(dashboardContentMock.getType()).thenReturn(null);

        dashboardService.addContent(dashboardMock, dashboardContentMock);
    }

    @Test
    public void testAddContentToFullDashboard() {
        doReturn(40).when(dashboardService).countItems(dashboardMock);
        when(dashboardContentMock.getType()).thenReturn(DashboardContent.TYPE_MAP);

        boolean flag = dashboardService.addContent(dashboardMock, dashboardContentMock);

        assertFalse(flag);
        verify(dashboardService, times(1)).countItems(dashboardMock);
        verify(dashboardItemServiceMock, times(1)).create(any(Dashboard.class), anyString());
        verify(dashboardElementServiceMock, times(1)).create(any(DashboardItem.class), any(DashboardContent.class));

        verify(dashboardItemStoreMock, never()).save(any(DashboardItem.class));
        verify(dashboardElementStoreMock, never()).save(any(DashboardElement.class));
    }

    @Test
    public void testAddChartToDashboard() {
        when(dashboardContentMock.getType()).thenReturn(DashboardContent.TYPE_CHART);
        when(dashboardItemStoreMock.save(any(DashboardItem.class))).thenReturn(true);
        when(dashboardElementStoreMock.save(any(DashboardElement.class))).thenReturn(true);
        doReturn(16).when(dashboardService).countItems(dashboardMock);

        boolean status = dashboardService.addContent(dashboardMock, dashboardContentMock);

        assertTrue(status);
        verify(dashboardService, times(1)).countItems(dashboardMock);
        verify(dashboardItemServiceMock, times(1)).create(any(Dashboard.class), anyString());
        verify(dashboardElementServiceMock, times(1)).create(any(DashboardItem.class), any(DashboardContent.class));
        verify(dashboardElementStoreMock, times(1)).save(any(DashboardElement.class));
        verify(dashboardItemStoreMock, times(1)).save(any(DashboardItem.class));
    }


    @Test
    public void testAddEventChartToDashboard() {
        when(dashboardContentMock.getType()).thenReturn(DashboardContent.TYPE_EVENT_CHART);
        when(dashboardItemStoreMock.save(any(DashboardItem.class))).thenReturn(true);
        when(dashboardElementStoreMock.save(any(DashboardElement.class))).thenReturn(true);
        doReturn(16).when(dashboardService).countItems(dashboardMock);

        boolean status = dashboardService.addContent(dashboardMock, dashboardContentMock);

        assertTrue(status);
        verify(dashboardService, times(1)).countItems(dashboardMock);
        verify(dashboardItemServiceMock, times(1)).create(any(Dashboard.class), anyString());
        verify(dashboardElementServiceMock, times(1)).create(any(DashboardItem.class), any(DashboardContent.class));
        verify(dashboardElementStoreMock, times(1)).save(any(DashboardElement.class));
        verify(dashboardItemStoreMock, times(1)).save(any(DashboardItem.class));
    }


    @Test
    public void testAddMapToDashboard() {
        when(dashboardContentMock.getType()).thenReturn(DashboardContent.TYPE_MAP);
        when(dashboardItemStoreMock.save(any(DashboardItem.class))).thenReturn(true);
        when(dashboardElementStoreMock.save(any(DashboardElement.class))).thenReturn(true);
        doReturn(16).when(dashboardService).countItems(dashboardMock);

        boolean status = dashboardService.addContent(dashboardMock, dashboardContentMock);

        assertTrue(status);
        verify(dashboardService, times(1)).countItems(dashboardMock);
        verify(dashboardItemServiceMock, times(1)).create(any(Dashboard.class), anyString());
        verify(dashboardElementServiceMock, times(1)).create(any(DashboardItem.class), any(DashboardContent.class));
        verify(dashboardElementStoreMock, times(1)).save(any(DashboardElement.class));
        verify(dashboardItemStoreMock, times(1)).save(any(DashboardItem.class));
    }


    @Test
    public void testAddEventReportToDashboard() {
        when(dashboardContentMock.getType()).thenReturn(DashboardContent.TYPE_EVENT_REPORT);
        when(dashboardItemStoreMock.save(any(DashboardItem.class))).thenReturn(true);
        when(dashboardElementStoreMock.save(any(DashboardElement.class))).thenReturn(true);
        doReturn(16).when(dashboardService).countItems(dashboardMock);

        boolean status = dashboardService.addContent(dashboardMock, dashboardContentMock);

        assertTrue(status);
        verify(dashboardService, times(1)).countItems(dashboardMock);
        verify(dashboardItemServiceMock, times(1)).create(any(Dashboard.class), anyString());
        verify(dashboardElementServiceMock, times(1)).create(any(DashboardItem.class), any(DashboardContent.class));
        verify(dashboardElementStoreMock, times(1)).save(any(DashboardElement.class));
        verify(dashboardItemStoreMock, times(1)).save(any(DashboardItem.class));
    }


    @Test
    public void testAddReportTableToDashboard() {
        when(dashboardContentMock.getType()).thenReturn(DashboardContent.TYPE_REPORT_TABLE);
        when(dashboardItemStoreMock.save(any(DashboardItem.class))).thenReturn(true);
        when(dashboardElementStoreMock.save(any(DashboardElement.class))).thenReturn(true);
        doReturn(16).when(dashboardService).countItems(dashboardMock);

        boolean status = dashboardService.addContent(dashboardMock, dashboardContentMock);

        assertTrue(status);
        verify(dashboardService, times(1)).countItems(dashboardMock);
        verify(dashboardItemServiceMock, times(1)).create(any(Dashboard.class), anyString());
        verify(dashboardElementServiceMock, times(1)).create(any(DashboardItem.class), any(DashboardContent.class));
        verify(dashboardElementStoreMock, times(1)).save(any(DashboardElement.class));
        verify(dashboardItemStoreMock, times(1)).save(any(DashboardItem.class));
    }

    @Test
    public void testAddListItemToDashboardWithItemOfSameType() {
        when(dashboardContentMock.getType()).thenReturn(DashboardContent.TYPE_USERS);
        when(dashboardItemMock.getType()).thenReturn(DashboardContent.TYPE_USERS);

        doReturn(1).when(dashboardService).countItems(dashboardMock);

        when(dashboardItemServiceMock.countElements(dashboardItemMock)).thenReturn(3);
        when(dashboardItemServiceMock.list(dashboardMock)).thenReturn(Arrays.asList(dashboardItemMock));

        when(dashboardItemStoreMock.save(any(DashboardItem.class))).thenReturn(true);
        when(dashboardElementStoreMock.save(any(DashboardElement.class))).thenReturn(true);

        boolean status = dashboardService.addContent(dashboardMock, dashboardContentMock);

        assertTrue(status);
        verify(dashboardItemServiceMock, never()).create(dashboardMock, DashboardContent.TYPE_USERS);
        verify(dashboardElementServiceMock, times(1)).create(dashboardItemMock, dashboardContentMock);
        verify(dashboardItemStoreMock, times(1)).save(any(DashboardItem.class));
        verify(dashboardElementStoreMock, times(1)).save(any(DashboardElement.class));
    }


    @Test
    public void testAddListItemToDashboardWithFullItemOfSameType() {
        when(dashboardContentMock.getType()).thenReturn(DashboardContent.TYPE_USERS);
        when(dashboardItemMock.getType()).thenReturn(DashboardContent.TYPE_USERS);

        doReturn(1).when(dashboardService).countItems(dashboardMock);

        when(dashboardItemServiceMock.countElements(dashboardItemMock)).thenReturn(8);
        when(dashboardItemServiceMock.list(dashboardMock)).thenReturn(Arrays.asList(dashboardItemMock));

        when(dashboardItemStoreMock.save(any(DashboardItem.class))).thenReturn(true);
        when(dashboardElementStoreMock.save(any(DashboardElement.class))).thenReturn(true);

        boolean status = dashboardService.addContent(dashboardMock, dashboardContentMock);

        assertTrue(status);
        verify(dashboardItemServiceMock, times(1)).create(dashboardMock, DashboardContent.TYPE_USERS);
        verify(dashboardElementServiceMock, times(1)).create(any(DashboardItem.class), any(DashboardContent.class));
        verify(dashboardItemStoreMock, times(1)).save(any(DashboardItem.class));
        verify(dashboardElementStoreMock, times(1)).save(any(DashboardElement.class));
    }


    @Test
    public void testAddListItemToFullDashboardWithItemWithFreeSpace() {
        when(dashboardContentMock.getType()).thenReturn(DashboardContent.TYPE_USERS);
        when(dashboardItemMock.getType()).thenReturn(DashboardContent.TYPE_USERS);

        doReturn(40).when(dashboardService).countItems(dashboardMock);

        when(dashboardItemServiceMock.countElements(dashboardItemMock)).thenReturn(4);
        when(dashboardItemServiceMock.list(dashboardMock)).thenReturn(Arrays.asList(dashboardItemMock));

        when(dashboardItemStoreMock.save(any(DashboardItem.class))).thenReturn(true);
        when(dashboardElementStoreMock.save(any(DashboardElement.class))).thenReturn(true);

        boolean status = dashboardService.addContent(dashboardMock, dashboardContentMock);

        assertTrue(status);
        verify(dashboardItemServiceMock, never()).create(dashboardMock, DashboardContent.TYPE_USERS);
        verify(dashboardElementServiceMock, times(1)).create(dashboardItemMock, dashboardContentMock);
        verify(dashboardItemStoreMock, times(1)).save(any(DashboardItem.class));
        verify(dashboardElementStoreMock, times(1)).save(any(DashboardElement.class));
    }


    @Test
    public void testAddListItemToFullDashboardWithItemWithoutFreeSpace() {
        when(dashboardContentMock.getType()).thenReturn(DashboardContent.TYPE_USERS);
        when(dashboardItemMock.getType()).thenReturn(DashboardContent.TYPE_USERS);

        doReturn(40).when(dashboardService).countItems(dashboardMock);

        when(dashboardItemServiceMock.countElements(dashboardItemMock)).thenReturn(8);
        when(dashboardItemServiceMock.list(dashboardMock)).thenReturn(Arrays.asList(dashboardItemMock));

        when(dashboardItemStoreMock.save(any(DashboardItem.class))).thenReturn(true);
        when(dashboardElementStoreMock.save(any(DashboardElement.class))).thenReturn(true);

        boolean status = dashboardService.addContent(dashboardMock, dashboardContentMock);

        assertFalse(status);
        verify(dashboardItemStoreMock, never()).save(any(DashboardItem.class));
        verify(dashboardElementStoreMock, never()).save(any(DashboardElement.class));
    }


    @Test
    public void testAddListItemToDashboardWithoutItemOfSameType() {
        when(dashboardContentMock.getType()).thenReturn(DashboardContent.TYPE_USERS);
        when(dashboardItemMock.getType()).thenReturn(DashboardContent.TYPE_REPORTS);

        doReturn(1).when(dashboardService).countItems(dashboardMock);

        when(dashboardItemServiceMock.countElements(dashboardItemMock)).thenReturn(3);
        when(dashboardItemServiceMock.list(dashboardMock)).thenReturn(Arrays.asList(dashboardItemMock));

        when(dashboardItemStoreMock.save(any(DashboardItem.class))).thenReturn(true);
        when(dashboardElementStoreMock.save(any(DashboardElement.class))).thenReturn(true);

        boolean status = dashboardService.addContent(dashboardMock, dashboardContentMock);

        assertTrue(status);
        verify(dashboardItemServiceMock, times(1)).create(dashboardMock, DashboardContent.TYPE_USERS);
        verify(dashboardElementServiceMock, times(1)).create(any(DashboardItem.class), any(DashboardContent.class));
        verify(dashboardItemStoreMock, times(1)).save(any(DashboardItem.class));
        verify(dashboardElementStoreMock, times(1)).save(any(DashboardElement.class));
    }


    @Test
    public void testAddContentWithStoreFailingItToSaveItem() {
        when(dashboardContentMock.getType()).thenReturn(DashboardContent.TYPE_CHART);
        when(dashboardItemStoreMock.save(any(DashboardItem.class))).thenReturn(false);
        when(dashboardElementStoreMock.save(any(DashboardElement.class))).thenReturn(true);
        doReturn(0).when(dashboardService).countItems(dashboardMock);

        boolean status = dashboardService.addContent(dashboardMock, dashboardContentMock);

        assertFalse(status);
        verify(dashboardService, times(1)).countItems(dashboardMock);
        verify(dashboardItemServiceMock, times(1)).create(any(Dashboard.class), anyString());
        verify(dashboardElementServiceMock, times(1)).create(any(DashboardItem.class), any(DashboardContent.class));
        verify(dashboardItemStoreMock, times(1)).save(any(DashboardItem.class));
    }


    @Test
    public void testAddContentWithStoreFailingItToSaveElement() {
        when(dashboardContentMock.getType()).thenReturn(DashboardContent.TYPE_CHART);
        when(dashboardItemStoreMock.save(any(DashboardItem.class))).thenReturn(true);
        when(dashboardElementStoreMock.save(any(DashboardElement.class))).thenReturn(false);
        doReturn(0).when(dashboardService).countItems(dashboardMock);

        boolean status = dashboardService.addContent(dashboardMock, dashboardContentMock);

        assertFalse(status);
        verify(dashboardService, times(1)).countItems(dashboardMock);
        verify(dashboardItemServiceMock, times(1)).create(any(Dashboard.class), anyString());
        verify(dashboardElementServiceMock, times(1)).create(any(DashboardItem.class), any(DashboardContent.class));
        verify(dashboardItemStoreMock, times(1)).save(any(DashboardItem.class));
        verify(dashboardElementStoreMock, times(1)).save(any(DashboardElement.class));
    }


    @Test
    public void testAddUserToDashboard() {
        when(dashboardContentMock.getType()).thenReturn(DashboardContent.TYPE_USERS);
        when(dashboardItemMock.getType()).thenReturn(DashboardContent.TYPE_USERS);

        when(dashboardItemStoreMock.queryByDashboard(dashboardMock))
                .thenReturn(Arrays.asList(dashboardItemMock, dashboardItemMock));
        when(dashboardItemStoreMock.save(any(DashboardItem.class))).thenReturn(true);
        when(dashboardElementStoreMock.save(any(DashboardElement.class))).thenReturn(true);

        when(dashboardItemServiceMock.countElements(dashboardItemMock)).thenReturn(6);

        boolean status = dashboardService.addContent(dashboardMock, dashboardContentMock);

        assertTrue(status);
        verify(dashboardService, times(1)).countItems(dashboardMock);
        verify(dashboardElementStoreMock, times(1)).save(any(DashboardElement.class));
        verify(dashboardItemStoreMock, times(1)).save(any(DashboardItem.class));
    }
}

