package org.hisp.dhis.sdk.java;

import org.hisp.dhis.sdk.java.dashboard.DashboardContentServiceTest;
import org.hisp.dhis.sdk.java.dashboard.DashboardElementServiceTest;
import org.hisp.dhis.sdk.java.user.UserAccountServiceTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;


@RunWith(Suite.class)
@Suite.SuiteClasses({
        DashboardContentServiceTest.class,
        DashboardElementServiceTest.class,
        UserAccountServiceTest.class
})
public class CoreTestSuite {
}
