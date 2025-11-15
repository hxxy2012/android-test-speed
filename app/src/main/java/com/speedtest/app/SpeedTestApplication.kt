package com.speedtest.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Application class for SpeedTest app
 */
@HiltAndroidApp
class SpeedTestApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        // Initialize app-wide components if needed
    }
}
