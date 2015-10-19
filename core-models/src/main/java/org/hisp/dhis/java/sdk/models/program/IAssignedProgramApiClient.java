package org.hisp.dhis.java.sdk.models.program;

import org.hisp.dhis.java.sdk.models.organisationunit.OrganisationUnit;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface IAssignedProgramApiClient {
    Map<OrganisationUnit, Set<Program>> getAssignedPrograms();
}
