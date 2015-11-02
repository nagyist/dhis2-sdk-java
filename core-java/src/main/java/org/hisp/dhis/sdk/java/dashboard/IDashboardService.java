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

import org.hisp.dhis.java.sdk.models.dashboard.Dashboard;
import org.hisp.dhis.java.sdk.models.dashboard.DashboardContent;
import org.hisp.dhis.sdk.java.common.services.*;

public interface IDashboardService extends IService, ISave<Dashboard>, IRemove<Dashboard>, IGet<Dashboard>,
        IGetUid<Dashboard>, IList<Dashboard> {

    /**
     * Will try to append DashboardContent to current dashboard.
     * If the type of DashboardContent is embedded (chart, eventChart, map, eventReport, reportTable),
     * method will create a new item and append it to dashboard.
     * <p/>
     * If the type of DashboardContent is link type (users, reports, resources),
     * method will try to append content to existing item. Otherwise it will create a new dashboard item.
     * <p/>
     * If the overall count of items in dashboard is bigger that Dashboard.MAX_ITEMS, method will not
     * add content and return false;
     *
     * @param dashboard dashboard to which we want add new content.
     * @param content   content which we want to add to given dashboard.
     * @return false if item count is bigger than MAX_ITEMS.
     * @throws IllegalArgumentException if dashboard or content is null.
     */
    boolean addContent(Dashboard dashboard, DashboardContent content);


    /**
     * Returns an item from this dashboard of the given type which number of
     * content is less than max. Returns null if no item matches the criteria.
     *
     * @param type the type of content to return.
     * @return an item.
     * @throws IllegalArgumentException if dashboard or type is null.
     */
    // DashboardItem getAvailableItemByType(Dashboard dashboard, String type);
}
