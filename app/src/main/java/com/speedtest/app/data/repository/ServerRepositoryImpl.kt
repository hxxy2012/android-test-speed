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
            Server(
                id = "server_cn_beijing_1",
                name = "Beijing Server 1",
                country = "China",
                city = "Beijing",
                host = "speedtest-bj1.example.com",
                port = 8080,
                latitude = 39.9042,
                longitude = 116.4074,
                sponsor = "Example ISP",
                isActive = true
            ),
            Server(
                id = "server_cn_shanghai_1",
                name = "Shanghai Server 1",
                country = "China",
                city = "Shanghai",
                host = "speedtest-sh1.example.com",
                port = 8080,
                latitude = 31.2304,
                longitude = 121.4737,
                sponsor = "Example ISP",
                isActive = true
            ),
            Server(
                id = "server_cn_guangzhou_1",
                name = "Guangzhou Server 1",
                country = "China",
                city = "Guangzhou",
                host = "speedtest-gz1.example.com",
                port = 8080,
                latitude = 23.1291,
                longitude = 113.2644,
                sponsor = "Example ISP",
                isActive = true
            ),
            Server(
                id = "server_hk_1",
                name = "Hong Kong Server 1",
                country = "Hong Kong",
                city = "Hong Kong",
                host = "speedtest-hk1.example.com",
                port = 8080,
                latitude = 22.3193,
                longitude = 114.1694,
                sponsor = "Example ISP",
                isActive = true
            ),
            Server(
                id = "server_us_la_1",
                name = "Los Angeles Server 1",
                country = "United States",
                city = "Los Angeles",
                host = "speedtest-la1.example.com",
                port = 8080,
                latitude = 34.0522,
                longitude = -118.2437,
                sponsor = "Example ISP",
                isActive = true
            ),
            Server(
                id = "server_sg_1",
                name = "Singapore Server 1",
                country = "Singapore",
                city = "Singapore",
                host = "speedtest-sg1.example.com",
                port = 8080,
                latitude = 1.3521,
                longitude = 103.8198,
                sponsor = "Example ISP",
                isActive = true
            )
        )
    }
}
