package com.speedtest.app.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.speedtest.app.data.local.datastore.PreferencesManager
import com.speedtest.app.data.local.datastore.SpeedUnit
import com.speedtest.app.data.local.datastore.ThemeMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Settings screen
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

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
            preferencesManager.setAutoTestEnabled(enabled)
        }
    }

    fun setAutoTestInterval(hours: Int) {
        viewModelScope.launch {
            preferencesManager.setAutoTestInterval(hours)
        }
    }

    fun setNotificationEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.setNotificationEnabled(enabled)
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            // TODO: Implement clear all data
        }
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
