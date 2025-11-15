package com.speedtest.app.domain.usecase

import com.speedtest.app.domain.model.TestProgress
import com.speedtest.app.domain.repository.SpeedTestRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * UseCase for executing speed test
 */
class ExecuteSpeedTestUseCase @Inject constructor(
    private val repository: SpeedTestRepository
) {
    operator fun invoke(serverId: String? = null): Flow<TestProgress> {
        return repository.executeSpeedTest(serverId)
    }

    suspend fun stop() {
        repository.stopSpeedTest()
    }
}
