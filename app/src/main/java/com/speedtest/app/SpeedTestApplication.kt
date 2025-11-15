package com.speedtest.app

import android.app.Application
import com.speedtest.app.utils.NotificationHelper
import dagger.hilt.android.HiltAndroidApp

/**
 * Application class for SpeedTest app
 */
@HiltAndroidApp
class SpeedTestApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Initialize notification channels
        NotificationHelper.createNotificationChannel(this)
    }
}
