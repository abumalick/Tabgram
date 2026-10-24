package org.telegram.tabgram;

import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC;

public final class QrLogin {

    public enum Action { SHOW_TOKEN, MIGRATE, SUCCESS, SIGN_UP_REQUIRED, PASSWORD_NEEDED, RETRY }

    private static final char[] BASE64_URL = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-_".toCharArray();
    private static final int MIN_DELAY_SECONDS = 5;
    // a scan that lands in the last moments of a token's life fails with an expired-token error
    private static final int REFRESH_MARGIN_SECONDS = 3;
    private static final int NETWORK_ERROR_CODE = -1000;

    private QrLogin() {}

    public static String tokenLink(byte[] token) {
        StringBuilder sb = new StringBuilder("tg://login?token=");
        for (int i = 0; i < token.length; i += 3) {
            int remaining = Math.min(3, token.length - i);
            int chunk = (token[i] & 0xff) << 16;
            if (remaining > 1) {
                chunk |= (token[i + 1] & 0xff) << 8;
            }
            if (remaining > 2) {
                chunk |= token[i + 2] & 0xff;
            }
            for (int j = 0; j <= remaining; j++) {
                sb.append(BASE64_URL[(chunk >> (18 - 6 * j)) & 0x3f]);
            }
        }
        return sb.toString();
    }

    // Returns null for a response that is neither an error nor a known auth.LoginToken: it is ignored.
    public static Action next(TLObject response, TLRPC.TL_error error) {
        if (error != null) {
            return error.text != null && error.text.contains("SESSION_PASSWORD_NEEDED") ? Action.PASSWORD_NEEDED : Action.RETRY;
        }
        if (response instanceof TLRPC.TL_auth_loginToken) {
            return Action.SHOW_TOKEN;
        } else if (response instanceof TLRPC.TL_auth_loginTokenMigrateTo) {
            return Action.MIGRATE;
        } else if (response instanceof TLRPC.TL_auth_loginTokenSuccess) {
            return ((TLRPC.TL_auth_loginTokenSuccess) response).authorization instanceof TLRPC.TL_auth_authorization ? Action.SUCCESS : Action.SIGN_UP_REQUIRED;
        }
        return null;
    }

    public static int refreshDelaySeconds(int expires, int now) {
        return Math.max(MIN_DELAY_SECONDS, expires - now - REFRESH_MARGIN_SECONDS);
    }

    public static int retryDelaySeconds(TLRPC.TL_error error) {
        if (error.text != null && error.text.startsWith("FLOOD_WAIT")) {
            return Math.max(MIN_DELAY_SECONDS, firstNumber(error.text));
        }
        return MIN_DELAY_SECONDS;
    }

    public static boolean shouldAlert(TLRPC.TL_error error, int failedAttemptsBefore) {
        return error.code != NETWORK_ERROR_CODE && failedAttemptsBefore == 0;
    }

    public static boolean isNetworkError(TLRPC.TL_error error) {
        return error.code == NETWORK_ERROR_CODE;
    }

    private static int firstNumber(String text) {
        int value = 0;
        boolean found = false;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c >= '0' && c <= '9') {
                found = true;
                value = value * 10 + (c - '0');
                if (value > 1_000_000) {
                    break;
                }
            } else if (found) {
                break;
            }
        }
        return value;
    }
}
