package org.telegram.tabgram;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class MediaPermissionsTest {

    @Test
    public void asksFullMediaBeforeLimitedAccessExists() {
        assertTrue(MediaPermissions.askFullMediaAtStartup(33, false));
        assertTrue(MediaPermissions.askFullMediaAtStartup(33, true));
    }

    @Test
    public void asksFullMediaWhenNoPartialAccessWasGranted() {
        assertTrue(MediaPermissions.askFullMediaAtStartup(36, false));
    }

    @Test
    public void skipsStartupRequestAfterLimitedAccess() {
        assertFalse(MediaPermissions.askFullMediaAtStartup(34, true));
        assertFalse(MediaPermissions.askFullMediaAtStartup(36, true));
    }
}
