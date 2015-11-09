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

import org.hisp.dhis.java.sdk.common.IStateStore;
import org.hisp.dhis.java.sdk.models.common.state.Action;
import org.hisp.dhis.java.sdk.models.organisationunit.OrganisationUnit;
import org.hisp.dhis.java.sdk.models.relationship.Relationship;
import org.hisp.dhis.java.sdk.models.relationship.RelationshipType;
import org.hisp.dhis.java.sdk.models.trackedentity.TrackedEntity;
import org.hisp.dhis.java.sdk.models.trackedentity.TrackedEntityInstance;
import org.hisp.dhis.java.sdk.relationship.IRelationshipStore;
import org.hisp.dhis.java.sdk.utils.CodeGenerator;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.verifyStatic;

@RunWith(PowerMockRunner.class)
@PrepareForTest(CodeGenerator.class)
public class TrackedEntityInstanceServiceTest {
    private ITrackedEntityInstanceStore trackedEntityInstanceStoreMock;
    private TrackedEntityInstanceService trackedEntityInstanceService;
    private IRelationshipStore relationshipStoreMock;
    private IStateStore stateStoreMock;
    private TrackedEntity trackedEntity;
    private OrganisationUnit organisationUnit;

    private final String TRACKED_ENTITY_INSTANCE_UID_MOCK = "AY0xO7xLka3";
    private final long TRACKED_ENTITY_INSTANCE_ID_MOCK = 2L;
    private final long INVALID_TRACKED_ENTITY_INSTANCE_ID = 5;
    private final String TRACKED_ENTITY_UID = "A9ilo6sX65s";
    private final String ORGANISATION_UNIT_UID = "Qe1zQpL24x";
    private final String RELATIONSHIP_TYPE_UID = "M8uy5Ao0lx";

    private TrackedEntityInstance trackedEntityInstanceMock;
    private TrackedEntityInstance trackedEntityInstanceToSync;
    private TrackedEntityInstance trackedEntityInstanceToPost;
    private TrackedEntityInstance trackedEntityInstanceToUpdate;
    private TrackedEntityInstance trackedEntityInstanceToDelete;
    private TrackedEntityInstance trackedEntityInstanceA;
    private TrackedEntityInstance trackedEntityInstanceB;
    private RelationshipType relationshipType;
    private Relationship relationship;

    @Before
    public void setUp() {
        trackedEntityInstanceStoreMock = mock(ITrackedEntityInstanceStore.class);
        relationshipStoreMock = mock(IRelationshipStore.class);
        stateStoreMock = mock(IStateStore.class);

        trackedEntityInstanceMock = mock(TrackedEntityInstance.class);
        trackedEntityInstanceToPost = mock(TrackedEntityInstance.class);
        trackedEntityInstanceToSync = mock(TrackedEntityInstance.class);
        trackedEntityInstanceToUpdate = mock(TrackedEntityInstance.class);
        trackedEntityInstanceToDelete = mock(TrackedEntityInstance.class);

        trackedEntityInstanceA = mock(TrackedEntityInstance.class);
        trackedEntityInstanceB = mock(TrackedEntityInstance.class);
        relationshipType = new RelationshipType();
        relationshipType.setUId(RELATIONSHIP_TYPE_UID);


        trackedEntity = new TrackedEntity();
        trackedEntity.setUId(TRACKED_ENTITY_UID);

        organisationUnit = new OrganisationUnit();
        organisationUnit.setUId(ORGANISATION_UNIT_UID);

        relationship = new Relationship();
        relationship.setRelationship(RELATIONSHIP_TYPE_UID);
        relationship.setTrackedEntityInstanceA(trackedEntityInstanceA);
        relationship.setTrackedEntityInstanceB(trackedEntityInstanceB);

        when(stateStoreMock.queryActionForModel(trackedEntityInstanceToSync)).thenReturn(Action.SYNCED);
        when(stateStoreMock.queryActionForModel(trackedEntityInstanceToPost)).thenReturn(Action.TO_POST);
        when(stateStoreMock.queryActionForModel(trackedEntityInstanceToUpdate)).thenReturn(Action.TO_UPDATE);
        when(stateStoreMock.queryActionForModel(trackedEntityInstanceToDelete)).thenReturn(Action.TO_DELETE);
        when(stateStoreMock.saveActionForModel(any(TrackedEntityInstance.class), any(Action.class))).thenReturn(true);

        when(trackedEntityInstanceStoreMock.query(TRACKED_ENTITY_INSTANCE_ID_MOCK)).thenReturn(trackedEntityInstanceMock);
        when(trackedEntityInstanceStoreMock.query(TRACKED_ENTITY_INSTANCE_UID_MOCK)).thenReturn(trackedEntityInstanceMock);
        when(trackedEntityInstanceStoreMock.delete(any(TrackedEntityInstance.class))).thenReturn(true);

        trackedEntityInstanceService = new TrackedEntityInstanceService(trackedEntityInstanceStoreMock, relationshipStoreMock, stateStoreMock);
    }

