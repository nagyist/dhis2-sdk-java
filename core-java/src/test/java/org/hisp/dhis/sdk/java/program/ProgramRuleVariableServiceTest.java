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

package org.hisp.dhis.sdk.java.program;

import org.hisp.dhis.java.sdk.models.program.Program;
import org.hisp.dhis.java.sdk.models.program.ProgramRuleVariable;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class ProgramRuleVariableServiceTest {
    IProgramRuleVariableStore programRuleVariableStoreMock;
    ProgramRuleVariableService programRuleVariableService;

    @Before
    public void setUp() {
        programRuleVariableStoreMock = mock(IProgramRuleVariableStore.class);

        programRuleVariableService = new ProgramRuleVariableService(programRuleVariableStoreMock);
    }

    @Test
    public void testGetWithNumberShouldReturnAProgramRuleVariable() {
        ProgramRuleVariable programRuleVariable = new ProgramRuleVariable();
        when(programRuleVariableStoreMock.queryById(1)).thenReturn(programRuleVariable);

        assertEquals(programRuleVariable, programRuleVariableService.get(1));
        verify(programRuleVariableStoreMock, times(1)).queryById(1);
    }

    @Test
    public void testGetWithStringShouldReturnAProgramRule() {
        ProgramRuleVariable programRuleVariable = new ProgramRuleVariable();
        when(programRuleVariableStoreMock.queryByUid("ueuQlqb8ccl")).thenReturn(programRuleVariable);

        assertEquals(programRuleVariable, programRuleVariableService.get("ueuQlqb8ccl"));
        verify(programRuleVariableStoreMock, times(1)).queryByUid("ueuQlqb8ccl");
    }

    @Test
    public void testListWithoutParametersShouldQueryStoreForAll() {
        programRuleVariableService.list();

        verify(programRuleVariableStoreMock, times(1)).queryAll();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetByNameWherePassedNameIsNullShouldThrowException() {
        Program program = new Program();
        programRuleVariableService.getByName(program, null);
    }

    @Test
    public void testGetByNameWhereVariableExistsShouldReturnProgramRuleVariable() {
        ProgramRuleVariable programRuleVariable1 = new ProgramRuleVariable();
        programRuleVariable1.setUId("aaaaaaaa");
        programRuleVariable1.setName("name1");

        ProgramRuleVariable programRuleVariable2 = new ProgramRuleVariable();
        programRuleVariable2.setUId("bbbbbbbb");
        programRuleVariable2.setName("name2");

        ProgramRuleVariable programRuleVariable3 = new ProgramRuleVariable();
        programRuleVariable3.setUId("cccccccc");
        programRuleVariable3.setName("name3");

        List<ProgramRuleVariable> programRuleVariables = new ArrayList<>();
        programRuleVariables.add(programRuleVariable1);
        programRuleVariables.add(programRuleVariable2);
        programRuleVariables.add(programRuleVariable3);

        Program program = new Program();

        when(programRuleVariableStoreMock.query(program)).thenReturn(programRuleVariables);

        assertEquals(programRuleVariable3, programRuleVariableService.getByName(program, "name3"));
        verify(programRuleVariableStoreMock, times(1)).query(program);
    }

    @Test
    public void testGetByNameWhereVariableDoesNotExistsShouldReturnNull() {
        ProgramRuleVariable programRuleVariable1 = new ProgramRuleVariable();
        programRuleVariable1.setUId("aaaaaaaa");
        programRuleVariable1.setName("name1");

        ProgramRuleVariable programRuleVariable2 = new ProgramRuleVariable();
        programRuleVariable2.setUId("bbbbbbbb");
        programRuleVariable2.setName("name2");

        ProgramRuleVariable programRuleVariable3 = new ProgramRuleVariable();
        programRuleVariable3.setUId("cccccccc");
        programRuleVariable3.setName("name3");

        List<ProgramRuleVariable> programRuleVariables = new ArrayList<>();
        programRuleVariables.add(programRuleVariable1);
        programRuleVariables.add(programRuleVariable2);
        programRuleVariables.add(programRuleVariable3);

        Program program = new Program();

        when(programRuleVariableStoreMock.query(program)).thenReturn(programRuleVariables);

        assertEquals(null, programRuleVariableService.getByName(program, "name4"));
        verify(programRuleVariableStoreMock, times(1)).query(program);
    }
}
