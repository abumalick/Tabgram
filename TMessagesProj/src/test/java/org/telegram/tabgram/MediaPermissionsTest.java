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

    @Test
    public void selectedPhotosCountAsGalleryAccessFromAndroid14() {
        assertTrue(MediaPermissions.hasSelectedAccess(34, true));
        assertTrue(MediaPermissions.hasSelectedAccess(36, true));
        assertFalse(MediaPermissions.hasSelectedAccess(36, false));
        assertFalse(MediaPermissions.hasSelectedAccess(33, true));
    }

    @Test
    public void limitedAccessIsPartialUntilFullAccessIsGranted() {
        assertTrue(MediaPermissions.isPartialAccess(34, true, false));
        assertTrue(MediaPermissions.isPartialAccess(36, true, false));
        assertFalse(MediaPermissions.isPartialAccess(36, true, true));
    }

    @Test
    public void noSelectionOrOldAndroidIsNotPartial() {
        assertFalse(MediaPermissions.isPartialAccess(36, false, false));
        assertFalse(MediaPermissions.isPartialAccess(33, true, false));
    }
}
