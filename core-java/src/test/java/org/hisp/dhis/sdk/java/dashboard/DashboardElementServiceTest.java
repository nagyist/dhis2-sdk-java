package org.hisp.dhis.sdk.java.dashboard;

import org.hisp.dhis.java.sdk.models.dashboard.DashboardElement;
import org.hisp.dhis.java.sdk.models.dashboard.DashboardItem;
import org.hisp.dhis.sdk.java.common.IStateStore;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.mock;

public class DashboardElementServiceTest {
    private DashboardItem dashboardItemMock;
    private DashboardElement dashboardElementMock;

    private List<DashboardElement> dashboardElementListMock;

    private IStateStore stateStoreMock;
    private IDashboardElementStore dashboardElementStoreMock;
    private IDashboardItemService dashboardItemServiceMock;

    private IDashboardElementService dashboardElementService;

    @Before
    public void setUp() {
        dashboardItemMock = mock(DashboardItem.class);
        dashboardElementMock = mock(DashboardElement.class);

        dashboardElementListMock = new ArrayList<>();
        for (int i = 0; i < 16; i++) {
            dashboardElementListMock.add(mock(DashboardElement.class));
        }

        stateStoreMock = mock(IStateStore.class);
        dashboardElementStoreMock = mock(IDashboardElementStore.class);
        dashboardItemServiceMock = mock(IDashboardItemService.class);

        dashboardElementService = new DashboardElementService(
                dashboardElementStoreMock, dashboardItemServiceMock, stateStoreMock);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRemoveNullDashboardElement() {
        dashboardElementService.remove(null);
    }
}
