package org.telegram.tabgram;

import org.telegram.messenger.R;

/** Strings carrying the app name, which cloud language packs must not override with "Telegram". */
public final class Branding {

    private Branding() {}

    public static boolean isBrandingString(String key, int stringRes) {
        if (stringRes != 0) {
            return stringRes == R.string.AppName || stringRes == R.string.AppNameBeta
                    || stringRes == R.string.Page1Title || stringRes == R.string.TelegramVersion;
        }
        return "AppName".equals(key) || "AppNameBeta".equals(key)
                || "Page1Title".equals(key) || "TelegramVersion".equals(key);
    }
}
