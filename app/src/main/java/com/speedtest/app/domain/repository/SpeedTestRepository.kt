package com.speedtest.app.domain.repository

import com.speedtest.app.data.local.entity.Server
import com.speedtest.app.data.local.entity.SpeedTestResult
import com.speedtest.app.domain.model.TestProgress
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for speed test operations
 */
interface SpeedTestRepository {

    /**
     * Execute a speed test
     * @return Flow emitting test progress updates
     */
    fun executeSpeedTest(serverId: String?): Flow<TestProgress>

    /**
     * Stop current speed test
     */
    suspend fun stopSpeedTest()

    /**
     * Get all test results
     */
    fun getAllResults(): Flow<List<SpeedTestResult>>

    /**
     * Get recent test results
     */
    fun getRecentResults(limit: Int): Flow<List<SpeedTestResult>>

    /**
     * Get results by date range
     */
    fun getResultsByDateRange(startTime: Long, endTime: Long): Flow<List<SpeedTestResult>>

    /**
     * Get results by network type
     */
    fun getResultsByNetworkType(networkType: String): Flow<List<SpeedTestResult>>

    /**
     * Save test result
     */
    suspend fun saveResult(result: SpeedTestResult): Long

    /**
     * Delete test result
     */
    suspend fun deleteResult(resultId: Long)

    /**
     * Delete all test results
     */
    suspend fun deleteAllResults()

    /**
     * Get statistics
     */
    suspend fun getAverageDownloadSpeed(startTime: Long): Double?
    suspend fun getAverageUploadSpeed(startTime: Long): Double?
    suspend fun getMaxDownloadSpeed(): Double?
    suspend fun getMaxUploadSpeed(): Double?
}
