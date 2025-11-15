package com.speedtest.app.domain.usecase

import com.speedtest.app.data.local.entity.SpeedTestResult
import com.speedtest.app.domain.repository.SpeedTestRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * UseCase for getting test history
 */
class GetTestHistoryUseCase @Inject constructor(
    private val repository: SpeedTestRepository
) {
    fun getAllResults(): Flow<List<SpeedTestResult>> {
        return repository.getAllResults()
    }

    fun getRecentResults(limit: Int = 10): Flow<List<SpeedTestResult>> {
        return repository.getRecentResults(limit)
    }

    fun getResultsByDateRange(startTime: Long, endTime: Long): Flow<List<SpeedTestResult>> {
        return repository.getResultsByDateRange(startTime, endTime)
    }

    fun getResultsByNetworkType(networkType: String): Flow<List<SpeedTestResult>> {
        return repository.getResultsByNetworkType(networkType)
    }
}
