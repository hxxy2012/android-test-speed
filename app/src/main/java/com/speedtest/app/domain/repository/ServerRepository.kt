package com.speedtest.app.domain.repository

import com.speedtest.app.data.local.entity.Server
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for server operations
 */
interface ServerRepository {

    /**
     * Get all active servers
     */
    fun getAllActiveServers(): Flow<List<Server>>

    /**
     * Get all servers
     */
    fun getAllServers(): Flow<List<Server>>

    /**
     * Get server by ID
     */
    suspend fun getServerById(id: String): Server?

    /**
     * Get servers by country
     */
    fun getServersByCountry(country: String): Flow<List<Server>>

    /**
     * Get nearest server based on location
     */
    suspend fun getNearestServer(): Server?

    /**
     * Get fastest server based on ping
     */
    suspend fun getFastestServer(): Server?

    /**
     * Test ping to server
     */
    suspend fun pingServer(server: Server): Int

    /**
     * Test ping to all servers and update their latency
     */
    suspend fun pingAllServers()

    /**
     * Fetch servers from remote API
     */
    suspend fun fetchServersFromRemote(): Result<List<Server>>

    /**
     * Sync servers (fetch from remote and save to local)
     */
    suspend fun syncServers(): Result<Unit>

    /**
     * Save server
     */
    suspend fun saveServer(server: Server)

    /**
     * Delete server
     */
    suspend fun deleteServer(serverId: String)
}
