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
    private Dashboard dashboard;
    private DashboardItem dashboardItem;
    private DashboardContent dashboardContent;

    private IDashboardStore dashboardStoreMock;
    private IDashboardItemStore dashboardItemStoreMock;
    private IDashboardElementStore dashboardElementStoreMock;
    private IStateStore stateStoreMock;

    private IDashboardItemService dashboardItemServiceMock;
    private IDashboardElementService dashboardElementServiceMock;

    private IDashboardService dashboardService;

    @Before
    public void setUp() {
        dashboard = new Dashboard();
        dashboardItem = new DashboardItem();
        dashboardContent = new DashboardContent();

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

        boolean status = dashboardService.remove(dashboard);

        assertFalse(status);
        verify(stateStoreMock, never()).saveActionForModel(dashboard, Action.TO_DELETE);
    }

    @Test
    public void testRemoveDashboardWithStateToPost() {
        when(dashboardStoreMock.delete(dashboard)).thenReturn(true);
        when(stateStoreMock.queryActionForModel(dashboard)).thenReturn(Action.TO_POST);

        boolean status = dashboardService.remove(dashboard);

        assertTrue(status);
        verify(stateStoreMock, times(1)).queryActionForModel(dashboard);
        verify(stateStoreMock, never()).saveActionForModel(dashboard, Action.TO_DELETE);
        verify(dashboardStoreMock, times(1)).delete(dashboard);
    }

    @Test
    public void testRemoveDashboardWithStateToUpdate() {
        when(stateStoreMock.queryActionForModel(dashboard)).thenReturn(Action.TO_UPDATE);
        when(stateStoreMock.saveActionForModel(dashboard, Action.TO_DELETE)).thenReturn(true);

        boolean status = dashboardService.remove(dashboard);

        assertTrue(status);
        verify(stateStoreMock, times(1)).queryActionForModel(dashboard);
        verify(stateStoreMock, times(1)).saveActionForModel(dashboard, Action.TO_DELETE);
    }

    @Test
    public void testRemoveDashboardWithStateSynced() {
        when(stateStoreMock.queryActionForModel(dashboard)).thenReturn(Action.SYNCED);
        when(stateStoreMock.saveActionForModel(dashboard, Action.TO_DELETE)).thenReturn(true);

        boolean status = dashboardService.remove(dashboard);

        assertTrue(status);
        verify(stateStoreMock, times(1)).queryActionForModel(dashboard);
        verify(stateStoreMock, times(1)).saveActionForModel(dashboard, Action.TO_DELETE);
    }


    @Test
    public void testRemoveDashboardWithStateToDelete() {
        when(stateStoreMock.queryActionForModel(dashboard)).thenReturn(Action.TO_DELETE);
        when(stateStoreMock.saveActionForModel(dashboard, Action.TO_DELETE)).thenReturn(true);

        boolean status = dashboardService.remove(dashboard);

        assertFalse(status);
        verify(stateStoreMock, times(1)).queryActionForModel(dashboard);
        verify(stateStoreMock, never()).saveActionForModel(dashboard, Action.TO_DELETE);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSaveNullDashboard() {
        dashboardService.save(null);
    }

    @Test
    public void testSaveDashboardWithoutState() {
        when(stateStoreMock.queryActionForModel(dashboard)).thenReturn(null);
        when(stateStoreMock.saveActionForModel(dashboard, Action.TO_POST)).thenReturn(true);
        when(dashboardStoreMock.queryById(anyInt())).thenReturn(null);
        when(dashboardStoreMock.save(dashboard)).thenReturn(true);

        boolean status = dashboardService.save(dashboard);

        assertTrue(status);
        verify(stateStoreMock, times(1)).queryActionForModel(dashboard);
        verify(stateStoreMock, times(1)).saveActionForModel(dashboard, Action.TO_POST);
        verify(dashboardStoreMock, times(1)).save(dashboard);
    }

    @Test
    public void testSaveDashboardWithStateToPost() {
        when(stateStoreMock.queryActionForModel(dashboard)).thenReturn(Action.TO_POST);
        when(dashboardStoreMock.queryById(anyInt())).thenReturn(dashboard);
        when(dashboardStoreMock.save(dashboard)).thenReturn(true);

        boolean status = dashboardService.save(dashboard);

        assertTrue(status);
        verify(stateStoreMock, times(1)).queryActionForModel(dashboard);
        verify(dashboardStoreMock, times(1)).save(dashboard);
    }

    @Test
    public void testSaveDashboardWithStateToUpdate() {
        when(stateStoreMock.queryActionForModel(dashboard)).thenReturn(Action.TO_UPDATE);
        when(dashboardStoreMock.queryById(anyInt())).thenReturn(dashboard);
        when(dashboardStoreMock.save(dashboard)).thenReturn(true);

        boolean status = dashboardService.save(dashboard);

        assertTrue(status);
        verify(stateStoreMock, times(1)).queryActionForModel(dashboard);
        verify(dashboardStoreMock, times(1)).save(dashboard);
    }

    @Test
    public void testSaveDashboardWithStateSynced() {
        when(stateStoreMock.queryActionForModel(dashboard)).thenReturn(Action.SYNCED);
        when(stateStoreMock.saveActionForModel(dashboard, Action.TO_UPDATE)).thenReturn(true);
        when(dashboardStoreMock.queryById(anyInt())).thenReturn(dashboard);
        when(dashboardStoreMock.save(dashboard)).thenReturn(true);

        boolean status = dashboardService.save(dashboard);

        assertTrue(status);
        verify(stateStoreMock, times(1)).queryActionForModel(dashboard);
        verify(stateStoreMock, times(1)).saveActionForModel(dashboard, Action.TO_UPDATE);
        verify(dashboardStoreMock, times(1)).save(dashboard);
    }

    @Test
    public void testSaveDashboardWithStateToDelete() {
        when(stateStoreMock.queryActionForModel(dashboard)).thenReturn(Action.TO_DELETE);
        when(dashboardStoreMock.queryById(anyInt())).thenReturn(dashboard);

        boolean status = dashboardService.save(dashboard);

        assertFalse(status);
        verify(stateStoreMock, times(1)).queryActionForModel(dashboard);
    }

    @Test
    public void testSaveDashboardWithStoreFailingToSaveIt() {
        when(stateStoreMock.queryActionForModel(dashboard)).thenReturn(Action.SYNCED);
        when(stateStoreMock.saveActionForModel(dashboard, Action.TO_UPDATE)).thenReturn(true);
        when(dashboardStoreMock.queryById(anyInt())).thenReturn(dashboard);
        when(dashboardStoreMock.save(dashboard)).thenReturn(false);

        boolean status = dashboardService.save(dashboard);

        assertFalse(status);
        verify(stateStoreMock, times(1)).queryActionForModel(dashboard);
        verify(dashboardStoreMock, times(1)).save(dashboard);
        verify(stateStoreMock, never()).saveActionForModel(dashboard, Action.TO_UPDATE);
    }

    @Test
    public void testSaveNewDashboardWithStoreFailingToSaveIt() {
        when(stateStoreMock.queryActionForModel(dashboard)).thenReturn(null);
        when(stateStoreMock.saveActionForModel(dashboard, Action.TO_POST)).thenReturn(true);
        when(dashboardStoreMock.queryById(anyInt())).thenReturn(dashboard);
        when(dashboardStoreMock.save(dashboard)).thenReturn(false);

        boolean status = dashboardService.save(dashboard);

        assertFalse(status);
        verify(stateStoreMock, times(1)).queryActionForModel(dashboard);
        verify(dashboardStoreMock, times(1)).save(dashboard);
        verify(stateStoreMock, never()).saveActionForModel(dashboard, Action.TO_POST);
    }

    @Test
    public void testGetDashboardByIdWithStateSynced() {
        when(dashboardStoreMock.queryById(anyInt())).thenReturn(dashboard);
        when(stateStoreMock.queryActionForModel(dashboard)).thenReturn(Action.SYNCED);

        Dashboard dashboard = dashboardService.get(12);

        assertSame(dashboard, this.dashboard);
        verify(dashboardStoreMock, times(1)).queryById(12);
        verify(stateStoreMock, times(1)).queryActionForModel(this.dashboard);
    }


    @Test
    public void testGetDashboardByIdWithStateToPost() {
        when(dashboardStoreMock.queryById(anyInt())).thenReturn(dashboard);
        when(stateStoreMock.queryActionForModel(dashboard)).thenReturn(Action.TO_POST);

        Dashboard dashboard = dashboardService.get(12);

        assertSame(dashboard, this.dashboard);
        verify(dashboardStoreMock, times(1)).queryById(12);
        verify(stateStoreMock, times(1)).queryActionForModel(this.dashboard);
    }


    @Test
    public void testGetDashboardByIdWithStateToUpdate() {
        when(dashboardStoreMock.queryById(anyInt())).thenReturn(dashboard);
        when(stateStoreMock.queryActionForModel(dashboard)).thenReturn(Action.TO_UPDATE);

        Dashboard dashboard = dashboardService.get(12);

        assertSame(dashboard, this.dashboard);
        verify(dashboardStoreMock, times(1)).queryById(12);
        verify(stateStoreMock, times(1)).queryActionForModel(this.dashboard);
    }


    @Test
    public void testGetDashboardByIdWithStateToDelete() {
        when(dashboardStoreMock.queryById(anyInt())).thenReturn(dashboard);
        when(stateStoreMock.queryActionForModel(dashboard)).thenReturn(Action.TO_DELETE);

        Dashboard dashboard = dashboardService.get(12);

        assertNull(dashboard);
        verify(dashboardStoreMock, times(1)).queryById(12);
        verify(stateStoreMock, times(1)).queryActionForModel(this.dashboard);
    }


    @Test
    public void testGetDashboardByUidWithStateSynced() {
        when(dashboardStoreMock.queryByUid(anyString())).thenReturn(dashboard);
        when(stateStoreMock.queryActionForModel(dashboard)).thenReturn(Action.SYNCED);

        Dashboard dashboard = dashboardService.get("asfkj234");

        assertSame(dashboard, this.dashboard);
        verify(dashboardStoreMock, times(1)).queryByUid("asfkj234");
        verify(stateStoreMock, times(1)).queryActionForModel(this.dashboard);
    }


    @Test
    public void testGetDashboardByUidWithStateToPost() {
        when(dashboardStoreMock.queryByUid(anyString())).thenReturn(dashboard);
        when(stateStoreMock.queryActionForModel(dashboard)).thenReturn(Action.TO_POST);

        Dashboard dashboard = dashboardService.get("asfkj234");

        assertSame(dashboard, this.dashboard);
        verify(dashboardStoreMock, times(1)).queryByUid("asfkj234");
        verify(stateStoreMock, times(1)).queryActionForModel(this.dashboard);
    }


    @Test
    public void testGetDashboardByUidWithStateToUpdate() {
        when(dashboardStoreMock.queryByUid(anyString())).thenReturn(dashboard);
        when(stateStoreMock.queryActionForModel(dashboard)).thenReturn(Action.TO_UPDATE);

        Dashboard dashboard = dashboardService.get("asfkj234");

        assertSame(dashboard, this.dashboard);
        verify(dashboardStoreMock, times(1)).queryByUid("asfkj234");
        verify(stateStoreMock, times(1)).queryActionForModel(this.dashboard);
    }


    @Test
    public void testGetDashboardByUidWithStateToDelete() {
        when(dashboardStoreMock.queryByUid(anyString())).thenReturn(dashboard);
        when(stateStoreMock.queryActionForModel(dashboard)).thenReturn(Action.TO_DELETE);

        Dashboard dashboard = dashboardService.get("asfkj234");

        assertNull(dashboard);
        verify(dashboardStoreMock, times(1)).queryByUid("asfkj234");
        verify(stateStoreMock, times(1)).queryActionForModel(this.dashboard);
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
        when(dashboardItemServiceMock.list(dashboard)).thenReturn(null);

        int itemCount = dashboardService.countItems(dashboard);

        assertEquals(itemCount, 0);
        verify(dashboardItemServiceMock, times(1)).list(dashboard);
    }

    @Test
    public void testCountItemsOnNoneEmptyDashboard() {
        when(dashboardItemServiceMock.list(dashboard)).thenReturn(Arrays.asList(
                dashboardItem, dashboardItem, dashboardItem));

        int itemCount = dashboardService.countItems(dashboard);

        assertEquals(itemCount, 3);
        verify(dashboardItemServiceMock, times(1)).list(dashboard);
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
        dashboardService.addContent(null, dashboardContent);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddNullContentToDashboard() {
        dashboardService.addContent(dashboard, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddContentWithUnsupportedType() {
        dashboardContent.setType("UnknownType");

        dashboardService.addContent(dashboard, dashboardContent);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddContentWithNullTypeToDashboard() {
        dashboardContent.setType(null);

        dashboardService.addContent(dashboard, dashboardContent);
    }

    @Test
    public void testAddContentToFullDashboard() {
        doReturn(40).when(dashboardService).countItems(dashboard);
        dashboardContent.setType(DashboardContent.TYPE_MAP);

        boolean flag = dashboardService.addContent(dashboard, dashboardContent);

        assertFalse(flag);
        verify(dashboardService, times(1)).countItems(dashboard);
        verify(dashboardItemServiceMock, times(1)).create(any(Dashboard.class), anyString());
        verify(dashboardElementServiceMock, times(1)).create(any(DashboardItem.class), any(DashboardContent.class));

        verify(dashboardItemStoreMock, never()).save(any(DashboardItem.class));
        verify(dashboardElementStoreMock, never()).save(any(DashboardElement.class));
    }

    @Test
    public void testAddChartToDashboard() {
        dashboardContent.setType(DashboardContent.TYPE_CHART);

        when(dashboardItemStoreMock.save(any(DashboardItem.class))).thenReturn(true);
        when(dashboardElementStoreMock.save(any(DashboardElement.class))).thenReturn(true);
        doReturn(16).when(dashboardService).countItems(dashboard);

        boolean status = dashboardService.addContent(dashboard, dashboardContent);

        assertTrue(status);
        verify(dashboardService, times(1)).countItems(dashboard);
        verify(dashboardItemServiceMock, times(1)).create(any(Dashboard.class), anyString());
        verify(dashboardElementServiceMock, times(1)).create(any(DashboardItem.class), any(DashboardContent.class));
        verify(dashboardElementStoreMock, times(1)).save(any(DashboardElement.class));
        verify(dashboardItemStoreMock, times(1)).save(any(DashboardItem.class));
    }


    @Test
    public void testAddEventChartToDashboard() {
        dashboardContent.setType(DashboardContent.TYPE_EVENT_CHART);

        when(dashboardItemStoreMock.save(any(DashboardItem.class))).thenReturn(true);
        when(dashboardElementStoreMock.save(any(DashboardElement.class))).thenReturn(true);
        doReturn(16).when(dashboardService).countItems(dashboard);

        boolean status = dashboardService.addContent(dashboard, dashboardContent);

        assertTrue(status);
        verify(dashboardService, times(1)).countItems(dashboard);
        verify(dashboardItemServiceMock, times(1)).create(any(Dashboard.class), anyString());
        verify(dashboardElementServiceMock, times(1)).create(any(DashboardItem.class), any(DashboardContent.class));
        verify(dashboardElementStoreMock, times(1)).save(any(DashboardElement.class));
        verify(dashboardItemStoreMock, times(1)).save(any(DashboardItem.class));
    }


    @Test
    public void testAddMapToDashboard() {
        dashboardContent.setType(DashboardContent.TYPE_MAP);

        when(dashboardItemStoreMock.save(any(DashboardItem.class))).thenReturn(true);
        when(dashboardElementStoreMock.save(any(DashboardElement.class))).thenReturn(true);
        doReturn(16).when(dashboardService).countItems(dashboard);

        boolean status = dashboardService.addContent(dashboard, dashboardContent);

        assertTrue(status);
        verify(dashboardService, times(1)).countItems(dashboard);
        verify(dashboardItemServiceMock, times(1)).create(any(Dashboard.class), anyString());
        verify(dashboardElementServiceMock, times(1)).create(any(DashboardItem.class), any(DashboardContent.class));
        verify(dashboardElementStoreMock, times(1)).save(any(DashboardElement.class));
        verify(dashboardItemStoreMock, times(1)).save(any(DashboardItem.class));
    }


    @Test
    public void testAddEventReportToDashboard() {
        dashboardContent.setType(DashboardContent.TYPE_EVENT_REPORT);

        when(dashboardItemStoreMock.save(any(DashboardItem.class))).thenReturn(true);
        when(dashboardElementStoreMock.save(any(DashboardElement.class))).thenReturn(true);
        doReturn(16).when(dashboardService).countItems(dashboard);

        boolean status = dashboardService.addContent(dashboard, dashboardContent);

        assertTrue(status);
        verify(dashboardService, times(1)).countItems(dashboard);
        verify(dashboardItemServiceMock, times(1)).create(any(Dashboard.class), anyString());
        verify(dashboardElementServiceMock, times(1)).create(any(DashboardItem.class), any(DashboardContent.class));
        verify(dashboardElementStoreMock, times(1)).save(any(DashboardElement.class));
        verify(dashboardItemStoreMock, times(1)).save(any(DashboardItem.class));
    }


    @Test
    public void testAddReportTableToDashboard() {
        dashboardContent.setType(DashboardContent.TYPE_REPORT_TABLE);

        when(dashboardItemStoreMock.save(any(DashboardItem.class))).thenReturn(true);
        when(dashboardElementStoreMock.save(any(DashboardElement.class))).thenReturn(true);
        doReturn(16).when(dashboardService).countItems(dashboard);

        boolean status = dashboardService.addContent(dashboard, dashboardContent);

        assertTrue(status);
        verify(dashboardService, times(1)).countItems(dashboard);
        verify(dashboardItemServiceMock, times(1)).create(any(Dashboard.class), anyString());
        verify(dashboardElementServiceMock, times(1)).create(any(DashboardItem.class), any(DashboardContent.class));
        verify(dashboardElementStoreMock, times(1)).save(any(DashboardElement.class));
        verify(dashboardItemStoreMock, times(1)).save(any(DashboardItem.class));
    }

    @Test
    public void testAddListItemToDashboardWithItemOfSameType() {
        dashboardContent.setType(DashboardContent.TYPE_USERS);
        dashboardItem.setType(DashboardContent.TYPE_USERS);

        doReturn(1).when(dashboardService).countItems(dashboard);

        when(dashboardItemServiceMock.countElements(dashboardItem)).thenReturn(3);
        when(dashboardItemServiceMock.list(dashboard)).thenReturn(Arrays.asList(dashboardItem));

        when(dashboardItemStoreMock.save(any(DashboardItem.class))).thenReturn(true);
        when(dashboardElementStoreMock.save(any(DashboardElement.class))).thenReturn(true);

        boolean status = dashboardService.addContent(dashboard, dashboardContent);

        assertTrue(status);
        verify(dashboardItemServiceMock, never()).create(dashboard, DashboardContent.TYPE_USERS);
        verify(dashboardElementServiceMock, times(1)).create(dashboardItem, dashboardContent);
        verify(dashboardItemStoreMock, times(1)).save(any(DashboardItem.class));
        verify(dashboardElementStoreMock, times(1)).save(any(DashboardElement.class));
    }


    @Test
    public void testAddListItemToDashboardWithFullItemOfSameType() {
        dashboardContent.setType(DashboardContent.TYPE_USERS);
        dashboardItem.setType(DashboardContent.TYPE_USERS);

        doReturn(1).when(dashboardService).countItems(dashboard);

        when(dashboardItemServiceMock.countElements(dashboardItem)).thenReturn(8);
        when(dashboardItemServiceMock.list(dashboard)).thenReturn(Arrays.asList(dashboardItem));

        when(dashboardItemStoreMock.save(any(DashboardItem.class))).thenReturn(true);
        when(dashboardElementStoreMock.save(any(DashboardElement.class))).thenReturn(true);

        boolean status = dashboardService.addContent(dashboard, dashboardContent);

        assertTrue(status);
        verify(dashboardItemServiceMock, times(1)).create(dashboard, DashboardContent.TYPE_USERS);
        verify(dashboardElementServiceMock, times(1)).create(any(DashboardItem.class), any(DashboardContent.class));
        verify(dashboardItemStoreMock, times(1)).save(any(DashboardItem.class));
        verify(dashboardElementStoreMock, times(1)).save(any(DashboardElement.class));
    }


    @Test
    public void testAddListItemToFullDashboardWithItemWithFreeSpace() {
        dashboardContent.setType(DashboardContent.TYPE_USERS);
        dashboardItem.setType(DashboardContent.TYPE_USERS);

        doReturn(40).when(dashboardService).countItems(dashboard);

        when(dashboardItemServiceMock.countElements(dashboardItem)).thenReturn(4);
        when(dashboardItemServiceMock.list(dashboard)).thenReturn(Arrays.asList(dashboardItem));

        when(dashboardItemStoreMock.save(any(DashboardItem.class))).thenReturn(true);
        when(dashboardElementStoreMock.save(any(DashboardElement.class))).thenReturn(true);

        boolean status = dashboardService.addContent(dashboard, dashboardContent);

        assertTrue(status);
        verify(dashboardItemServiceMock, never()).create(dashboard, DashboardContent.TYPE_USERS);
        verify(dashboardElementServiceMock, times(1)).create(dashboardItem, dashboardContent);
        verify(dashboardItemStoreMock, times(1)).save(any(DashboardItem.class));
        verify(dashboardElementStoreMock, times(1)).save(any(DashboardElement.class));
    }


    @Test
    public void testAddListItemToFullDashboardWithItemWithoutFreeSpace() {
        dashboardContent.setType(DashboardContent.TYPE_USERS);
        dashboardItem.setType(DashboardContent.TYPE_USERS);

        doReturn(40).when(dashboardService).countItems(dashboard);

        when(dashboardItemServiceMock.countElements(dashboardItem)).thenReturn(8);
        when(dashboardItemServiceMock.list(dashboard)).thenReturn(Arrays.asList(dashboardItem));

        when(dashboardItemStoreMock.save(any(DashboardItem.class))).thenReturn(true);
        when(dashboardElementStoreMock.save(any(DashboardElement.class))).thenReturn(true);

        boolean status = dashboardService.addContent(dashboard, dashboardContent);

        assertFalse(status);
        verify(dashboardItemStoreMock, never()).save(any(DashboardItem.class));
        verify(dashboardElementStoreMock, never()).save(any(DashboardElement.class));
    }


    @Test
    public void testAddListItemToDashboardWithoutItemOfSameType() {
        dashboardContent.setType(DashboardContent.TYPE_USERS);
        dashboardItem.setType(DashboardContent.TYPE_REPORTS);

        doReturn(1).when(dashboardService).countItems(dashboard);

        when(dashboardItemServiceMock.countElements(dashboardItem)).thenReturn(3);
        when(dashboardItemServiceMock.list(dashboard)).thenReturn(Arrays.asList(dashboardItem));

        when(dashboardItemStoreMock.save(any(DashboardItem.class))).thenReturn(true);
        when(dashboardElementStoreMock.save(any(DashboardElement.class))).thenReturn(true);

        boolean status = dashboardService.addContent(dashboard, dashboardContent);

        assertTrue(status);
        verify(dashboardItemServiceMock, times(1)).create(dashboard, DashboardContent.TYPE_USERS);
        verify(dashboardElementServiceMock, times(1)).create(any(DashboardItem.class), any(DashboardContent.class));
        verify(dashboardItemStoreMock, times(1)).save(any(DashboardItem.class));
        verify(dashboardElementStoreMock, times(1)).save(any(DashboardElement.class));
    }


    @Test
    public void testAddContentWithStoreFailingItToSaveItem() {
        dashboardContent.setType(DashboardContent.TYPE_CHART);

        when(dashboardItemStoreMock.save(any(DashboardItem.class))).thenReturn(false);
        when(dashboardElementStoreMock.save(any(DashboardElement.class))).thenReturn(true);
        doReturn(0).when(dashboardService).countItems(dashboard);

        boolean status = dashboardService.addContent(dashboard, dashboardContent);

        assertFalse(status);
        verify(dashboardService, times(1)).countItems(dashboard);
        verify(dashboardItemServiceMock, times(1)).create(any(Dashboard.class), anyString());
        verify(dashboardElementServiceMock, times(1)).create(any(DashboardItem.class), any(DashboardContent.class));
        verify(dashboardItemStoreMock, times(1)).save(any(DashboardItem.class));
    }


    @Test
    public void testAddContentWithStoreFailingItToSaveElement() {
        dashboardContent.setType(DashboardContent.TYPE_CHART);

        when(dashboardItemStoreMock.save(any(DashboardItem.class))).thenReturn(true);
        when(dashboardElementStoreMock.save(any(DashboardElement.class))).thenReturn(false);
        doReturn(0).when(dashboardService).countItems(dashboard);

        boolean status = dashboardService.addContent(dashboard, dashboardContent);

        assertFalse(status);
        verify(dashboardService, times(1)).countItems(dashboard);
        verify(dashboardItemServiceMock, times(1)).create(any(Dashboard.class), anyString());
        verify(dashboardElementServiceMock, times(1)).create(any(DashboardItem.class), any(DashboardContent.class));
        verify(dashboardItemStoreMock, times(1)).save(any(DashboardItem.class));
        verify(dashboardElementStoreMock, times(1)).save(any(DashboardElement.class));
    }


    @Test
    public void testAddUserToDashboard() {
        dashboardContent.setType(DashboardContent.TYPE_USERS);
        dashboardItem.setType(DashboardContent.TYPE_USERS);

        when(dashboardItemStoreMock.queryByDashboard(dashboard))
                .thenReturn(Arrays.asList(dashboardItem, dashboardItem));
        when(dashboardItemStoreMock.save(any(DashboardItem.class))).thenReturn(true);
        when(dashboardElementStoreMock.save(any(DashboardElement.class))).thenReturn(true);

        when(dashboardItemServiceMock.countElements(dashboardItem)).thenReturn(6);

        boolean status = dashboardService.addContent(dashboard, dashboardContent);

        assertTrue(status);
        verify(dashboardService, times(1)).countItems(dashboard);
        verify(dashboardElementStoreMock, times(1)).save(any(DashboardElement.class));
        verify(dashboardItemStoreMock, times(1)).save(any(DashboardItem.class));
    }
}

