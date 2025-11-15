package com.speedtest.app.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.speedtest.app.MainActivity
import com.speedtest.app.R
import com.speedtest.app.utils.Constants
import dagger.hilt.android.AndroidEntryPoint

/**
 * Foreground service to keep speed test running
 */
@AndroidEntryPoint
class SpeedTestService : Service() {

    private val binder = SpeedTestBinder()
    private var isTestRunning = false

    inner class SpeedTestBinder : Binder() {
        fun getService(): SpeedTestService = this@SpeedTestService
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_TEST -> startTest()
            ACTION_STOP_TEST -> stopTest()
        }
        return START_STICKY
    }

    private fun startTest() {
        isTestRunning = true
        val notification = createNotification("Running speed test...", true)
        startForeground(Constants.TEST_NOTIFICATION_ID, notification)
    }

    private fun stopTest() {
        isTestRunning = false
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Speed test notifications"
                setShowBadge(false)
            }

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(content: String, ongoing: Boolean): Notification {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("SpeedTest")
            .setContentText(content)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(pendingIntent)
            .setOngoing(ongoing)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
    }

    fun updateNotification(content: String) {
        if (isTestRunning) {
            val notification = createNotification(content, true)
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(Constants.TEST_NOTIFICATION_ID, notification)
        }
    }

    companion object {
        private const val CHANNEL_ID = "speedtest_service"
        private const val CHANNEL_NAME = "Speed Test Service"
        const val ACTION_START_TEST = "com.speedtest.app.START_TEST"
        const val ACTION_STOP_TEST = "com.speedtest.app.STOP_TEST"
    }
}
