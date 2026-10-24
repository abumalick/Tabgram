package org.telegram.tabgram;

import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.DialogsActivity;

import java.util.List;

public final class TabletPanes {

    private TabletPanes() {}

    public static int firstMovedIndex(boolean[] staysLeft) {
        int index = 1;
        while (index < staysLeft.length && staysLeft[index]) {
            index++;
        }
        return Math.max(index, 1);
    }

    public static int firstMovedIndex(List<BaseFragment> stack) {
        boolean[] staysLeft = new boolean[stack.size()];
        for (int i = 0; i < staysLeft.length; i++) {
            BaseFragment fragment = stack.get(i);
            staysLeft[i] = fragment instanceof DialogsActivity && (((DialogsActivity) fragment).isArchive() || ((DialogsActivity) fragment).isCommunity());
        }
        return firstMovedIndex(staysLeft);
    }
}
