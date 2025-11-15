package com.speedtest.app.data.remote.api

import android.util.Log
import com.speedtest.app.data.local.entity.Server
import com.speedtest.app.domain.model.TestPhase
import com.speedtest.app.domain.model.TestProgress
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs
import kotlin.math.sqrt

/**
 * Core engine for executing speed tests
 */
@Singleton
class SpeedTestEngine @Inject constructor() {

    private val TAG = "SpeedTestEngine"

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private var isCancelled = AtomicBoolean(false)

    /**
     * Execute full speed test
     */
    fun executeTest(server: Server): Flow<TestProgress> = flow {
        isCancelled.set(false)

        try {
            // Phase 1: Ping Test
            emit(TestProgress(phase = TestPhase.INITIALIZING))
            delay(500)

            val pingResults = testPing(server)
            emit(pingResults)

            if (isCancelled.get()) return@flow

            // Phase 2: Download Test
            val downloadResults = testDownloadSpeed(server, pingResults)
            emit(downloadResults)

            if (isCancelled.get()) return@flow

            // Phase 3: Upload Test
            val uploadResults = testUploadSpeed(server, downloadResults)
            emit(uploadResults)

            // Phase 4: Completed
            emit(uploadResults.copy(phase = TestPhase.COMPLETED))

        } catch (e: Exception) {
            Log.e(TAG, "Test failed", e)
            val errorMessage = when (e) {
                is IOException -> "Network connection error. Please check your internet connection."
                is java.net.UnknownHostException -> "Cannot reach test server. Please check server settings."
                is java.net.SocketTimeoutException -> "Connection timeout. Server may be unavailable."
                else -> e.message ?: "Unknown error occurred"
            }
            emit(TestProgress(
                phase = TestPhase.ERROR,
                error = errorMessage
            ))
        }
    }

    /**
     * Cancel current test
     */
    fun cancelTest() {
        isCancelled.set(true)
    }

    /**
     * Test ping/latency
     */
    private suspend fun testPing(server: Server): TestProgress = withContext(Dispatchers.IO) {
        val pingCount = 10
        val pings = mutableListOf<Int>()
        val url = "http://${server.host}:${server.port}/ping"

        for (i in 0 until pingCount) {
            if (isCancelled.get()) break

            try {
                val startTime = System.currentTimeMillis()

                val request = Request.Builder()
                    .url(url)
                    .head()
                    .build()

                httpClient.newCall(request).execute().use { response ->
                    val ping = (System.currentTimeMillis() - startTime).toInt()
                    pings.add(ping)
                }

            } catch (e: Exception) {
                Log.e(TAG, "Ping failed: ${e.message}")
            }

            delay(100)
        }

        if (pings.isEmpty()) {
            throw IOException("All ping attempts failed")
        }

        val minPing = pings.minOrNull() ?: 0
        val maxPing = pings.maxOrNull() ?: 0
        val avgPing = pings.average().toInt()
        val jitter = calculateJitter(pings)

        TestProgress(
            phase = TestPhase.PING,
            progress = 0.33f,
            currentPing = avgPing,
            minPing = minPing,
            maxPing = maxPing,
            avgPing = avgPing,
            jitter = jitter,
            serverId = server.id,
            serverName = server.name,
            serverLocation = "${server.city}, ${server.country}"
        )
    }

