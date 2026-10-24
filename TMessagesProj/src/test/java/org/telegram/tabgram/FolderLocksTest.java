package org.telegram.tabgram;

import static org.junit.Assert.assertArrayEquals;

import org.junit.Test;

public class FolderLocksTest {

    @Test
    public void locksFromTheEndWhenDefaultIsFirst() {
        assertArrayEquals(new boolean[]{false, false, false, true}, FolderLocks.compute(new boolean[]{true, false, false, false}, 2));
    }

    @Test
    public void defaultFolderInTheMiddleIsNeverLocked() {
        assertArrayEquals(new boolean[]{false, true, false, true}, FolderLocks.compute(new boolean[]{false, false, true, false}, 1));
    }

    @Test
    public void defaultFolderAtTheEndIsNeverLocked() {
        assertArrayEquals(new boolean[]{false, true, true, false}, FolderLocks.compute(new boolean[]{false, false, false, true}, 1));
    }

    @Test
    public void locksNothingWhenWithinTheLimit() {
        assertArrayEquals(new boolean[]{false, false}, FolderLocks.compute(new boolean[]{false, true}, 5));
    }
}
