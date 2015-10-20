package org.hisp.dhis.sdk.java.program;

import org.hisp.dhis.java.sdk.models.program.Program;
import org.hisp.dhis.java.sdk.models.program.ProgramRuleVariable;

import java.util.List;

public final class ProgramRuleVariableService implements IProgramRuleVariableService {

    private final IProgramRuleVariableStore programRuleVariableStore;

    public ProgramRuleVariableService(IProgramRuleVariableStore programRuleVariableStore) {
        this.programRuleVariableStore = programRuleVariableStore;
    }

    @Override
    public ProgramRuleVariable get(long id) {
        return null;
    }

    @Override
    public ProgramRuleVariable get(String uid) {
        return null;
    }

    @Override
    public List<ProgramRuleVariable> list() {
        return null;
    }

    @Override
    public ProgramRuleVariable getByName(Program program, String programRuleVariableName) {
        List<ProgramRuleVariable> programRuleVariables = programRuleVariableStore.query(program);
        for(ProgramRuleVariable programRuleVariable : programRuleVariables) {
            if(programRuleVariable.getName().equals(programRuleVariableName)) {
                return programRuleVariable;
            }
        }
        return null;
    }
}
