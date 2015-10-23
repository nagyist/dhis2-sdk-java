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
        return stateStore.queryModelsWithActions(Event.class, Action.TO_POST, Action.SYNCED, Action.TO_UPDATE);
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
