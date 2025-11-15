package com.speedtest.app.presentation.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.speedtest.app.data.local.datastore.PreferencesManager
import com.speedtest.app.data.local.datastore.SpeedUnit
import com.speedtest.app.data.local.datastore.ThemeMode
import com.speedtest.app.domain.repository.SpeedTestRepository
import com.speedtest.app.worker.WorkManagerScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Settings screen
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val preferencesManager: PreferencesManager,
    private val speedTestRepository: SpeedTestRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            combine(
                preferencesManager.speedUnit,
                preferencesManager.themeMode,
                preferencesManager.testDuration,
                preferencesManager.threadCount,
                preferencesManager.autoTestEnabled,
                preferencesManager.autoTestInterval,
                preferencesManager.notificationEnabled
            ) { speedUnit, themeMode, testDuration, threadCount, autoTestEnabled, autoTestInterval, notificationEnabled ->
                SettingsUiState(
                    speedUnit = speedUnit,
                    themeMode = themeMode,
                    testDuration = testDuration,
                    threadCount = threadCount,
                    autoTestEnabled = autoTestEnabled,
                    autoTestInterval = autoTestInterval,
                    notificationEnabled = notificationEnabled
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    fun setSpeedUnit(unit: SpeedUnit) {
        viewModelScope.launch {
            preferencesManager.setSpeedUnit(unit)
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            preferencesManager.setThemeMode(mode)
        }
    }

    fun setTestDuration(duration: Int) {
        viewModelScope.launch {
            preferencesManager.setTestDuration(duration)
        }
    }

    fun setThreadCount(count: Int) {
        viewModelScope.launch {
            preferencesManager.setThreadCount(count)
        }
    }

    fun setAutoTestEnabled(enabled: Boolean) {
        viewModelScope.launch {
            try {
                preferencesManager.setAutoTestEnabled(enabled)

                // Schedule or cancel auto test
                if (enabled) {
                    val interval = preferencesManager.autoTestInterval.first()
                    WorkManagerScheduler.scheduleAutoSpeedTest(context, interval)
                } else {
                    WorkManagerScheduler.cancelAutoSpeedTest(context)
                }
            } catch (e: Exception) {
                _error.value = "Failed to update auto test: ${e.message}"
            }
        }
    }

    fun setAutoTestInterval(hours: Int) {
        viewModelScope.launch {
            try {
                preferencesManager.setAutoTestInterval(hours)

                // Reschedule if auto test is enabled
                val isEnabled = preferencesManager.autoTestEnabled.first()
                if (isEnabled) {
                    WorkManagerScheduler.scheduleAutoSpeedTest(context, hours)
                }
            } catch (e: Exception) {
                _error.value = "Failed to update interval: ${e.message}"
            }
        }
    }

    fun setNotificationEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.setNotificationEnabled(enabled)
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            try {
                speedTestRepository.deleteAllResults()
            } catch (e: Exception) {
                _error.value = "Failed to clear data: ${e.message}"
            }
        }
    }

    fun clearError() {
        _error.value = null
    }
}

/**
 * UI State for Settings screen
 */
data class SettingsUiState(
    val speedUnit: SpeedUnit = SpeedUnit.MBPS,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val testDuration: Int = 30,
    val threadCount: Int = 0, // 0 = auto
    val autoTestEnabled: Boolean = false,
    val autoTestInterval: Int = 24, // hours
    val notificationEnabled: Boolean = true
)
