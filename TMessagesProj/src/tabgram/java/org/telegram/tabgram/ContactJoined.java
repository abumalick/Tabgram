package org.telegram.tabgram;

import org.telegram.messenger.MessagesController;
import org.telegram.tgnet.TLRPC;

public final class ContactJoined {

    private ContactJoined() {}

    public static boolean isEnabled(int account) {
        return MessagesController.getNotificationsSettings(account).getBoolean("EnableContactJoined", true);
    }

    public static boolean suppress(boolean enabledPref, boolean silent, boolean isJoinAction) {
        return isJoinAction && (silent || !enabledPref);
    }

    public static boolean suppress(int account, TLRPC.Message message) {
        boolean join = message.action instanceof TLRPC.TL_messageActionContactSignUp || message.action instanceof TLRPC.TL_messageActionUserJoined;
        return join && suppress(isEnabled(account), message.silent, true);
    }
}
