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
package org.hisp.dhis.sdk.java.enrollment;


import org.hisp.dhis.sdk.java.common.IFailedItemStore;
import org.hisp.dhis.sdk.java.common.IStateStore;
import org.hisp.dhis.sdk.java.common.persistence.ITransactionManager;
import org.hisp.dhis.sdk.java.common.preferences.ILastUpdatedPreferences;
import org.hisp.dhis.sdk.java.event.IEventController;
import org.hisp.dhis.sdk.java.event.IEventStore;
import org.hisp.dhis.sdk.java.systeminfo.ISystemInfoApiClient;
import org.hisp.dhis.sdk.java.utils.IIdentifialModelUtils;
import org.hisp.dhis.sdk.java.utils.IModelUtils;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.mock;

public class EnrollmentControllerTest {

    private IEnrollmentApiClient enrollmentApiClient;
    private ISystemInfoApiClient systemInfoApiClient;
    private ILastUpdatedPreferences lastUpdatedPreferences;
    private ITransactionManager transactionManager;

    private IEventController eventController;
    private IEnrollmentStore enrollmentStore;
    private IEventStore eventStore;
    private IStateStore stateStore;
    private IFailedItemStore failedItemStore;
    private IModelUtils modelUtils;
    private EnrollmentController enrollmentController;

    @Before
    public void setUp() {
        enrollmentApiClient = mock(IEnrollmentApiClient.class);
        systemInfoApiClient = mock(ISystemInfoApiClient.class);
        lastUpdatedPreferences = mock(ILastUpdatedPreferences.class);
        transactionManager = mock(ITransactionManager.class);
        eventStore = mock(IEventStore.class);
        stateStore = mock(IStateStore.class);
        failedItemStore = mock(IFailedItemStore.class);
        eventController = mock(IEventController.class);
        enrollmentStore = mock(IEnrollmentStore.class);
        modelUtils = mock(IModelUtils.class);

        enrollmentController = new EnrollmentController(enrollmentApiClient, systemInfoApiClient,
                lastUpdatedPreferences, transactionManager, eventController, enrollmentStore,
                eventStore, stateStore, failedItemStore, modelUtils);
    }


    @Test
    public void test() {

    }
}
