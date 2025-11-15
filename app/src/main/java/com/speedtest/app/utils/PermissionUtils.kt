package com.speedtest.app.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat

/**
 * Utility for handling runtime permissions
 */
object PermissionUtils {

    /**
     * Required permissions for the app
     */
    val REQUIRED_PERMISSIONS = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        arrayOf(
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.INTERNET,
            Manifest.permission.POST_NOTIFICATIONS
        )
    } else {
        arrayOf(
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.INTERNET
        )
    }

    /**
     * Optional permissions
     */
    val OPTIONAL_PERMISSIONS = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    /**
     * Check if all required permissions are granted
     */
    fun hasRequiredPermissions(context: Context): Boolean {
        return REQUIRED_PERMISSIONS.all { permission ->
            ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
        }
    }

    /**
     * Check if a specific permission is granted
     */
    fun hasPermission(context: Context, permission: String): Boolean {
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Check if location permission is granted
     */
    fun hasLocationPermission(context: Context): Boolean {
        return hasPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ||
                hasPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
    }

    /**
     * Check if notification permission is granted (Android 13+)
     */
    fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            hasPermission(context, Manifest.permission.POST_NOTIFICATIONS)
        } else {
            true // Notifications don't require permission before Android 13
        }
    }

    /**
     * Get list of denied permissions
     */
    fun getDeniedPermissions(context: Context, permissions: Array<String>): List<String> {
        return permissions.filter { permission ->
            !hasPermission(context, permission)
        }
    }

    /**
     * Get permission rationale message
     */
    fun getPermissionRationale(permission: String): String {
        return when (permission) {
            Manifest.permission.INTERNET -> "Required to test internet speed"
            Manifest.permission.ACCESS_NETWORK_STATE -> "Required to detect network type"
            Manifest.permission.ACCESS_WIFI_STATE -> "Required to get WiFi information"
            Manifest.permission.POST_NOTIFICATIONS -> "Required to show test completion notifications"
            Manifest.permission.ACCESS_FINE_LOCATION -> "Used to find nearest test server"
            Manifest.permission.ACCESS_COARSE_LOCATION -> "Used to find nearest test server"
            else -> "Required for app functionality"
        }
    }
}
