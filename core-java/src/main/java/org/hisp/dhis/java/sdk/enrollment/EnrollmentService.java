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

package org.hisp.dhis.java.sdk.enrollment;

import org.hisp.dhis.java.sdk.common.IStateStore;
import org.hisp.dhis.java.sdk.models.common.state.Action;
import org.hisp.dhis.java.sdk.models.enrollment.Enrollment;
import org.hisp.dhis.java.sdk.models.event.Event;
import org.hisp.dhis.java.sdk.models.organisationunit.OrganisationUnit;
import org.hisp.dhis.java.sdk.models.program.Program;
import org.hisp.dhis.java.sdk.models.program.ProgramStage;
import org.hisp.dhis.java.sdk.models.trackedentity.TrackedEntityInstance;
import org.hisp.dhis.java.sdk.utils.Preconditions;
import org.hisp.dhis.java.sdk.event.IEventService;
import org.hisp.dhis.java.sdk.utils.CodeGenerator;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

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
        Preconditions.isNull(uid, "Uid must not be null");
        Enrollment enrollment = enrollmentStore.queryByUid(uid);
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
        Preconditions.isNull(organisationUnit, "Organisation unit must not be null");
        Preconditions.isNull(trackedEntityInstance, "Tracked entity instance must not be null");
        Preconditions.isNull(program, "Program must not be null");
        Preconditions.isNull(dateOfEnrollment, "Date of enrollment must not be null");

        if (program.isDisplayIncidentDate()) {
            Preconditions.isNull(dateOfIncident, "Date of incident must not be null");
        }

        if (!program.isSelectEnrollmentDatesInFuture()) {
            if (dateOfEnrollment.isAfterNow()) {
                throw new IllegalArgumentException("Program doesn't allow to set future enrollment dates");
            }
        }
        if (!program.isSelectIncidentDatesInFuture()) {
            if (dateOfIncident.isAfterNow()) {
                throw new IllegalArgumentException("Program doesn't allow to set future incident dates");
            }
        }

        if (program.isOnlyEnrollOnce()) {
            List<Enrollment> enrollments = enrollmentStore.query(program, trackedEntityInstance);
            if (enrollments.size() > 0) {
                throw new IllegalArgumentException("Tracked entity instance can only be enrolled once");
            }
        }

        Enrollment enrollment = new Enrollment();
        enrollment.setUId(CodeGenerator.generateCode());
        enrollment.setTrackedEntityInstance(trackedEntityInstance);
        enrollment.setOrgUnit(organisationUnit.getUId());
        enrollment.setProgram(program.getUId());
        enrollment.setStatus(Enrollment.ACTIVE);
        enrollment.setFollowup(followUp);
        enrollment.setDateOfEnrollment(dateOfEnrollment);
        enrollment.setDateOfIncident(dateOfIncident);
        save(enrollment);

        List<Event> events = new ArrayList<>();
        for (ProgramStage programStage : program.getProgramStages()) {
            if (programStage.isAutoGenerateEvent()) {
                Event event = eventService.create(trackedEntityInstance, enrollment,
                        organisationUnit, program, programStage, Event.STATUS_FUTURE_VISIT);
                events.add(event);
            }
        }
        enrollment.setEvents(events);
        return enrollment;
    }

    @Override
    public Enrollment getActiveEnrollment(TrackedEntityInstance trackedEntityInstance,
                                          OrganisationUnit organisationUnit, Program program) {
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
        Preconditions.isNull(object, "Enrollment argument must not be null");

        if (!enrollmentStore.delete(object)) {
            return false;
        }
        return stateStore.deleteActionForModel(object);
    }

    @Override
    public boolean save(Enrollment object) {
        Preconditions.isNull(object, "Enrollment argument must not be null");

        if (!enrollmentStore.save(object)) {
            return false;
        }
        Action action = stateStore.queryActionForModel(object);
        if (action == null || Action.TO_POST.equals(action)) {
            return stateStore.saveActionForModel(object, Action.TO_POST);
        } else {
            return stateStore.saveActionForModel(object, Action.TO_UPDATE);
        }
    }
}
