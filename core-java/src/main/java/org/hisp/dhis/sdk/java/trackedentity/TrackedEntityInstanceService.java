package org.hisp.dhis.sdk.java.trackedentity;

import org.hisp.dhis.java.sdk.models.common.state.Action;
import org.hisp.dhis.java.sdk.models.organisationunit.OrganisationUnit;
import org.hisp.dhis.java.sdk.models.relationship.Relationship;
import org.hisp.dhis.java.sdk.models.relationship.RelationshipType;
import org.hisp.dhis.java.sdk.models.trackedentity.TrackedEntity;
import org.hisp.dhis.java.sdk.models.trackedentity.TrackedEntityInstance;
import org.hisp.dhis.sdk.java.common.IStateStore;
import org.hisp.dhis.sdk.java.relationship.IRelationshipStore;
import org.hisp.dhis.sdk.java.utils.CodeGenerator;

import java.util.List;

import static org.hisp.dhis.sdk.java.utils.Preconditions.isNull;

public final class TrackedEntityInstanceService implements ITrackedEntityInstanceService {

    private final ITrackedEntityInstanceStore trackedEntityInstanceStore;
    private final IRelationshipStore relationshipStore;
    private final IStateStore stateStore;

    public TrackedEntityInstanceService(ITrackedEntityInstanceStore trackedEntityInstanceStore, IRelationshipStore relationshipStore, IStateStore stateStore) {
        this.trackedEntityInstanceStore = trackedEntityInstanceStore;
        this.relationshipStore = relationshipStore;
        this.stateStore = stateStore;
    }

    @Override
    public TrackedEntityInstance get(String uid) {
        TrackedEntityInstance trackedEntityInstance = trackedEntityInstanceStore.query(uid);
        Action action = stateStore.queryActionForModel(trackedEntityInstance);

        if (!Action.TO_DELETE.equals(action)) {
            return trackedEntityInstance;
        }

        return null;
    }

    @Override
    public TrackedEntityInstance create(TrackedEntity trackedEntity, OrganisationUnit organisationUnit) {
        TrackedEntityInstance trackedEntityInstance = new TrackedEntityInstance();
        trackedEntityInstance.setOrgUnit(organisationUnit.getUId());
        trackedEntityInstance.setTrackedEntity(trackedEntity.getUId());
        trackedEntityInstance.setTrackedEntityInstanceUid(CodeGenerator.generateCode());
        if(add(trackedEntityInstance)) {
            return trackedEntityInstance;
        } else {
            return null;
        }
    }

    @Override
    public boolean addRelationship(TrackedEntityInstance trackedEntityInstanceA, TrackedEntityInstance trackedEntityInstanceB, RelationshipType relationshipType) {
        List<Relationship> existingRelationships = trackedEntityInstanceA.getRelationships();
        for(Relationship existingRelationship : existingRelationships) {
            if(existingRelationship.getTrackedEntityInstanceB()
                    .getTrackedEntityInstanceUid()
                    .equals(trackedEntityInstanceB.getTrackedEntityInstanceUid()) &&
                    relationshipType.equals(existingRelationship.getRelationship())) {
                return false;
            }
        }
        Relationship relationship = new Relationship();
        relationship.setTrackedEntityInstanceA(trackedEntityInstanceA);
        relationship.setTrackedEntityInstanceB(trackedEntityInstanceB);
        relationship.setRelationship(relationship.getRelationship());
        relationshipStore.insert(relationship);
        trackedEntityInstanceA.getRelationships().add(relationship);
        trackedEntityInstanceB.getRelationships().add(relationship);
        update(trackedEntityInstanceA);
        update(trackedEntityInstanceB);
        return true;
    }

    @Override
    public boolean removeRelationship(Relationship relationship) {
        relationshipStore.delete(relationship);
        relationship.getTrackedEntityInstanceA().getRelationships().remove(relationship);
        relationship.getTrackedEntityInstanceB().getRelationships().remove(relationship);
        update(relationship.getTrackedEntityInstanceA());
        update(relationship.getTrackedEntityInstanceB());
        return true;
    }

    @Override
    public boolean add(TrackedEntityInstance object) {
        trackedEntityInstanceStore.insert(object);
        stateStore.saveActionForModel(object, Action.TO_POST);

        return true;
    }

    @Override
    public TrackedEntityInstance get(long id) {
        TrackedEntityInstance trackedEntityInstance = trackedEntityInstanceStore.queryById(id);
        Action action = stateStore.queryActionForModel(trackedEntityInstance);

        if (!Action.TO_DELETE.equals(action)) {
            return trackedEntityInstance;
        }

        return null;
    }

    @Override
    public List<TrackedEntityInstance> list() {
        return stateStore.filterModelsByAction(TrackedEntityInstance.class, Action.TO_DELETE);
    }

    @Override
    public boolean remove(TrackedEntityInstance object) {
        isNull(object, "trackedEntityInstance argument must not be null");
        trackedEntityInstanceStore.delete(object);
        return true;
    }

    @Override
    public boolean save(TrackedEntityInstance object) {
        trackedEntityInstanceStore.save(object);

        // TODO check if object was created earlier (then set correct flag)
        Action action = stateStore.queryActionForModel(object);

        if (action == null) {
            stateStore.saveActionForModel(object, Action.TO_POST);
        } else {
            stateStore.saveActionForModel(object, Action.TO_UPDATE);
        }

        return true;
    }

    @Override
    public boolean update(TrackedEntityInstance object) {
        isNull(object, "object argument must not be null");

        Action action = stateStore.queryActionForModel(object);
        if (Action.TO_DELETE.equals(action)) {
            throw new IllegalArgumentException("The object with Action." +
                    "TO_DELETE cannot be updated");
        }

        /* if object was not posted to the server before,
        you don't have anything to update */
        if (!Action.TO_POST.equals(action)) {
            stateStore.saveActionForModel(object, Action.TO_UPDATE);
        }

        trackedEntityInstanceStore.update(object);

        return true;
    }
}
