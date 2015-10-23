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
import org.hisp.dhis.java.sdk.models.trackedentity.TrackedEntityInstance;
import org.hisp.dhis.sdk.java.common.IStateStore;
import org.hisp.dhis.sdk.java.utils.CodeGenerator;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.BDDMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.verifyStatic;

@RunWith(PowerMockRunner.class)
@PrepareForTest(CodeGenerator.class)
public class EventServiceTest {

    private Event event;

    private Event previouslyAddedEvent;
    private static final long previouslyAddedEventId = 1;
    private static final String previouslyAddedEventUid = "Eg8cFa38";

    private IStateStore stateStore;
    private IEventStore eventStore;
    private IEventService eventService;


    private static final long eventIdThatDoesntExistInDatabase = 99;
    private static final String eventUidThatDoesntExistInDatabase = "abcdef00";

    private OrganisationUnit organisationUnit;
    private static final String organisationUnitUid = "aaaabbbb";

    private Program program;
    private static final String programUid = "ccccdddd";

    private ProgramStage programStage;
    private static final String programStageUid = "aabbccdd";
    private static final int programStageNumberOfDaysFromStart = 3;

    private Enrollment enrollment;
    private static final String enrollmentUid = "ffffffff";
    private static final DateTime enrollmentDate = new DateTime(2015, 1, 1, 10, 30, 30, 500);

    private TrackedEntityInstance trackedEntityInstance;
    private static final String trackedEntityInstanceUid = "aaaaaaaa";

