package org.hisp.dhis.sdk.java.program;

import org.hisp.dhis.java.sdk.models.program.Program;
import org.hisp.dhis.java.sdk.models.program.ProgramRule;
import org.hisp.dhis.java.sdk.models.program.ProgramStage;

import java.util.List;

public final class ProgramRuleService implements IProgramRuleService {

    private final IProgramRuleStore programRuleStore;

    public ProgramRuleService(IProgramRuleStore programRuleStore) {
        this.programRuleStore = programRuleStore;
    }

    @Override
    public List<ProgramRule> list(ProgramStage programStage) {
        return programRuleStore.query(programStage);
    }

    @Override
    public List<ProgramRule> list(Program program) {
        return programRuleStore.query(program);
    }

    @Override
    public ProgramRule get(long id) {
        return programRuleStore.queryById(id);
    }

    @Override
    public ProgramRule get(String uid) {
        return programRuleStore.queryByUid(uid);
    }

    @Override
    public List<ProgramRule> list() {
        return programRuleStore.queryAll();
    }
}