    /**
     * Test download speed
     */
    private suspend fun testDownloadSpeed(
        server: Server,
        previousProgress: TestProgress
    ): TestProgress = coroutineScope {
        val threadCount = 4
        val testDuration = 10_000L // 10 seconds
        val totalBytes = AtomicLong(0)
        val speeds = mutableListOf<Double>()
        var peakSpeed = 0.0

        val jobs = List(threadCount) { threadIndex ->
            async(Dispatchers.IO) {
                val startTime = System.currentTimeMillis()
                var threadBytes = 0L

                while (System.currentTimeMillis() - startTime < testDuration && !isCancelled.get()) {
                    try {
                        // Download a chunk of data
                        val size = 5 * 1024 * 1024 // 5MB chunks
                        val url = "http://${server.host}:${server.port}/download?size=$size"

                        val request = Request.Builder()
                            .url(url)
                            .get()
                            .build()

                        val chunkStartTime = System.currentTimeMillis()
                        httpClient.newCall(request).execute().use { response ->
                            val bytes = response.body?.bytes()?.size?.toLong() ?: 0
                            threadBytes += bytes
                            totalBytes.addAndGet(bytes)

                            val chunkDuration = System.currentTimeMillis() - chunkStartTime
                            if (chunkDuration > 0) {
                                val speed = (bytes * 8.0) / (chunkDuration / 1000.0) / 1_000_000.0 // Mbps
                                synchronized(speeds) {
                                    speeds.add(speed)
                                    if (speed > peakSpeed) {
                                        peakSpeed = speed
                                    }
                                }
                            }
                        }

                    } catch (e: Exception) {
                        Log.e(TAG, "Download chunk failed: ${e.message}")
                        delay(100)
                    }
                }

                threadBytes
            }
        }

        // Monitor progress
        val monitorJob = launch {
            val startTime = System.currentTimeMillis()
            while (isActive && !isCancelled.get()) {
                val elapsed = System.currentTimeMillis() - startTime
                val currentBytes = totalBytes.get()
                val currentSpeed = if (elapsed > 0) {
                    (currentBytes * 8.0) / (elapsed / 1000.0) / 1_000_000.0 // Mbps
                } else {
                    0.0
                }

                delay(100)
            }
        }

        // Wait for all threads
        jobs.awaitAll()
        monitorJob.cancel()

        val finalBytes = totalBytes.get()
        val actualDuration = testDuration / 1000.0
        val avgSpeed = (finalBytes * 8.0) / actualDuration / 1_000_000.0 // Mbps

        previousProgress.copy(
            phase = TestPhase.DOWNLOAD,
            progress = 0.66f,
            currentDownloadSpeed = avgSpeed,
            avgDownloadSpeed = avgSpeed,
            peakDownloadSpeed = peakSpeed,
            bytesDownloaded = finalBytes
        )
    }

    /**
     * Test upload speed
     */
    private suspend fun testUploadSpeed(
        server: Server,
        previousProgress: TestProgress
    ): TestProgress = coroutineScope {
        val threadCount = 4
        val testDuration = 10_000L // 10 seconds
        val totalBytes = AtomicLong(0)
        val speeds = mutableListOf<Double>()
        var peakSpeed = 0.0

        val jobs = List(threadCount) { threadIndex ->
            async(Dispatchers.IO) {
                val startTime = System.currentTimeMillis()
                var threadBytes = 0L

                while (System.currentTimeMillis() - startTime < testDuration && !isCancelled.get()) {
                    try {
                        // Upload a chunk of data
                        val size = 2 * 1024 * 1024 // 2MB chunks
                        val data = ByteArray(size) { it.toByte() }
                        val url = "http://${server.host}:${server.port}/upload"

                        val requestBody = data.toRequestBody("application/octet-stream".toMediaType())
                        val request = Request.Builder()
                            .url(url)
                            .post(requestBody)
                            .build()

                        val chunkStartTime = System.currentTimeMillis()
                        httpClient.newCall(request).execute().use { response ->
                            threadBytes += size
                            totalBytes.addAndGet(size.toLong())

                            val chunkDuration = System.currentTimeMillis() - chunkStartTime
                            if (chunkDuration > 0) {
                                val speed = (size * 8.0) / (chunkDuration / 1000.0) / 1_000_000.0 // Mbps
                                synchronized(speeds) {
                                    speeds.add(speed)
                                    if (speed > peakSpeed) {
                                        peakSpeed = speed
                                    }
                                }
                            }
                        }

                    } catch (e: Exception) {
                        Log.e(TAG, "Upload chunk failed: ${e.message}")
                        delay(100)
                    }
                }

                threadBytes
            }
        }

        // Wait for all threads
        jobs.awaitAll()

        val finalBytes = totalBytes.get()
        val actualDuration = testDuration / 1000.0
        val avgSpeed = (finalBytes * 8.0) / actualDuration / 1_000_000.0 // Mbps

        previousProgress.copy(
            phase = TestPhase.UPLOAD,
            progress = 1.0f,
            currentUploadSpeed = avgSpeed,
            avgUploadSpeed = avgSpeed,
            peakUploadSpeed = peakSpeed,
            bytesUploaded = finalBytes
        )
    }

    /**
     * Calculate jitter from ping measurements
     */
    private fun calculateJitter(pings: List<Int>): Double {
        if (pings.size < 2) return 0.0

        val differences = mutableListOf<Double>()
        for (i in 1 until pings.size) {
            differences.add(abs(pings[i] - pings[i - 1]).toDouble())
        }

        return differences.average()
    }

    /**
     * Calculate packet loss percentage
     */
    private fun calculatePacketLoss(sent: Int, received: Int): Double {
        if (sent == 0) return 0.0
        return ((sent - received).toDouble() / sent) * 100.0
    }
}
