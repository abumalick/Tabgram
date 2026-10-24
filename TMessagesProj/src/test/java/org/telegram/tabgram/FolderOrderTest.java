package org.telegram.tabgram;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FolderOrderTest {

    private static List<Integer> apply(List<Integer> remote, int localDefaultIndex) {
        ArrayList<Integer> order = new ArrayList<>(remote);
        FolderOrder.moveDefault(order, localDefaultIndex);
        return order;
    }

    @Test
    public void restoresLocalPositionOfDefaultFolder() {
        assertEquals(Arrays.asList(2, 0, 114, 157), apply(Arrays.asList(0, 2, 114, 157), 1));
        assertEquals(Arrays.asList(2, 114, 0, 157), apply(Arrays.asList(0, 2, 114, 157), 2));
    }

    @Test
    public void clampsToTheEndWhenFoldersWereRemoved() {
        assertEquals(Arrays.asList(2, 114, 0), apply(Arrays.asList(0, 2, 114), 7));
    }

    @Test
    public void leavesOrderAloneWhenDefaultIsFirstOrUnknown() {
        assertEquals(Arrays.asList(0, 2, 114), apply(Arrays.asList(0, 2, 114), 0));
        assertEquals(Arrays.asList(0, 2, 114), apply(Arrays.asList(0, 2, 114), -1));
        assertEquals(Arrays.asList(2, 114), apply(Arrays.asList(2, 114), 1));
    }

    @Test
    public void keepsServerOrderOfOtherFolders() {
        assertEquals(Arrays.asList(114, 0, 2), apply(Arrays.asList(114, 0, 2), 1));
        assertEquals(Arrays.asList(114, 2, 0), apply(Arrays.asList(0, 114, 2), 2));
    }
}
