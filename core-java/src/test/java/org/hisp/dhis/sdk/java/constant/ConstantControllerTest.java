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

package org.hisp.dhis.sdk.java.constant;

import org.hisp.dhis.java.sdk.models.constant.Constant;
import org.hisp.dhis.java.sdk.models.program.Program;
import org.hisp.dhis.java.sdk.models.program.ProgramRule;
import org.hisp.dhis.java.sdk.models.program.ProgramStage;
import org.hisp.dhis.sdk.java.common.persistence.IIdentifiableObjectStore;
import org.hisp.dhis.sdk.java.common.persistence.ITransactionManager;
import org.hisp.dhis.sdk.java.common.preferences.ILastUpdatedPreferences;
import org.hisp.dhis.sdk.java.program.IProgramRuleStore;
import org.hisp.dhis.sdk.java.program.ProgramRuleService;
import org.hisp.dhis.sdk.java.systeminfo.ISystemInfoApiClient;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class ConstantControllerTest {
    private IConstantApiClient constantApiClientMock;
    private ITransactionManager transactionManagerMock;
    private ISystemInfoApiClient systemInfoApiClientMock;
    private ILastUpdatedPreferences lastUpdatedPreferencesMock;
    private IIdentifiableObjectStore<Constant> constantStoreMock;
    private ConstantController constantController;

    @Before
    public void setUp() {
        constantApiClientMock = mock(IConstantApiClient.class);
        transactionManagerMock = mock(ITransactionManager.class);
        systemInfoApiClientMock = mock(ISystemInfoApiClient.class);
        lastUpdatedPreferencesMock = mock(ILastUpdatedPreferences.class);
        constantStoreMock = mock(IIdentifiableObjectStore.class);
        constantController = new ConstantController(constantApiClientMock, transactionManagerMock,
                systemInfoApiClientMock, lastUpdatedPreferencesMock, constantStoreMock);
    }

    @Test
    public void testConstantsFromServerShouldBeSavedInStore() {

    }

    @Test
    public void testUpdatedConstantsFromServerShouldUpdateLocallySavedConstants() {

    }

    @Test
    public void testNewConstantsOnServerShouldBeSavedLocallyWhenConstantsHavePreviouslyBeenLoaded() {

    }

    @Test
    public void testNonUpdatedConstantsOnServerThatAlreadyHaveBeenSavedLocallyShouldNotBeLoaded() {

    }

    @Test
    public void deletedConstantsOnServerShouldDeleteLocallySavedConstants() {

    }
}
