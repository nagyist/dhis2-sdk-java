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

package org.hisp.dhis.java.sdk;

import org.hisp.dhis.java.sdk.dashboard.*;
import org.hisp.dhis.java.sdk.event.EventControllerTest;
import org.hisp.dhis.java.sdk.interpretation.InterpretationCommentServiceTest;
import org.hisp.dhis.java.sdk.user.UserAccountServiceTest;
import org.hisp.dhis.java.sdk.enrollment.EnrollmentControllerTest;
import org.hisp.dhis.java.sdk.enrollment.EnrollmentServiceTest;
import org.hisp.dhis.java.sdk.event.EventServiceTest;
import org.hisp.dhis.java.sdk.interpretation.InterpretationElementServiceTest;
import org.hisp.dhis.java.sdk.interpretation.InterpretationServiceTest;
import org.hisp.dhis.java.sdk.program.ProgramRuleServiceTest;
import org.hisp.dhis.java.sdk.program.ProgramRuleVariableServiceTest;
import org.hisp.dhis.java.sdk.program.ProgramServiceTest;
import org.hisp.dhis.java.sdk.trackedentity.TrackedEntityAttributeControllerTest;
import org.hisp.dhis.java.sdk.trackedentity.TrackedEntityInstanceServiceTest;
import org.hisp.dhis.java.sdk.user.UserAccountControllerTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;


@RunWith(Suite.class)
@Suite.SuiteClasses({
        DashboardContentServiceTest.class,
        DashboardElementServiceTest.class,
        DashboardItemServiceTest.class,
        DashboardServiceTest.class,
        DashboardControllerTest.class,

        ProgramServiceTest.class,
        ProgramRuleServiceTest.class,
        ProgramRuleVariableServiceTest.class,
        UserAccountServiceTest.class,

        UserAccountControllerTest.class,
        TrackedEntityInstanceServiceTest.class,
        TrackedEntityAttributeControllerTest.class,
        EnrollmentServiceTest.class,
        EnrollmentControllerTest.class,
        EventServiceTest.class,
        EventControllerTest.class,

        InterpretationElementServiceTest.class,
        InterpretationCommentServiceTest.class,
        InterpretationServiceTest.class,
})
public class CoreTestSuite {
}
