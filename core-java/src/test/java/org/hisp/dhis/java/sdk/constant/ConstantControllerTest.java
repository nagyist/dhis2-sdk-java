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

package org.hisp.dhis.java.sdk.constant;

import org.hisp.dhis.java.sdk.common.persistence.IIdentifiableObjectStore;
import org.hisp.dhis.java.sdk.common.preferences.ResourceType;
import org.hisp.dhis.java.sdk.models.common.SystemInfo;
import org.hisp.dhis.java.sdk.models.constant.Constant;
import org.hisp.dhis.java.sdk.systeminfo.ISystemInfoApiClient;
import org.hisp.dhis.java.sdk.common.persistence.IDbOperation;
import org.hisp.dhis.java.sdk.common.persistence.ITransactionManager;
import org.hisp.dhis.java.sdk.common.preferences.ILastUpdatedPreferences;
import org.hisp.dhis.java.sdk.utils.IModelUtils;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class ConstantControllerTest {
    private IConstantApiClient constantApiClient;
    private ITransactionManager transactionManagerMock;
    private ILastUpdatedPreferences lastUpdatedPreferencesMock;
    private ISystemInfoApiClient systemInfoApiClient;
    private IIdentifiableObjectStore<Constant> constantStore;
    private IModelUtils modelUtilsMock;
    private ConstantController constantController;
    private List<Constant> constantList;
    private List<Constant> constantListLastUpdated;
    private Constant constant1;
    private Constant constant2;
    private Constant constant3;
    private Constant constant4;
    private Constant constant5;
    private SystemInfo systemInfo;
    private DateTime lastUpdated;

    @Before
    public void setUp() {
        constantApiClient = mock(IConstantApiClient.class);
        constantStore = mock(IIdentifiableObjectStore.class);
        transactionManagerMock = mock(ITransactionManager.class);
        lastUpdatedPreferencesMock = mock(ILastUpdatedPreferences.class);
        systemInfoApiClient = mock(ISystemInfoApiClient.class);
        modelUtilsMock = mock(IModelUtils.class);

        constant1 = new Constant();
        constant2 = new Constant();
        constant3 = new Constant();
        constant4 = new Constant();
        constant5 = new Constant();
        constant1.setLastUpdated(new DateTime());
        constant2.setLastUpdated(new DateTime());
        constant3.setLastUpdated(new DateTime());
        constant4.setLastUpdated(new DateTime());
        constant5.setLastUpdated(new DateTime());

        constantList = new ArrayList<>();

        constantList.add(constant1);
        constantList.add(constant2);
        constantList.add(constant3);
        constantList.add(constant4);

        constantListLastUpdated = new ArrayList<>();
        constantListLastUpdated.add(constant5);

        systemInfo = new SystemInfo();
        systemInfo.setServerDate(new DateTime());
        lastUpdated = new DateTime(2015, 10, 15, 0, 0);

        when(systemInfoApiClient.getSystemInfo()).thenReturn(systemInfo);
        when(lastUpdatedPreferencesMock.get(ResourceType.CONSTANTS)).thenReturn(lastUpdated);
        when(constantApiClient.getBasicConstants(null)).thenReturn(constantList);
        when(constantApiClient.getFullConstants(lastUpdated)).thenReturn(constantListLastUpdated);
        constantController = new ConstantController(constantApiClient, transactionManagerMock,
                systemInfoApiClient, lastUpdatedPreferencesMock, constantStore, modelUtilsMock);
    }

    /**
     * This test synchronizes with the server and tests that the mock methods is
     * being called. This includes data(locally and from server) is being merged,
     * being saved in store and updating lastUpdated fields
     */

    @Test
    public void testGetConstantsFromServer() {
        constantController.sync();
        List<Constant> mergedLists = new ArrayList<>();
        mergedLists.addAll(constantList);
        mergedLists.addAll(constantListLastUpdated);

        List<IDbOperation> operations = new ArrayList<>();

        when(transactionManagerMock.createOperations(constantStore,
                mergedLists, constantStore.queryAll())).thenReturn(operations);

        verify(modelUtilsMock, times(1)).merge(constantList, constantListLastUpdated, constantStore.queryAll());
        verify(constantApiClient, times(1)).getBasicConstants(null);
        verify(constantApiClient, times(1)).getFullConstants(lastUpdated);
        assertEquals(constantApiClient.getFullConstants(lastUpdated), constantListLastUpdated);
        assertEquals(constantApiClient.getBasicConstants(null), constantList);
        verify(transactionManagerMock, atLeastOnce()).transact(any(Collection.class));
        verify(transactionManagerMock, times(1)).transact(operations);
        verify(lastUpdatedPreferencesMock, times(1)).save(ResourceType.CONSTANTS, systemInfo.getServerDate());
    }
}
