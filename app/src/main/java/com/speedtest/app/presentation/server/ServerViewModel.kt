package com.speedtest.app.presentation.server

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.speedtest.app.data.local.datastore.PreferencesManager
import com.speedtest.app.data.local.entity.Server
import com.speedtest.app.domain.usecase.GetServersUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Server Selection screen
 */
@HiltViewModel
class ServerViewModel @Inject constructor(
    private val getServersUseCase: GetServersUseCase,
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(ServerUiState())
    val uiState: StateFlow<ServerUiState> = _uiState.asStateFlow()

    init {
        loadServers()
    }

    private fun loadServers() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                // Sync servers from remote
                getServersUseCase.syncServers()
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    isLoading = false,
                    error = e.message
                ) }
                return@launch
            }

            // Load servers (this is a continuous flow)
            getServersUseCase.getAllActiveServers()
                .catch { e ->
                    _uiState.update { it.copy(
                        isLoading = false,
                        error = e.message
                    ) }
                }
                .collect { servers ->
                    _uiState.update { it.copy(
                        servers = servers,
                        isLoading = false
                    ) }
                }
        }
    }

    fun pingAllServers() {
        viewModelScope.launch {
            _uiState.update { it.copy(isPinging = true) }

            try {
                getServersUseCase.pingAllServers()
                _uiState.update { it.copy(isPinging = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    isPinging = false,
                    error = e.message
                ) }
            }
        }
    }

    fun selectServer(server: Server) {
        viewModelScope.launch {
            _uiState.update { it.copy(selectedServer = server) }
            // Save to preferences
            preferencesManager.setSelectedServerId(server.id)
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

/**
 * UI State for Server screen
 */
data class ServerUiState(
    val isLoading: Boolean = true,
    val isPinging: Boolean = false,
    val servers: List<Server> = emptyList(),
    val selectedServer: Server? = null,
    val error: String? = null
)
