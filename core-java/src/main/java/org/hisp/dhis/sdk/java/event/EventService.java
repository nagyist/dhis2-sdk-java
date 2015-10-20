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
        Event event = eventStore.query(uid);
        Action action = stateStore.queryActionForModel(event);

        if (!Action.TO_DELETE.equals(action)) {
            return event;
        }

        return null;
    }

    @Override
    public Event create(TrackedEntityInstance trackedEntityInstance, Enrollment enrollment, OrganisationUnit organisationUnit, Program program, ProgramStage programStage, String status) {
        Event event = new Event();
        event.setEventUid(CodeGenerator.generateCode());
        event.setEnrollment(enrollment);
        event.setStatus(status);
        event.setTrackedEntityInstance(trackedEntityInstance);
        event.setProgramId(program.getUId());
        event.setProgramStageId(programStage.getUId());
        event.setTrackedEntityDataValues(new ArrayList<TrackedEntityDataValue>());

        DateTime dueDate = enrollment.getDateOfEnrollment();
        dueDate.plusDays(programStage.getMinDaysFromStart());
        event.setDueDate(dueDate);
        add(event);
        return event;
    }

    @Override
    public Event create(OrganisationUnit organisationUnit, String status, Program program, ProgramStage programStage) {
        Event event = new Event();
        event.setEventUid(CodeGenerator.generateCode());
        event.setStatus(status);
        event.setProgramId(program.getUId());
        event.setProgramStageId(programStage.getUId());
        event.setTrackedEntityDataValues(new ArrayList<TrackedEntityDataValue>());

        add(event);
        return event;
    }

    @Override
    public List<Event> list(Program program, OrganisationUnit organisationUnit, DateTime startDate, DateTime endDate) {
        List<Event> events = eventStore.query(organisationUnit, program);
        for(int i = 0; i<events.size(); i++) {
            Event event = events.get(i);
            if(event.getDueDate().isBefore(startDate) || event.getDueDate().isAfter(endDate)) {
                events.remove(i);
            }
            i--;
        }
        return events;
    }

    @Override
    public boolean add(Event object) {
        eventStore.insert(object);
        stateStore.saveActionForModel(object, Action.TO_POST);

        return true;
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
        return stateStore.filterModelsByAction(Event.class, Action.TO_DELETE);
    }

    @Override
    public boolean remove(Event object) {
        isNull(object, "event argument must not be null");
        eventStore.delete(object);
        return true;
    }

    @Override
    public boolean save(Event object) {
        eventStore.save(object);

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

        eventStore.update(object);

        return true;
    }
}
