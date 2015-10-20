package org.hisp.dhis.sdk.java;

import org.hisp.dhis.sdk.java.dashboard.DashboardContentServiceTest;
import org.hisp.dhis.sdk.java.dashboard.DashboardElementServiceTest;
import org.hisp.dhis.sdk.java.program.ProgramRuleServiceTest;
import org.hisp.dhis.sdk.java.program.ProgramServiceTest;
import org.hisp.dhis.sdk.java.user.UserAccountControllerTest;
import org.hisp.dhis.sdk.java.user.UserAccountServiceTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;


@RunWith(Suite.class)
@Suite.SuiteClasses({
        DashboardContentServiceTest.class,
        DashboardElementServiceTest.class,
        ProgramServiceTest.class,
        ProgramRuleServiceTest.class,
        UserAccountServiceTest.class,

        UserAccountControllerTest.class
})
public class CoreTestSuite {
}
