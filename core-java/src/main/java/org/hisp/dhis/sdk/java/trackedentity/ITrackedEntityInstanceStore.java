package org.hisp.dhis.sdk.java.trackedentity;

import org.hisp.dhis.java.sdk.models.trackedentity.TrackedEntityInstance;
import org.hisp.dhis.sdk.java.common.persistence.IStore;

public interface ITrackedEntityInstanceStore extends IStore<TrackedEntityInstance> {
    TrackedEntityInstance query(long id);

    TrackedEntityInstance query(String uid);
}
