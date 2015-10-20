package org.hisp.dhis.sdk.java.dashboard;

import org.hisp.dhis.java.sdk.models.common.state.Action;
import org.hisp.dhis.java.sdk.models.dashboard.DashboardElement;
import org.hisp.dhis.java.sdk.models.dashboard.DashboardItem;
import org.hisp.dhis.sdk.java.common.IStateStore;

import java.util.List;

import static org.hisp.dhis.sdk.java.utils.Preconditions.isNull;

public class DashboardElementService2 implements IDashboardElementService {
    private final IStateStore stateStore;
    private final IDashboardElementStore dashboardElementStore;
    private final IDashboardItemService dashboardItemService;

    public DashboardElementService2(IStateStore stateStore, IDashboardElementStore elementStore,
                                    IDashboardItemService dashboardItemService) {
        this.stateStore = stateStore;
        this.dashboardElementStore = elementStore;
        this.dashboardItemService = dashboardItemService;
    }

    @Override
    public boolean remove(DashboardElement object) {
        isNull(object, "DashboardElement object must not be null");

        Action action = stateStore.queryActionForModel(object);
        if (action == null) {
            return false;
        }

        boolean status;
        switch (action) {
            case TO_POST: {
                status = dashboardElementStore.delete(object);
                break;
            }
            case TO_DELETE: {
                status = false;
                break;
            }
            default: {
                status = stateStore.saveActionForModel(object, Action.TO_DELETE);
            }
        }

        if (!(count(object.getDashboardItem()) > 1)) {
            status = dashboardItemService.remove(object.getDashboardItem()) && status;
        }

        return status;
    }

    @Override
    public List<DashboardElement> list(DashboardItem dashboardItem) {
        return null;
    }

    @Override
    public int count(DashboardItem dashboardItem) {
        return 0;
    }

    @Override
    public List<DashboardElement> list() {
        return null;
    }
}
