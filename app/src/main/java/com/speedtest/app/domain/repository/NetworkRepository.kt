package com.speedtest.app.domain.repository

import com.speedtest.app.domain.model.NetworkInfo
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for network information
 */
interface NetworkRepository {

    /**
     * Get current network information as Flow
     */
    fun getNetworkInfo(): Flow<NetworkInfo>

    /**
     * Get current network information (one-time)
     */
    suspend fun getCurrentNetworkInfo(): NetworkInfo

    /**
     * Check if device is connected to network
     */
    suspend fun isConnected(): Boolean

    /**
     * Get external IP address
     */
    suspend fun getExternalIpAddress(): String
}
