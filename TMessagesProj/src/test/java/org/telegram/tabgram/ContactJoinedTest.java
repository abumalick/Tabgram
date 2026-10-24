package org.telegram.tabgram;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

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
}