    @Test
    public void testGetTrackedEntityInstanceByLongId() {
        TrackedEntityInstance trackedEntityInstance = trackedEntityInstanceStoreMock.query(TRACKED_ENTITY_INSTANCE_ID_MOCK);
        assertEquals(trackedEntityInstance, trackedEntityInstanceMock);
    }

    @Test
    public void testGetTrackedEntityInstanceByStringUID() {
        TrackedEntityInstance trackedEntityInstance = trackedEntityInstanceStoreMock.query(TRACKED_ENTITY_INSTANCE_UID_MOCK);
        assertEquals(trackedEntityInstance, trackedEntityInstanceMock);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSaveNullEvent() {
        trackedEntityInstanceService.save(null);
    }

    @Test
    public void testSaveUnsavedTrackedEntityInstance() {
        when(trackedEntityInstanceStoreMock.save(trackedEntityInstanceMock)).thenReturn(true);
        assertTrue(trackedEntityInstanceService.save(trackedEntityInstanceMock));
        verify(trackedEntityInstanceStoreMock).save(trackedEntityInstanceMock);
        verify(stateStoreMock).saveActionForModel(trackedEntityInstanceMock, Action.TO_POST);
    }

    @Test
    public void testSavePreviouslyAddedToPostTrackedEntityInstance() {
        when(trackedEntityInstanceStoreMock.save(trackedEntityInstanceMock)).thenReturn(true);

        assertTrue(trackedEntityInstanceService.save(trackedEntityInstanceMock));
        verify(trackedEntityInstanceStoreMock).save(trackedEntityInstanceMock);
        verify(stateStoreMock).saveActionForModel(trackedEntityInstanceMock, Action.TO_POST);
    }

    @Test
    public void testSavePreviouslyAddedToUpdateTrackedEntityInstance() {
        when(trackedEntityInstanceStoreMock.save(trackedEntityInstanceToUpdate)).thenReturn(true);
        assertTrue(trackedEntityInstanceService.save(trackedEntityInstanceToUpdate));
        verify(trackedEntityInstanceStoreMock).save(trackedEntityInstanceToUpdate);
        verify(stateStoreMock).saveActionForModel(trackedEntityInstanceToUpdate, Action.TO_UPDATE);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRemoveNullTrackedEntityInstance() {
        trackedEntityInstanceService.remove(null);
    }

    @Test
    public void testRemoveNotPreviouslySavedTrackedEntityInstance() {

        assertFalse(trackedEntityInstanceService.remove(trackedEntityInstanceToDelete));
        verify(trackedEntityInstanceStoreMock).delete(trackedEntityInstanceToDelete);
    }

    @Test
    public void testRemovePreviouslySavedTrackedEntityInstance() {
        when(trackedEntityInstanceStoreMock.delete(trackedEntityInstanceMock)).thenReturn(true);
        when(stateStoreMock.deleteActionForModel(trackedEntityInstanceMock)).thenReturn(true);
        assertTrue(trackedEntityInstanceService.remove(trackedEntityInstanceMock));
        verify(trackedEntityInstanceStoreMock).delete(trackedEntityInstanceMock);
        verify(stateStoreMock).deleteActionForModel(trackedEntityInstanceMock);
    }


    @Test
    public void testGetSyncedTrackedEntityInstanceById() {
        when(stateStoreMock.queryActionForModel(trackedEntityInstanceMock)).thenReturn(Action.SYNCED);
        assertTrue(trackedEntityInstanceMock.equals(trackedEntityInstanceService.get(TRACKED_ENTITY_INSTANCE_UID_MOCK)));
        verify(trackedEntityInstanceStoreMock).query(TRACKED_ENTITY_INSTANCE_UID_MOCK);
    }

    @Test
    public void testGetToPostTrackedEntityInstanceById() {
        when(stateStoreMock.queryActionForModel(trackedEntityInstanceMock)).thenReturn(Action.TO_POST);
        assertTrue(trackedEntityInstanceMock.equals(trackedEntityInstanceService.get(TRACKED_ENTITY_INSTANCE_UID_MOCK)));
        verify(trackedEntityInstanceStoreMock).query(TRACKED_ENTITY_INSTANCE_UID_MOCK);
    }


    @Test
    public void testGetToUpdateTrackedEntityInstanceById() {
        when(stateStoreMock.queryActionForModel(trackedEntityInstanceMock)).thenReturn(Action.TO_UPDATE);
        assertTrue(trackedEntityInstanceMock.equals(trackedEntityInstanceService.get(TRACKED_ENTITY_INSTANCE_UID_MOCK)));
        verify(trackedEntityInstanceStoreMock).query(TRACKED_ENTITY_INSTANCE_UID_MOCK);
    }

    @Test
    public void testGetToDeleteTrackedEntityInstanceById() {
        when(stateStoreMock.queryActionForModel(trackedEntityInstanceMock)).thenReturn(Action.TO_DELETE);
        assertTrue(null == trackedEntityInstanceService.get(TRACKED_ENTITY_INSTANCE_UID_MOCK));
        verify(trackedEntityInstanceStoreMock).query(TRACKED_ENTITY_INSTANCE_UID_MOCK);
    }

    @Test
    public void testGetTrackedEntityInstanceByIdThatDoesntExistInDatabase() {
        assertTrue(null == trackedEntityInstanceService.get(INVALID_TRACKED_ENTITY_INSTANCE_ID));
        verify(trackedEntityInstanceStoreMock).query(INVALID_TRACKED_ENTITY_INSTANCE_ID);
    }

    @Test
    public void testGetToPostTrackedEntityInstanceByUid() {
        when(stateStoreMock.queryActionForModel(trackedEntityInstanceMock)).thenReturn(Action.TO_POST);
        assertTrue(trackedEntityInstanceMock.equals(trackedEntityInstanceService.get(TRACKED_ENTITY_INSTANCE_UID_MOCK)));
        verify(trackedEntityInstanceStoreMock).query(TRACKED_ENTITY_INSTANCE_UID_MOCK);
    }

    @Test
    public void testGetToUpdateTrackedEntityInstanceByUid() {
        when(stateStoreMock.queryActionForModel(trackedEntityInstanceMock)).thenReturn(Action.TO_UPDATE);
        assertTrue(trackedEntityInstanceMock.equals(trackedEntityInstanceService.get(TRACKED_ENTITY_INSTANCE_UID_MOCK)));
        verify(trackedEntityInstanceStoreMock).query(TRACKED_ENTITY_INSTANCE_UID_MOCK);
    }

    @Test
    public void testGetSyncedTrackedEntityInstanceByUid() {
        when(stateStoreMock.queryActionForModel(trackedEntityInstanceMock)).thenReturn(Action.SYNCED);
        assertTrue(trackedEntityInstanceMock.equals(trackedEntityInstanceService.get(TRACKED_ENTITY_INSTANCE_UID_MOCK)));
        verify(trackedEntityInstanceStoreMock).query(TRACKED_ENTITY_INSTANCE_UID_MOCK);
    }

    @Test
    public void testGetToDeleteTrackedEntityInstanceByUid() {
        when(stateStoreMock.queryActionForModel(trackedEntityInstanceMock)).thenReturn(Action.TO_DELETE);
        assertTrue(null == trackedEntityInstanceService.get(TRACKED_ENTITY_INSTANCE_UID_MOCK));
        verify(trackedEntityInstanceStoreMock).query(TRACKED_ENTITY_INSTANCE_UID_MOCK);
    }

    @Test
    public void testGetTrackedEntityInstanceByUidThatDoesntExistInDatabase() {
        assertTrue(null == trackedEntityInstanceService.get(INVALID_TRACKED_ENTITY_INSTANCE_ID));
        verify(trackedEntityInstanceStoreMock).query(INVALID_TRACKED_ENTITY_INSTANCE_ID);
    }

    @Test
    public void testListTrackedEntityInstances() {
        List<TrackedEntityInstance> trackedEntityInstancesList = new ArrayList<>();
        trackedEntityInstancesList.add(trackedEntityInstanceToUpdate);
        trackedEntityInstancesList.add(trackedEntityInstanceToSync);
        trackedEntityInstancesList.add(trackedEntityInstanceToPost);
        when(stateStoreMock.queryModelsWithActions(TrackedEntityInstance.class, Action.SYNCED, Action.TO_UPDATE, Action.TO_POST))
                .thenReturn(trackedEntityInstancesList);
        assertTrue(trackedEntityInstancesList.equals(trackedEntityInstanceService.list()));
    }

    @Test
    public void testCreateTrackedEntityInstance() {
        when(trackedEntityInstanceStoreMock.insert(any(TrackedEntityInstance.class))).thenReturn(true);
        String trackedEntityInstanceMockUId = "Yx1u6op9s";

        mockStatic(CodeGenerator.class);
        given(CodeGenerator.generateCode()).willReturn(trackedEntityInstanceMockUId);

        TrackedEntityInstance trackedEntityInstance = trackedEntityInstanceService.create(trackedEntity, organisationUnit);
        verifyStatic();
        CodeGenerator.generateCode();

        assertTrue(organisationUnit.getUId().equals(trackedEntityInstance.getOrgUnit()));
        assertTrue(trackedEntity.getUId().equals(trackedEntityInstance.getTrackedEntity()));
        assertTrue(trackedEntityInstanceMockUId.equals(trackedEntityInstance.getTrackedEntityInstanceUid()));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateTrackedEntityInstanceWithOrganisationUnitWithNullTrackedEntityReference() {
        trackedEntityInstanceService.create(null, organisationUnit);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateTrackedEntityInstanceWithTrackedEntityWithNullOrganisationUnitReference() {
        trackedEntityInstanceService.create(trackedEntity, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateTrackedEntityInstanceWithNullTrackedEntityReferenceWithNullOrganisationUnitReference() {
        trackedEntityInstanceService.create(null, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddRelationshipWithNullTrackedEntityInstanceAReference() {
        trackedEntityInstanceService.addRelationship(null, trackedEntityInstanceB, relationshipType);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddRelationshipWithNullTrackedEntityInstanceBReference() {
        trackedEntityInstanceService.addRelationship(trackedEntityInstanceA, null, relationshipType);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddRelationshipWithNullRelationshipReference() {
        trackedEntityInstanceService.addRelationship(trackedEntityInstanceA, trackedEntityInstanceB, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddRelationshipWithNullTrackedEntityInstanceANullTrackedEntityInstanceBReference() {
        trackedEntityInstanceService.addRelationship(null, null, relationshipType);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddRelationshipWithNullTrackedEntityInstanceANullRelationshipTypeReference() {
        trackedEntityInstanceService.addRelationship(trackedEntityInstanceA, null, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddRelationshipWithNullTrackedEntityInstanceBNullRelationshipTypeReference() {
        trackedEntityInstanceService.addRelationship(null, trackedEntityInstanceB, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddRelationshipWithNullTrackedEntityInstanceANullTrackedEntityInstanceBNullRelationshipTypeReference() {
        trackedEntityInstanceService.addRelationship(null, null, null);
    }

    @Test
    public void testAddRelationship() {
        assertTrue(trackedEntityInstanceService.addRelationship(trackedEntityInstanceA, trackedEntityInstanceB, relationshipType));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRemoveNullRelationship() {
        trackedEntityInstanceService.removeRelationship(null);
    }

    @Test
    public void testRemoveRelationship() {
        assertTrue(trackedEntityInstanceService.removeRelationship(relationship));
    }

    @Test
    public void testGetTrackedEntityInstanceToDelete() {
        when(stateStoreMock.queryActionForModel(trackedEntityInstanceMock)).thenReturn(Action.TO_DELETE);
        assertTrue(null == trackedEntityInstanceService.get(TRACKED_ENTITY_INSTANCE_ID_MOCK));
    }

    @Test
    public void testRemoveTrackedEntityInstanceReturnFalse() {
        when(trackedEntityInstanceStoreMock.delete(trackedEntityInstanceMock)).thenReturn(false);
        assertFalse(trackedEntityInstanceService.remove(trackedEntityInstanceMock));
    }

    @Test
    public void testSaveTrackedEntityInstanceReturnFalse() {
        when(trackedEntityInstanceStoreMock.save(trackedEntityInstanceMock)).thenReturn(false);
        assertFalse(trackedEntityInstanceService.save(trackedEntityInstanceMock));
    }

    @Test
    public void testAddRelationshipReturnFalse() {
        TrackedEntityInstance trackedEntityInstanceBRelationship = new TrackedEntityInstance();
        trackedEntityInstanceBRelationship.setTrackedEntityInstanceUid(TRACKED_ENTITY_INSTANCE_UID_MOCK);
        relationship.setTrackedEntityInstanceB(trackedEntityInstanceBRelationship);
        relationship.setTrackedEntityInstanceB(trackedEntityInstanceBRelationship.getTrackedEntityInstanceUid());
        relationship.setRelationship(relationshipType.getUId());
        when(trackedEntityInstanceA.getRelationships()).thenReturn(Arrays.asList(relationship));
        assertFalse(trackedEntityInstanceService.addRelationship(trackedEntityInstanceA, trackedEntityInstanceBRelationship, relationshipType));
    }

    @Test
    public void testAddRelationshipWithoutExistingRelationships() {
        when(trackedEntityInstanceA.getRelationships()).thenReturn(new ArrayList<Relationship>());
        assertTrue(trackedEntityInstanceService.addRelationship(trackedEntityInstanceA, trackedEntityInstanceB, relationshipType));
    }
}
