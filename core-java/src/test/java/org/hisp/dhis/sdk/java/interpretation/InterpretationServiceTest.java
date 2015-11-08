package org.hisp.dhis.sdk.java.interpretation;

import org.hisp.dhis.java.sdk.models.common.state.Action;
import org.hisp.dhis.java.sdk.models.dashboard.DashboardContent;
import org.hisp.dhis.java.sdk.models.dashboard.DashboardElement;
import org.hisp.dhis.java.sdk.models.dashboard.DashboardItem;
import org.hisp.dhis.java.sdk.models.interpretation.Interpretation;
import org.hisp.dhis.java.sdk.models.interpretation.InterpretationElement;
import org.hisp.dhis.java.sdk.models.user.User;
import org.hisp.dhis.sdk.java.common.IStateStore;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class InterpretationServiceTest {
    private IInterpretationStore interpretationStoreMock;
    private IStateStore stateStoreMock;
    private IInterpretationElementService interpretationElementServiceMock;

    private User user;
    private Interpretation interpretation;
    private InterpretationElement interpretationElement;
    private DashboardItem dashboardItem;
    private DashboardElement dashboardElement;

    private IInterpretationService interpretationService;

    @Before
    public void setUp() {
        interpretationStoreMock = mock(IInterpretationStore.class);
        interpretationElementServiceMock = mock(IInterpretationElementService.class);
        stateStoreMock = mock(IStateStore.class);

        user = new User();
        interpretation = new Interpretation();
        interpretationElement = new InterpretationElement();
        dashboardItem = new DashboardItem();
        dashboardElement = new DashboardElement();

        interpretationService = new InterpretationService(interpretationStoreMock,
                stateStoreMock, interpretationElementServiceMock);
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


    @Test(expected = IllegalArgumentException.class)
    public void testCreateInterpretationWithNullDashboardItem() {
        interpretationService.create(null, user, "Interpretation Text");
    }


    @Test(expected = IllegalArgumentException.class)
    public void testCreateInterpretationWithNullUser() {
        interpretationService.create(dashboardItem, null, "Interpretation text");
    }


    @Test(expected = IllegalArgumentException.class)
    public void testCreateInterpretationWithNullText() {
        interpretationService.create(dashboardItem, user, null);
    }

    @Test
    public void testCreateInterpretationCommentReturnsValidObject() {
        final String text = "SomeComment";
        dashboardItem.setType(DashboardContent.TYPE_CHART);
        dashboardItem.setChart(dashboardElement);

        when(interpretationElementServiceMock.create(any(Interpretation.class),
                any(DashboardElement.class), anyString())).thenReturn(interpretationElement);

        Interpretation interpretation = interpretationService
                .create(dashboardItem, user, text);

        assertNotNull(interpretation.getUId());
        assertNotNull(interpretation.getCreated());
        assertNotNull(interpretation.getLastUpdated());
        assertNotNull(interpretation.getAccess());
        assertNotNull(interpretation.getChart());

        assertEquals(interpretation.getName(), text);
        assertEquals(interpretation.getDisplayName(), text);
        assertEquals(interpretation.getText(), text);
        assertEquals(interpretation.getUser(), user);
        assertEquals(interpretation.getType(), DashboardContent.TYPE_CHART);
    }
}
