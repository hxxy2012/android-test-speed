package com.speedtest.app.presentation.home

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.speedtest.app.data.local.datastore.PreferencesManager
import com.speedtest.app.data.local.datastore.SpeedUnit
import com.speedtest.app.domain.model.NetworkInfo
import com.speedtest.app.domain.model.TestPhase
import com.speedtest.app.domain.model.TestProgress
import com.speedtest.app.domain.usecase.ExecuteSpeedTestUseCase
import com.speedtest.app.domain.usecase.GetNetworkInfoUseCase
import com.speedtest.app.domain.usecase.GetServersUseCase
import com.speedtest.app.utils.NotificationHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Home/Speed Test screen
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val executeSpeedTestUseCase: ExecuteSpeedTestUseCase,
    private val getNetworkInfoUseCase: GetNetworkInfoUseCase,
    private val getServersUseCase: GetServersUseCase,
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        observeNetworkInfo()
        observeSpeedUnit()
        loadServers()
    }

    private fun observeNetworkInfo() {
        viewModelScope.launch {
            getNetworkInfoUseCase.getNetworkInfo().collect { networkInfo ->
                _uiState.update { it.copy(networkInfo = networkInfo) }
            }
        }
    }

    private fun observeSpeedUnit() {
        viewModelScope.launch {
            preferencesManager.speedUnit.collect { unit ->
                _uiState.update { it.copy(speedUnit = unit) }
            }
        }
    }

    private fun loadServers() {
        viewModelScope.launch {
            try {
                // Sync servers from remote
                getServersUseCase.syncServers()

                // Get fastest server
                val server = getServersUseCase.getFastestServer()
                _uiState.update { it.copy(selectedServer = server) }
            } catch (e: Exception) {
                // Use default server if sync fails
            }
        }
    }

    fun startTest() {
        viewModelScope.launch {
            try {
                // Check network connection first
                val networkInfo = _uiState.value.networkInfo
                if (!networkInfo.isConnected) {
                    _uiState.update { it.copy(error = "No network connection") }
                    return@launch
                }

                _uiState.update { it.copy(isLoading = true, error = null) }

                val serverId = _uiState.value.selectedServer?.id

                executeSpeedTestUseCase(serverId).collect { progress ->
                    _uiState.update { it.copy(
                        testProgress = progress,
                        isLoading = progress.isInProgress
                    ) }

                    // Show notification when test completes
                    if (progress.isCompleted) {
                        val notificationEnabled = preferencesManager.notificationEnabled.first()
                        if (notificationEnabled) {
                            NotificationHelper.showTestCompletionNotification(
                                context,
                                downloadSpeed = progress.avgDownloadSpeed,
                                uploadSpeed = progress.avgUploadSpeed,
                                ping = progress.avgPing
                            )
                        }
                    }
                }

            } catch (e: Exception) {
                _uiState.update { it.copy(
                    isLoading = false,
                    error = e.message ?: "Unknown error"
                ) }
            }
        }
    }

    fun stopTest() {
        viewModelScope.launch {
            executeSpeedTestUseCase.stop()
            _uiState.update { it.copy(
                isLoading = false,
                testProgress = TestProgress(phase = TestPhase.IDLE)
            ) }
        }
    }

    fun selectServer(serverId: String?) {
        viewModelScope.launch {
            val server = if (serverId != null) {
                getServersUseCase.getServerById(serverId)
            } else {
                getServersUseCase.getFastestServer()
            }
            _uiState.update { it.copy(selectedServer = server) }

            // Save preference
            serverId?.let {
                preferencesManager.setSelectedServerId(it)
            }
        }
    }

    fun toggleSpeedUnit() {
        viewModelScope.launch {
            val newUnit = when (_uiState.value.speedUnit) {
                SpeedUnit.MBPS -> SpeedUnit.MBYTES
                SpeedUnit.MBYTES -> SpeedUnit.MBPS
            }
            preferencesManager.setSpeedUnit(newUnit)
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

/**
 * UI State for Home screen
 */
data class HomeUiState(
    val isLoading: Boolean = false,
    val testProgress: TestProgress = TestProgress(),
    val networkInfo: NetworkInfo = NetworkInfo(),
    val selectedServer: com.speedtest.app.data.local.entity.Server? = null,
    val speedUnit: SpeedUnit = SpeedUnit.MBPS,
    val error: String? = null
) {
    val isTestRunning: Boolean
        get() = testProgress.isInProgress

    val canStartTest: Boolean
        get() = !isLoading && !isTestRunning && networkInfo.isConnected
}
