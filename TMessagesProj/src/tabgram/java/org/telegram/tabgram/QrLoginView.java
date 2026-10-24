package org.telegram.tabgram;

import static org.telegram.messenger.LocaleController.getString;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.zxing.EncodeHintType;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.BuildVars;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.messenger.TelegramQRCodeWriter;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.Utilities;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.RequestDelegate;
import org.telegram.tgnet.SerializedData;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC;
import org.telegram.tgnet.tl.TL_account;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.AlertsCreator;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.RadialProgressView;
import org.telegram.ui.Components.SlideView;
import org.telegram.ui.TwoStepVerificationActivity;

import java.util.HashMap;

public class QrLoginView extends SlideView {

    public interface Host {
        boolean isQrPageShown();
        void showPhoneInput();
        void showPassword(Bundle params);
        void onAuthorized(TLRPC.TL_auth_authorization auth);
        void alert(String title, String text);
        Activity getParentActivity();
    }

    private final int currentAccount;
    private final Host host;

    private final TextView titleView;
    private final ImageView qrImageView;
    private final RadialProgressView qrProgressView;
    private final TextView infoTextView;
    private final TextView usePhoneButton;

    private Bitmap qrBitmap;
    private boolean connectionReset;
    private int failedAttempts;
    private int reqId;
    private int requestGeneration;
    private final Runnable refreshRunnable = this::exportToken;

    public static TextView createPhoneScreenButton(Context context, Runnable onClick) {
        return createLinkButton(context, R.string.LoginQrButton, onClick);
    }

