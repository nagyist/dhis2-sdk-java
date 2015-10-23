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
import org.hisp.dhis.java.sdk.models.program.ProgramRule;
import org.hisp.dhis.java.sdk.models.program.ProgramStage;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class ProgramRuleServiceTest {
    IProgramRuleStore programRuleStoreMock;
    ProgramRuleService programRuleService;

    @Before
    public void setUp() {
        programRuleStoreMock = mock(IProgramRuleStore.class);

        programRuleService = new ProgramRuleService(programRuleStoreMock);
    }

    @Test
    public void testListWithProgramStageShouldReturnAListOfProgramRules() {
        List<ProgramRule> programRules = new ArrayList<>();

        assertEquals(programRuleService.list(new ProgramStage()), programRules);
    }

    @Test
    public void testListWithProgramStageShouldQueryTheStoreWithProgramStage() {
        ProgramStage programStage = new ProgramStage();

        programRuleService.list(programStage);

        verify(programRuleStoreMock, times(1)).query(programStage);
    }

    @Test
    public void testListWithoutParametersShouldReturnAListOfProgramRules() {
        List<ProgramRule> programRules = new ArrayList<>();

        assertEquals(programRules, programRuleService.list());
    }

    @Test
    public void testListWithoutParametersShouldQueryStoreForAll() {
        programRuleService.list();

        verify(programRuleStoreMock, times(1)).queryAll();
    }

    @Test
    public void testListWithProgramShouldReturnAListOfProgramRules() {
        List<ProgramRule> programRules = new ArrayList<>();

        assertEquals(programRules, programRuleService.list(new Program()));
    }

    @Test
    public void testListWithProgramShouldQueryStoreForWithProgram() {
        Program program = new Program();

        programRuleService.list(program);

        verify(programRuleStoreMock, times(1)).query(program);
    }

    @Test
    public void testGetWithNumberShouldReturnAProgramRule() {
        ProgramRule programRule = new ProgramRule();
        when(programRuleStoreMock.queryById(1)).thenReturn(programRule);

        assertEquals(programRule, programRuleService.get(1));
    }

    @Test
    public void testGetWithStringShouldReturnAProgramRule() {
        ProgramRule programRule = new ProgramRule();
        when(programRuleStoreMock.queryByUid("ueuQlqb8ccl")).thenReturn(programRule);

        assertEquals(programRule, programRuleService.get("ueuQlqb8ccl"));
    }
}
