package com.speedtest.app.data.local.dao

import androidx.room.*
import com.speedtest.app.data.local.entity.SpeedTestResult
import kotlinx.coroutines.flow.Flow

/**
 * DAO for SpeedTestResult operations
 */
@Dao
interface SpeedTestResultDao {

    @Query("SELECT * FROM speed_test_results ORDER BY timestamp DESC")
    fun getAllResults(): Flow<List<SpeedTestResult>>

    @Query("SELECT * FROM speed_test_results ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentResults(limit: Int = 10): Flow<List<SpeedTestResult>>

    @Query("SELECT * FROM speed_test_results WHERE id = :id")
    suspend fun getResultById(id: Long): SpeedTestResult?

    @Query("""
        SELECT * FROM speed_test_results
        WHERE timestamp >= :startTime AND timestamp <= :endTime
        ORDER BY timestamp DESC
    """)
    fun getResultsByDateRange(startTime: Long, endTime: Long): Flow<List<SpeedTestResult>>

    @Query("""
        SELECT * FROM speed_test_results
        WHERE networkType = :networkType
        ORDER BY timestamp DESC
    """)
    fun getResultsByNetworkType(networkType: String): Flow<List<SpeedTestResult>>

    @Query("""
        SELECT * FROM speed_test_results
        WHERE timestamp >= :startTime
        ORDER BY timestamp DESC
    """)
    fun getResultsSince(startTime: Long): Flow<List<SpeedTestResult>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertResult(result: SpeedTestResult): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertResults(results: List<SpeedTestResult>)

    @Update
    suspend fun updateResult(result: SpeedTestResult)

    @Delete
    suspend fun deleteResult(result: SpeedTestResult)

    @Query("DELETE FROM speed_test_results WHERE id = :id")
    suspend fun deleteResultById(id: Long)

    @Query("DELETE FROM speed_test_results")
    suspend fun deleteAllResults()

    @Query("DELETE FROM speed_test_results WHERE timestamp < :timestamp")
    suspend fun deleteResultsOlderThan(timestamp: Long)

    @Query("SELECT COUNT(*) FROM speed_test_results")
    suspend fun getResultCount(): Int

    @Query("SELECT AVG(downloadSpeed) FROM speed_test_results WHERE timestamp >= :startTime")
    suspend fun getAverageDownloadSpeed(startTime: Long): Double?

    @Query("SELECT AVG(uploadSpeed) FROM speed_test_results WHERE timestamp >= :startTime")
    suspend fun getAverageUploadSpeed(startTime: Long): Double?

    @Query("SELECT MAX(downloadSpeed) FROM speed_test_results")
    suspend fun getMaxDownloadSpeed(): Double?

    @Query("SELECT MAX(uploadSpeed) FROM speed_test_results")
    suspend fun getMaxUploadSpeed(): Double?
}
