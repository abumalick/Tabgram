package org.telegram.tabgram;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.telegram.ui.Components.ReactionsContainerLayout;
import org.telegram.ui.PremiumPreviewFragment;
import org.telegram.ui.SelectAnimatedEmojiDialog;

public class HidePremiumTest {

    @Test
    public void hidesOnlyForNonPremiumWhenEnabled() {
        assertTrue(HidePremium.hides(true, false));
        assertFalse(HidePremium.hides(true, true));
        assertFalse(HidePremium.hides(false, false));
        assertFalse(HidePremium.hides(false, true));
    }

    @Test
    public void reactionsExemptMessageEffectsAndStickerSetEmoji() {
        assertTrue(HidePremium.hidesReactions(true, false, ReactionsContainerLayout.TYPE_DEFAULT));
        assertFalse(HidePremium.hidesReactions(true, true, ReactionsContainerLayout.TYPE_DEFAULT));
        assertFalse(HidePremium.hidesReactions(false, false, ReactionsContainerLayout.TYPE_DEFAULT));
        assertFalse(HidePremium.hidesReactions(true, false, ReactionsContainerLayout.TYPE_MESSAGE_EFFECTS));
        assertFalse(HidePremium.hidesReactions(true, false, ReactionsContainerLayout.TYPE_STICKER_SET_EMOJI));
    }

    @Test
    public void animatedEmojiPacksExemptStatusAvatarReplyAndChatReactionPickers() {
        assertTrue(HidePremium.hidesAnimatedEmojiPacks(true, false, SelectAnimatedEmojiDialog.TYPE_REACTIONS));
        assertFalse(HidePremium.hidesAnimatedEmojiPacks(true, true, SelectAnimatedEmojiDialog.TYPE_REACTIONS));
        assertFalse(HidePremium.hidesAnimatedEmojiPacks(false, false, SelectAnimatedEmojiDialog.TYPE_REACTIONS));
        int[] exempt = {
            SelectAnimatedEmojiDialog.TYPE_AVATAR_CONSTRUCTOR,
            SelectAnimatedEmojiDialog.TYPE_SET_REPLY_ICON,
            SelectAnimatedEmojiDialog.TYPE_SET_REPLY_ICON_BOTTOM,
            SelectAnimatedEmojiDialog.TYPE_CHAT_REACTIONS,
            SelectAnimatedEmojiDialog.TYPE_EMOJI_STATUS,
            SelectAnimatedEmojiDialog.TYPE_EMOJI_STATUS_TOP,
            SelectAnimatedEmojiDialog.TYPE_EMOJI_STATUS_CHANNEL,
            SelectAnimatedEmojiDialog.TYPE_EMOJI_STATUS_CHANNEL_TOP,
        };
        for (int type : exempt) {
            assertFalse("type " + type, HidePremium.hidesAnimatedEmojiPacks(true, false, type));
        }
    }

    @Test
    public void featureSheetStaysForGifts() {
        assertTrue(HidePremium.hidesFeatureSheet(true, false, PremiumPreviewFragment.PREMIUM_FEATURE_REACTIONS));
        assertFalse(HidePremium.hidesFeatureSheet(true, true, PremiumPreviewFragment.PREMIUM_FEATURE_REACTIONS));
        assertFalse(HidePremium.hidesFeatureSheet(false, false, PremiumPreviewFragment.PREMIUM_FEATURE_REACTIONS));
        assertFalse(HidePremium.hidesFeatureSheet(true, false, PremiumPreviewFragment.FEATURE_GIFTS));
    }

    @Test
    public void previewSheetStaysForGiftFlows() {
        assertTrue(HidePremium.hidesPreviewSheet(true, false, true, false));
        assertFalse(HidePremium.hidesPreviewSheet(true, true, true, false));
        assertFalse(HidePremium.hidesPreviewSheet(false, false, true, false));
        assertFalse(HidePremium.hidesPreviewSheet(true, false, false, false));
        assertFalse(HidePremium.hidesPreviewSheet(true, false, true, true));
    }
}