    @Before
    public void setUp() {
        event = new Event();
        previouslyAddedEvent = new Event();
        stateStore = mock(IStateStore.class);
        eventStore = mock(IEventStore.class);
        when(eventStore.queryById(previouslyAddedEventId)).thenReturn(previouslyAddedEvent);
        when(eventStore.queryByUid(previouslyAddedEventUid)).thenReturn(previouslyAddedEvent);

        eventService = new EventService(eventStore, stateStore);

        organisationUnit = new OrganisationUnit();
        organisationUnit.setUId(organisationUnitUid);

        program = new Program();
        program.setUId(programUid);

        programStage = new ProgramStage();
        programStage.setUId(programStageUid);
        programStage.setMinDaysFromStart(programStageNumberOfDaysFromStart);

        enrollment = new Enrollment();
        enrollment.setEnrollmentUid(enrollmentUid);
        enrollment.setDateOfEnrollment(enrollmentDate);

        trackedEntityInstance = new TrackedEntityInstance();
        trackedEntityInstance.setTrackedEntityInstanceUid(trackedEntityInstanceUid);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddNullEvent() {
        eventService.add(null);
    }

    @Test
    public void testAddEvent() {
        when(eventStore.insert(event)).thenReturn(true);
        when(stateStore.saveActionForModel(event, Action.TO_POST)).thenReturn(true);
        assertTrue(eventService.add(event));
        verify(eventStore).insert(event);
        verify(stateStore).saveActionForModel(event, Action.TO_POST);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSaveNullEvent() {
        eventService.save(null);
    }

    @Test
    public void testSaveUnsavedEvent() {
        when(eventStore.save(event)).thenReturn(true);
        when(stateStore.queryActionForModel(event)).thenReturn(null);
        when(stateStore.saveActionForModel(event, Action.TO_POST)).thenReturn(true);
        assertTrue(eventService.save(event));
        verify(eventStore).save(event);
        verify(stateStore).saveActionForModel(event, Action.TO_POST);
    }

    @Test
    public void testSavePreviouslyAddedToPostEvent() {
        when(eventStore.save(previouslyAddedEvent)).thenReturn(true);
        when(stateStore.queryActionForModel(previouslyAddedEvent)).thenReturn(Action.TO_POST);
        when(stateStore.saveActionForModel(previouslyAddedEvent, Action.TO_POST)).thenReturn(true);
        assertTrue(eventService.save(previouslyAddedEvent));
        verify(eventStore).save(previouslyAddedEvent);
        verify(stateStore).saveActionForModel(previouslyAddedEvent, Action.TO_POST);
    }

    @Test
    public void testSavePreviouslyAddedToUpdateEvent() {
        when(eventStore.save(previouslyAddedEvent)).thenReturn(true);
        when(stateStore.queryActionForModel(previouslyAddedEvent)).thenReturn(Action.TO_UPDATE);
        when(stateStore.saveActionForModel(previouslyAddedEvent, Action.TO_UPDATE)).thenReturn(true);
        assertTrue(eventService.save(previouslyAddedEvent));
        verify(eventStore).save(previouslyAddedEvent);
        verify(stateStore).saveActionForModel(previouslyAddedEvent, Action.TO_UPDATE);
    }

    @Test
    public void testSavePreviouslyAddedSyncedEvent() {
        when(eventStore.save(previouslyAddedEvent)).thenReturn(true);
        when(stateStore.queryActionForModel(previouslyAddedEvent)).thenReturn(Action.SYNCED);
        when(stateStore.saveActionForModel(previouslyAddedEvent, Action.TO_UPDATE)).thenReturn(true);
        assertTrue(eventService.save(previouslyAddedEvent));
        verify(eventStore).save(previouslyAddedEvent);
        verify(stateStore).saveActionForModel(previouslyAddedEvent, Action.TO_UPDATE);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUpdateNullEvent() {
        eventService.update(null);
    }

    @Test
    public void testUpdatePreviouslySavedToPostEvent() {
        when(eventStore.update(previouslyAddedEvent)).thenReturn(true);
        when(stateStore.queryActionForModel(previouslyAddedEvent)).thenReturn(Action.TO_POST);
        assertTrue(eventService.update(previouslyAddedEvent));
        verify(eventStore).update(previouslyAddedEvent);
    }

    @Test
    public void testUpdatePreviouslySavedToUpdateEvent() {
        when(eventStore.update(previouslyAddedEvent)).thenReturn(true);
        when(stateStore.saveActionForModel(previouslyAddedEvent, Action.TO_UPDATE)).thenReturn(true);
        when(stateStore.queryActionForModel(previouslyAddedEvent)).thenReturn(Action.TO_UPDATE);
        assertTrue(eventService.update(previouslyAddedEvent));
        verify(eventStore).update(previouslyAddedEvent);
        verify(stateStore).saveActionForModel(previouslyAddedEvent, Action.TO_UPDATE);
    }

    @Test
    public void testUpdatePreviouslySavedSyncedEvent() {
        when(eventStore.update(previouslyAddedEvent)).thenReturn(true);
        when(stateStore.saveActionForModel(previouslyAddedEvent, Action.TO_UPDATE)).thenReturn(true);
        when(stateStore.queryActionForModel(previouslyAddedEvent)).thenReturn(Action.SYNCED);
        assertTrue(eventService.update(previouslyAddedEvent));
        verify(eventStore).update(previouslyAddedEvent);
        verify(stateStore).saveActionForModel(previouslyAddedEvent, Action.TO_UPDATE);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRemoveNullEvent() {
        eventService.remove(null);
    }

    @Test
    public void testRemoveNotPreviouslySavedEvent() {
        assertFalse(eventService.remove(event));
        verify(eventStore).delete(event);
    }

    @Test
    public void testRemovePreviouslySavedEvent() {
        when(eventStore.delete(previouslyAddedEvent)).thenReturn(true);
        when(stateStore.deleteActionForModel(previouslyAddedEvent)).thenReturn(true);
        assertTrue(eventService.remove(previouslyAddedEvent));
        verify(eventStore).delete(previouslyAddedEvent);
        verify(stateStore).deleteActionForModel(previouslyAddedEvent);
    }

    @Test
    public void testGetSyncedEventById() {
        when(stateStore.queryActionForModel(previouslyAddedEvent)).thenReturn(Action.SYNCED);
        assertTrue(previouslyAddedEvent.equals(eventService.get(previouslyAddedEventId)));
        verify(eventStore).queryById(previouslyAddedEventId);
    }

    @Test
    public void testGetToPostEventById() {
        when(stateStore.queryActionForModel(previouslyAddedEvent)).thenReturn(Action.TO_POST);
        assertTrue(previouslyAddedEvent.equals(eventService.get(previouslyAddedEventId)));
        verify(eventStore).queryById(previouslyAddedEventId);
    }


    @Test
    public void testGetToUpdateEventById() {
        when(stateStore.queryActionForModel(previouslyAddedEvent)).thenReturn(Action.TO_UPDATE);
        assertTrue(previouslyAddedEvent.equals(eventService.get(previouslyAddedEventId)));
        verify(eventStore).queryById(previouslyAddedEventId);
    }

    @Test
    public void testGetToDeleteEventById() {
        when(stateStore.queryActionForModel(previouslyAddedEvent)).thenReturn(Action.TO_DELETE);
        assertTrue(null == eventService.get(previouslyAddedEventId));
        verify(eventStore).queryById(previouslyAddedEventId);
    }

    @Test
    public void testGetEventByIdThatDoesntExistInDatabase() {
        assertTrue(null == eventService.get(eventIdThatDoesntExistInDatabase));
        verify(eventStore).queryById(eventIdThatDoesntExistInDatabase);
    }

    @Test
    public void testGetToPostEventByUid() {
        when(stateStore.queryActionForModel(previouslyAddedEvent)).thenReturn(Action.TO_POST);
        assertTrue(previouslyAddedEvent.equals(eventService.get(previouslyAddedEventUid)));
        verify(eventStore).queryByUid(previouslyAddedEventUid);
    }

    @Test
    public void testGetToUpdateEventByUid() {
        when(stateStore.queryActionForModel(previouslyAddedEvent)).thenReturn(Action.TO_UPDATE);
        assertTrue(previouslyAddedEvent.equals(eventService.get(previouslyAddedEventUid)));
        verify(eventStore).queryByUid(previouslyAddedEventUid);
    }

    @Test
    public void testGetSyncedEventByUid() {
        when(stateStore.queryActionForModel(previouslyAddedEvent)).thenReturn(Action.SYNCED);
        assertTrue(previouslyAddedEvent.equals(eventService.get(previouslyAddedEventUid)));
        verify(eventStore).queryByUid(previouslyAddedEventUid);
    }

    @Test
    public void testGetToDeleteEventByUid() {
        when(stateStore.queryActionForModel(previouslyAddedEvent)).thenReturn(Action.TO_DELETE);
        assertTrue(null == eventService.get(previouslyAddedEventUid));
        verify(eventStore).queryByUid(previouslyAddedEventUid);
    }

    @Test
    public void testGetEventByUidThatDoesntExistInDatabase() {
        assertTrue(null == eventService.get(eventUidThatDoesntExistInDatabase));
        verify(eventStore).queryByUid(eventUidThatDoesntExistInDatabase);
    }

    @Test
    public void testListEvents() {
        List<Event> listEventsMock = new ArrayList<>();
        listEventsMock.add(new Event());
        listEventsMock.add(new Event());
        listEventsMock.add(new Event());
        listEventsMock.add(new Event());
        when(stateStore.queryModelsWithActions(Event.class, Action.TO_POST, Action.SYNCED, Action.TO_UPDATE))
                .thenReturn(listEventsMock);
        assertTrue(listEventsMock.equals(eventService.list()));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateEventWithEnrollmentWithNullTrackedEntityInstanceReference() {
        eventService.create(null, enrollment, organisationUnit, program, programStage, Event.STATUS_ACTIVE);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateEventWithEnrollmentWithNulEnrollmentReference() {
        eventService.create(trackedEntityInstance, null, organisationUnit, program, programStage, Event.STATUS_ACTIVE);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateEventWithEnrollmentWithNullOrganisationUnitReference() {
        eventService.create(trackedEntityInstance, enrollment, null, program, programStage, Event.STATUS_ACTIVE);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateEventWithEnrollmentWithNullProgramReference() {
        eventService.create(trackedEntityInstance, enrollment, organisationUnit, null, programStage, Event.STATUS_ACTIVE);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateEventWithEnrollmentWithNullProgramStageReference() {
        eventService.create(trackedEntityInstance, enrollment, organisationUnit, program, null, Event.STATUS_ACTIVE);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateEventWithEnrollmentWithNullStatusReference() {
        eventService.create(trackedEntityInstance, enrollment, organisationUnit, program, programStage, null);
    }

    @Test
    public void testCreateActiveEventWithEnrollment() {
        String eventMockUid = "00112233";

        mockStatic(CodeGenerator.class);
        given(CodeGenerator.generateCode()).willReturn(eventMockUid);

        Event event = eventService.create(trackedEntityInstance, enrollment, organisationUnit, program, programStage, Event.STATUS_ACTIVE);
        verifyStatic();
        CodeGenerator.generateCode();

        assertTrue(event.getTrackedEntityInstanceUid().equals(trackedEntityInstance.getTrackedEntityInstanceUid()));
        assertTrue(event.getEnrollmentUid().equals(enrollment.getEnrollmentUid()));
        assertTrue(event.getOrganisationUnitId().equals(organisationUnit.getUId()));
        assertTrue(event.getProgramId().equals(program.getUId()));
        assertTrue(event.getProgramStageId().equals(programStage.getUId()));
        assertTrue(event.getStatus().equals(Event.STATUS_ACTIVE));

        DateTime eventDueDate = event.getDueDate();
        DateTime enrollmentDatePlusProgramStageScheduledDaysFromStart = enrollmentDate.plusDays(programStage.getMinDaysFromStart());
        assertTrue(eventDueDate.isEqual(enrollmentDatePlusProgramStageScheduledDaysFromStart));
    }

    @Test
    public void testCreateCompletedEventWithEnrollment() {
        String eventMockUid = "00112233";

        mockStatic(CodeGenerator.class);
        given(CodeGenerator.generateCode()).willReturn(eventMockUid);

        Event event = eventService.create(trackedEntityInstance, enrollment, organisationUnit, program, programStage, Event.STATUS_COMPLETED);
        verifyStatic();
        CodeGenerator.generateCode();

        assertTrue(event.getTrackedEntityInstanceUid().equals(trackedEntityInstance.getTrackedEntityInstanceUid()));
        assertTrue(event.getEnrollmentUid().equals(enrollment.getEnrollmentUid()));
        assertTrue(event.getOrganisationUnitId().equals(organisationUnit.getUId()));
        assertTrue(event.getProgramId().equals(program.getUId()));
        assertTrue(event.getProgramStageId().equals(programStage.getUId()));
        assertTrue(event.getStatus().equals(Event.STATUS_COMPLETED));

        DateTime eventDueDate = event.getDueDate();
        DateTime enrollmentDatePlusProgramStageScheduledDaysFromStart = enrollmentDate.plusDays(programStage.getMinDaysFromStart());
        assertTrue(eventDueDate.isEqual(enrollmentDatePlusProgramStageScheduledDaysFromStart));
    }

    @Test
    public void testCreateScheduledEventWithEnrollment() {
        String eventMockUid = "00112233";

        mockStatic(CodeGenerator.class);
        given(CodeGenerator.generateCode()).willReturn(eventMockUid);

        Event event = eventService.create(trackedEntityInstance, enrollment, organisationUnit, program, programStage, Event.STATUS_FUTURE_VISIT);
        verifyStatic();
        CodeGenerator.generateCode();

        assertTrue(event.getTrackedEntityInstanceUid().equals(trackedEntityInstance.getTrackedEntityInstanceUid()));
        assertTrue(event.getEnrollmentUid().equals(enrollment.getEnrollmentUid()));
        assertTrue(event.getOrganisationUnitId().equals(organisationUnit.getUId()));
        assertTrue(event.getProgramId().equals(program.getUId()));
        assertTrue(event.getProgramStageId().equals(programStage.getUId()));
        assertTrue(event.getStatus().equals(Event.STATUS_FUTURE_VISIT));

        DateTime eventDueDate = event.getDueDate();
        DateTime enrollmentDatePlusProgramStageScheduledDaysFromStart = enrollmentDate.plusDays(programStage.getMinDaysFromStart());
        assertTrue(eventDueDate.isEqual(enrollmentDatePlusProgramStageScheduledDaysFromStart));
    }

    @Test
    public void testCreateSkippedEventWithEnrollment() {
        String eventMockUid = "00112233";

        mockStatic(CodeGenerator.class);
        given(CodeGenerator.generateCode()).willReturn(eventMockUid);

        Event event = eventService.create(trackedEntityInstance, enrollment, organisationUnit, program, programStage, Event.STATUS_SKIPPED);
        verifyStatic();
        CodeGenerator.generateCode();

        assertTrue(event.getTrackedEntityInstanceUid().equals(trackedEntityInstance.getTrackedEntityInstanceUid()));
        assertTrue(event.getEnrollmentUid().equals(enrollment.getEnrollmentUid()));
        assertTrue(event.getOrganisationUnitId().equals(organisationUnit.getUId()));
        assertTrue(event.getProgramId().equals(program.getUId()));
        assertTrue(event.getProgramStageId().equals(programStage.getUId()));
        assertTrue(event.getStatus().equals(Event.STATUS_SKIPPED));

        DateTime eventDueDate = event.getDueDate();
        DateTime enrollmentDatePlusProgramStageScheduledDaysFromStart = enrollmentDate.plusDays(programStage.getMinDaysFromStart());
        assertTrue(eventDueDate.isEqual(enrollmentDatePlusProgramStageScheduledDaysFromStart));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateActiveEventWithoutEnrollmentWithNullOrganisationUnitReference() {
        eventService.create(null, Event.STATUS_ACTIVE, program, programStage);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateActiveEventWithoutEnrollmentWithNullStatusReference() {
        eventService.create(organisationUnit, null, program, programStage);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateActiveEventWithoutEnrollmentWithNullProgramReference() {
        eventService.create(organisationUnit, Event.STATUS_ACTIVE, null, programStage);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateActiveEventWithoutEnrollmentWithNullProgramStageReference() {
        eventService.create(organisationUnit, Event.STATUS_ACTIVE, program, null);
    }

    @Test
    public void testCreateActiveEventWithoutEnrollment() {
        String eventMockUid = "00112233";

        mockStatic(CodeGenerator.class);
        given(CodeGenerator.generateCode()).willReturn(eventMockUid);

        Event event = eventService.create(organisationUnit, Event.STATUS_ACTIVE, program, programStage);
        verifyStatic();
        CodeGenerator.generateCode();

        assertTrue(event.getOrganisationUnitId().equals(organisationUnit.getUId()));
        assertTrue(event.getProgramId().equals(program.getUId()));
        assertTrue(event.getProgramStageId().equals(programStage.getUId()));
        assertTrue(event.getStatus().equals(Event.STATUS_ACTIVE));
    }

    @Test
    public void testCreateCompletedEventWithoutEnrollment() {
        String eventMockUid = "00112233";

        mockStatic(CodeGenerator.class);
        given(CodeGenerator.generateCode()).willReturn(eventMockUid);

        Event event = eventService.create(organisationUnit, Event.STATUS_COMPLETED, program, programStage);
        verifyStatic();
        CodeGenerator.generateCode();

        assertTrue(event.getOrganisationUnitId().equals(organisationUnit.getUId()));
        assertTrue(event.getProgramId().equals(program.getUId()));
        assertTrue(event.getProgramStageId().equals(programStage.getUId()));
        assertTrue(event.getStatus().equals(Event.STATUS_COMPLETED));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateScheduledEventWithoutEnrollment() {
        eventService.create(organisationUnit, Event.STATUS_FUTURE_VISIT, program, programStage);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateSkippedEventWithoutEnrollment() {
        eventService.create(organisationUnit, Event.STATUS_SKIPPED, program, programStage);
    }

    @Test
    public void testListEventsWithDateAndOrganisationUnitAndProgramFilter() {
        DateTime filterStartingDate = new DateTime(2015, 1, 1, 0, 0);
        DateTime filterEndingDate = new DateTime(2015, 1, 5, 0, 0);

        Event eventBeforeDateRange = new Event();
        DateTime dateBeforeDateRange = filterStartingDate.minusDays(1);
        eventBeforeDateRange.setDueDate(dateBeforeDateRange);

        Event eventAfterDateRange = new Event();
        DateTime dateAfterDateRange = filterEndingDate.plusDays(1);
        eventAfterDateRange.setDueDate(dateAfterDateRange);

        Event eventInsideDateRange = new Event();
        DateTime dateInsideDateRange = filterStartingDate.plusDays(1);
        eventInsideDateRange.setDueDate(dateInsideDateRange);

        Event eventWithDateOfStartRange = new Event();
        eventWithDateOfStartRange.setDueDate(filterStartingDate);

        Event eventWithDateOfEndRange = new Event();
        eventWithDateOfEndRange.setDueDate(filterEndingDate);

        List<Event> events = new ArrayList<>();
        events.add(eventBeforeDateRange);
        events.add(eventAfterDateRange);
        events.add(eventInsideDateRange);
        events.add(eventWithDateOfStartRange);
        events.add(eventWithDateOfEndRange);

        when(eventStore.query(organisationUnit, program)).thenReturn(events);

        List<Event> filteredEvents = eventService.list(program, organisationUnit, filterStartingDate, filterEndingDate);

        assertFalse(filteredEvents.contains(eventBeforeDateRange));
        assertFalse(filteredEvents.contains(eventAfterDateRange));
        assertTrue(filteredEvents.contains(eventInsideDateRange));
        assertTrue(filteredEvents.contains(eventWithDateOfStartRange));
        assertTrue(filteredEvents.contains(eventWithDateOfEndRange));

        verify(eventStore).query(organisationUnit, program);
    }
}
