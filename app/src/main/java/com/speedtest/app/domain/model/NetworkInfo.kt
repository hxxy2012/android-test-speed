package com.speedtest.app.domain.model

/**
 * Represents current network information
 */
data class NetworkInfo(
    val networkType: NetworkType = NetworkType.NONE,
    val operator: String = "Unknown",
    val ipAddress: String = "0.0.0.0",
    val isConnected: Boolean = false,
    val isMetered: Boolean = false
)
