package com.speedtest.app.presentation.history

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.speedtest.app.data.local.entity.SpeedTestResult
import com.speedtest.app.domain.repository.SpeedTestRepository
import com.speedtest.app.domain.usecase.GetTestHistoryUseCase
import com.speedtest.app.utils.ExportUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

/**
 * ViewModel for History screen
 */
@HiltViewModel
class HistoryViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val getTestHistoryUseCase: GetTestHistoryUseCase,
    private val speedTestRepository: SpeedTestRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    private val _filterPeriod = MutableStateFlow(FilterPeriod.ALL)
    private val _filterNetworkType = MutableStateFlow<String?>(null)

    init {
        loadHistory()
    }

    private fun loadHistory() {
        viewModelScope.launch {
            combine(
                _filterPeriod,
                _filterNetworkType
            ) { period, networkType ->
                Pair(period, networkType)
            }.collectLatest { (period, networkType) ->
                val results = when {
                    networkType != null -> {
                        getTestHistoryUseCase.getResultsByNetworkType(networkType)
                    }
                    period != FilterPeriod.ALL -> {
                        val (startTime, endTime) = period.getTimeRange()
                        getTestHistoryUseCase.getResultsByDateRange(startTime, endTime)
                    }
                    else -> {
                        getTestHistoryUseCase.getAllResults()
                    }
                }

                results.collect { list ->
                    _uiState.update { it.copy(
                        results = list,
                        isLoading = false,
                        filterPeriod = period,
                        filterNetworkType = networkType
                    ) }
                }
            }
        }
    }

    fun setFilterPeriod(period: FilterPeriod) {
        _filterPeriod.value = period
    }

    fun setFilterNetworkType(networkType: String?) {
        _filterNetworkType.value = networkType
    }

    fun deleteResult(resultId: Long) {
        viewModelScope.launch {
            try {
                speedTestRepository.deleteResult(resultId)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun deleteAllResults() {
        viewModelScope.launch {
            try {
                speedTestRepository.deleteAllResults()
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    suspend fun exportToCsv(): Result<Uri> {
        return try {
            val results = _uiState.value.results
            if (results.isEmpty()) {
                return Result.failure(Exception("No test results to export"))
            }
            ExportUtils.exportToCsv(context, results)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun exportToJson(): Result<Uri> {
        return try {
            val results = _uiState.value.results
            if (results.isEmpty()) {
                return Result.failure(Exception("No test results to export"))
            }
            ExportUtils.exportToJson(context, results)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun shareExportedFile(uri: Uri, mimeType: String) {
        ExportUtils.shareFile(context, uri, mimeType)
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

/**
 * UI State for History screen
 */
data class HistoryUiState(
    val isLoading: Boolean = true,
    val results: List<SpeedTestResult> = emptyList(),
    val filterPeriod: FilterPeriod = FilterPeriod.ALL,
    val filterNetworkType: String? = null,
    val error: String? = null
)

/**
 * Filter periods for history
 */
enum class FilterPeriod {
    TODAY,
    WEEK,
    MONTH,
    ALL;

    fun getTimeRange(): Pair<Long, Long> {
        val calendar = Calendar.getInstance()
        val endTime = calendar.timeInMillis

        calendar.apply {
            when (this@FilterPeriod) {
                TODAY -> {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                WEEK -> {
                    add(Calendar.DAY_OF_YEAR, -7)
                }
                MONTH -> {
                    add(Calendar.MONTH, -1)
                }
                ALL -> {
                    timeInMillis = 0
                }
            }
        }

        return Pair(calendar.timeInMillis, endTime)
    }
}
