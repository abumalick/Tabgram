package org.telegram.tabgram;

public final class FolderLocks {

    private FolderLocks() {}

    public static boolean[] compute(boolean[] isDefaultInListOrder, int limitDefault) {
        int size = isDefaultInListOrder.length;
        boolean[] locked = new boolean[size];
        int toLock = size - 1 - limitDefault;
        for (int i = size - 1; i >= 0 && toLock > 0; i--) {
            if (!isDefaultInListOrder[i]) {
                locked[i] = true;
                toLock--;
            }
        }
        return locked;
    }
}
