package org.telegram.tabgram;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.telegram.tgnet.TLRPC;

public class ContactJoinedTest {

    @Test
    public void suppressesJoinWhenSwitchOffOrSilent() {
        assertFalse(ContactJoined.suppress(true, false, true));
        assertTrue(ContactJoined.suppress(false, false, true));
        assertTrue(ContactJoined.suppress(true, true, true));
        assertTrue(ContactJoined.suppress(false, true, true));
    }

    @Test
    public void neverSuppressesOtherActions() {
        assertFalse(ContactJoined.suppress(false, false, false));
        assertFalse(ContactJoined.suppress(false, true, false));
    }

    @Test
    public void hidesChatWhoseTopMessageIsAJoinWhileSwitchIsOff() {
        assertTrue(ContactJoined.hideDialog(false, 42, false, service(new TLRPC.TL_messageActionContactSignUp())));
        assertTrue(ContactJoined.hideDialog(false, 42, false, service(new TLRPC.TL_messageActionUserJoined())));
    }

    @Test
    public void showsChatWhileSwitchIsOn() {
        assertFalse(ContactJoined.hideDialog(true, 42, false, service(new TLRPC.TL_messageActionContactSignUp())));
    }

    @Test
    public void showsChatOnceItHasAnyOtherTopMessage() {
        TLRPC.Message text = new TLRPC.TL_message();
        text.message = "hi";
        assertFalse(ContactJoined.hideDialog(false, 42, false, text));
        assertFalse(ContactJoined.hideDialog(false, 42, false, service(new TLRPC.TL_messageActionPhoneCall())));
        assertFalse(ContactJoined.hideDialog(false, 42, false, null));
    }

    @Test
    public void neverHidesGroupsOrChannels() {
        assertFalse(ContactJoined.hideDialog(false, -42, false, service(new TLRPC.TL_messageActionUserJoined())));
    }

    @Test
    public void neverHidesPinnedChats() {
        assertFalse(ContactJoined.hideDialog(false, 42, true, service(new TLRPC.TL_messageActionContactSignUp())));
    }

    @Test
    public void marksHiddenChatReadOnlyWhenItIsUnread() {
        assertTrue(ContactJoined.needsMarkRead(1, false));
        assertTrue(ContactJoined.needsMarkRead(0, true));
        assertFalse(ContactJoined.needsMarkRead(0, false));
    }

    private static TLRPC.Message service(TLRPC.MessageAction action) {
        TLRPC.Message message = new TLRPC.TL_messageService();
        message.action = action;
        return message;
    }
}
