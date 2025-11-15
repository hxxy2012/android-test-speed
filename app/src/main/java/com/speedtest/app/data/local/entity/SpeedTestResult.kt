package com.speedtest.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room Entity for storing speed test results
 */
@Entity(tableName = "speed_test_results")
data class SpeedTestResult(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    // Timestamp
    val timestamp: Long,

    // Speed metrics (in Mbps)
    val downloadSpeed: Double,
    val uploadSpeed: Double,
    val peakDownloadSpeed: Double = 0.0,
    val peakUploadSpeed: Double = 0.0,

    // Latency metrics (in ms)
    val ping: Int,
    val jitter: Double,
    val minPing: Int = 0,
    val maxPing: Int = 0,
    val avgPing: Int = 0,

    // Packet loss (percentage)
    val packetLoss: Double = 0.0,

    // Server information
    val serverId: String,
    val serverName: String,
    val serverLocation: String,
    val serverHost: String = "",

    // Network information
    val networkType: String,    // WIFI, 4G, 5G, ETHERNET
    val operator: String,        // Network operator/carrier
    val ipAddress: String,

    // Test metadata
    val testDuration: Long = 0,  // Duration in milliseconds
    val bytesDownloaded: Long = 0,
    val bytesUploaded: Long = 0
)
