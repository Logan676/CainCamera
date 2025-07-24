package com.cgfay.uitls.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity

/**
 * Utility for runtime permission checks and requests.
 */
object PermissionUtils {
    const val REQUEST_CAMERA_PERMISSION = 0x01
    const val REQUEST_STORAGE_PERMISSION = 0x02
    const val REQUEST_SOUND_PERMISSION = 0x03

    /** Check whether a permission is granted. */
    @JvmStatic
    fun permissionChecking(context: Context, permission: String): Boolean {
        var targetVersion = 1
        try {
            val info: PackageInfo = context.packageManager
                .getPackageInfo(context.packageName, 0)
            targetVersion = info.applicationInfo.targetSdkVersion
        } catch (_: PackageManager.NameNotFoundException) {
        }
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
            targetVersion >= Build.VERSION_CODES.M
        ) {
            ContextCompat.checkSelfPermission(
                context,
                permission
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            PermissionChecker.checkSelfPermission(
                context,
                permission
            ) == PermissionChecker.PERMISSION_GRANTED
        }
    }

    /** Check whether a permission is granted for a fragment. */
    @JvmStatic
    fun permissionChecking(fragment: Fragment, permission: String): Boolean {
        val context = fragment.context ?: return false
        return permissionChecking(context, permission)
    }

    /** Check whether a list of permissions are granted. */
    @JvmStatic
    fun permissionsChecking(context: Context, permissions: Array<String>): Boolean {
        var targetVersion = 1
        try {
            val info = context.packageManager.getPackageInfo(context.packageName, 0)
            targetVersion = info.applicationInfo.targetSdkVersion
        } catch (_: PackageManager.NameNotFoundException) {
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
            targetVersion >= Build.VERSION_CODES.M
        ) {
            for (permission in permissions) {
                if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false
                }
            }
        } else {
            for (permission in permissions) {
                if (PermissionChecker.checkSelfPermission(context, permission) != PermissionChecker.PERMISSION_GRANTED) {
                    return false
                }
            }
        }
        return true
    }

    /** Check whether a list of permissions are granted for a fragment. */
    @JvmStatic
    fun permissionsChecking(fragment: Fragment, permissions: Array<String>): Boolean {
        val context = fragment.context ?: return false
        return permissionsChecking(context, permissions)
    }

    /** Request camera permission from a fragment. */
    @JvmStatic
    fun requestCameraPermission(fragment: Fragment) {
        fragment.requestPermissions(
            arrayOf(Manifest.permission.CAMERA),
            REQUEST_CAMERA_PERMISSION
        )
    }

    /** Request camera permission from an activity. */
    @JvmStatic
    fun requestCameraPermission(activity: FragmentActivity) {
        ActivityCompat.requestPermissions(
            activity,
            arrayOf(Manifest.permission.CAMERA),
            REQUEST_CAMERA_PERMISSION
        )
    }

    /** Request storage permission from a fragment. */
    @JvmStatic
    fun requestStoragePermission(fragment: Fragment) {
        fragment.requestPermissions(
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
            REQUEST_STORAGE_PERMISSION
        )
    }

    /** Request storage permission from an activity. */
    @JvmStatic
    fun requestStoragePermission(activity: FragmentActivity) {
        ActivityCompat.requestPermissions(
            activity,
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
            REQUEST_STORAGE_PERMISSION
        )
    }

    /** Request record audio permission from a fragment. */
    @JvmStatic
    fun requestRecordSoundPermission(fragment: Fragment) {
        fragment.requestPermissions(
            arrayOf(Manifest.permission.RECORD_AUDIO),
            REQUEST_SOUND_PERMISSION
        )
    }

    /** Request record audio permission from an activity. */
    @JvmStatic
    fun requestRecordSoundPermission(activity: FragmentActivity) {
        ActivityCompat.requestPermissions(
            activity,
            arrayOf(Manifest.permission.RECORD_AUDIO),
            REQUEST_SOUND_PERMISSION
        )
    }

    /** Request a list of permissions from a fragment. */
    @JvmStatic
    fun requestPermissions(fragment: Fragment, permissions: Array<String>, requestCode: Int) {
        if (!permissionsChecking(fragment, permissions)) {
            fragment.requestPermissions(permissions, requestCode)
        }
    }

    /** Request a list of permissions from an activity. */
    @JvmStatic
    fun requestPermissions(activity: FragmentActivity, permissions: Array<String>, requestCode: Int) {
        if (!permissionsChecking(activity, permissions)) {
            ActivityCompat.requestPermissions(activity, permissions, requestCode)
        }
    }

    /** Launch the system permission settings from a fragment. */
    @JvmStatic
    fun launchPermissionSettings(fragment: Fragment) {
        fragment.activity?.let { launchPermissionSettings(it) }
    }

    /** Launch the system permission settings from an activity. */
    @JvmStatic
    fun launchPermissionSettings(activity: Activity) {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", activity.packageName, null)
        }
        activity.startActivity(intent)
    }
}

