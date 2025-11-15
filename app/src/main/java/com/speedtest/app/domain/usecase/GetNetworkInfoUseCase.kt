package com.speedtest.app.domain.usecase

import com.speedtest.app.domain.model.NetworkInfo
import com.speedtest.app.domain.repository.NetworkRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * UseCase for getting network information
 */
class GetNetworkInfoUseCase @Inject constructor(
    private val repository: NetworkRepository
) {
    fun getNetworkInfo(): Flow<NetworkInfo> {
        return repository.getNetworkInfo()
    }

    suspend fun getCurrentNetworkInfo(): NetworkInfo {
        return repository.getCurrentNetworkInfo()
    }
}
