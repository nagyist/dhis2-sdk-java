package org.hisp.dhis.sdk.java.event;

import org.hisp.dhis.java.sdk.models.common.importsummary.ImportSummary;
import org.hisp.dhis.java.sdk.models.event.Event;
import org.joda.time.DateTime;

import java.util.List;

public interface IEventApiClient {
    List<Event> getFullEvents(String programUid, String organisationUnitId, int count, DateTime lastUpdated);

    List<Event> getBasicEvents(String programUid, String enrollmentStatus, String trackedEntityInstanceUid, DateTime lastUpdated);

    List<Event> getFullEvents(String programUid, String enrollmentStatus, String trackedEntityInstanceUid, DateTime lastUpdated);

    Event getFullEvent(String uid, DateTime lastUpdated);

    Event getBasicEvent(String uid, DateTime lastUpdated);

    ImportSummary postEvent(Event event);

    ImportSummary putEvent(Event event);
}
