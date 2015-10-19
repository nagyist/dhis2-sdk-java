package org.hisp.dhis.sdk.java.dashboard;

import org.hisp.dhis.java.sdk.models.dashboard.DashboardContent;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DashboardContentServiceTest {
    /* mocks */
    private DashboardContent dashboardContentMock;
    private List<DashboardContent> dashboardContentListMock;
    private IDashboardItemContentStore dashboardItemContentStoreMock;

    /* should be a real implementation */
    private IDashboardItemContentService dashboardItemContentService;

    @Before
    public void setUp() {
        dashboardContentMock = mock(DashboardContent.class);
        dashboardContentListMock = new ArrayList<>();
        for (int i = 0; i < 16; i++) {
            dashboardContentListMock.add(mock(DashboardContent.class));
            dashboardContentListMock.add(mock(DashboardContent.class));
            dashboardContentListMock.add(mock(DashboardContent.class));
        }

        dashboardItemContentStoreMock = mock(IDashboardItemContentStore.class);
        when(dashboardItemContentStoreMock.queryById(anyInt())).thenReturn(dashboardContentMock);
        when(dashboardItemContentStoreMock.queryByUid(anyString())).thenReturn(dashboardContentMock);
        when(dashboardItemContentStoreMock.queryAll()).thenReturn(dashboardContentListMock);
        when(dashboardItemContentStoreMock.queryByTypes(anyListOf(String.class))).thenReturn(dashboardContentListMock);

        dashboardItemContentService = new DashboardContentService(dashboardItemContentStoreMock);
    }

    @Test
    public void testGetById() {
        DashboardContent dashboardContent = dashboardItemContentService.get(12);
        assertEquals(dashboardContent, dashboardContentMock);
    }

    @Test
    public void testGetByUid() {
        DashboardContent dashboardContent = dashboardItemContentService.get("123");
        assertEquals(dashboardContent, dashboardContentMock);
    }

    @Test
    public void testList() {
        List<DashboardContent> dashboardContents = dashboardItemContentService.list();
        assertEquals(dashboardContents, dashboardContentListMock);
    }

    @Test
    public void testListByTypes() {
        List<DashboardContent> dashboardContents = dashboardItemContentService
                .list(Arrays.asList("type_one", "type_two"));
        assertEquals(dashboardContents, dashboardContentListMock);
    }
}
