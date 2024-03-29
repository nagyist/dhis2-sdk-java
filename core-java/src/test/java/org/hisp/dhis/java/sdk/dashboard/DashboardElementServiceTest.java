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
import org.hisp.dhis.java.sdk.models.common.state.Action;
import org.hisp.dhis.java.sdk.models.dashboard.DashboardContent;
import org.hisp.dhis.java.sdk.models.dashboard.DashboardElement;
import org.hisp.dhis.java.sdk.models.dashboard.DashboardItem;
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
    private DashboardItem dashboardItem;
    private DashboardElement dashboardElement;
    private DashboardContent dashboardContent;

    private IStateStore stateStoreMock;
    private IDashboardItemService dashboardItemServiceMock;
    private IDashboardElementStore dashboardElementStoreMock;

    private IDashboardElementService dashboardElementService;

    @Before
    public void setUp() {
        dashboardItem = new DashboardItem();
        dashboardElement = new DashboardElement();
        dashboardContent = new DashboardContent();

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
        when(dashboardItemServiceMock.countElements(any(DashboardItem.class))).thenReturn(4);
        when(stateStoreMock.queryActionForModel(dashboardElement)).thenReturn(null);

        boolean status = dashboardElementService.remove(dashboardElement);

        assertFalse(status);
    }

    @Test
    public void testRemoveDashboardElementSynced() {
        when(dashboardItemServiceMock.countElements(any(DashboardItem.class))).thenReturn(4);
        when(stateStoreMock.queryActionForModel(dashboardElement)).thenReturn(Action.SYNCED);

        boolean status = dashboardElementService.remove(dashboardElement);

        assertTrue(status);
        verify(stateStoreMock, times(1)).saveActionForModel(dashboardElement, Action.TO_DELETE);
    }

    @Test
    public void testRemoveDashboardElementToUpdate() {
        when(dashboardItemServiceMock.countElements(any(DashboardItem.class))).thenReturn(4);
        when(stateStoreMock.queryActionForModel(dashboardElement)).thenReturn(Action.TO_UPDATE);

        boolean status = dashboardElementService.remove(dashboardElement);

        assertTrue(status);
        verify(stateStoreMock).saveActionForModel(dashboardElement, Action.TO_DELETE);
    }

    @Test
    public void testRemoveDashboardElementToPost() {
        when(dashboardItemServiceMock.countElements(any(DashboardItem.class))).thenReturn(4);
        when(stateStoreMock.queryActionForModel(dashboardElement)).thenReturn(Action.TO_POST);

        boolean status = dashboardElementService.remove(dashboardElement);

        assertTrue(status);
        verify(stateStoreMock, never()).saveActionForModel(any(DashboardContent.class), any(Action.class));
        verify(dashboardElementStoreMock, times(1)).delete(dashboardElement);
    }

    @Test
    public void testRemoveDashboardElementToDelete() {
        when(dashboardItemServiceMock.countElements(any(DashboardItem.class))).thenReturn(4);
        when(stateStoreMock.queryActionForModel(dashboardElement)).thenReturn(Action.TO_DELETE);

        boolean status = dashboardElementService.remove(dashboardElement);

        assertFalse(status);
        verify(stateStoreMock, never()).saveActionForModel(any(DashboardElement.class), any(Action.class));
    }

    @Test
    public void testRemoveLastDashboardElement() {
        when(dashboardItemServiceMock.countElements(any(DashboardItem.class))).thenReturn(1);
        when(stateStoreMock.queryActionForModel(dashboardElement)).thenReturn(Action.SYNCED);
        dashboardElement.setDashboardItem(dashboardItem);

        boolean status = dashboardElementService.remove(dashboardElement);

        assertTrue(status);
        verify(dashboardItemServiceMock, times(1)).remove(dashboardItem);
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
        DashboardElement dashboardElementSynced = new DashboardElement();
        DashboardElement dashboardElementToUpdate = new DashboardElement();
        DashboardElement dashboardElementToDelete = new DashboardElement();
        DashboardElement dashboardElementToPost = new DashboardElement();

        List<DashboardElement> dashboardElementMockList = Arrays.asList(dashboardElementSynced,
                dashboardElementToUpdate, dashboardElementToDelete, dashboardElementToPost);

        dashboardElementSynced.setId(1L);
        dashboardElementToUpdate.setId(2L);
        dashboardElementToDelete.setId(3L);
        dashboardElementToPost.setId(4L);

        Map<Long, Action> actionMap = new HashMap<>();
        actionMap.put(dashboardElementSynced.getId(), Action.SYNCED);
        actionMap.put(dashboardElementToUpdate.getId(), Action.TO_UPDATE);
        actionMap.put(dashboardElementToDelete.getId(), Action.TO_DELETE);
        actionMap.put(dashboardElementToPost.getId(), Action.TO_POST);

        when(stateStoreMock.queryActionsForModel(DashboardElement.class)).thenReturn(actionMap);
        when(dashboardElementStoreMock.queryByDashboardItem(dashboardItem)).thenReturn(dashboardElementMockList);

        List<DashboardElement> dashboardElements = dashboardElementService.list(dashboardItem);

        verify(dashboardElementStoreMock, times(1)).queryByDashboardItem(dashboardItem);
        verify(stateStoreMock, times(1)).queryActionsForModel(DashboardElement.class);
        assertFalse(dashboardElements.contains(dashboardElementToDelete));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateDashboardElementShouldThrowOnNullDashboardItem() {
        dashboardElementService.create(null, dashboardContent);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateDashboardElementShouldThrowOnNullDashboardContent() {
        dashboardElementService.create(dashboardItem, null);
    }

    @Test
    public void testCreateDashboardElement() {
        DateTime dateTime = DateTime.now();

        DashboardContent content = new DashboardContent();
        content.setName("fancyName");
        content.setDisplayName("fancyDisplayName");
        content.setCreated(dateTime);
        content.setLastUpdated(dateTime);
        content.setType(DashboardContent.TYPE_CHART);

        DashboardElement dashboardElement = dashboardElementService.create(dashboardItem, content);

        assertNotNull(dashboardElement.getUId());
        assertNotNull(dashboardElement.getAccess());
        assertEquals(dashboardElement.getCreated(), dateTime);
        assertEquals(dashboardElement.getLastUpdated(), dateTime);
        assertEquals(dashboardElement.getName(), content.getName());
        assertEquals(dashboardElement.getDisplayName(), content.getDisplayName());
        assertSame(dashboardElement.getDashboardItem(), dashboardItem);
    }
}
