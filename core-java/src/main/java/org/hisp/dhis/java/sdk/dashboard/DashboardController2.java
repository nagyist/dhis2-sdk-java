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
import org.hisp.dhis.java.sdk.common.persistence.IDbOperation;
import org.hisp.dhis.java.sdk.common.persistence.ITransactionManager;
import org.hisp.dhis.java.sdk.common.preferences.ILastUpdatedPreferences;
import org.hisp.dhis.java.sdk.common.preferences.ResourceType;
import org.hisp.dhis.java.sdk.models.common.MergeStrategy;
import org.hisp.dhis.java.sdk.models.common.state.Action;
import org.hisp.dhis.java.sdk.models.dashboard.Dashboard;
import org.hisp.dhis.java.sdk.models.dashboard.DashboardItem;
import org.hisp.dhis.java.sdk.models.utils.ModelUtils;
import org.joda.time.DateTime;

import java.util.List;

public class DashboardController2 implements IDashboardController {
    private final IStateStore stateStore;
    private final IDashboardStore dashboardStore;
    private final IDashboardApiClient dashboardApiClient;
    private final ModelUtils modelUtils;
    private final ILastUpdatedPreferences lastUpdatedPreferences;
    private final ITransactionManager transactionManager;

    public DashboardController2(IStateStore stateStore, IDashboardStore dashboardStore,
                                IDashboardApiClient dashboardApiClient, ModelUtils modelUtils,
                                ILastUpdatedPreferences lastUpdatedPreferences,
                                ITransactionManager transactionManager) {
        this.stateStore = stateStore;
        this.dashboardStore = dashboardStore;
        this.dashboardApiClient = dashboardApiClient;
        this.modelUtils = modelUtils;
        this.lastUpdatedPreferences = lastUpdatedPreferences;
        this.transactionManager = transactionManager;
    }

    @Override
    public void sync() {
        update();
        send();
    }

    @Override
    public void update() {
        DateTime lastUpdated = lastUpdatedPreferences.get(ResourceType.DASHBOARDS);

        List<IDbOperation> updatedDashboards = updateDashboards(lastUpdated);
        List<IDbOperation> updateDashboardItems = updateDashboardItems(lastUpdated);
        // List<DashboardElement> updateDashboardElements = updateDashboardElements(updateDashboardItems, lastUpdated);
    }

    @Override
    public void send() {

    }

    @Override
    public List<IDbOperation> updateDashboards(DateTime lastUpdated) {
        List<Dashboard> actualDashboards = dashboardApiClient.getDashboardUids(lastUpdated);
        List<Dashboard> updatedDashboards = dashboardApiClient.getDashboards(lastUpdated);
        List<Dashboard> persistedDashboards = stateStore.queryModelsWithActions(Dashboard.class,
                Action.SYNCED, Action.TO_UPDATE, Action.TO_DELETE);

        // List<Dashboard> mergedDashboards = modelUtils.merge(actualDashboards, updatedDashboards, persistedDashboards);
        // return transactionManager.createOperations(dashboardStore, persistedDashboards, mergedDashboards);

        List<Dashboard> newAndUpdatedDashboards = modelUtils.mergeWith(
                actualDashboards, updatedDashboards, MergeStrategy.REPLACE);
        List<Dashboard> allDashboards = modelUtils.mergeWith(
                persistedDashboards, newAndUpdatedDashboards, MergeStrategy.REPLACE_IF_UPDATED);
        return transactionManager.createOperations(dashboardStore, allDashboards, persistedDashboards);
    }

    @Override
    public List<IDbOperation> updateDashboardItems(DateTime lastUpdated) {
        List<DashboardItem> existingDashboardItems = dashboardApiClient.getBaseDashboardItems(lastUpdated);
        List<DashboardItem> updatedDashboardItems = dashboardApiClient.getDashboardItems(lastUpdated);
        List<DashboardItem> persistedDashboardItems = stateStore.queryModelsWithActions(DashboardItem.class,
                Action.SYNCED, Action.TO_UPDATE, Action.TO_DELETE);

        return null;
        // return modelUtils.merge(existingDashboardItems, updatedDashboardItems, persistedDashboardItems);
    }


