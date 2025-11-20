package com.speedtest.app

import android.app.Application
import android.util.Log
import com.speedtest.app.utils.NotificationHelper
import dagger.hilt.android.HiltAndroidApp

/**
 * Application class for SpeedTest app
 */
@HiltAndroidApp
class SpeedTestApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        try {
            // Initialize notification channels
            NotificationHelper.createNotificationChannel(this)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create notification channel", e)
            // Don't crash - notifications are optional
        }
    }

    companion object {
        private const val TAG = "SpeedTestApplication"
    }
}
