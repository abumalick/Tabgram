package org.telegram.tabgram;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.tgnet.TLRPC;

import java.util.ArrayList;
import java.util.HashSet;

public final class ContactJoined {

    private static final HashSet<String> markedRead = new HashSet<>();

    private ContactJoined() {}

    public static boolean isEnabled(int account) {
        return MessagesController.getNotificationsSettings(account).getBoolean("EnableContactJoined", true);
    }

    public static boolean suppress(boolean enabledPref, boolean silent, boolean isJoinAction) {
        return isJoinAction && (silent || !enabledPref);
    }

    public static boolean suppress(int account, TLRPC.Message message) {
        return isJoin(message) && suppress(isEnabled(account), message.silent, true);
    }

    // A pinned chat stays visible: pinning or reordering rebuilds the server's pinned list from
    // the visible one, which would unpin it.
    public static boolean hideDialog(boolean enabledPref, long dialogId, boolean pinned, TLRPC.Message topMessage) {
        return !enabledPref && dialogId > 0 && !pinned && isJoin(topMessage);
    }

    public static boolean needsMarkRead(int unreadCount, boolean unreadMark) {
        return unreadCount > 0 || unreadMark;
    }

    // Called while MessagesController sorts its dialog lists, and when a dialog's unread count
    // arrives after the sort: a hidden chat is kept in allDialogs and on the server, only left out
    // of the visible lists, and marked read so it adds nothing to unread counters.
    public static boolean hideDialog(MessagesController controller, int account, TLRPC.Dialog dialog) {
        if (!(dialog instanceof TLRPC.TL_dialog)) {
            return false;
        }
        ArrayList<MessageObject> messages = controller.dialogMessage.get(dialog.id);
        TLRPC.Message top = messages == null || messages.isEmpty() || messages.get(0) == null ? null : messages.get(0).messageOwner;
        if (!hideDialog(controller.enableJoined, dialog.id, dialog.pinned, top)) {
            return false;
        }
        if (needsMarkRead(dialog.unread_count, dialog.unread_mark) && markedRead.add(account + ":" + dialog.id + ":" + dialog.top_message)) {
            final long dialogId = dialog.id;
            final int topMessage = dialog.top_message;
            final int date = dialog.last_message_date;
            AndroidUtilities.runOnUIThread(() -> controller.markDialogAsRead(dialogId, topMessage, topMessage, date, false, 0, 0, true, 0));
        }
        return true;
    }

    public static void onSwitchChanged(int account) {
        MessagesController.getInstance(account).sortDialogs(null);
        NotificationCenter.getInstance(account).postNotificationName(NotificationCenter.dialogsNeedReload);
    }

    private static boolean isJoin(TLRPC.Message message) {
        return message != null && (message.action instanceof TLRPC.TL_messageActionContactSignUp || message.action instanceof TLRPC.TL_messageActionUserJoined);
    }
}
