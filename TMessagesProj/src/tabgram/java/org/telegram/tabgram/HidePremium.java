package org.telegram.tabgram;

import android.content.SharedPreferences;

import org.telegram.messenger.MessagesController;
import org.telegram.ui.Components.ReactionsContainerLayout;
import org.telegram.ui.PremiumPreviewFragment;
import org.telegram.ui.SelectAnimatedEmojiDialog;

public final class HidePremium {

    private static final String KEY = "hidePremiumUpsell";

    private static volatile boolean loaded;
    private static volatile boolean enabled;

    private HidePremium() {}

    public static boolean isEnabled() {
        if (!loaded) {
            synchronized (HidePremium.class) {
                if (!loaded) {
                    enabled = MessagesController.getGlobalMainSettings().getBoolean(KEY, true);
                    loaded = true;
                }
            }
        }
        return enabled;
    }

    public static synchronized void toggle() {
        enabled = !isEnabled();
        SharedPreferences.Editor editor = MessagesController.getGlobalMainSettings().edit();
        editor.putBoolean(KEY, enabled);
        editor.apply();
    }

    public static boolean hides(boolean enabled, boolean isPremium) {
        return enabled && !isPremium;
    }

    public static boolean hidesReactions(boolean enabled, boolean isPremium, int type) {
        return hides(enabled, isPremium) && type != ReactionsContainerLayout.TYPE_MESSAGE_EFFECTS && type != ReactionsContainerLayout.TYPE_STICKER_SET_EMOJI;
    }

    public static boolean hidesAnimatedEmojiPacks(boolean enabled, boolean isPremium, int type) {
        switch (type) {
            case SelectAnimatedEmojiDialog.TYPE_AVATAR_CONSTRUCTOR:
            case SelectAnimatedEmojiDialog.TYPE_SET_REPLY_ICON:
            case SelectAnimatedEmojiDialog.TYPE_SET_REPLY_ICON_BOTTOM:
            case SelectAnimatedEmojiDialog.TYPE_CHAT_REACTIONS:
            case SelectAnimatedEmojiDialog.TYPE_EMOJI_STATUS:
            case SelectAnimatedEmojiDialog.TYPE_EMOJI_STATUS_TOP:
            case SelectAnimatedEmojiDialog.TYPE_EMOJI_STATUS_CHANNEL:
            case SelectAnimatedEmojiDialog.TYPE_EMOJI_STATUS_CHANNEL_TOP:
                return false;
            default:
                return hides(enabled, isPremium);
        }
    }

    public static boolean hidesFeatureSheet(boolean enabled, boolean isPremium, int startType) {
        return hides(enabled, isPremium) && startType != PremiumPreviewFragment.FEATURE_GIFTS;
    }

    // Gift flows stay visible: they pass a gift tier, star gift or collectible status, or no user at all (gift codes, giveaways, boost gifts).
    public static boolean hidesPreviewSheet(boolean enabled, boolean isPremium, boolean hasUser, boolean isGift) {
        return hides(enabled, isPremium) && hasUser && !isGift;
    }
}
