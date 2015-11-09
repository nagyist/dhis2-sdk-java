package org.hisp.dhis.java.sdk.interpretation;

import org.hisp.dhis.java.sdk.models.common.state.Action;
import org.hisp.dhis.java.sdk.models.interpretation.Interpretation;
import org.hisp.dhis.java.sdk.models.interpretation.InterpretationComment;
import org.hisp.dhis.java.sdk.models.user.User;
import org.hisp.dhis.java.sdk.common.IStateStore;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class InterpretationCommentServiceTest {
    private IStateStore stateStoreMock;
    private IInterpretationCommentStore interpretationCommentStoreMock;
    private IInterpretationCommentService interpretationCommentService;

    private InterpretationComment interpretationComment;
    private Interpretation interpretation;
    private User user;

    @Before
    public void setUp() {
        stateStoreMock = mock(IStateStore.class);
        interpretationCommentStoreMock = mock(IInterpretationCommentStore.class);
        interpretationCommentService = new InterpretationCommentService(
                interpretationCommentStoreMock, stateStoreMock);

        interpretationComment = new InterpretationComment();
        interpretation = new Interpretation();
        user = new User();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRemoveNullInterpretationComment() {
        interpretationCommentService.remove(null);
    }

    @Test
    public void testRemoveNotExistingInterpretationComment() {
        when(stateStoreMock.queryActionForModel(interpretationComment)).thenReturn(null);

        boolean status = interpretationCommentService.remove(interpretationComment);

        assertFalse(status);
        verify(stateStoreMock, times(1)).queryActionForModel(interpretationComment);
    }

    @Test
    public void testRemoveInterpretationCommentWithStateToPost() {
        when(stateStoreMock.queryActionForModel(interpretationComment)).thenReturn(Action.TO_POST);
        when(interpretationCommentStoreMock.delete(interpretationComment)).thenReturn(true);

        boolean status = interpretationCommentService.remove(interpretationComment);

        assertTrue(status);
        verify(stateStoreMock, times(1)).queryActionForModel(interpretationComment);
        verify(interpretationCommentStoreMock, times(1)).delete(interpretationComment);
    }

    @Test
    public void testRemoveInterpretationCommentWWithStateToUpdate() {
        when(stateStoreMock.queryActionForModel(interpretationComment)).thenReturn(Action.TO_UPDATE);
        when(stateStoreMock.saveActionForModel(interpretationComment, Action.TO_DELETE)).thenReturn(true);

        boolean status = interpretationCommentService.remove(interpretationComment);

        assertTrue(status);
        verify(stateStoreMock, times(1)).queryActionForModel(interpretationComment);
        verify(stateStoreMock, times(1)).saveActionForModel(interpretationComment, Action.TO_DELETE);
    }

    @Test
    public void testRemoveInterpretationCommentWithStateSynced() {
        when(stateStoreMock.queryActionForModel(interpretationComment)).thenReturn(Action.SYNCED);
        when(stateStoreMock.saveActionForModel(interpretationComment, Action.TO_DELETE)).thenReturn(true);

        boolean status = interpretationCommentService.remove(interpretationComment);

        assertTrue(status);
        verify(stateStoreMock, times(1)).queryActionForModel(interpretationComment);
        verify(stateStoreMock, times(1)).saveActionForModel(interpretationComment, Action.TO_DELETE);
    }

    @Test
    public void testRemoveInterpretationCommentWithStateToDelete() {
        when(stateStoreMock.queryActionForModel(interpretationComment)).thenReturn(Action.TO_DELETE);

        boolean status = interpretationCommentService.remove(interpretationComment);

        assertFalse(status);
        verify(stateStoreMock, times(1)).queryActionForModel(interpretationComment);
        verify(stateStoreMock, never()).saveActionForModel(interpretationComment, Action.TO_DELETE);
    }


    @Test(expected = IllegalArgumentException.class)
    public void testSaveNullInterpretationComment() {
        interpretationCommentService.save(null);
    }

    @Test
    public void testSaveInterpretationCommentWithoutState() {
        when(stateStoreMock.queryActionForModel(interpretationComment)).thenReturn(null);
        when(stateStoreMock.saveActionForModel(interpretationComment, Action.TO_POST)).thenReturn(true);
        when(interpretationCommentStoreMock.queryById(anyInt())).thenReturn(null);
        when(interpretationCommentStoreMock.save(interpretationComment)).thenReturn(true);

        boolean status = interpretationCommentService.save(interpretationComment);

        assertTrue(status);
        verify(stateStoreMock, times(1)).queryActionForModel(interpretationComment);
        verify(stateStoreMock, times(1)).saveActionForModel(interpretationComment, Action.TO_POST);
        verify(interpretationCommentStoreMock, times(1)).save(interpretationComment);
    }

    @Test
    public void testSaveInterpretationCommentWithStateToPost() {
        when(stateStoreMock.queryActionForModel(interpretationComment)).thenReturn(Action.TO_POST);
        when(interpretationCommentStoreMock.queryById(anyInt())).thenReturn(interpretationComment);
        when(interpretationCommentStoreMock.save(interpretationComment)).thenReturn(true);

        boolean status = interpretationCommentService.save(interpretationComment);

        assertTrue(status);
        verify(stateStoreMock, times(1)).queryActionForModel(interpretationComment);
        verify(interpretationCommentStoreMock, times(1)).save(interpretationComment);
    }

    @Test
    public void testSaveInterpretationCommentWithStateToUpdate() {
        when(stateStoreMock.queryActionForModel(interpretationComment)).thenReturn(Action.TO_UPDATE);
        when(interpretationCommentStoreMock.queryById(anyInt())).thenReturn(interpretationComment);
        when(interpretationCommentStoreMock.save(interpretationComment)).thenReturn(true);

        boolean status = interpretationCommentService.save(interpretationComment);

        assertTrue(status);
        verify(stateStoreMock, times(1)).queryActionForModel(interpretationComment);
        verify(interpretationCommentStoreMock, times(1)).save(interpretationComment);
    }

    @Test
    public void testSaveInterpretationCommentWithStateSynced() {
        when(stateStoreMock.queryActionForModel(interpretationComment)).thenReturn(Action.SYNCED);
        when(stateStoreMock.saveActionForModel(interpretationComment, Action.TO_UPDATE)).thenReturn(true);
        when(interpretationCommentStoreMock.queryById(anyInt())).thenReturn(interpretationComment);
        when(interpretationCommentStoreMock.save(interpretationComment)).thenReturn(true);

        boolean status = interpretationCommentService.save(interpretationComment);

        assertTrue(status);
        verify(stateStoreMock, times(1)).queryActionForModel(interpretationComment);
        verify(stateStoreMock, times(1)).saveActionForModel(interpretationComment, Action.TO_UPDATE);
        verify(interpretationCommentStoreMock, times(1)).save(interpretationComment);
    }

    @Test
    public void testSaveInterpretationCommentWithStateToDelete() {
        when(stateStoreMock.queryActionForModel(interpretationComment)).thenReturn(Action.TO_DELETE);
        when(interpretationCommentStoreMock.queryById(anyInt())).thenReturn(interpretationComment);

        boolean status = interpretationCommentService.save(interpretationComment);

        assertFalse(status);
        verify(stateStoreMock, times(1)).queryActionForModel(interpretationComment);
    }

    @Test
    public void testSaveInterpretationCommentWithStoreFailingToSaveIt() {
        when(stateStoreMock.queryActionForModel(interpretationComment)).thenReturn(Action.SYNCED);
        when(stateStoreMock.saveActionForModel(interpretationComment, Action.TO_UPDATE)).thenReturn(true);
        when(interpretationCommentStoreMock.queryById(anyInt())).thenReturn(interpretationComment);
        when(interpretationCommentStoreMock.save(interpretationComment)).thenReturn(false);

        boolean status = interpretationCommentService.save(interpretationComment);

        assertFalse(status);
        verify(stateStoreMock, times(1)).queryActionForModel(interpretationComment);
        verify(interpretationCommentStoreMock, times(1)).save(interpretationComment);
        verify(stateStoreMock, never()).saveActionForModel(interpretationComment, Action.TO_UPDATE);
    }

    @Test
    public void testSaveNewInterpretationCommentStoreFailingToSaveIt() {
        when(stateStoreMock.queryActionForModel(interpretationComment)).thenReturn(null);
        when(stateStoreMock.saveActionForModel(interpretationComment, Action.TO_POST)).thenReturn(true);
        when(interpretationCommentStoreMock.queryById(anyInt())).thenReturn(interpretationComment);
        when(interpretationCommentStoreMock.save(interpretationComment)).thenReturn(false);

        boolean status = interpretationCommentService.save(interpretationComment);

        assertFalse(status);
        verify(stateStoreMock, times(1)).queryActionForModel(interpretationComment);
        verify(interpretationCommentStoreMock, times(1)).save(interpretationComment);
        verify(stateStoreMock, never()).saveActionForModel(interpretationComment, Action.TO_POST);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateInterpretationCommentWithNullInterpretation() {
        interpretationCommentService.create(null, user, "Some comment");
    }


    @Test(expected = IllegalArgumentException.class)
    public void testCreateInterpretationCommentWithNullUser() {
        interpretationCommentService.create(interpretation, null, "Some comment");
    }


    @Test(expected = IllegalArgumentException.class)
    public void testCreateInterpretationCommentWithNullText() {
        interpretationCommentService.create(interpretation, user, null);
    }

    @Test
    public void testCreateInterpretationCommentReturnsValidObject() {
        final String text = "SomeComment";

        InterpretationComment interpretationComment = interpretationCommentService
                .create(interpretation, user, text);

        assertNotNull(interpretationComment.getUId());
        assertNotNull(interpretationComment.getAccess());
        assertNotNull(interpretationComment.getCreated());
        assertNotNull(interpretationComment.getLastUpdated());

        assertEquals(interpretationComment.getName(), text);
        assertEquals(interpretationComment.getDisplayName(), text);
        assertEquals(interpretationComment.getText(), text);
        assertEquals(interpretationComment.getUser(), user);
        assertEquals(interpretationComment.getInterpretation(), interpretation);
    }
}
