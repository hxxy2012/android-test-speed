package com.speedtest.app.di

import com.speedtest.app.data.repository.NetworkRepositoryImpl
import com.speedtest.app.data.repository.ServerRepositoryImpl
import com.speedtest.app.data.repository.SpeedTestRepositoryImpl
import com.speedtest.app.domain.repository.NetworkRepository
import com.speedtest.app.domain.repository.ServerRepository
import com.speedtest.app.domain.repository.SpeedTestRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for repository dependencies
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindSpeedTestRepository(
        impl: SpeedTestRepositoryImpl
    ): SpeedTestRepository

    @Binds
    @Singleton
    abstract fun bindServerRepository(
        impl: ServerRepositoryImpl
    ): ServerRepository

    @Binds
    @Singleton
    abstract fun bindNetworkRepository(
        impl: NetworkRepositoryImpl
    ): NetworkRepository
}
