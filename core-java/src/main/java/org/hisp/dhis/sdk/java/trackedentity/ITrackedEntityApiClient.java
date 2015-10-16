package org.hisp.dhis.sdk.java.trackedentity;

import org.hisp.dhis.java.sdk.models.trackedentity.TrackedEntityAttribute;
import org.joda.time.DateTime;

import java.util.List;

public interface ITrackedEntityApiClient {
    List<TrackedEntityAttribute> getBasicTrackedEntityAttributes(DateTime lastUpdated);

    List<TrackedEntityAttribute> getFullTrackedEntityAttributes(DateTime lastUpdated);
}
