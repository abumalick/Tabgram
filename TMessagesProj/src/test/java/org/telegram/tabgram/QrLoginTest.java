package org.telegram.tabgram;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.telegram.tgnet.TLRPC;

import java.util.Base64;

public class QrLoginTest {

    private static final String PREFIX = "tg://login?token=";

    private static TLRPC.TL_error error(int code, String text) {
        TLRPC.TL_error error = new TLRPC.TL_error();
        error.code = code;
        error.text = text;
        return error;
    }

    @Test
    public void tokenLinkUsesUrlSafeBase64WithoutPadding() {
        assertEquals(PREFIX + "-_8", QrLogin.tokenLink(new byte[]{(byte) 0xfb, (byte) 0xff}));
        assertEquals(PREFIX + "AQID", QrLogin.tokenLink(new byte[]{1, 2, 3}));
        assertEquals(PREFIX + "AQ", QrLogin.tokenLink(new byte[]{1}));
    }

    @Test
    public void tokenLinkRoundTrips() {
        byte[] token = new byte[32];
        for (int i = 0; i < token.length; i++) {
            token[i] = (byte) (i * 37 + 200);
        }
        String link = QrLogin.tokenLink(token);
        assertTrue(link.startsWith(PREFIX));
        assertArrayEquals(token, Base64.getUrlDecoder().decode(link.substring(PREFIX.length())));
    }

    @Test
    public void nextMapsResponses() {
        assertEquals(QrLogin.Action.SHOW_TOKEN, QrLogin.next(new TLRPC.TL_auth_loginToken(), null));
        assertEquals(QrLogin.Action.MIGRATE, QrLogin.next(new TLRPC.TL_auth_loginTokenMigrateTo(), null));

        TLRPC.TL_auth_loginTokenSuccess success = new TLRPC.TL_auth_loginTokenSuccess();
        success.authorization = new TLRPC.TL_auth_authorization();
        assertEquals(QrLogin.Action.SUCCESS, QrLogin.next(success, null));

        TLRPC.TL_auth_loginTokenSuccess signUp = new TLRPC.TL_auth_loginTokenSuccess();
        signUp.authorization = new TLRPC.TL_auth_authorizationSignUpRequired();
        assertEquals(QrLogin.Action.SIGN_UP_REQUIRED, QrLogin.next(signUp, null));
    }

    @Test
    public void nextMapsErrors() {
        assertEquals(QrLogin.Action.PASSWORD_NEEDED, QrLogin.next(null, error(401, "SESSION_PASSWORD_NEEDED")));
        assertEquals(QrLogin.Action.RETRY, QrLogin.next(null, error(420, "FLOOD_WAIT_30")));
    }

    @Test
    public void refreshDelayIsAtLeastFiveSeconds() {
        assertEquals(5, QrLogin.refreshDelaySeconds(100, 98));
        assertEquals(5, QrLogin.refreshDelaySeconds(100, 93));
    }

    @Test
    public void refreshHappensThreeSecondsBeforeExpiry() {
        assertEquals(27, QrLogin.refreshDelaySeconds(100, 70));
        assertEquals(57, QrLogin.refreshDelaySeconds(100, 40));
    }

    @Test
    public void retryDelayHonoursFloodWait() {
        assertEquals(30, QrLogin.retryDelaySeconds(error(420, "FLOOD_WAIT_30")));
        assertEquals(5, QrLogin.retryDelaySeconds(error(420, "FLOOD_WAIT_3")));
        assertEquals(5, QrLogin.retryDelaySeconds(error(500, "INTERNAL")));
    }

    @Test
    public void alertsOnlyOnFirstServerFailure() {
        assertFalse(QrLogin.shouldAlert(error(-1000, "NETWORK"), 0));
        assertTrue(QrLogin.shouldAlert(error(400, "BAD"), 0));
        assertFalse(QrLogin.shouldAlert(error(400, "BAD"), 1));
        assertTrue(QrLogin.isNetworkError(error(-1000, "NETWORK")));
        assertFalse(QrLogin.isNetworkError(error(400, "BAD")));
    }
}
