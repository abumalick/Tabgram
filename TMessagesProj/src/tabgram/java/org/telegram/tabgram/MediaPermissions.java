package org.telegram.tabgram;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.content.PermissionChecker;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MediaController;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.ActionBarMenuItem;
import org.telegram.ui.BasePermissionsActivity;

public final class MediaPermissions {

    // Negative, as the sheet's album items use 10 + index and -1 is Back.
    public static final int SELECT_MORE_ITEM = -1000;

    private MediaPermissions() {}

    public static boolean askFullMediaAtStartup(int sdkInt, boolean userSelectedGranted) {
        return !hasSelectedAccess(sdkInt, userSelectedGranted);
    }

    public static boolean askFullMediaAtStartup(Activity activity) {
        return !hasSelectedAccess(activity);
    }

    public static boolean hasSelectedAccess(int sdkInt, boolean userSelectedGranted) {
        return sdkInt >= 34 && userSelectedGranted;
    }

    public static boolean isPartialAccess(int sdkInt, boolean userSelectedGranted, boolean fullImagesGranted) {
        return hasSelectedAccess(sdkInt, userSelectedGranted) && !fullImagesGranted;
    }

    public static boolean hasSelectedAccess(Context context) {
        return hasSelectedAccess(Build.VERSION.SDK_INT, userSelectedGranted(context));
    }

    // After "Allow limited access", Android also reports READ_MEDIA_IMAGES as granted until the
    // process dies, with its app op set to ignore; PermissionChecker sees through that.
    public static boolean isPartialAccess(Context context) {
        return isPartialAccess(Build.VERSION.SDK_INT, userSelectedGranted(context),
            context != null && PermissionChecker.checkSelfPermission(context, Manifest.permission.READ_MEDIA_IMAGES) == PermissionChecker.PERMISSION_GRANTED);
    }

    private static boolean userSelectedGranted(Context context) {
        return context != null && Build.VERSION.SDK_INT >= 34 && context.checkSelfPermission(Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED) == PackageManager.PERMISSION_GRANTED;
    }

    public static void addSelectMoreItem(ActionBarMenuItem menu) {
        menu.addSubItem(SELECT_MORE_ITEM, R.drawable.msg_gallery, LocaleController.getString(R.string.SelectMorePhotos));
        menu.hideSubItem(SELECT_MORE_ITEM);
    }

    public static void updateSelectMoreItem(ActionBarMenuItem menu, Context context) {
        if (isPartialAccess(context)) {
            menu.showSubItem(SELECT_MORE_ITEM);
        } else {
            menu.hideSubItem(SELECT_MORE_ITEM);
        }
    }

    public static boolean onMenuItemClick(int id, Context context) {
        if (id != SELECT_MORE_ITEM) {
            return false;
        }
        Activity activity = AndroidUtilities.findActivity(context);
        if (activity != null) {
            activity.requestPermissions(new String[]{Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.READ_MEDIA_VIDEO}, BasePermissionsActivity.REQUEST_CODE_EXTERNAL_STORAGE);
        }
        return true;
    }

    // The attach sheet only loads albums when none are cached, so a wider selection would not
    // show up until the app restarts.
    public static void onStoragePermissionResult(ActionBarMenuItem menu, Context context) {
        updateSelectMoreItem(menu, context);
        if (hasSelectedAccess(context)) {
            MediaController.loadGalleryPhotosAlbums(0);
        }
    }
}
