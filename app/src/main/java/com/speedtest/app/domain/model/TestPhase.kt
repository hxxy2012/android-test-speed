package com.speedtest.app.domain.model

/**
 * Represents different phases of speed test
 */
enum class TestPhase {
    IDLE,           // Not testing
    INITIALIZING,   // Preparing test
    PING,          // Testing latency
    DOWNLOAD,      // Testing download speed
    UPLOAD,        // Testing upload speed
    COMPLETED,     // Test completed successfully
    ERROR          // Test failed
}
