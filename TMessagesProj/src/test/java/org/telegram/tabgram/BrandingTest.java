package org.telegram.tabgram;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.telegram.messenger.R;

public class BrandingTest {

    @Test
    public void brandingIdsAreRecognisedWithoutKey() {
        assertTrue(Branding.isBrandingString(null, R.string.AppName));
        assertTrue(Branding.isBrandingString(null, R.string.AppNameBeta));
        assertTrue(Branding.isBrandingString(null, R.string.Page1Title));
        assertTrue(Branding.isBrandingString(null, R.string.TelegramVersion));
    }

    @Test
    public void otherIdsAreNotBranding() {
        assertFalse(Branding.isBrandingString(null, R.string.Settings));
    }

    @Test
    public void keyIsUsedWhenThereIsNoId() {
        assertTrue(Branding.isBrandingString("AppName", 0));
        assertTrue(Branding.isBrandingString("AppNameBeta", 0));
        assertTrue(Branding.isBrandingString("Page1Title", 0));
        assertTrue(Branding.isBrandingString("TelegramVersion", 0));
        assertFalse(Branding.isBrandingString("Settings", 0));
    }

    @Test
    public void nothingToCompareIsNotBranding() {
        assertFalse(Branding.isBrandingString(null, 0));
    }
}
