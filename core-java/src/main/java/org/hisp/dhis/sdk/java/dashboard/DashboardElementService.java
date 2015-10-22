package org.hisp.dhis.sdk.java.dashboard;

import org.hisp.dhis.java.sdk.models.common.state.Action;
import org.hisp.dhis.java.sdk.models.dashboard.DashboardElement;
import org.hisp.dhis.java.sdk.models.dashboard.DashboardItem;
import org.hisp.dhis.sdk.java.common.IStateStore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.hisp.dhis.sdk.java.utils.Preconditions.isNull;

public class DashboardElementService implements IDashboardElementService {
    private final IStateStore stateStore;
    private final IDashboardElementStore dashboardElementStore;
    private final IDashboardItemService dashboardItemService;

    public DashboardElementService(IStateStore stateStore, IDashboardElementStore elementStore,
                                   IDashboardItemService dashboardItemService) {
        this.stateStore = stateStore;
        this.dashboardElementStore = elementStore;
        this.dashboardItemService = dashboardItemService;
    }

    @Override
    public boolean remove(DashboardElement object) {
        isNull(object, "DashboardElement object must not be null");

        Action action = stateStore.queryActionForModel(object);
        boolean isRemoved = false;
        if (action != null) {
            switch (action) {
                case SYNCED:
                case TO_UPDATE: {
                    /* for SYNCED and TO_UPDATE states we need only to mark model as removed */
                    isRemoved = stateStore.saveActionForModel(object, Action.TO_DELETE);
                    break;
                }
                case TO_POST: {
                    isRemoved = dashboardElementStore.delete(object);
                    break;
                }
                case TO_DELETE: {
                    isRemoved = false;
                    break;
                }
            }
        }

        if (isRemoved && !(count(object.getDashboardItem()) > 1)) {
            isRemoved = dashboardItemService.remove(object.getDashboardItem());
        }

        return isRemoved;
    }

    @Override
    public List<DashboardElement> list() {
        return stateStore.filterModelsByAction(DashboardElement.class, Action.TO_DELETE);
    }

    @Override
    public List<DashboardElement> list(DashboardItem dashboardItem) {
        isNull(dashboardItem, "DashboardItem object must not be null");

        List<DashboardElement> allDashboardElements = dashboardElementStore.queryByDashboardItem(dashboardItem);
        Map<Long, Action> actionMap = stateStore.queryActionsForModel(DashboardElement.class);

        List<DashboardElement> dashboardElements = new ArrayList<>();
        for (DashboardElement dashboardElement : allDashboardElements) {
            Action action = actionMap.get(dashboardElement.getId());

            if (!Action.TO_DELETE.equals(action)) {
                dashboardElements.add(dashboardElement);
            }
        }

        return dashboardElements;
    }

    @Override
    public int count(DashboardItem dashboardItem) {
        isNull(dashboardItem, "DashboardItem object must not be null");
        return stateStore.filterModelsByAction(DashboardElement.class, Action.TO_DELETE).size();
    }
}
