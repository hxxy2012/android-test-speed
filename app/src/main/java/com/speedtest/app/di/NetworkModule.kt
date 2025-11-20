package com.speedtest.app.di

import com.speedtest.app.data.remote.api.SpeedTestEngine
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for network and API dependencies
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideSpeedTestEngine(): SpeedTestEngine {
        return SpeedTestEngine()
    }
}
