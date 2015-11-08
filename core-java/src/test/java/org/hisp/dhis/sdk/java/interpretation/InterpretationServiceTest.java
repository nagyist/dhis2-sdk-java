package org.hisp.dhis.sdk.java.interpretation;

import org.hisp.dhis.java.sdk.models.common.state.Action;
import org.hisp.dhis.java.sdk.models.interpretation.Interpretation;
import org.hisp.dhis.sdk.java.common.IStateStore;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class InterpretationServiceTest {
    private IInterpretationStore interpretationStoreMock;
    private IStateStore stateStoreMock;
    private IInterpretationElementService interpretationElementServiceMock;
    private IInterpretationService interpretationService;
    private Interpretation interpretation;

    @Before
    public void setUp() {
        interpretationStoreMock = mock(IInterpretationStore.class);
        interpretationElementServiceMock = mock(IInterpretationElementService.class);
        stateStoreMock = mock(IStateStore.class);

        interpretationService = new InterpretationService(interpretationStoreMock,
                stateStoreMock, interpretationElementServiceMock);
        interpretation = new Interpretation();
    }


    @Test(expected = IllegalArgumentException.class)
    public void testRemoveNullDashboard() {
        interpretationService.remove(null);
    }

    @Test
    public void testRemoveInterpretationWhichDoesNotExist() {
        when(stateStoreMock.queryActionForModel(any(Interpretation.class))).thenReturn(null);

        boolean status = interpretationService.remove(interpretation);

        assertFalse(status);
        verify(stateStoreMock, never()).saveActionForModel(interpretation, Action.TO_DELETE);
    }

    @Test
    public void testRemoveInterpretationWithStateToPost() {
        when(interpretationStoreMock.delete(interpretation)).thenReturn(true);
        when(stateStoreMock.queryActionForModel(interpretation)).thenReturn(Action.TO_POST);

        boolean status = interpretationService.remove(interpretation);

        assertTrue(status);
        verify(stateStoreMock, times(1)).queryActionForModel(interpretation);
        verify(stateStoreMock, never()).saveActionForModel(interpretation, Action.TO_DELETE);
        verify(interpretationStoreMock, times(1)).delete(interpretation);
    }

    @Test
    public void testRemoveInterpretationWithStateToUpdate() {
        when(stateStoreMock.queryActionForModel(interpretation)).thenReturn(Action.TO_UPDATE);
        when(stateStoreMock.saveActionForModel(interpretation, Action.TO_DELETE)).thenReturn(true);

        boolean status = interpretationService.remove(interpretation);

        assertTrue(status);
        verify(stateStoreMock, times(1)).queryActionForModel(interpretation);
        verify(stateStoreMock, times(1)).saveActionForModel(interpretation, Action.TO_DELETE);
    }

    @Test
    public void testRemoveInterpretationWithStateSynced() {
        when(stateStoreMock.queryActionForModel(interpretation)).thenReturn(Action.SYNCED);
        when(stateStoreMock.saveActionForModel(interpretation, Action.TO_DELETE)).thenReturn(true);

        boolean status = interpretationService.remove(interpretation);

        assertTrue(status);
        verify(stateStoreMock, times(1)).queryActionForModel(interpretation);
        verify(stateStoreMock, times(1)).saveActionForModel(interpretation, Action.TO_DELETE);
    }


    @Test
    public void testRemoveInterpretationWithStateToDelete() {
        when(stateStoreMock.queryActionForModel(interpretation)).thenReturn(Action.TO_DELETE);
        when(stateStoreMock.saveActionForModel(interpretation, Action.TO_DELETE)).thenReturn(true);

        boolean status = interpretationService.remove(interpretation);

        assertFalse(status);
        verify(stateStoreMock, times(1)).queryActionForModel(interpretation);
        verify(stateStoreMock, never()).saveActionForModel(interpretation, Action.TO_DELETE);
    }


    @Test(expected = IllegalArgumentException.class)
    public void testSaveNullInterpretation() {
        interpretationService.save(null);
    }

    @Test
    public void testSaveInterpretationWithoutState() {
        when(stateStoreMock.queryActionForModel(interpretation)).thenReturn(null);
        when(stateStoreMock.saveActionForModel(interpretation, Action.TO_POST)).thenReturn(true);
        when(interpretationStoreMock.queryById(anyInt())).thenReturn(null);
        when(interpretationStoreMock.save(interpretation)).thenReturn(true);

        boolean status = interpretationService.save(interpretation);

        assertTrue(status);
        verify(stateStoreMock, times(1)).queryActionForModel(interpretation);
        verify(stateStoreMock, times(1)).saveActionForModel(interpretation, Action.TO_POST);
        verify(interpretationStoreMock, times(1)).save(interpretation);
    }

    @Test
    public void testSaveInterpretationWithStateToPost() {
        when(stateStoreMock.queryActionForModel(interpretation)).thenReturn(Action.TO_POST);
        when(interpretationStoreMock.queryById(anyInt())).thenReturn(interpretation);
        when(interpretationStoreMock.save(interpretation)).thenReturn(true);

        boolean status = interpretationService.save(interpretation);

        assertTrue(status);
        verify(stateStoreMock, times(1)).queryActionForModel(interpretation);
        verify(interpretationStoreMock, times(1)).save(interpretation);
    }

    @Test
    public void testSaveInterpretationWithStateToUpdate() {
        when(stateStoreMock.queryActionForModel(interpretation)).thenReturn(Action.TO_UPDATE);
        when(interpretationStoreMock.queryById(anyInt())).thenReturn(interpretation);
        when(interpretationStoreMock.save(interpretation)).thenReturn(true);

        boolean status = interpretationService.save(interpretation);

        assertTrue(status);
        verify(stateStoreMock, times(1)).queryActionForModel(interpretation);
        verify(interpretationStoreMock, times(1)).save(interpretation);
    }

    @Test
    public void testSaveInterpretationWithStateSynced() {
        when(stateStoreMock.queryActionForModel(interpretation)).thenReturn(Action.SYNCED);
        when(stateStoreMock.saveActionForModel(interpretation, Action.TO_UPDATE)).thenReturn(true);
        when(interpretationStoreMock.queryById(anyInt())).thenReturn(interpretation);
        when(interpretationStoreMock.save(interpretation)).thenReturn(true);

        boolean status = interpretationService.save(interpretation);

        assertTrue(status);
        verify(stateStoreMock, times(1)).queryActionForModel(interpretation);
        verify(stateStoreMock, times(1)).saveActionForModel(interpretation, Action.TO_UPDATE);
        verify(interpretationStoreMock, times(1)).save(interpretation);
    }

    @Test
    public void testSaveInterpretationWithStateToDelete() {
        when(stateStoreMock.queryActionForModel(interpretation)).thenReturn(Action.TO_DELETE);
        when(interpretationStoreMock.queryById(anyInt())).thenReturn(interpretation);

        boolean status = interpretationService.save(interpretation);

        assertFalse(status);
        verify(stateStoreMock, times(1)).queryActionForModel(interpretation);
    }

    @Test
    public void testSaveInterpretationWithStoreFailingToSaveIt() {
        when(stateStoreMock.queryActionForModel(interpretation)).thenReturn(Action.SYNCED);
        when(stateStoreMock.saveActionForModel(interpretation, Action.TO_UPDATE)).thenReturn(true);
        when(interpretationStoreMock.queryById(anyInt())).thenReturn(interpretation);
        when(interpretationStoreMock.save(interpretation)).thenReturn(false);

        boolean status = interpretationService.save(interpretation);

        assertFalse(status);
        verify(stateStoreMock, times(1)).queryActionForModel(interpretation);
        verify(interpretationStoreMock, times(1)).save(interpretation);
        verify(stateStoreMock, never()).saveActionForModel(interpretation, Action.TO_UPDATE);
    }

    @Test
    public void testSaveNewInterpretationWithStoreFailingToSaveIt() {
        when(stateStoreMock.queryActionForModel(interpretation)).thenReturn(null);
        when(stateStoreMock.saveActionForModel(interpretation, Action.TO_POST)).thenReturn(true);
        when(interpretationStoreMock.queryById(anyInt())).thenReturn(interpretation);
        when(interpretationStoreMock.save(interpretation)).thenReturn(false);

        boolean status = interpretationService.save(interpretation);

        assertFalse(status);
        verify(stateStoreMock, times(1)).queryActionForModel(interpretation);
        verify(interpretationStoreMock, times(1)).save(interpretation);
        verify(stateStoreMock, never()).saveActionForModel(interpretation, Action.TO_POST);
    }
}
