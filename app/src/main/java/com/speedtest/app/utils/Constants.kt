package com.speedtest.app.utils

/**
 * Application-wide constants
 */
object Constants {

    // Test Configuration
    const val DEFAULT_TEST_DURATION_SECONDS = 30
    const val DEFAULT_THREAD_COUNT = 4
    const val MIN_THREAD_COUNT = 1
    const val MAX_THREAD_COUNT = 8

    // Network
    const val DEFAULT_TIMEOUT_SECONDS = 30L
    const val PING_COUNT = 10
    const val DOWNLOAD_CHUNK_SIZE = 5 * 1024 * 1024 // 5MB
    const val UPLOAD_CHUNK_SIZE = 2 * 1024 * 1024 // 2MB

    // Database
    const val MAX_HISTORY_ITEMS = 1000
    const val AUTO_DELETE_AFTER_DAYS = 90

    // Preferences
    const val PREF_FIRST_LAUNCH = "first_launch"
    const val PREF_SELECTED_SERVER = "selected_server"
    const val PREF_SPEED_UNIT = "speed_unit"
    const val PREF_THEME_MODE = "theme_mode"

    // Notifications
    const val NOTIFICATION_CHANNEL_ID = "speedtest_notifications"
    const val NOTIFICATION_CHANNEL_NAME = "Speed Test Notifications"
    const val TEST_NOTIFICATION_ID = 1001

    // WorkManager
    const val WORK_AUTO_TEST = "auto_test_work"
    const val WORK_TAG_AUTO_TEST = "auto_test"

    // Speed Thresholds (Mbps)
    const val SPEED_EXCELLENT = 100.0
    const val SPEED_VERY_GOOD = 50.0
    const val SPEED_GOOD = 25.0
    const val SPEED_FAIR = 10.0
    const val SPEED_POOR = 5.0

    // Ping Thresholds (ms)
    const val PING_EXCELLENT = 20
    const val PING_GOOD = 50
    const val PING_FAIR = 100
    const val PING_POOR = 200

    // URLs
    const val PRIVACY_POLICY_URL = "https://example.com/privacy"
    const val TERMS_OF_SERVICE_URL = "https://example.com/terms"
    const val HELP_URL = "https://example.com/help"

    // Export
    const val EXPORT_FILE_NAME = "speedtest_history"
    const val EXPORT_DATE_FORMAT = "yyyy-MM-dd_HH-mm-ss"

    // Animation
    const val ANIMATION_DURATION_SHORT = 200
    const val ANIMATION_DURATION_MEDIUM = 300
    const val ANIMATION_DURATION_LONG = 500
}
