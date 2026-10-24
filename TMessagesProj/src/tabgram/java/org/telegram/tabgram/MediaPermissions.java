package org.telegram.tabgram;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;

public final class MediaPermissions {

    private MediaPermissions() {}

    public static boolean askFullMediaAtStartup(int sdkInt, boolean userSelectedGranted) {
        return !(sdkInt >= 34 && userSelectedGranted);
    }

    public static boolean askFullMediaAtStartup(Activity activity) {
        return askFullMediaAtStartup(Build.VERSION.SDK_INT,
            Build.VERSION.SDK_INT >= 34 && activity.checkSelfPermission(Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED) == PackageManager.PERMISSION_GRANTED);
    }
}
