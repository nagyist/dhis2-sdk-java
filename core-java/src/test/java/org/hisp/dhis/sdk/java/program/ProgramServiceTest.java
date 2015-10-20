package org.hisp.dhis.sdk.java.program;

import org.hisp.dhis.java.sdk.models.organisationunit.OrganisationUnit;
import org.hisp.dhis.java.sdk.models.program.Program;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.*;

public class ProgramServiceTest {
    IProgramStore programStoreMock;
    IProgramService programService;
    Program programMock;

    @Before
    public void setUp() {
        programStoreMock = mock(IProgramStore.class);
        programMock = new Program();

        programService = new ProgramService(programStoreMock);
    }

    @Test
    public void testGetWithIdShouldCallQueryByIdOnProgramStore() {
        programService.get(1);

        verify(programStoreMock, times(1)).queryById(1);
    }

    @Test
    public void testGetWithIdShouldReturnTheProgram() {
        when(programStoreMock.queryById(1)).thenReturn(programMock);

        assertSame(programService.get(1), programMock);
    }

    @Test
    public void testGetWithUidShouldCallQueryByUidOnProgramStore() {
        programService.get("ueuQlqb8ccl");

        verify(programStoreMock, times(1)).queryByUid("ueuQlqb8ccl");
    }

    @Test
    public void testGetWithUidShouldReturnTheProgram() {
        when(programStoreMock.queryByUid("ueuQlqb8ccl")).thenReturn(programMock);

        assertSame(programService.get("ueuQlqb8ccl"), programMock);
    }

    @Test
    public void testListShouldAskTheProgramStoreForAllPrograms() {
        programService.list();

        verify(programStoreMock, times(1)).queryAll();
    }

    @Test
    public void testListShouldReturnTheListOfPrograms() {
        List<Program> programList = new ArrayList<>();

        assertEquals(programService.list(), programList);
    }

    @Test
    public void testListWithOrgUnitAndProgramTypesShouldQueryTheStoreWithThoseCriteria() {
        OrganisationUnit organisationUnit = new OrganisationUnit();

        programService.list(organisationUnit, Program.ProgramType.MULTIPLE_EVENTS_WITH_REGISTRATION);

        verify(programStoreMock, times(1)).query(organisationUnit, Program.ProgramType.MULTIPLE_EVENTS_WITH_REGISTRATION);
    }

    @Test
    public void testListWithOrgUnitAndSingleProgramTypeShouldReturnAProgramList() {
        OrganisationUnit organisationUnit = new OrganisationUnit();
        List<Program> programList = new ArrayList<>();

        assertEquals(programService.list(organisationUnit, Program.ProgramType.MULTIPLE_EVENTS_WITH_REGISTRATION), programList);
    }

    @Test
    public void testListWithOrgUnitAndProgramShouldQueryStoreWithProgramTypesList() {
        OrganisationUnit organisationUnit = new OrganisationUnit();

        programService.list(
                organisationUnit,
                Program.ProgramType.MULTIPLE_EVENTS_WITH_REGISTRATION,
                Program.ProgramType.SINGLE_EVENT_WITH_REGISTRATION
        );

        verify(programStoreMock, times(1)).query(
                organisationUnit,
                Program.ProgramType.MULTIPLE_EVENTS_WITH_REGISTRATION,
                Program.ProgramType.SINGLE_EVENT_WITH_REGISTRATION
        );
    }

    @Test
    public void testListWithOrgUnitAndMultipleProgramTypeShouldReturnAProgramList() {
        OrganisationUnit organisationUnit = new OrganisationUnit();
        List<Program> programList = new ArrayList<>();

        assertEquals(programService.list(
                organisationUnit,
                Program.ProgramType.MULTIPLE_EVENTS_WITH_REGISTRATION,
                Program.ProgramType.SINGLE_EVENT_WITH_REGISTRATION
        ), programList);
    }
}
