package org.hisp.dhis.java.sdk.models.program;

import org.joda.time.DateTime;

import java.util.List;

public interface IProgramRuleApiClient {
    List<ProgramRule> getBasicProgramRules(DateTime lastUpdated);

    List<ProgramRule> getFullProgramRules(DateTime lastUpdated);
}
