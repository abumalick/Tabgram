package org.telegram.tabgram;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class TabletPanesTest {

    @Test
    public void archiveStaysLeftAndChatMoves() {
        assertEquals(2, TabletPanes.firstMovedIndex(new boolean[]{false, true, false}));
    }

    @Test
    public void chatOnlyMovesFromIndexOne() {
        assertEquals(1, TabletPanes.firstMovedIndex(new boolean[]{false, false}));
    }

    @Test
    public void consecutiveListsStayLeft() {
        assertEquals(3, TabletPanes.firstMovedIndex(new boolean[]{false, true, true, false}));
    }

    @Test
    public void nothingMovesWhenAllListsStay() {
        assertEquals(2, TabletPanes.firstMovedIndex(new boolean[]{false, true}));
    }

    @Test
    public void listAfterChatStillMovesToKeepOrder() {
        assertEquals(1, TabletPanes.firstMovedIndex(new boolean[]{false, false, true}));
    }

    @Test
    public void singleFragmentHasNothingToMove() {
        assertEquals(1, TabletPanes.firstMovedIndex(new boolean[]{false}));
    }
}
