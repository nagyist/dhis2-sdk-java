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

import org.hisp.dhis.java.sdk.models.common.SystemInfo;
import org.hisp.dhis.java.sdk.models.constant.Constant;
import org.hisp.dhis.sdk.java.common.persistence.IDbOperation;
import org.hisp.dhis.sdk.java.common.persistence.IIdentifiableObjectStore;
import org.hisp.dhis.sdk.java.common.persistence.ITransactionManager;
import org.hisp.dhis.sdk.java.common.preferences.ILastUpdatedPreferences;
import org.hisp.dhis.sdk.java.common.preferences.ResourceType;
import org.hisp.dhis.sdk.java.systeminfo.ISystemInfoApiClient;
import org.hisp.dhis.sdk.java.utils.IModelUtils;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;

public class ConstantControllerTest {
    private IConstantApiClient constantApiClientMock;
    private ITransactionManager transactionManagerMock;
    private ISystemInfoApiClient systemInfoApiClientMock;
    private ILastUpdatedPreferences lastUpdatedPreferencesMock;
    private IModelUtils modelUtilsMock;
    private IIdentifiableObjectStore<Constant> constantStoreMock;
    private ConstantController constantController;

    @Before
    public void setUp() {
        constantApiClientMock = mock(IConstantApiClient.class);
        transactionManagerMock = mock(ITransactionManager.class);
        systemInfoApiClientMock = mock(ISystemInfoApiClient.class);
        lastUpdatedPreferencesMock = mock(ILastUpdatedPreferences.class);
        modelUtilsMock = mock(IModelUtils.class);
        constantStoreMock = mock(IIdentifiableObjectStore.class);
        constantController = new ConstantController(constantApiClientMock, transactionManagerMock,
                systemInfoApiClientMock, lastUpdatedPreferencesMock, constantStoreMock, modelUtilsMock);
    }

    @Test
    public void testAllConstantsFromServerShouldBeSavedInStoreWhenNoConstantsHaveBeenLoadedBefore() {
        Constant constant1 = new Constant();
        constant1.setUId("aaaaaaaa");

        Constant constant2 = new Constant();
        constant2.setUId("bbbbbbbb");

        Constant constant3 = new Constant();
        constant3.setUId("cccccccc");

        List<Constant> updatedConstantsFromServer = new ArrayList<>();
        updatedConstantsFromServer.add(constant1);
        updatedConstantsFromServer.add(constant2);
        updatedConstantsFromServer.add(constant3);

        List<Constant> persistedConstants = new ArrayList<>();

        SystemInfo systemInfo = new SystemInfo();
        DateTime dateTime = new DateTime(2015, 1, 1, 12, 30);
        systemInfo.setServerDate(dateTime);
        when(systemInfoApiClientMock.getSystemInfo()).thenReturn(systemInfo);
        when(lastUpdatedPreferencesMock.get(ResourceType.CONSTANTS)).thenReturn(null);

        when(constantApiClientMock.getBasicConstants(null)).thenReturn(updatedConstantsFromServer);
        when(constantApiClientMock.getFullConstants(null)).thenReturn(updatedConstantsFromServer);

        when(modelUtilsMock.merge(updatedConstantsFromServer, updatedConstantsFromServer,
                persistedConstants)).thenReturn(updatedConstantsFromServer);

        List<IDbOperation> operations = new ArrayList<>();

        when(transactionManagerMock.createOperations(constantStoreMock,
                updatedConstantsFromServer, persistedConstants)).thenReturn(operations);

        when(lastUpdatedPreferencesMock.save(ResourceType.CONSTANTS, systemInfo.getServerDate(), null)).thenReturn(true);

        constantController.sync();

        verify(systemInfoApiClientMock, times(1)).getSystemInfo();
        verify(lastUpdatedPreferencesMock, times(1)).get(ResourceType.CONSTANTS);
        verify(constantApiClientMock, times(1)).getBasicConstants(null);
        verify(constantApiClientMock, times(1)).getFullConstants(null);
        verify(modelUtilsMock, times(1)).merge(updatedConstantsFromServer,
                updatedConstantsFromServer, persistedConstants);
        verify(constantStoreMock, atLeastOnce()).queryAll();
        verify(transactionManagerMock, times(1)).transact(operations);
        verify(lastUpdatedPreferencesMock, times(1)).save(ResourceType.CONSTANTS, dateTime);
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