    /* @Override
    public List<DashboardItem> updateDashboardItems(DateTime lastUpdated) {
        List<DashboardItem> actualDashboardItems = new ArrayList<>();
        for (Dashboard dashboard : dashboards) {
            actualDashboardItems.addAll(dashboard.getDashboardItems());
        }

        Map<String, DashboardItem> updatedItemsMap = modelUtils
                .toMap(dashboardApiClient.getBaseDashboardItems(lastUpdated));
        Map<String, DashboardItem> persistedItemsMap = modelUtils.toMap(stateStore
                .queryModelsWithActions(DashboardItem.class, Action.SYNCED, Action.TO_UPDATE, Action.TO_DELETE));

        // merging updated items with actual
        for (DashboardItem actualItem : actualDashboardItems) {
            DashboardItem updatedItem = updatedItemsMap.get(actualItem.getUId());
            DashboardItem persistedItem = persistedItemsMap.get(actualItem.getUId());

            if (persistedItem != null) {
                actualItem.setId(persistedItem.getId());
            }

            if (updatedItem != null) {
                actualItem.setCreated(updatedItem.getCreated());
                actualItem.setLastUpdated(updatedItem.getLastUpdated());
                actualItem.setShape(updatedItem.getShape());
            }
        }

        return actualDashboardItems;
    } */

    // @Override
    // public List<DashboardElement> updateDashboardElements(List<DashboardItem> dashboardItems, DateTime lastUpdated) {
    //    return null;
    // }
//
//    private List<Dashboard> queryDashboards(Action... actions) {
//        /* reading persisted dashboards (excluding those which were not posted to server yet) */
//        List<Dashboard> persistedDashboards = stateStore.queryModelsWithActions(Dashboard.class, actions);
//        Map<Long, List<DashboardItem>> persistedDashboardItems = queryDashboardItems(actions);
//        Map<Long, List<DashboardElement>> persistedDashboardElements = queryDashboardElements(actions);
//
//        /* build relationships */
//        for (Dashboard dashboard : persistedDashboards) {
//            List<DashboardItem> dashboardItems = persistedDashboardItems.get(dashboard.getId());
//
//            if (dashboardItems != null) {
//                dashboard.setDashboardItems(dashboardItems);
//
//                for (DashboardItem dashboardItem : dashboardItems) {
//                    List<DashboardElement> dashboardElements = persistedDashboardElements.get(dashboardItem.getId());
//
//                    if (dashboardElements != null) {
//                        dashboardItem.setDashboardElements(dashboardElements);
//                    }
//                }
//            }
//        }
//
//        return persistedDashboards;
//    }
//
//    /* returns map where key is id of dashboard and value is list of dashboard items */
//    private Map<Long, List<DashboardItem>> queryDashboardItems(Action... actions) {
//        List<DashboardItem> dashboardItemsList = stateStore.queryModelsWithActions(DashboardItem.class, actions);
//        Map<Long, List<DashboardItem>> dashboardItemMap = new HashMap<>();
//
//        for (DashboardItem dashboardItem : dashboardItemsList) {
//            Long dashboardId = dashboardItem.getDashboard().getId();
//
//            List<DashboardItem> bag = dashboardItemMap.get(dashboardId);
//            if (bag == null) {
//                bag = new ArrayList<>();
//                dashboardItemMap.put(dashboardId, bag);
//            }
//
//            bag.add(dashboardItem);
//        }
//
//        return dashboardItemMap;
//    }
//
//    /* returns map where key is id of dashboard item and value is list of dashboard elements */
//    private Map<Long, List<DashboardElement>> queryDashboardElements(Action... actions) {
//        List<DashboardElement> dashboardElementsList = stateStore.queryModelsWithActions(DashboardElement.class, actions);
//        Map<Long, List<DashboardElement>> dashboardElementMap = new HashMap<>();
//
//        for (DashboardElement dashboardElement : dashboardElementsList) {
//            Long dashboardItemId = dashboardElement.getDashboardItem().getId();
//
//            List<DashboardElement> bag = dashboardElementMap.get(dashboardItemId);
//            if (bag == null) {
//                bag = new ArrayList<>();
//                dashboardElementMap.put(dashboardItemId, bag);
//            }
//
//            bag.add(dashboardElement);
//        }
//
//        return dashboardElementMap;
//    }
}
