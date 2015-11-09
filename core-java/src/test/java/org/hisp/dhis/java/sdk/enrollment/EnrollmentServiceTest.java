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
import org.hisp.dhis.java.sdk.event.EventService;
import org.hisp.dhis.java.sdk.event.IEventService;
import org.hisp.dhis.java.sdk.event.IEventStore;
import org.hisp.dhis.java.sdk.models.common.state.Action;
import org.hisp.dhis.java.sdk.models.enrollment.Enrollment;
import org.hisp.dhis.java.sdk.models.organisationunit.OrganisationUnit;
import org.hisp.dhis.java.sdk.models.program.Program;
import org.hisp.dhis.java.sdk.models.program.ProgramStage;
import org.hisp.dhis.java.sdk.models.trackedentity.TrackedEntityInstance;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class EnrollmentServiceTest {
    private IEnrollmentStore enrollmentStore;
    private IEventStore eventStore;
    private IStateStore stateStore;
    private IEventService eventService;
    private IEnrollmentService enrollmentService;
    private Enrollment enrollmentMock;
    private Enrollment enrollmentToUpdate;
    private Enrollment enrollmentToSync;
    private Enrollment enrollmentToDelete;
    private Enrollment enrollmentToPost;
    private OrganisationUnit organisationUnit;
    private Program program;
    private ProgramStage programStage;
    private TrackedEntityInstance trackedEntityInstanceMock;

    private final String ENROLLMENT_MOCK_UID = "Tx3A5h9Lo";
    private final String PROGRAM_UID = "YxL5xa25L";
    private final String ORGANISATION_UNIT_ID = "Px3AxP25U";
    private final long ENROLLMENT_MOCK_ID = 1L;
    private final long INVALID_ENROLLMENT_ID = 0L;

    @Before
    public void setUp() {
        enrollmentStore = mock(IEnrollmentStore.class);
        stateStore = mock(IStateStore.class);
        eventStore = mock(IEventStore.class);
        trackedEntityInstanceMock = mock(TrackedEntityInstance.class);


        program = new Program();
        program.setUId(PROGRAM_UID);
        organisationUnit = new OrganisationUnit();
        organisationUnit.setUId(ORGANISATION_UNIT_ID);

        programStage = new ProgramStage();
        program.setProgramStages(Arrays.asList(programStage));

        enrollmentMock = new Enrollment();
        enrollmentMock.setUId(ENROLLMENT_MOCK_UID);
        enrollmentMock.setId(ENROLLMENT_MOCK_ID);

        enrollmentToDelete = new Enrollment();
        enrollmentToPost = new Enrollment();
        enrollmentToSync = new Enrollment();
        enrollmentToUpdate = new Enrollment();

        when(enrollmentStore.query(ENROLLMENT_MOCK_UID)).thenReturn(enrollmentMock);
        when(enrollmentStore.query(ENROLLMENT_MOCK_ID)).thenReturn(enrollmentMock);
        when(stateStore.queryActionForModel(enrollmentToSync)).thenReturn(Action.SYNCED);
        when(stateStore.queryActionForModel(enrollmentToPost)).thenReturn(Action.TO_POST);
        when(stateStore.queryActionForModel(enrollmentToUpdate)).thenReturn(Action.TO_UPDATE);
        when(stateStore.queryActionForModel(enrollmentToDelete)).thenReturn(Action.TO_DELETE);
        when(stateStore.saveActionForModel(any(Enrollment.class), any(Action.class))).thenReturn(true);

        eventService = new EventService(eventStore, stateStore);
        enrollmentService = new EnrollmentService(enrollmentStore, stateStore, eventService);
    }

    @Test
    public void testGetEnrollmentByUid() {
        Enrollment enrollment = enrollmentService.get(ENROLLMENT_MOCK_UID);
        assertEquals(enrollment, enrollmentMock);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetEnrollmentByNullUid() {
        assertNull(enrollmentService.get(null));
    }

    @Test
    public void getEnrollmentToUpdate() {
        when(stateStore.queryActionForModel(enrollmentMock)).thenReturn(Action.TO_UPDATE);
        assertEquals(enrollmentService.get(ENROLLMENT_MOCK_UID), enrollmentMock);
    }

    @Test
    public void getEnrollmentToPost() {
        when(stateStore.queryActionForModel(enrollmentMock)).thenReturn(Action.TO_POST);
        assertEquals(enrollmentService.get(ENROLLMENT_MOCK_UID), enrollmentMock);
    }

    @Test
    public void getEnrollmentToDelete() {
        when(stateStore.queryActionForModel(enrollmentMock)).thenReturn(Action.TO_DELETE);
        assertNull(enrollmentService.get(ENROLLMENT_MOCK_UID));
    }

    @Test
    public void testCreateEnrollment() {
        boolean followUp = true;
        DateTime dateOfEnrollment = new DateTime(2015, 5, 3, 0, 0);
        DateTime dateOfIncident = new DateTime(2015, 4, 1, 0, 0);

        Enrollment enrollment = enrollmentService.create(organisationUnit, trackedEntityInstanceMock, program, followUp, dateOfEnrollment, dateOfIncident);
        assertNotNull(enrollment);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateEnrollmentNullOrganisationUnit() {
        boolean followUp = true;
        DateTime dateOfEnrollment = new DateTime(2015, 5, 3, 0, 0);
        DateTime dateOfIncident = new DateTime(2015, 4, 1, 0, 0);

        Enrollment enrollment = enrollmentService.create(null, trackedEntityInstanceMock, program, followUp, dateOfEnrollment, dateOfIncident);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateEnrollmentNullTrackedEntityInstance() {
        boolean followUp = true;
        DateTime dateOfEnrollment = new DateTime(2015, 5, 3, 0, 0);
        DateTime dateOfIncident = new DateTime(2015, 4, 1, 0, 0);

        Enrollment enrollment = enrollmentService.create(organisationUnit, null, program, followUp, dateOfEnrollment, dateOfIncident);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateEnrollmentNullProgram() {
        boolean followUp = true;
        DateTime dateOfEnrollment = new DateTime(2015, 5, 3, 0, 0);
        DateTime dateOfIncident = new DateTime(2015, 4, 1, 0, 0);

        Enrollment enrollment = enrollmentService.create(organisationUnit, trackedEntityInstanceMock, null, followUp, dateOfEnrollment, dateOfIncident);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateEnrollmentNullDateOfEnrollment() {
        boolean followUp = true;
        DateTime dateOfEnrollment = null;
        DateTime dateOfIncident = new DateTime(2015, 4, 1, 0, 0);

        Enrollment enrollment = enrollmentService.create(organisationUnit, trackedEntityInstanceMock, program, followUp, dateOfEnrollment, dateOfIncident);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateEnrollmentNullDateOfIncident() {
        program.setDisplayIncidentDate(true);
        boolean followUp = true;
        DateTime dateOfEnrollment = new DateTime(2015, 5, 3, 0, 0);
        DateTime dateOfIncident = null;

        Enrollment enrollment = enrollmentService.create(organisationUnit, trackedEntityInstanceMock, program, followUp, dateOfEnrollment, dateOfIncident);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateEnrollmentFutureEnrollmentDateNotAllowed() {
        program.setSelectEnrollmentDatesInFuture(false);
        boolean followUp = true;
        DateTime dateOfEnrollment = new DateTime(2100, 5, 3, 0, 0);
        DateTime dateOfIncident = new DateTime(2015, 4, 1, 0, 0);

        Enrollment enrollment = enrollmentService.create(organisationUnit, trackedEntityInstanceMock, program, followUp, dateOfEnrollment, dateOfIncident);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateEnrollmentFutureIncidentDateNotAllowed() {
        program.setSelectIncidentDatesInFuture(false);
        boolean followUp = true;
        DateTime dateOfEnrollment = new DateTime(2015, 5, 3, 0, 0);
        DateTime dateOfIncident = new DateTime(2100, 4, 1, 0, 0);

        Enrollment enrollment = enrollmentService.create(organisationUnit, trackedEntityInstanceMock, program, followUp, dateOfEnrollment, dateOfIncident);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateEnrollmentOnlyEnrollOnceTrackedEntityInstanceNotAllowed() {
        program.setOnlyEnrollOnce(true);
        boolean followUp = true;
        DateTime dateOfEnrollment = new DateTime(2015, 5, 3, 0, 0);
        DateTime dateOfIncident = new DateTime(2015, 4, 1, 0, 0);
        enrollmentMock.setProgram(program.getUId());
        enrollmentMock.setTrackedEntityInstance(trackedEntityInstanceMock);

        when(enrollmentStore.query(program, trackedEntityInstanceMock)).thenReturn(Arrays.asList(enrollmentMock));
        Enrollment enrollment = enrollmentService.create(organisationUnit, trackedEntityInstanceMock, program, followUp, dateOfEnrollment, dateOfIncident);
    }

    @Test
    public void testCreateEnrollmentWithScheduledEvents() {
        programStage.setAutoGenerateEvent(true);
        programStage.setMinDaysFromStart(10);
        boolean followUp = true;
        DateTime dateOfEnrollment = new DateTime(2015, 5, 3, 0, 0);
        DateTime dateOfIncident = new DateTime(2015, 4, 1, 0, 0);

        Enrollment enrollment = enrollmentService.create(organisationUnit, trackedEntityInstanceMock, program, followUp, dateOfEnrollment, dateOfIncident);
        assertTrue(enrollment.getEvents().size() > 0);
    }

    @Test
    public void testGetActiveEnrollment() {
        boolean followUp = true;
        DateTime dateOfEnrollment = new DateTime(2015, 5, 3, 0, 0);
        DateTime dateOfIncident = new DateTime(2015, 4, 1, 0, 0);

        Enrollment enrollment = enrollmentService.create(organisationUnit, trackedEntityInstanceMock, program, followUp, dateOfEnrollment, dateOfIncident);
        when(enrollmentStore.queryActiveEnrollment(trackedEntityInstanceMock, organisationUnit, program)).thenReturn(enrollment);
        Enrollment enrollment1 = enrollmentService.getActiveEnrollment(trackedEntityInstanceMock, organisationUnit, program);

        assertEquals(enrollment, enrollment1);
        assertTrue(Enrollment.ACTIVE.equals(enrollment.getStatus()));
    }

    @Test
    public void testListEnrollment() {
        List<Enrollment> listEnrollments = new ArrayList<>();
        listEnrollments.add(new Enrollment());
        listEnrollments.add(new Enrollment());
        listEnrollments.add(new Enrollment());
        listEnrollments.add(new Enrollment());

        when(stateStore.queryModelsWithActions(Enrollment.class, Action.SYNCED, Action.TO_UPDATE, Action.TO_POST))
                .thenReturn(listEnrollments);
        assertTrue(listEnrollments.equals(enrollmentService.list()));
    }

    @Test
    public void testListTrackedEntityInstance() {
        boolean followUp = true;
        DateTime dateOfEnrollment = new DateTime(2015, 5, 3, 0, 0);
        DateTime dateOfIncident = new DateTime(2015, 4, 1, 0, 0);
        Enrollment enrollment1 = enrollmentService.create(organisationUnit, trackedEntityInstanceMock, program, followUp, dateOfEnrollment, dateOfIncident);
        Enrollment enrollment2 = enrollmentService.create(organisationUnit, trackedEntityInstanceMock, program, !followUp, dateOfEnrollment, dateOfIncident);
        Enrollment enrollment3 = enrollmentService.create(organisationUnit, trackedEntityInstanceMock, program, followUp, dateOfEnrollment, dateOfIncident);
        when(enrollmentStore.query(trackedEntityInstanceMock)).thenReturn(Arrays.asList(enrollment1, enrollment2, enrollment3));
        List<Enrollment> enrollmentList = enrollmentService.list(trackedEntityInstanceMock);

        for (int i = 0; i < enrollmentList.size(); i++) {
            assertTrue(enrollmentService.list(trackedEntityInstanceMock).get(i).getTrackedEntityInstance().equals(trackedEntityInstanceMock));
        }
    }

    @Test
    public void testListByProgramAndOrganisationUnit() {
        boolean followUp = true;
        DateTime dateOfEnrollment = new DateTime(2015, 5, 3, 0, 0);
        DateTime dateOfIncident = new DateTime(2015, 4, 1, 0, 0);
        Enrollment enrollment1 = enrollmentService.create(organisationUnit, trackedEntityInstanceMock, program, followUp, dateOfEnrollment, dateOfIncident);
        Enrollment enrollment2 = enrollmentService.create(organisationUnit, trackedEntityInstanceMock, program, !followUp, dateOfEnrollment, dateOfIncident);
        Enrollment enrollment3 = enrollmentService.create(organisationUnit, trackedEntityInstanceMock, program, followUp, dateOfEnrollment, dateOfIncident);
        when(enrollmentStore.query(program, organisationUnit)).thenReturn(Arrays.asList(enrollment1, enrollment2, enrollment3));
        List<Enrollment> enrollmentList = enrollmentService.list(program, organisationUnit);

        for (int i = 0; i < enrollmentList.size(); i++) {
            assertTrue(enrollmentService.list(program, organisationUnit).get(i).getProgram().equals(program.getUId()));
            assertTrue(enrollmentService.list(program, organisationUnit).get(i).getOrgUnit().equals(organisationUnit.getUId()));
        }
    }

    @Test
    public void testGetEnrollmentByLongId() {
        Enrollment enrollment = enrollmentStore.query(ENROLLMENT_MOCK_ID);
        assertEquals(enrollment, enrollmentMock);
    }

    @Test
    public void testGetEnrollmentByStringUID() {
        Enrollment enrollment = enrollmentStore.query(ENROLLMENT_MOCK_UID);
        assertEquals(enrollment, enrollmentMock);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetNullEnrollment() {
        enrollmentService.get(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSaveNullEnrollment() {
        enrollmentService.save(null);
    }

    @Test
    public void testSaveUnsavedEnrollment() {
        when(enrollmentStore.save(enrollmentMock)).thenReturn(true);
        assertTrue(enrollmentService.save(enrollmentMock));
        verify(enrollmentStore).save(enrollmentMock);
        verify(stateStore).saveActionForModel(enrollmentMock, Action.TO_POST);
    }

    @Test
    public void testSavePreviouslyAddedToPostEnrollment() {
        when(enrollmentStore.save(enrollmentMock)).thenReturn(true);
        assertTrue(enrollmentService.save(enrollmentMock));
        verify(enrollmentStore).save(enrollmentMock);
        verify(stateStore).saveActionForModel(enrollmentMock, Action.TO_POST);
    }

    @Test
    public void testSavePreviouslyAddedToUpdateEnrollment() {
        when(enrollmentStore.save(enrollmentToUpdate)).thenReturn(true);
        assertTrue(enrollmentService.save(enrollmentToUpdate));
        verify(enrollmentStore).save(enrollmentToUpdate);
        verify(stateStore).saveActionForModel(enrollmentToUpdate, Action.TO_UPDATE);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRemoveNullEnrollment() {
        enrollmentService.remove(null);
    }

    @Test
    public void testRemoveNotPreviouslySavedEnrollment() {
        assertFalse(enrollmentService.remove(enrollmentMock));
        verify(enrollmentStore).delete(enrollmentMock);
    }

    @Test
    public void testRemovePreviouslySavedEnrollment() {
        when(enrollmentStore.delete(enrollmentMock)).thenReturn(true);
        when(stateStore.deleteActionForModel(enrollmentMock)).thenReturn(true);
        assertTrue(enrollmentService.remove(enrollmentMock));
        verify(enrollmentStore).delete(enrollmentMock);
        verify(stateStore).deleteActionForModel(enrollmentMock);
    }


    @Test
    public void testGetSyncedEnrollmentById() {
        when(stateStore.queryActionForModel(enrollmentMock)).thenReturn(Action.SYNCED);
        assertTrue(enrollmentMock.equals(enrollmentService.get(ENROLLMENT_MOCK_UID)));
        verify(enrollmentStore).query(ENROLLMENT_MOCK_UID);
    }

    @Test
    public void testGetDeletedEnrollmentById() {
        when(stateStore.queryActionForModel(enrollmentToDelete)).thenReturn(Action.TO_DELETE);
        enrollmentToDelete = enrollmentService.get(ENROLLMENT_MOCK_ID);
        assertNull(enrollmentToDelete);
        verify(enrollmentStore).queryById(ENROLLMENT_MOCK_ID);
    }

    @Test
    public void testGetToPostEnrollmentById() {
        when(stateStore.queryActionForModel(enrollmentMock)).thenReturn(Action.TO_POST);
        assertTrue(enrollmentMock.equals(enrollmentService.get(ENROLLMENT_MOCK_UID)));
        verify(enrollmentStore).query(ENROLLMENT_MOCK_UID);
    }

    @Test
    public void testSaveEnrollmentStoreReturnsFalse() {
        when(enrollmentStore.save(enrollmentMock)).thenReturn(false);
        assertFalse(enrollmentService.save(enrollmentMock));
    }

    @Test
    public void testGetToUpdateEnrollmentById() {
        when(stateStore.queryActionForModel(enrollmentMock)).thenReturn(Action.TO_UPDATE);
        assertTrue(enrollmentMock.equals(enrollmentService.get(ENROLLMENT_MOCK_UID)));
        verify(enrollmentStore).query(ENROLLMENT_MOCK_UID);
    }

    @Test
    public void testGetToDeleteEnrollmentById() {
        when(stateStore.queryActionForModel(enrollmentMock)).thenReturn(Action.TO_DELETE);
        assertTrue(null == enrollmentService.get(ENROLLMENT_MOCK_UID));
        verify(enrollmentStore).query(ENROLLMENT_MOCK_UID);
    }

    @Test
    public void testGetEnrollmentByIdThatDoesntExistInDatabase() {
        assertTrue(null == enrollmentService.get(INVALID_ENROLLMENT_ID));
        verify(enrollmentStore).queryById(INVALID_ENROLLMENT_ID);
    }

    @Test
    public void testGetToPostEnrollmentByUid() {
        when(stateStore.queryActionForModel(enrollmentMock)).thenReturn(Action.TO_POST);
        assertTrue(enrollmentMock.equals(enrollmentService.get(ENROLLMENT_MOCK_UID)));
        verify(enrollmentStore).query(ENROLLMENT_MOCK_UID);
    }

    @Test
    public void testGetToUpdateEnrollmentByUid() {
        when(stateStore.queryActionForModel(enrollmentMock)).thenReturn(Action.TO_UPDATE);
        assertTrue(enrollmentMock.equals(enrollmentService.get(ENROLLMENT_MOCK_UID)));
        verify(enrollmentStore).query(ENROLLMENT_MOCK_UID);
    }

    @Test
    public void testGetSyncedEnrollmentByUid() {
        when(stateStore.queryActionForModel(enrollmentMock)).thenReturn(Action.SYNCED);
        assertTrue(enrollmentMock.equals(enrollmentService.get(ENROLLMENT_MOCK_UID)));
        verify(enrollmentStore).query(ENROLLMENT_MOCK_UID);
    }

    @Test
    public void testGetToDeleteEnrollmentByUid() {
        when(stateStore.queryActionForModel(enrollmentMock)).thenReturn(Action.TO_DELETE);
        assertTrue(null == enrollmentService.get(ENROLLMENT_MOCK_UID));
        verify(enrollmentStore).query(ENROLLMENT_MOCK_UID);
    }

    @Test
    public void testGetEnrollmentByUidThatDoesntExistInDatabase() {
        assertTrue(null == enrollmentService.get(INVALID_ENROLLMENT_ID));
        verify(enrollmentStore).queryById(INVALID_ENROLLMENT_ID);
    }

    @Test
    public void testGetEnrollmentByIdToDelete() {
        when(stateStore.queryActionForModel(enrollmentToDelete)).thenReturn(Action.TO_DELETE);
        when(enrollmentStore.queryById(ENROLLMENT_MOCK_ID)).thenReturn(enrollmentToDelete);
        assertTrue(null == enrollmentService.get(ENROLLMENT_MOCK_ID));
        verify(enrollmentStore).queryById(ENROLLMENT_MOCK_ID);
    }

}
