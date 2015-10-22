package org.hisp.dhis.sdk.java.enrollment;

import org.hisp.dhis.java.sdk.models.common.state.Action;
import org.hisp.dhis.java.sdk.models.enrollment.Enrollment;
import org.hisp.dhis.java.sdk.models.event.Event;
import org.hisp.dhis.java.sdk.models.organisationunit.OrganisationUnit;
import org.hisp.dhis.java.sdk.models.program.Program;
import org.hisp.dhis.java.sdk.models.program.ProgramStage;
import org.hisp.dhis.java.sdk.models.trackedentity.TrackedEntityInstance;
import org.hisp.dhis.sdk.java.common.IStateStore;
import org.hisp.dhis.sdk.java.event.IEventService;
import org.hisp.dhis.sdk.java.utils.CodeGenerator;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

import static org.hisp.dhis.sdk.java.utils.Preconditions.isNull;

public class EnrollmentService implements IEnrollmentService {

    private final IEnrollmentStore enrollmentStore;
    private final IStateStore stateStore;
    private final IEventService eventService;

    public EnrollmentService(IEnrollmentStore enrollmentStore, IStateStore stateStore, IEventService eventService) {
        this.enrollmentStore = enrollmentStore;
        this.stateStore = stateStore;
        this.eventService = eventService;
    }

    @Override
    public Enrollment get(String uid) {
        Enrollment enrollment = enrollmentStore.query(uid);
        Action action = stateStore.queryActionForModel(enrollment);

        if (!Action.TO_DELETE.equals(action)) {
            return enrollment;
        }

        return null;
    }

    @Override
    public Enrollment create(OrganisationUnit organisationUnit,
                             TrackedEntityInstance trackedEntityInstance,
                             Program program, boolean followUp, DateTime dateOfEnrollment,
                             DateTime dateOfIncident) {
        Enrollment enrollment = new Enrollment();
        enrollment.setEnrollmentUid(CodeGenerator.generateCode());
        enrollment.setTrackedEntityInstance(trackedEntityInstance);
        enrollment.setOrgUnit(organisationUnit.getUId());
        enrollment.setProgram(program.getUId());
        enrollment.setStatus(Enrollment.ACTIVE);
        enrollment.setFollowup(followUp);
        enrollment.setDateOfEnrollment(dateOfEnrollment);
        enrollment.setDateOfIncident(dateOfIncident);
        add(enrollment);

        List<Event> events = new ArrayList<>();
        for(ProgramStage programStage : program.getProgramStages()) {
            if(programStage.isAutoGenerateEvent()) {
                Event event = eventService.create(trackedEntityInstance, enrollment, organisationUnit, program, programStage, Event.STATUS_FUTURE_VISIT);
                events.add(event);
            }
        }
        enrollment.setEvents(events);
        return enrollment;
    }

    @Override
    public Enrollment getActiveEnrollment(TrackedEntityInstance trackedEntityInstance, OrganisationUnit organisationUnit, Program program) {
        return enrollmentStore.queryActiveEnrollment(trackedEntityInstance, organisationUnit, program);
    }

    @Override
    public List<Enrollment> list(TrackedEntityInstance trackedEntityInstance) {
        return enrollmentStore.query(trackedEntityInstance);
    }

    @Override
    public List<Enrollment> list(Program program, OrganisationUnit organisationUnit) {
        return enrollmentStore.query(program, organisationUnit);
    }

    @Override
    public boolean add(Enrollment object) {
        enrollmentStore.insert(object);
        stateStore.saveActionForModel(object, Action.TO_POST);

        return true;
    }

    @Override
    public Enrollment get(long id) {
        Enrollment enrollment = enrollmentStore.queryById(id);
        Action action = stateStore.queryActionForModel(enrollment);

        if (!Action.TO_DELETE.equals(action)) {
            return enrollment;
        }

        return null;
    }

    @Override
    public List<Enrollment> list() {
        return stateStore.queryModelsWithActions(Enrollment.class, Action.SYNCED, Action.TO_UPDATE, Action.TO_POST);
    }

    @Override
    public boolean remove(Enrollment object) {
        isNull(object, "enrollment argument must not be null");
        enrollmentStore.delete(object);
        return true;
    }

    @Override
    public boolean save(Enrollment object) {
        enrollmentStore.save(object);

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
    public boolean update(Enrollment object) {
        isNull(object, "enrollment argument must not be null");

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

        enrollmentStore.update(object);

        return true;
    }
}
