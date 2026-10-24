package org.telegram.tabgram;

import static org.telegram.messenger.AndroidUtilities.dp;
import static org.telegram.messenger.LocaleController.getString;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.text.Layout;
import android.text.SpannableStringBuilder;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.SoundEffectConstants;
import android.view.accessibility.AccessibilityNodeInfo;

import org.telegram.messenger.Emoji;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.ChatMessageCell;

public final class CompactSponsoredPill {

    public static final int ROW_HEIGHT_DP = 36;
    public static final int PILL_HEIGHT_DP = 22;
    public static final int PADDING_DP = 10;
    public static final int MARGIN_DP = 12;

    public interface LongPress {
        void start(float x, float y);

        void cancel();
    }

    private final ChatMessageCell cell;
    private final LongPress longPress;
    private final RectF rect = new RectF();
    private TextPaint paint;
    private StaticLayout layout;
    private float labelWidth;
    private boolean rtl;
    private boolean pressed;

    public CompactSponsoredPill(ChatMessageCell cell, LongPress longPress) {
        this.cell = cell;
        this.longPress = longPress;
    }

    public static boolean isOnLabel(boolean rtl, float x, float left, float right, float labelEdge) {
        return rtl ? x >= right - labelEdge : x <= left + labelEdge;
    }

    public static boolean isInsidePill(float left, float top, float right, float bottom, float x, float y, float slop) {
        return x >= left && x <= right && y >= top - slop && y <= bottom + slop;
    }

    public void layout(MessageObject messageObject, String authorName, int parentWidth) {
        if (paint == null) {
            paint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        }
        paint.setTextSize(dp(12));
        final String label = getString(R.string.SponsoredMessageAd) + " · ";
        final String title = !TextUtils.isEmpty(messageObject.sponsoredTitle) ? messageObject.sponsoredTitle : authorName;
        final int maxTextWidth = Math.max(dp(40), Math.min(parentWidth - dp(80), dp(240)) - dp(PADDING_DP * 2));
        labelWidth = paint.measureText(label);
        CharSequence titleText = Emoji.replaceEmoji(title == null ? "" : title, paint.getFontMetricsInt(), false);
        titleText = TextUtils.ellipsize(titleText, paint, Math.max(0, maxTextWidth - labelWidth), TextUtils.TruncateAt.END);
        final SpannableStringBuilder text = new SpannableStringBuilder(label).append(titleText);
        // the title is already ellipsized; the extra width only keeps measureText rounding from wrapping it to a second line
        layout = new StaticLayout(text, paint, maxTextWidth + dp(100), Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
        rtl = layout.getParagraphDirection(0) == Layout.DIR_RIGHT_TO_LEFT;
        pressed = false;
    }

    public int contentWidth() {
        return (int) Math.ceil(layout.getLineWidth(0)) + dp(PADDING_DP * 2);
    }

    public CharSequence text() {
        return layout == null ? "" : layout.getText();
    }

    private void updateRect() {
        final float width = (layout == null ? 0 : (float) Math.ceil(layout.getLineWidth(0))) + dp(PADDING_DP * 2);
        final float right = cell.getMeasuredWidth() - dp(MARGIN_DP);
        final float top = (dp(ROW_HEIGHT_DP) - dp(PILL_HEIGHT_DP)) / 2f;
        rect.set(right - width, top, right, top + dp(PILL_HEIGHT_DP));
    }

    private boolean isInside(float x, float y) {
        return isInsidePill(rect.left, rect.top, rect.right, rect.bottom, x, y, dp(3));
    }

    private boolean isOnLabel(float x) {
        return isOnLabel(rtl, x, rect.left, rect.right, dp(PADDING_DP) + labelWidth);
    }

    public void draw(Canvas canvas) {
        if (layout == null) {
            return;
        }
        updateRect();
        final float r = rect.height() / 2f;
        cell.applyServiceShaderMatrix();
        canvas.drawRoundRect(rect, r, r, cell.getThemedPaint(pressed ? Theme.key_paint_chatActionBackgroundSelected : Theme.key_paint_chatActionBackground));
        if (cell.hasGradientService()) {
            canvas.drawRoundRect(rect, r, r, Theme.chat_actionBackgroundGradientDarkenPaint);
        }
        paint.setColor(cell.getThemedColor(Theme.key_chat_serviceText));
        canvas.save();
        canvas.translate(rect.left + dp(PADDING_DP) - layout.getLineLeft(0), rect.centerY() - layout.getHeight() / 2f);
        layout.draw(canvas);
        canvas.restore();
    }

    public boolean onTouchEvent(MotionEvent event, float x, float y) {
        final int action = event.getAction();
        if (action == MotionEvent.ACTION_DOWN) {
            updateRect();
            if (layout == null || !isInside(x, y)) {
                return false;
            }
            pressed = true;
            longPress.start(x, y);
            cell.invalidate();
            return true;
        }
        if (!pressed) {
            return false;
        }
        if (action == MotionEvent.ACTION_UP) {
            pressed = false;
            longPress.cancel();
            cell.invalidate();
            cell.playSoundEffect(SoundEffectConstants.CLICK);
            if (cell.getDelegate() != null) {
                if (isOnLabel(x)) {
                    cell.getDelegate().didPressSponsoredInfo(cell, x, y);
                } else {
                    cell.getDelegate().didPressInstantButton(cell, cell.drawInstantViewType);
                }
            }
            return true;
        } else if (action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_MOVE && !isInside(x, y)) {
            pressed = false;
            longPress.cancel();
            cell.invalidate();
        }
        return pressed;
    }

    public boolean onLongPress() {
        pressed = false;
        cell.invalidate();
        if (cell.getDelegate() != null) {
            cell.getDelegate().didPressSponsoredInfo(cell, cell.getLastTouchX(), cell.getLastTouchY());
        }
        return true;
    }

    public boolean performAccessibilityAction(int action) {
        if (cell.getDelegate() == null) {
            return false;
        }
        if (action == AccessibilityNodeInfo.ACTION_CLICK) {
            cell.getDelegate().didPressInstantButton(cell, cell.drawInstantViewType);
            return true;
        } else if (action == R.id.acc_action_msg_options || action == AccessibilityNodeInfo.ACTION_LONG_CLICK) {
            updateRect();
            cell.getDelegate().didPressSponsoredInfo(cell, rect.centerX(), rect.centerY());
            return true;
        }
        return false;
    }
}
