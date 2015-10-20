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
