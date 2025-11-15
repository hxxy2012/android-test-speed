package com.speedtest.app.domain.model

/**
 * Represents the progress and current state of a speed test
 */
data class TestProgress(
    val phase: TestPhase = TestPhase.IDLE,
    val progress: Float = 0f, // 0.0 to 1.0

    // Current speeds (Mbps)
    val currentDownloadSpeed: Double = 0.0,
    val currentUploadSpeed: Double = 0.0,

    // Peak speeds (Mbps)
    val peakDownloadSpeed: Double = 0.0,
    val peakUploadSpeed: Double = 0.0,

    // Average speeds (Mbps)
    val avgDownloadSpeed: Double = 0.0,
    val avgUploadSpeed: Double = 0.0,

    // Latency (ms)
    val currentPing: Int = 0,
    val minPing: Int = 0,
    val maxPing: Int = 0,
    val avgPing: Int = 0,
    val jitter: Double = 0.0,

    // Packet loss
    val packetLoss: Double = 0.0,

    // Data transferred
    val bytesDownloaded: Long = 0L,
    val bytesUploaded: Long = 0L,

    // Server info
    val serverId: String? = null,
    val serverName: String? = null,
    val serverLocation: String? = null,

    // Error info
    val error: String? = null
) {
    val isInProgress: Boolean
        get() = phase in listOf(
            TestPhase.INITIALIZING,
            TestPhase.PING,
            TestPhase.DOWNLOAD,
            TestPhase.UPLOAD
        )

    val isCompleted: Boolean
        get() = phase == TestPhase.COMPLETED

    val hasError: Boolean
        get() = phase == TestPhase.ERROR
}
