package com.speedtest.app.domain.usecase

import com.speedtest.app.data.local.entity.Server
import com.speedtest.app.domain.repository.ServerRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * UseCase for getting servers
 */
class GetServersUseCase @Inject constructor(
    private val repository: ServerRepository
) {
    fun getAllActiveServers(): Flow<List<Server>> {
        return repository.getAllActiveServers()
    }

    fun getServersByCountry(country: String): Flow<List<Server>> {
        return repository.getServersByCountry(country)
    }

    suspend fun getServerById(id: String): Server? {
        return repository.getServerById(id)
    }

    suspend fun getNearestServer(): Server? {
        return repository.getNearestServer()
    }

    suspend fun getFastestServer(): Server? {
        return repository.getFastestServer()
    }

    suspend fun pingAllServers() {
        repository.pingAllServers()
    }

    suspend fun syncServers(): Result<Unit> {
        return repository.syncServers()
    }
}
