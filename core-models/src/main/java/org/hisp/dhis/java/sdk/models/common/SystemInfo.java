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

package org.hisp.dhis.java.sdk.models.common;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.joda.time.DateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
public final class SystemInfo {

    @JsonProperty("buildTime")
    DateTime buildTime;

    @JsonProperty("serverDate")
    DateTime serverDate;

    @JsonProperty("calendar")
    String calendar;

    @JsonProperty("dateFormat")
    String dateFormat;

    @JsonProperty("intervalSinceLastAnalyticsTableSuccess")
    String intervalSinceLastAnalyticsTableSuccess;

    @JsonProperty("lastAnalyticsTableSuccess")
    String lastAnalyticsTableSuccess;

    @JsonProperty("revision")
    int revision;

    @JsonProperty("version")
    String version;

    public DateTime getBuildTime() {
        return buildTime;
    }

    public void setBuildTime(DateTime buildTime) {
        this.buildTime = buildTime;
    }

    public DateTime getServerDate() {
        return serverDate;
    }

    public void setServerDate(DateTime serverDate) {
        this.serverDate = serverDate;
    }

    public String getCalendar() {
        return calendar;
    }

    public void setCalendar(String calendar) {
        this.calendar = calendar;
    }

    public String getDateFormat() {
        return dateFormat;
    }

    public void setDateFormat(String dateFormat) {
        this.dateFormat = dateFormat;
    }

    public String getIntervalSinceLastAnalyticsTableSuccess() {
        return intervalSinceLastAnalyticsTableSuccess;
    }

    public void setIntervalSinceLastAnalyticsTableSuccess(String date) {
        this.intervalSinceLastAnalyticsTableSuccess = date;
    }

    public String getLastAnalyticsTableSuccess() {
        return lastAnalyticsTableSuccess;
    }

    public void setLastAnalyticsTableSuccess(String lastAnalyticsTableSuccess) {
        this.lastAnalyticsTableSuccess = lastAnalyticsTableSuccess;
    }

    public int getRevision() {
        return revision;
    }

    public void setRevision(int revision) {
        this.revision = revision;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}
