package org.hisp.dhis.java.sdk.event;


import org.hisp.dhis.java.sdk.common.IFailedItemStore;
import org.hisp.dhis.java.sdk.common.IStateStore;
import org.hisp.dhis.java.sdk.common.persistence.ITransactionManager;
import org.hisp.dhis.java.sdk.common.preferences.ILastUpdatedPreferences;
import org.hisp.dhis.java.sdk.common.preferences.ResourceType;
import org.hisp.dhis.java.sdk.models.common.SystemInfo;
import org.hisp.dhis.java.sdk.models.common.state.Action;
import org.hisp.dhis.java.sdk.models.enrollment.Enrollment;
import org.hisp.dhis.java.sdk.models.event.Event;
import org.hisp.dhis.java.sdk.models.organisationunit.OrganisationUnit;
import org.hisp.dhis.java.sdk.models.program.Program;
import org.hisp.dhis.java.sdk.models.trackedentity.TrackedEntityDataValue;
import org.hisp.dhis.java.sdk.models.trackedentity.TrackedEntityInstance;
import org.hisp.dhis.java.sdk.organisationunit.IOrganisationUnitStore;
import org.hisp.dhis.java.sdk.program.IProgramStore;
import org.hisp.dhis.java.sdk.systeminfo.ISystemInfoApiClient;
import org.hisp.dhis.java.sdk.trackedentity.ITrackedEntityDataValueStore;
import org.hisp.dhis.java.sdk.models.utils.IModelUtils;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.mockito.Mockito.*;

public class EventControllerTest {


    private EventController eventController;
    private IEventApiClient eventApiClientMock;
    private ISystemInfoApiClient systemInfoApiClientMock;
    private ILastUpdatedPreferences lastUpdatedPreferencesMock;
    private IStateStore stateStoreMock;
    private ITransactionManager transactionManagerMock;
    private IEventStore eventStoreMock;
    private ITrackedEntityDataValueStore trackedEntityDataValueStoreMock;
    private IOrganisationUnitStore organisationUnitStoreMock;
    private IProgramStore programStoreMock;
    private IFailedItemStore failedItemStoreMock;
    private IModelUtils modelUtilsMock;

    private DateTime lastUpdated;
    private DateTime serverDateTime;
    private SystemInfo systemInfo;
    private Enrollment enrollment;
    private Program program;
    private OrganisationUnit organisationUnit;
    private Event fullEvent;
    private Event basicEvent;
    private TrackedEntityInstance trackedEntityInstance;
    private TrackedEntityDataValue dataValue;


    private final String ENROLLMENT_UID = "Y6xLkm9oI4";
    private final String PROGRAM_UID = "B3xKcO3oI4";
    private final String TRACKEDENITYINSTANCE_UID = "V5oPD34mIq";
    private final String ORGANISATIONUNIT_UID = "M8nK0i8y5Re";
    private final String EVENT_UID = "O8jnG4t2Lkm1";




    @Before
    public void setUp() {

        eventApiClientMock = mock(IEventApiClient.class);
        systemInfoApiClientMock = mock(ISystemInfoApiClient.class);
        lastUpdatedPreferencesMock = mock(ILastUpdatedPreferences.class);
        stateStoreMock = mock(IStateStore.class);
        transactionManagerMock = mock(ITransactionManager.class);
        eventStoreMock = mock(IEventStore.class);
        trackedEntityDataValueStoreMock = mock(ITrackedEntityDataValueStore.class);
        organisationUnitStoreMock = mock(IOrganisationUnitStore.class);
        programStoreMock = mock(IProgramStore.class);
        failedItemStoreMock = mock(IFailedItemStore.class);
        modelUtilsMock = mock(IModelUtils.class);

        program = new Program();
        program.setUId(PROGRAM_UID);

        trackedEntityInstance = new TrackedEntityInstance();
        trackedEntityInstance.setTrackedEntityInstanceUid(TRACKEDENITYINSTANCE_UID);

        enrollment = new Enrollment();
        enrollment.setUId(ENROLLMENT_UID);
        enrollment.setProgram(program.getUId());
        enrollment.setStatus(Enrollment.ACTIVE);
        enrollment.setTrackedEntityInstance(trackedEntityInstance);

        organisationUnit = new OrganisationUnit();
        organisationUnit.setUId(ORGANISATIONUNIT_UID);

        dataValue = new TrackedEntityDataValue();

        lastUpdated = new DateTime(2015, 5, 1, 1, 1);
        systemInfo = new SystemInfo();
        serverDateTime = new DateTime();
        systemInfo.setServerDate(serverDateTime);

        basicEvent = new Event();
        basicEvent.setUId(EVENT_UID);
        basicEvent.setLastUpdated(lastUpdated);

        fullEvent = new Event();
        fullEvent.setUId(EVENT_UID);
        fullEvent.setLastUpdated(new DateTime());
        fullEvent.setTrackedEntityDataValues(Arrays.asList(dataValue));

        when(lastUpdatedPreferencesMock.get(ResourceType.EVENTS, ENROLLMENT_UID)).thenReturn(lastUpdated);
        when(systemInfoApiClientMock.getSystemInfo()).thenReturn(systemInfo);
        when(eventApiClientMock.getBasicEvents(program.getUId(), enrollment.getStatus(), trackedEntityInstance.getTrackedEntityInstanceUid(), null)).thenReturn(Arrays.asList(basicEvent));
        when(eventApiClientMock.getFullEvents(program.getUId(), enrollment.getStatus(), trackedEntityInstance.getTrackedEntityInstanceUid(), lastUpdated)).thenReturn(Arrays.asList(basicEvent));

        eventController = new EventController(eventApiClientMock, systemInfoApiClientMock, lastUpdatedPreferencesMock,
                transactionManagerMock, stateStoreMock, eventStoreMock, trackedEntityDataValueStoreMock,
                organisationUnitStoreMock, programStoreMock, failedItemStoreMock, modelUtilsMock);
    }

