package org.telegram.tabgram;

import org.telegram.messenger.MessagesController;

import java.util.List;

// The server accepts a new position for the default folder from a non-Premium account but
// keeps returning it first, so the remote order would undo the user's choice on every reload.
public final class FolderOrder {

    private FolderOrder() {}

    public static void keepLocalDefaultPosition(List<Integer> remoteOrder, List<MessagesController.DialogFilter> localFilters, boolean premium) {
        if (premium) {
            return;
        }
        int localDefaultIndex = -1;
        for (int i = 0; i < localFilters.size(); i++) {
            if (localFilters.get(i).isDefault()) {
                localDefaultIndex = i;
                break;
            }
        }
        moveDefault(remoteOrder, localDefaultIndex);
    }

    static void moveDefault(List<Integer> order, int localDefaultIndex) {
        int remoteIndex = order.indexOf(0);
        if (remoteIndex < 0 || localDefaultIndex < 0) {
            return;
        }
        order.remove(remoteIndex);
        order.add(Math.min(localDefaultIndex, order.size()), 0);
    }
}
