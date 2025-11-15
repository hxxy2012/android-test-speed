package com.speedtest.app.domain.usecase

import com.speedtest.app.domain.repository.ServerRepository
import javax.inject.Inject

/**
 * UseCase for initializing default servers on first launch
 */
class InitializeServersUseCase @Inject constructor(
    private val serverRepository: ServerRepository
) {
    suspend operator fun invoke(): Result<Unit> {
        return try {
            // Sync servers from remote (which includes default servers)
            serverRepository.syncServers()
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