    public static void updateLinkButtonColor(TextView button) {
        if (button != null) {
            button.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlueText4));
        }
    }

    private static TextView createLinkButton(Context context, int textRes, Runnable onClick) {
        TextView button = new TextView(context);
        button.setGravity(Gravity.CENTER);
        button.setText(getString(textRes));
        button.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
        button.setPadding(AndroidUtilities.dp(16), 0, AndroidUtilities.dp(16), 0);
        button.setOnClickListener(v -> onClick.run());
        return button;
    }

    public QrLoginView(Context context, int currentAccount, Host host) {
        super(context);
        this.currentAccount = currentAccount;
        this.host = host;

        setOrientation(VERTICAL);

        titleView = new TextView(context);
        titleView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
        titleView.setTypeface(AndroidUtilities.bold());
        titleView.setText(getString(R.string.LoginQrTitle));
        titleView.setGravity(Gravity.CENTER);
        titleView.setLineSpacing(AndroidUtilities.dp(2), 1.0f);
        addView(titleView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER_HORIZONTAL, 32, 16, 32, 0));

        qrImageView = new ImageView(context);
        qrImageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        qrImageView.setBackgroundColor(Color.WHITE);
        int qrPadding = AndroidUtilities.dp(12);
        qrImageView.setPadding(qrPadding, qrPadding, qrPadding, qrPadding);
        qrProgressView = new RadialProgressView(context);
        qrProgressView.setSize(AndroidUtilities.dp(32));
        FrameLayout qrContainer = new FrameLayout(context);
        qrContainer.addView(qrImageView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));
        qrContainer.addView(qrProgressView, LayoutHelper.createFrame(48, 48, Gravity.CENTER));
        int qrSize = AndroidUtilities.isSmallScreen() ? 180 : 240;
        addView(qrContainer, LayoutHelper.createLinear(qrSize, qrSize, Gravity.CENTER_HORIZONTAL, 0, 24, 0, 0));

        infoTextView = new TextView(context);
        infoTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        infoTextView.setGravity(LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT);
        infoTextView.setLineSpacing(AndroidUtilities.dp(4), 1.0f);
        infoTextView.setText(getString(R.string.LoginQrInfo));
        addView(infoTextView, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER_HORIZONTAL, 24, 24, 24, 0));

        usePhoneButton = createLinkButton(context, R.string.LoginQrUsePhone, host::showPhoneInput);

        FrameLayout bottomContainer = new FrameLayout(context);
        bottomContainer.addView(usePhoneButton, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 56, Gravity.BOTTOM, 0, 0, 0, 32));
        addView(bottomContainer, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, Gravity.BOTTOM));
    }

    @Override
    public void updateColors() {
        titleView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        infoTextView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText6));
        updateLinkButtonColor(usePhoneButton);
        qrProgressView.setProgressColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlueText4));
    }

    @Override
    public String getHeaderName() {
        return getString(R.string.LoginQrButton);
    }

    @Override
    public boolean needBackButton() {
        return true;
    }

    @Override
    public boolean onBackPressed(boolean force) {
        cancelRequests();
        return true;
    }

    @Override
    public void onShow() {
        super.onShow();
        if (!connectionReset) {
            connectionReset = true;
            ConnectionsManager.getInstance(currentAccount).cleanup(false);
        }
        failedAttempts = 0;
        exportToken();
    }

    @Override
    public void onHide() {
        super.onHide();
        cancelRequests();
    }

    @Override
    public void onDestroyActivity() {
        super.onDestroyActivity();
        cancelRequests();
    }

    private void cancelRequests() {
        AndroidUtilities.cancelRunOnUIThread(refreshRunnable);
        requestGeneration++;
        if (reqId != 0) {
            ConnectionsManager.getInstance(currentAccount).cancelRequest(reqId, true);
            reqId = 0;
        }
    }

    public void exportToken() {
        cancelRequests();
        if (!host.isQrPageShown() || UserConfig.getInstance(currentAccount).isClientActivated()) {
            return;
        }
        setQrLoading(true);
        TLRPC.TL_auth_exportLoginToken req = new TLRPC.TL_auth_exportLoginToken();
        req.api_id = BuildVars.APP_ID;
        req.api_hash = BuildVars.APP_HASH;
        for (int a = 0; a < UserConfig.MAX_ACCOUNT_COUNT; a++) {
            if (a == currentAccount) {
                continue;
            }
            UserConfig userConfig = UserConfig.getInstance(a);
            if (userConfig.isClientActivated()) {
                req.except_ids.add(userConfig.getClientUserId());
            }
        }
        sendRequest(req, this::onTokenResponse);
    }

    private void sendRequest(TLObject req, RequestDelegate onResult) {
        final int generation = ++requestGeneration;
        reqId = ConnectionsManager.getInstance(currentAccount).sendRequest(req, (response, error) -> AndroidUtilities.runOnUIThread(() -> {
            if (generation != requestGeneration || !host.isQrPageShown()) {
                return;
            }
            reqId = 0;
            onResult.run(response, error);
        }), ConnectionsManager.RequestFlagWithoutLogin | ConnectionsManager.RequestFlagFailOnServerErrors);
    }

    private void onTokenResponse(TLObject response, TLRPC.TL_error error) {
        QrLogin.Action action = QrLogin.next(response, error);
        if (action == QrLogin.Action.SHOW_TOKEN) {
            TLRPC.TL_auth_loginToken loginToken = (TLRPC.TL_auth_loginToken) response;
            failedAttempts = 0;
            showQr(QrLogin.tokenLink(loginToken.token));
            int delay = QrLogin.refreshDelaySeconds(loginToken.expires, ConnectionsManager.getInstance(currentAccount).getCurrentTime());
            AndroidUtilities.runOnUIThread(refreshRunnable, delay * 1000L);
        } else if (action == QrLogin.Action.MIGRATE) {
            TLRPC.TL_auth_loginTokenMigrateTo migrateTo = (TLRPC.TL_auth_loginTokenMigrateTo) response;
            // The account's home datacenter has to become the default one before the token is imported there
            ConnectionsManager.getInstance(currentAccount).setDefaultDatacenterId(migrateTo.dc_id);
            TLRPC.TL_auth_importLoginToken req = new TLRPC.TL_auth_importLoginToken();
            req.token = migrateTo.token;
            sendRequest(req, this::onTokenResponse);
        } else if (action == QrLogin.Action.SUCCESS) {
            cancelRequests();
            host.onAuthorized((TLRPC.TL_auth_authorization) ((TLRPC.TL_auth_loginTokenSuccess) response).authorization);
        } else if (action == QrLogin.Action.SIGN_UP_REQUIRED) {
            cancelRequests();
            host.alert(getString(R.string.RestorePasswordNoEmailTitle), getString(R.string.ErrorOccurred));
            host.showPhoneInput();
        } else if (action == QrLogin.Action.PASSWORD_NEEDED) {
            cancelRequests();
            sendRequest(new TL_account.getPassword(), (response1, error1) -> {
                if (error1 == null) {
                    TL_account.Password password = (TL_account.Password) response1;
                    if (!TwoStepVerificationActivity.canHandleCurrentPassword(password, true)) {
                        AlertsCreator.showUpdateAppAlert(host.getParentActivity(), getString("UpdateAppAlert", R.string.UpdateAppAlert), true);
                        host.showPhoneInput();
                        return;
                    }
                    Bundle bundle = new Bundle();
                    SerializedData data = new SerializedData(password.getObjectSize());
                    password.serializeToStream(data);
                    bundle.putString("password", Utilities.bytesToHex(data.toByteArray()));
                    bundle.putString("phoneFormated", "");
                    host.showPassword(bundle);
                } else {
                    onRequestError(error1);
                }
            });
        } else if (action == QrLogin.Action.RETRY) {
            onRequestError(error);
        }
    }

    private void onRequestError(TLRPC.TL_error error) {
        if (QrLogin.shouldAlert(error, failedAttempts)) {
            host.alert(getString(R.string.RestorePasswordNoEmailTitle), error.text);
        }
        if (!QrLogin.isNetworkError(error)) {
            failedAttempts++;
        }
        AndroidUtilities.runOnUIThread(refreshRunnable, QrLogin.retryDelaySeconds(error) * 1000L);
    }

    private void setQrLoading(boolean loading) {
        qrImageView.animate().alpha(loading ? 0.3f : 1f).setDuration(150).start();
        qrProgressView.setVisibility(loading ? VISIBLE : INVISIBLE);
    }

    private void showQr(String link) {
        try {
            HashMap<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
            hints.put(EncodeHintType.MARGIN, 0);
            qrBitmap = new TelegramQRCodeWriter().encode(link, 768, 768, hints, qrBitmap);
            qrImageView.setImageBitmap(qrBitmap);
            setQrLoading(false);
        } catch (Exception e) {
            FileLog.e(e);
        }
    }
}
