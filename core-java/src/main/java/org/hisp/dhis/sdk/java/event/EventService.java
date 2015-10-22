package org.hisp.dhis.sdk.java.event;

import org.hisp.dhis.java.sdk.models.common.state.Action;
import org.hisp.dhis.java.sdk.models.enrollment.Enrollment;
import org.hisp.dhis.java.sdk.models.event.Event;
import org.hisp.dhis.java.sdk.models.organisationunit.OrganisationUnit;
import org.hisp.dhis.java.sdk.models.program.Program;
import org.hisp.dhis.java.sdk.models.program.ProgramStage;
import org.hisp.dhis.java.sdk.models.trackedentity.TrackedEntityDataValue;
import org.hisp.dhis.java.sdk.models.trackedentity.TrackedEntityInstance;
import org.hisp.dhis.sdk.java.common.IStateStore;
import org.hisp.dhis.sdk.java.utils.CodeGenerator;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

import static org.hisp.dhis.sdk.java.utils.Preconditions.isNull;

public class EventService implements IEventService {

    private final IEventStore eventStore;
    private final IStateStore stateStore;

    public EventService(IEventStore eventStore, IStateStore stateStore) {
        this.eventStore = eventStore;
        this.stateStore = stateStore;
    }

    @Override
    public Event get(String uid) {
        Event event = eventStore.queryByUid(uid);
        Action action = stateStore.queryActionForModel(event);

        if (!Action.TO_DELETE.equals(action)) {
            return event;
        }

        return null;
    }

    @Override
    public Event create(TrackedEntityInstance trackedEntityInstance, Enrollment enrollment, OrganisationUnit organisationUnit, Program program, ProgramStage programStage, String status) {
        isNull(trackedEntityInstance, "trackedEntityInstance argument must not be null");
        isNull(enrollment, "enrollment argument must not be null");
        isNull(organisationUnit, "organisationUnit argument must not be null");
        isNull(program, "program argument must not be null");
        isNull(programStage, "programStage argument must not be null");
        isNull(status, "status argument must not be null");
        Event event = new Event();
        event.setEventUid(CodeGenerator.generateCode());
        event.setEnrollment(enrollment);
        event.setOrganisationUnitId(organisationUnit.getUId());
        event.setStatus(status);
        event.setTrackedEntityInstance(trackedEntityInstance);
        event.setProgramId(program.getUId());
        event.setProgramStageId(programStage.getUId());
        event.setTrackedEntityDataValues(new ArrayList<TrackedEntityDataValue>());

        DateTime dueDate = enrollment.getDateOfEnrollment();
        event.setDueDate(dueDate.plusDays(programStage.getMinDaysFromStart()));
        return event;
    }

    @Override
    public Event create(OrganisationUnit organisationUnit, String status, Program program, ProgramStage programStage) {
        isNull(organisationUnit, "organisationUnit argument must not be null");
        isNull(program, "program argument must not be null");
        isNull(programStage, "programStage argument must not be null");
        isNull(status, "status argument must not be null");
        if(!Event.STATUS_ACTIVE.equals(status) && !Event.STATUS_COMPLETED.equals(status)) {
            throw new IllegalArgumentException("event status must be either ACTIVE or COMPLETED");
        }

        Event event = new Event();
        event.setEventUid(CodeGenerator.generateCode());
        event.setStatus(status);
        event.setOrganisationUnitId(organisationUnit.getUId());
        event.setProgramId(program.getUId());
        event.setProgramStageId(programStage.getUId());
        event.setTrackedEntityDataValues(new ArrayList<TrackedEntityDataValue>());
        return event;
    }

    @Override
    public List<Event> list(Program program, OrganisationUnit organisationUnit, DateTime startDate, DateTime endDate) {
        List<Event> events = eventStore.query(organisationUnit, program);
        for(int i = 0; i<events.size(); i++) {
            Event event = events.get(i);
            if(event.getDueDate().isBefore(startDate) || event.getDueDate().isAfter(endDate)) {
                events.remove(i);
                i--;
            }
        }
        return events;
    }

    @Override
    public boolean add(Event object) {
        isNull(object, "event argument must not be null");
        if(!eventStore.insert(object)) {
            return false;
        }
        return stateStore.saveActionForModel(object, Action.TO_POST);
    }

    @Override
    public Event get(long id) {
        Event event = eventStore.queryById(id);
        Action action = stateStore.queryActionForModel(event);

        if (!Action.TO_DELETE.equals(action)) {
            return event;
        }
        return null;
    }

    @Override
    public List<Event> list() {
        return stateStore.queryModelsWithAction(Event.class, Action.TO_POST, Action.SYNCED, Action.TO_UPDATE);
    }

    @Override
    public boolean remove(Event object) {
        isNull(object, "event argument must not be null");
        if(!eventStore.delete(object)) {
            return false;
        }
        return stateStore.deleteActionForModel(object);
    }

    @Override
    public boolean save(Event object) {
        isNull(object, "event argument must not be null");
        if(!eventStore.save(object)) {
            return false;
        }

        Action action = stateStore.queryActionForModel(object);
        if (action == null || Action.TO_POST.equals(action)) {
            return stateStore.saveActionForModel(object, Action.TO_POST);
        } else {
            return stateStore.saveActionForModel(object, Action.TO_UPDATE);
        }
    }

    @Override
    public boolean update(Event object) {
        isNull(object, "event argument must not be null");

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

        return eventStore.update(object);
    }
}
