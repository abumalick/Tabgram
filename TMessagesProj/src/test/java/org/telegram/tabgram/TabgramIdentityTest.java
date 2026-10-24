package org.telegram.tabgram;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;

import org.junit.Test;
import org.telegram.messenger.BuildConfig;

public class TabgramIdentityTest {

    @Test
    public void usesOwnApiCredentials() {
        assertNotEquals(4, BuildConfig.UNOFFICIAL_APP_ID);
        assertNotEquals("014b35b6184100b085b0d0572f9b5103", BuildConfig.UNOFFICIAL_APP_HASH);
        assertFalse(BuildConfig.UNOFFICIAL_APP_HASH.isEmpty());
    }
}
