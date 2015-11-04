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

import org.hisp.dhis.java.sdk.models.dashboard.DashboardContent;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

public class DashboardContentServiceTest {
    private DashboardContent dashboardContentMock;
    private List<DashboardContent> dashboardContentsMockList;

    private IDashboardItemContentStore dashboardItemContentStoreMock;
    private IDashboardContentService dashboardItemContentService;

    @Before
    public void setUp() {
        dashboardContentMock = mock(DashboardContent.class);
        dashboardContentsMockList = Arrays.asList(dashboardContentMock, dashboardContentMock, dashboardContentMock);
        dashboardItemContentStoreMock = mock(IDashboardItemContentStore.class);
        dashboardItemContentService = new DashboardContentService(dashboardItemContentStoreMock);
    }

    @Test
    public void testGetById() {
        when(dashboardItemContentStoreMock.queryById(anyInt())).thenReturn(dashboardContentMock);

        dashboardItemContentService.get(12);

        verify(dashboardItemContentStoreMock, times(1)).queryById(12);
    }

    @Test
    public void testGetByUid() {
        when(dashboardItemContentStoreMock.queryByUid(anyString())).thenReturn(dashboardContentMock);

        dashboardItemContentService.get("123");

        verify(dashboardItemContentStoreMock, times(1)).queryByUid("123");
    }

    @Test
    public void testList() {
        when(dashboardItemContentStoreMock.queryAll())
                .thenReturn(dashboardContentsMockList);

        dashboardItemContentService.list();

        verify(dashboardItemContentStoreMock, times(1)).queryAll();
    }

    @Test
    public void testListByTypes() {
        when(dashboardItemContentStoreMock.queryByTypes(anyListOf(String.class)))
                .thenReturn(dashboardContentsMockList);

        List<String> types = Arrays.asList("type_one", "type_two");
        dashboardItemContentService.list(types);

        verify(dashboardItemContentStoreMock, times(1)).queryByTypes(types);
    }
}
