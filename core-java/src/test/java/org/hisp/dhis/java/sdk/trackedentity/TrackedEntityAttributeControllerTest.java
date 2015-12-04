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
package org.hisp.dhis.java.sdk.trackedentity;

import org.hisp.dhis.java.sdk.common.persistence.IIdentifiableObjectStore;
import org.hisp.dhis.java.sdk.common.preferences.ILastUpdatedPreferences;
import org.hisp.dhis.java.sdk.common.preferences.ResourceType;
import org.hisp.dhis.java.sdk.models.common.SystemInfo;
import org.hisp.dhis.java.sdk.models.trackedentity.TrackedEntityAttribute;
import org.hisp.dhis.java.sdk.systeminfo.ISystemInfoApiClient;
import org.hisp.dhis.java.sdk.common.persistence.IDbOperation;
import org.hisp.dhis.java.sdk.common.persistence.ITransactionManager;
import org.hisp.dhis.java.sdk.models.utils.IModelUtils;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;


public class TrackedEntityAttributeControllerTest {

    private ITrackedEntityAttributeApiClient trackedEntityAttributeApiClient;
    private ITransactionManager transactionManagerMock;
    private ILastUpdatedPreferences lastUpdatedPreferencesMock;
    private ISystemInfoApiClient systemInfoApiClient;
    private IIdentifiableObjectStore<TrackedEntityAttribute> trackedEntityAttributeStore;
    private IModelUtils modelUtilsMock;
    private TrackedEntityAttributeController trackedEntityAttributeController;
    private List<TrackedEntityAttribute> trackedEntityAttributeList;
    private List<TrackedEntityAttribute> trackedEntityAttributeListLastUpdated;
    private TrackedEntityAttribute trackedEntityAttribute1;
    private TrackedEntityAttribute trackedEntityAttribute2;
    private TrackedEntityAttribute trackedEntityAttribute3;
    private TrackedEntityAttribute trackedEntityAttribute4;
    private TrackedEntityAttribute trackedEntityAttribute5;
    private SystemInfo systemInfo;
    private DateTime lastUpdated;

    @Before
    public void setUp() {
        trackedEntityAttributeApiClient = mock(ITrackedEntityAttributeApiClient.class);
        trackedEntityAttributeStore = mock(IIdentifiableObjectStore.class);
        transactionManagerMock = mock(ITransactionManager.class);
        lastUpdatedPreferencesMock = mock(ILastUpdatedPreferences.class);
        systemInfoApiClient = mock(ISystemInfoApiClient.class);
        modelUtilsMock = mock(IModelUtils.class);

        trackedEntityAttribute1 = new TrackedEntityAttribute();
        trackedEntityAttribute2 = new TrackedEntityAttribute();
        trackedEntityAttribute3 = new TrackedEntityAttribute();
        trackedEntityAttribute4 = new TrackedEntityAttribute();
        trackedEntityAttribute5 = new TrackedEntityAttribute();
        trackedEntityAttribute1.setLastUpdated(new DateTime());
        trackedEntityAttribute2.setLastUpdated(new DateTime());
        trackedEntityAttribute3.setLastUpdated(new DateTime());
        trackedEntityAttribute4.setLastUpdated(new DateTime());
        trackedEntityAttribute5.setLastUpdated(new DateTime());

        trackedEntityAttributeList = new ArrayList<>();

        trackedEntityAttributeList.add(trackedEntityAttribute1);
        trackedEntityAttributeList.add(trackedEntityAttribute2);
        trackedEntityAttributeList.add(trackedEntityAttribute3);
        trackedEntityAttributeList.add(trackedEntityAttribute4);

        trackedEntityAttributeListLastUpdated = new ArrayList<>();
        trackedEntityAttributeListLastUpdated.add(trackedEntityAttribute5);

        systemInfo = new SystemInfo();
        systemInfo.setServerDate(new DateTime());
        lastUpdated = new DateTime(2015, 10, 15, 0, 0);

        when(systemInfoApiClient.getSystemInfo()).thenReturn(systemInfo);
        when(lastUpdatedPreferencesMock.get(ResourceType.TRACKED_ENTITY_ATTRIBUTES)).thenReturn(lastUpdated);
        when(trackedEntityAttributeApiClient.getBasicTrackedEntityAttributes(null)).thenReturn(trackedEntityAttributeList);
        when(trackedEntityAttributeApiClient.getFullTrackedEntityAttributes(lastUpdated)).thenReturn(trackedEntityAttributeListLastUpdated);
        trackedEntityAttributeController = new TrackedEntityAttributeController(trackedEntityAttributeApiClient, transactionManagerMock,
                lastUpdatedPreferencesMock, trackedEntityAttributeStore, systemInfoApiClient, modelUtilsMock);
    }

    /**
     * This test synchronizes with the server and tests that the mock methods is
     * being called. This includes data(locally and from server) is being merged,
     * being saved in store and updating lastUpdated fields
     */

    @Test
    public void testGetTrackedEntityAttributesFromServer() {
        trackedEntityAttributeController.sync();
        List<TrackedEntityAttribute> mergedLists = new ArrayList<>();
        mergedLists.addAll(trackedEntityAttributeList);
        mergedLists.addAll(trackedEntityAttributeListLastUpdated);

        List<IDbOperation> operations = new ArrayList<>();

        when(transactionManagerMock.createOperations(trackedEntityAttributeStore,
                mergedLists, trackedEntityAttributeStore.queryAll())).thenReturn(operations);

        verify(modelUtilsMock, times(1)).merge(trackedEntityAttributeList, trackedEntityAttributeListLastUpdated, trackedEntityAttributeStore.queryAll());
        verify(trackedEntityAttributeApiClient, times(1)).getBasicTrackedEntityAttributes(null);
        verify(trackedEntityAttributeApiClient, times(1)).getFullTrackedEntityAttributes(lastUpdated);
        assertEquals(trackedEntityAttributeApiClient.getFullTrackedEntityAttributes(lastUpdated), trackedEntityAttributeListLastUpdated);
        assertEquals(trackedEntityAttributeApiClient.getBasicTrackedEntityAttributes(null), trackedEntityAttributeList);
        verify(transactionManagerMock, atLeastOnce()).transact(any(Collection.class));
        verify(transactionManagerMock, times(1)).transact(operations);
        verify(lastUpdatedPreferencesMock, times(1)).save(ResourceType.TRACKED_ENTITY_ATTRIBUTES, systemInfo.getServerDate());
    }

}
