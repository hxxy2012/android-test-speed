package com.speedtest.app.data.local.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

/**
 * Manager for app preferences using DataStore
 */
class PreferencesManager(private val context: Context) {

    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "speedtest_preferences")

    companion object {
        // Keys
        private val SELECTED_SERVER_ID = stringPreferencesKey("selected_server_id")
        private val SPEED_UNIT = stringPreferencesKey("speed_unit") // MBPS or MBYTES
        private val THEME_MODE = stringPreferencesKey("theme_mode") // SYSTEM, LIGHT, DARK
        private val LANGUAGE = stringPreferencesKey("language") // EN, ZH
        private val TEST_DURATION = intPreferencesKey("test_duration") // seconds
        private val THREAD_COUNT = intPreferencesKey("thread_count")
        private val AUTO_TEST_ENABLED = booleanPreferencesKey("auto_test_enabled")
        private val AUTO_TEST_INTERVAL = intPreferencesKey("auto_test_interval") // hours
        private val NOTIFICATION_ENABLED = booleanPreferencesKey("notification_enabled")
        private val FIRST_LAUNCH = booleanPreferencesKey("first_launch")
        private val PRIVACY_ACCEPTED = booleanPreferencesKey("privacy_accepted")
    }

    // Selected Server ID
    val selectedServerId: Flow<String?> = context.dataStore.data
        .catch { handleException(it) }
        .map { preferences -> preferences[SELECTED_SERVER_ID] }

    suspend fun setSelectedServerId(serverId: String) {
        context.dataStore.edit { preferences ->
            preferences[SELECTED_SERVER_ID] = serverId
        }
    }

    // Speed Unit
    val speedUnit: Flow<SpeedUnit> = context.dataStore.data
        .catch { handleException(it) }
        .map { preferences ->
            SpeedUnit.valueOf(preferences[SPEED_UNIT] ?: SpeedUnit.MBPS.name)
        }

    suspend fun setSpeedUnit(unit: SpeedUnit) {
        context.dataStore.edit { preferences ->
            preferences[SPEED_UNIT] = unit.name
        }
    }

    // Theme Mode
    val themeMode: Flow<ThemeMode> = context.dataStore.data
        .catch { handleException(it) }
        .map { preferences ->
            ThemeMode.valueOf(preferences[THEME_MODE] ?: ThemeMode.SYSTEM.name)
        }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { preferences ->
            preferences[THEME_MODE] = mode.name
        }
    }

    // Language
    val language: Flow<String> = context.dataStore.data
        .catch { handleException(it) }
        .map { preferences ->
            preferences[LANGUAGE] ?: "EN"
        }

    suspend fun setLanguage(lang: String) {
        context.dataStore.edit { preferences ->
            preferences[LANGUAGE] = lang
        }
    }

    // Test Duration
    val testDuration: Flow<Int> = context.dataStore.data
        .catch { handleException(it) }
        .map { preferences ->
            preferences[TEST_DURATION] ?: 30 // Default 30 seconds
        }

    suspend fun setTestDuration(duration: Int) {
        context.dataStore.edit { preferences ->
            preferences[TEST_DURATION] = duration
        }
    }

    // Thread Count
    val threadCount: Flow<Int> = context.dataStore.data
        .catch { handleException(it) }
        .map { preferences ->
            preferences[THREAD_COUNT] ?: 0 // 0 = auto
        }

    suspend fun setThreadCount(count: Int) {
        context.dataStore.edit { preferences ->
            preferences[THREAD_COUNT] = count
        }
    }

    // Auto Test
    val autoTestEnabled: Flow<Boolean> = context.dataStore.data
        .catch { handleException(it) }
        .map { preferences ->
            preferences[AUTO_TEST_ENABLED] ?: false
        }

    suspend fun setAutoTestEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[AUTO_TEST_ENABLED] = enabled
        }
    }

    val autoTestInterval: Flow<Int> = context.dataStore.data
        .catch { handleException(it) }
        .map { preferences ->
            preferences[AUTO_TEST_INTERVAL] ?: 24 // Default 24 hours
        }

    suspend fun setAutoTestInterval(hours: Int) {
        context.dataStore.edit { preferences ->
            preferences[AUTO_TEST_INTERVAL] = hours
        }
    }

    // Notifications
    val notificationEnabled: Flow<Boolean> = context.dataStore.data
        .catch { handleException(it) }
        .map { preferences ->
            preferences[NOTIFICATION_ENABLED] ?: true
        }

    suspend fun setNotificationEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[NOTIFICATION_ENABLED] = enabled
        }
    }

    // First Launch
    val isFirstLaunch: Flow<Boolean> = context.dataStore.data
        .catch { handleException(it) }
        .map { preferences ->
            preferences[FIRST_LAUNCH] ?: true
        }

    suspend fun setFirstLaunchComplete() {
        context.dataStore.edit { preferences ->
            preferences[FIRST_LAUNCH] = false
        }
    }

    // Privacy
    val privacyAccepted: Flow<Boolean> = context.dataStore.data
        .catch { handleException(it) }
        .map { preferences ->
            preferences[PRIVACY_ACCEPTED] ?: false
        }

    suspend fun setPrivacyAccepted(accepted: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PRIVACY_ACCEPTED] = accepted
        }
    }

    // Clear all preferences
    suspend fun clearAll() {
        context.dataStore.edit { it.clear() }
    }

    private fun <T> handleException(exception: Throwable): Preferences {
        if (exception is IOException) {
            // Handle IOException appropriately
            exception.printStackTrace()
        }
        return emptyPreferences()
    }
}

enum class SpeedUnit {
    MBPS,    // Megabits per second
    MBYTES   // Megabytes per second
}

enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK
}
