package org.hisp.dhis.sdk.java.trackedentity;

import org.hisp.dhis.java.sdk.models.trackedentity.TrackedEntityAttribute;
import org.hisp.dhis.java.sdk.models.trackedentity.TrackedEntityInstance;
import org.hisp.dhis.sdk.java.common.network.Response;
import org.joda.time.DateTime;

import java.util.List;

public interface ITrackedEntityApiClient {
    List<TrackedEntityAttribute> getBasicTrackedEntityAttributes(DateTime lastUpdated);

    List<TrackedEntityAttribute> getFullTrackedEntityAttributes(DateTime lastUpdated);

    List<TrackedEntityInstance> getBasicTrackedEntityInstances(String organisationUnitId, DateTime lastUpdated);

    TrackedEntityInstance getFullTrackedEntityInstance(String uid, DateTime lastUpdated);

    TrackedEntityInstance getBasicTrackedEntityInstance(String uid, DateTime lastUpdated);

    Response postTrackedEntityInstance(TrackedEntityInstance instance);

    Response putTrackedEntityInstance(TrackedEntityInstance instance);
}
