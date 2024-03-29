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

package org.hisp.dhis.java.sdk.program;

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
