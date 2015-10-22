package org.hisp.dhis.sdk.java.dashboard;

import org.hisp.dhis.java.sdk.models.common.state.Action;
import org.hisp.dhis.java.sdk.models.dashboard.Dashboard;
import org.hisp.dhis.java.sdk.models.dashboard.DashboardItem;
import org.hisp.dhis.sdk.java.common.IStateStore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.hisp.dhis.sdk.java.utils.Preconditions.isNull;

public class DashboardItemService2 implements IDashboardItemService {
    private final IDashboardItemStore dashboardItemStore;
    private final IStateStore stateStore;

    public DashboardItemService2(IDashboardItemStore dashboardItemStore, IStateStore stateStore) {
        this.dashboardItemStore = dashboardItemStore;
        this.stateStore = stateStore;
    }

    @Override
    public List<DashboardItem> list(Dashboard dashboard) {
        isNull(dashboard, "Dashboard object must not be null");

        List<DashboardItem> allDashboardItems = dashboardItemStore.queryByDashboard(dashboard);
        Map<Long, Action> actionMap = stateStore.queryActionsForModel(DashboardItem.class);

        List<DashboardItem> dashboardItems = new ArrayList<>();
        for (DashboardItem dashboardItem : allDashboardItems) {
            Action action = actionMap.get(dashboardItem.getId());

            if (!Action.TO_DELETE.equals(action)) {
                dashboardItems.add(dashboardItem);
            }
        }

        return dashboardItems;
    }

    @Override
    public List<DashboardItem> list() {
        return stateStore.queryModelsWithActions(DashboardItem.class,
                Action.SYNCED, Action.TO_POST, Action.TO_UPDATE);
    }

    @Override
    public DashboardItem get(long id) {
        DashboardItem dashboardItem = dashboardItemStore.queryById(id);

        if (dashboardItem != null) {
            Action action = stateStore.queryActionForModel(dashboardItem);

            if (!Action.TO_DELETE.equals(action)) {
                return dashboardItem;
            }
        }

        return null;
    }

    @Override
    public DashboardItem get(String uid) {
        return null;
    }

    @Override
    public boolean remove(DashboardItem object) {
        return false;
    }
}
