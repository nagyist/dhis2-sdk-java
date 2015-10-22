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
    private IDashboardItemContentService dashboardItemContentService;

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
