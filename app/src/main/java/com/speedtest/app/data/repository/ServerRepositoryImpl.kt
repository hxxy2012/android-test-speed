package com.speedtest.app.data.repository

import com.speedtest.app.data.local.dao.ServerDao
import com.speedtest.app.data.local.entity.Server
import com.speedtest.app.domain.repository.ServerRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of ServerRepository
 */
@Singleton
class ServerRepositoryImpl @Inject constructor(
    private val serverDao: ServerDao
) : ServerRepository {

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(5, TimeUnit.SECONDS)
        .build()

    override fun getAllActiveServers(): Flow<List<Server>> {
        return serverDao.getAllActiveServers()
    }

    override fun getAllServers(): Flow<List<Server>> {
        return serverDao.getAllServers()
    }

    override suspend fun getServerById(id: String): Server? {
        return serverDao.getServerById(id)
    }

    override fun getServersByCountry(country: String): Flow<List<Server>> {
        return serverDao.getServersByCountry(country)
    }

    override suspend fun getNearestServer(): Server? {
        return serverDao.getNearestServer()
    }

    override suspend fun getFastestServer(): Server? {
        return serverDao.getFastestServer()
    }

    override suspend fun pingServer(server: Server): Int = withContext(Dispatchers.IO) {
        try {
            val url = "http://${server.host}:${server.port}/ping"
            val startTime = System.currentTimeMillis()

            val request = Request.Builder()
                .url(url)
                .head()
                .build()

            httpClient.newCall(request).execute().use { response ->
                val ping = (System.currentTimeMillis() - startTime).toInt()

                // Update server ping in database
                serverDao.updateServerPing(server.id, ping)

                ping
            }
        } catch (e: Exception) {
            -1
        }
    }

    override suspend fun pingAllServers() = withContext(Dispatchers.IO) {
        val servers = serverDao.getAllActiveServers().first()
        servers.forEach { server ->
            try {
                pingServer(server)
            } catch (e: Exception) {
                // Continue with next server
            }
        }
    }

    override suspend fun fetchServersFromRemote(): Result<List<Server>> = withContext(Dispatchers.IO) {
        try {
            // In a real app, this would fetch from a remote API
            // For now, return default servers
            val defaultServers = getDefaultServers()
            Result.success(defaultServers)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun syncServers(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val result = fetchServersFromRemote()
            if (result.isSuccess) {
                val servers = result.getOrNull() ?: emptyList()
                serverDao.insertServers(servers)
                Result.success(Unit)
            } else {
                Result.failure(result.exceptionOrNull() ?: Exception("Unknown error"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun saveServer(server: Server) {
        serverDao.insertServer(server)
    }

    override suspend fun deleteServer(serverId: String) {
        serverDao.deleteServerById(serverId)
    }

    /**
     * Get default test servers
     * In a real app, these would be fetched from a remote API
     */
    private fun getDefaultServers(): List<Server> {
        return listOf(
            // Localhost for testing with PHP server on same device
            Server(
                id = "server_localhost",
                name = "Local Server (Device)",
                country = "Local",
                city = "Local",
                host = "localhost",
                port = 8080,
                latitude = 0.0,
                longitude = 0.0,
                sponsor = "Local Testing",
                isActive = true,
                lastPing = 1
            ),
            // Android emulator host
            Server(
                id = "server_emulator_host",
                name = "Local Server (Emulator)",
                country = "Local",
                city = "Local",
                host = "10.0.2.2",
                port = 8080,
                latitude = 0.0,
                longitude = 0.0,
                sponsor = "Local Testing",
                isActive = true,
                lastPing = 1
            ),
            // Real local network (update with your actual IP)
            Server(
                id = "server_local_network",
                name = "Local Network Server",
                country = "Local",
                city = "Local",
                host = "192.168.1.100",
                port = 8080,
                latitude = 0.0,
                longitude = 0.0,
                sponsor = "Local Network",
                isActive = true,
                lastPing = 1
            ),
            // Public speedtest servers (may work)
            Server(
                id = "server_cloudflare",
                name = "Cloudflare Speed Test",
                country = "Global",
                city = "Global CDN",
                host = "speed.cloudflare.com",
                port = 443,
                latitude = 0.0,
                longitude = 0.0,
                sponsor = "Cloudflare",
                isActive = true,
                lastPing = 50
            )
        )
    }
}
