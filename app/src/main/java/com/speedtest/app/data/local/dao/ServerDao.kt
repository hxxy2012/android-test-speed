package com.speedtest.app.data.local.dao

import androidx.room.*
import com.speedtest.app.data.local.entity.Server
import kotlinx.coroutines.flow.Flow

/**
 * DAO for Server operations
 */
@Dao
interface ServerDao {

    @Query("SELECT * FROM servers WHERE isActive = 1 ORDER BY lastPing ASC")
    fun getAllActiveServers(): Flow<List<Server>>

    @Query("SELECT * FROM servers ORDER BY lastPing ASC")
    fun getAllServers(): Flow<List<Server>>

    @Query("SELECT * FROM servers WHERE id = :id")
    suspend fun getServerById(id: String): Server?

    @Query("SELECT * FROM servers WHERE country = :country AND isActive = 1 ORDER BY lastPing ASC")
    fun getServersByCountry(country: String): Flow<List<Server>>

    @Query("SELECT * FROM servers WHERE isActive = 1 ORDER BY distance ASC LIMIT 1")
    suspend fun getNearestServer(): Server?

    @Query("SELECT * FROM servers WHERE isActive = 1 ORDER BY lastPing ASC LIMIT 1")
    suspend fun getFastestServer(): Server?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertServer(server: Server)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertServers(servers: List<Server>)

    @Update
    suspend fun updateServer(server: Server)

    @Delete
    suspend fun deleteServer(server: Server)

    @Query("DELETE FROM servers WHERE id = :id")
    suspend fun deleteServerById(id: String)

    @Query("DELETE FROM servers")
    suspend fun deleteAllServers()

    @Query("UPDATE servers SET lastPing = :ping WHERE id = :serverId")
    suspend fun updateServerPing(serverId: String, ping: Int)

    @Query("UPDATE servers SET lastChecked = :timestamp WHERE id = :serverId")
    suspend fun updateServerLastChecked(serverId: String, timestamp: Long)

    @Query("SELECT COUNT(*) FROM servers WHERE isActive = 1")
    suspend fun getActiveServerCount(): Int
}
