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
import org.hisp.dhis.sdk.java.common.IStateStore;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

public class DashboardServiceTest {
    private Dashboard dashboardMock;
    private IDashboardStore dashboardStoreMock;
    private IStateStore stateStoreMock;

    private IDashboardService dashboardService;

    @Before
    public void setUp() {
        dashboardMock = mock(Dashboard.class);
        dashboardStoreMock = mock(IDashboardStore.class);

        stateStoreMock = mock(IStateStore.class);

        dashboardService = new DashboardService2(dashboardStoreMock, stateStoreMock);
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
}

