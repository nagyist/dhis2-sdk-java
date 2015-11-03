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
import org.hisp.dhis.java.sdk.models.dashboard.DashboardContent;
import org.hisp.dhis.java.sdk.models.dashboard.DashboardElement;
import org.hisp.dhis.java.sdk.models.dashboard.DashboardItem;
import org.hisp.dhis.sdk.java.common.IStateStore;
import org.joda.time.DateTime;
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
    private DashboardContent dashboardContentMock;

    private IStateStore stateStoreMock;
    private IDashboardItemService dashboardItemServiceMock;
    private IDashboardElementStore dashboardElementStoreMock;

    private IDashboardElementService dashboardElementService;

    @Before
    public void setUp() {
        dashboardItemMock = mock(DashboardItem.class);
        dashboardElementMock = mock(DashboardElement.class);
        dashboardContentMock = mock(DashboardContent.class);

        /* Mocking state store */
        stateStoreMock = mock(IStateStore.class);
        when(stateStoreMock.saveActionForModel(any(DashboardElement.class), any(Action.class))).thenReturn(true);

        dashboardElementStoreMock = mock(IDashboardElementStore.class);
        when(dashboardElementStoreMock.delete(any(DashboardElement.class))).thenReturn(true);

        dashboardItemServiceMock = mock(IDashboardItemService.class);
        when(dashboardItemServiceMock.remove(any(DashboardItem.class))).thenReturn(true);

        dashboardElementService = new DashboardElementService(stateStoreMock,
                dashboardElementStoreMock, dashboardItemServiceMock);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRemoveNullDashboardElement() {
        dashboardElementService.remove(null);
    }

    @Test
    public void testRemoveNoneExistingDashboardElement() {
        when(dashboardItemServiceMock.countItems(any(DashboardItem.class))).thenReturn(4);
        when(stateStoreMock.queryActionForModel(dashboardElementMock)).thenReturn(null);

        boolean status = dashboardElementService.remove(dashboardElementMock);

        assertFalse(status);
    }

    @Test
    public void testRemoveDashboardElementSynced() {
        when(dashboardItemServiceMock.countItems(any(DashboardItem.class))).thenReturn(4);
        when(stateStoreMock.queryActionForModel(dashboardElementMock)).thenReturn(Action.SYNCED);

        boolean status = dashboardElementService.remove(dashboardElementMock);

        assertTrue(status);
        verify(stateStoreMock, times(1)).saveActionForModel(dashboardElementMock, Action.TO_DELETE);
    }

    @Test
    public void testRemoveDashboardElementToUpdate() {
        when(dashboardItemServiceMock.countItems(any(DashboardItem.class))).thenReturn(4);
        when(stateStoreMock.queryActionForModel(dashboardElementMock)).thenReturn(Action.TO_UPDATE);

        boolean status = dashboardElementService.remove(dashboardElementMock);

        assertTrue(status);
        verify(stateStoreMock).saveActionForModel(dashboardElementMock, Action.TO_DELETE);
    }

    @Test
    public void testRemoveDashboardElementToPost() {
        when(dashboardItemServiceMock.countItems(any(DashboardItem.class))).thenReturn(4);
        when(stateStoreMock.queryActionForModel(dashboardElementMock)).thenReturn(Action.TO_POST);

        boolean status = dashboardElementService.remove(dashboardElementMock);

        assertTrue(status);
        verify(stateStoreMock, never()).saveActionForModel(any(DashboardContent.class), any(Action.class));
        verify(dashboardElementStoreMock, times(1)).delete(dashboardElementMock);
    }

    @Test
    public void testRemoveDashboardElementToDelete() {
        when(dashboardItemServiceMock.countItems(any(DashboardItem.class))).thenReturn(4);
        when(stateStoreMock.queryActionForModel(dashboardElementMock)).thenReturn(Action.TO_DELETE);

        boolean status = dashboardElementService.remove(dashboardElementMock);

        assertFalse(status);
        verify(stateStoreMock, never()).saveActionForModel(any(DashboardElement.class), any(Action.class));
    }

    @Test
    public void testRemoveLastDashboardElement() {
        when(dashboardItemServiceMock.countItems(any(DashboardItem.class))).thenReturn(1);
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
    public void testCreateDashboardElementShouldThrowOnNullDashboardItem() {
        dashboardElementService.create(null, dashboardContentMock);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateDashboardElementShouldThrowOnNullDashboardContent() {
        dashboardElementService.create(dashboardItemMock, null);
    }

    @Test
    public void testCreateDashboardElement() {
        DateTime dateTime = DateTime.now();
        when(dashboardContentMock.getName()).thenReturn("fancyName");
        when(dashboardContentMock.getDisplayName()).thenReturn("fancyDisplayName");
        when(dashboardContentMock.getCreated()).thenReturn(dateTime);
        when(dashboardContentMock.getLastUpdated()).thenReturn(dateTime);
        when(dashboardContentMock.getType()).thenReturn(DashboardContent.TYPE_CHART);

        DashboardElement dashboardElement = dashboardElementService.create(
                dashboardItemMock, dashboardContentMock);

        assertNotNull(dashboardElement.getUId());
        assertNotNull(dashboardElement.getAccess());
        assertEquals(dashboardElement.getCreated(), dateTime);
        assertEquals(dashboardElement.getLastUpdated(), dateTime);
        assertEquals(dashboardElement.getName(), dashboardContentMock.getName());
        assertEquals(dashboardElement.getDisplayName(), dashboardContentMock.getDisplayName());
        assertSame(dashboardElement.getDashboardItem(), dashboardItemMock);
    }
}
