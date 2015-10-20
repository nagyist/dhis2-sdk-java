package org.hisp.dhis.sdk.java.program;

import org.hisp.dhis.java.sdk.models.organisationunit.OrganisationUnit;
import org.hisp.dhis.java.sdk.models.program.Program;

import java.util.List;

public final class ProgramService implements IProgramService {

    private final IProgramStore programStore;

    public ProgramService(IProgramStore programStore) {
        this.programStore = programStore;
    }

    @Override
    public Program get(long id) {
        return programStore.queryById(id);
    }

    @Override
    public Program get(String uid) {
        return programStore.queryByUid(uid);
    }

    @Override
    public List<Program> list() {
        return programStore.queryAll();
    }

    @Override
    public List<Program> list(OrganisationUnit organisationUnit, Program.ProgramType... programTypes) {
        return programStore.query(organisationUnit, programTypes);
    }
}