    @Test
    public void testGetEventsFromServer()
    {
        when(programStoreMock.queryByUid(enrollment.getProgram())).thenReturn(program);
        when(modelUtilsMock.merge(any(List.class), any(List.class), any(List.class))).thenReturn(Arrays.asList(basicEvent, fullEvent));
        eventController.sync(enrollment);

        verify(eventApiClientMock, times(1)).getBasicEvents(program.getUId(),
                enrollment.getStatus(),
                enrollment.getTrackedEntityInstance().getTrackedEntityInstanceUid(),
                null);

        verify(eventApiClientMock, times(1)).getFullEvents(program.getUId(),
                enrollment.getStatus(),
                enrollment.getTrackedEntityInstance().getTrackedEntityInstanceUid(),
                lastUpdated);

        verify(transactionManagerMock, times(1)).transact(any(Collection.class));
    }

    @Test
    public void testGetEventsFromServerNullEnrollmentArgument() {
        Enrollment enrollment = null;
        eventController.sync(enrollment);
    }

    @Test
    public void testGetEventsFromServerProgramIsNull() {
        when(programStoreMock.queryByUid(enrollment.getProgram())).thenReturn(null);
        eventController.getEventsDataFromServer(enrollment);

        verify(programStoreMock, times(1)).queryByUid(enrollment.getProgram());
    }

    @Test
    public void testSyncWithOrgUnitProgramCountServerDateParameters() {
        eventController.sync(organisationUnit.getUId(), program.getUId(), 100, serverDateTime);
    }

    @Test
    public void testSyncWithEventUId() {
        when(eventApiClientMock.getFullEvent(EVENT_UID, null)).thenReturn(fullEvent);
        when(eventStoreMock.queryByUid(EVENT_UID)).thenReturn(basicEvent);
        eventController.sync(EVENT_UID);

        verify(eventStoreMock, times(1)).update(fullEvent);
    }

    @Test
    public void testSyncWithEventUIdPersistedItemIsNull() {
        when(eventApiClientMock.getFullEvent(EVENT_UID, null)).thenReturn(fullEvent);
        when(eventStoreMock.queryByUid(EVENT_UID)).thenReturn(null);
        eventController.sync(EVENT_UID);

        verify(eventStoreMock, times(1)).insert(fullEvent);
    }
    @Test
    public void testSyncSendEvents() {
        Event eventToPost = new Event();
        Event eventToUpdate = new Event();
        eventToPost.setEnrollment(enrollment);
        Enrollment enrollmentToUpdate = enrollment;
        eventToUpdate.setEnrollment(enrollmentToUpdate);

        when(stateStoreMock.queryModelsWithActions(Event.class, Action.TO_POST)).thenReturn(Arrays.asList(eventToPost));
        when(stateStoreMock.queryModelsWithActions(Event.class, Action.TO_UPDATE)).thenReturn(Arrays.asList(eventToUpdate));
        when(stateStoreMock.queryActionForModel(enrollment)).thenReturn(Action.TO_POST);
        when(stateStoreMock.queryActionForModel(enrollmentToUpdate)).thenReturn(Action.TO_UPDATE);

        eventController.sync();

    }

}
