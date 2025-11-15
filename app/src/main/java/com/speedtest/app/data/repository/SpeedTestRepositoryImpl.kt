package com.speedtest.app.data.repository

import android.util.Log
import com.speedtest.app.data.local.dao.SpeedTestResultDao
import com.speedtest.app.data.local.entity.Server
import com.speedtest.app.data.local.entity.SpeedTestResult
import com.speedtest.app.data.remote.api.SpeedTestEngine
import com.speedtest.app.domain.model.TestPhase
import com.speedtest.app.domain.model.TestProgress
import com.speedtest.app.domain.repository.NetworkRepository
import com.speedtest.app.domain.repository.ServerRepository
import com.speedtest.app.domain.repository.SpeedTestRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of SpeedTestRepository
 */
@Singleton
class SpeedTestRepositoryImpl @Inject constructor(
    private val speedTestEngine: SpeedTestEngine,
    private val speedTestResultDao: SpeedTestResultDao,
    private val serverRepository: ServerRepository,
    private val networkRepository: NetworkRepository
) : SpeedTestRepository {

    private val TAG = "SpeedTestRepository"

    override fun executeSpeedTest(serverId: String?): Flow<TestProgress> = flow {
        try {
            // Get server
            val server = if (serverId != null) {
                serverRepository.getServerById(serverId)
            } else {
                serverRepository.getFastestServer()
            } ?: throw Exception("No server available")

            // Get network info
            val networkInfo = networkRepository.getCurrentNetworkInfo()

            // Execute test
            speedTestEngine.executeTest(server).collect { progress ->
                emit(progress)

                // Save result when completed
                if (progress.phase == TestPhase.COMPLETED) {
                    try {
                        val result = SpeedTestResult(
                            timestamp = System.currentTimeMillis(),
                            downloadSpeed = progress.avgDownloadSpeed,
                            uploadSpeed = progress.avgUploadSpeed,
                            peakDownloadSpeed = progress.peakDownloadSpeed,
                            peakUploadSpeed = progress.peakUploadSpeed,
                            ping = progress.avgPing,
                            jitter = progress.jitter,
                            minPing = progress.minPing,
                            maxPing = progress.maxPing,
                            avgPing = progress.avgPing,
                            packetLoss = progress.packetLoss,
                            serverId = server.id,
                            serverName = server.name,
                            serverLocation = "${server.city}, ${server.country}",
                            serverHost = server.host,
                            networkType = networkInfo.networkType.name,
                            operator = networkInfo.operator,
                            ipAddress = networkInfo.ipAddress,
                            bytesDownloaded = progress.bytesDownloaded,
                            bytesUploaded = progress.bytesUploaded
                        )
                        saveResult(result)
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to save result", e)
                    }
                }
            }

        } catch (e: Exception) {
            Log.e(TAG, "Speed test failed", e)
            emit(TestProgress(
                phase = TestPhase.ERROR,
                error = e.message ?: "Unknown error"
            ))
        }
    }

    override suspend fun stopSpeedTest() {
        speedTestEngine.cancelTest()
    }

    override fun getAllResults(): Flow<List<SpeedTestResult>> {
        return speedTestResultDao.getAllResults()
    }

    override fun getRecentResults(limit: Int): Flow<List<SpeedTestResult>> {
        return speedTestResultDao.getRecentResults(limit)
    }

    override fun getResultsByDateRange(startTime: Long, endTime: Long): Flow<List<SpeedTestResult>> {
        return speedTestResultDao.getResultsByDateRange(startTime, endTime)
    }

    override fun getResultsByNetworkType(networkType: String): Flow<List<SpeedTestResult>> {
        return speedTestResultDao.getResultsByNetworkType(networkType)
    }

    override suspend fun saveResult(result: SpeedTestResult): Long {
        return speedTestResultDao.insertResult(result)
    }

    override suspend fun deleteResult(resultId: Long) {
        speedTestResultDao.deleteResultById(resultId)
    }

    override suspend fun deleteAllResults() {
        speedTestResultDao.deleteAllResults()
    }

    override suspend fun getAverageDownloadSpeed(startTime: Long): Double? {
        return speedTestResultDao.getAverageDownloadSpeed(startTime)
    }

    override suspend fun getAverageUploadSpeed(startTime: Long): Double? {
        return speedTestResultDao.getAverageUploadSpeed(startTime)
    }

    override suspend fun getMaxDownloadSpeed(): Double? {
        return speedTestResultDao.getMaxDownloadSpeed()
    }

    override suspend fun getMaxUploadSpeed(): Double? {
        return speedTestResultDao.getMaxUploadSpeed()
    }
}
