package com.speedtest.app.di

import android.content.Context
import androidx.room.Room
import com.speedtest.app.data.local.dao.ServerDao
import com.speedtest.app.data.local.dao.SpeedTestResultDao
import com.speedtest.app.data.local.database.SpeedTestDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for database dependencies
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideSpeedTestDatabase(
        @ApplicationContext context: Context
    ): SpeedTestDatabase {
        return Room.databaseBuilder(
            context,
            SpeedTestDatabase::class.java,
            SpeedTestDatabase.DATABASE_NAME
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideSpeedTestResultDao(database: SpeedTestDatabase): SpeedTestResultDao {
        return database.speedTestResultDao()
    }

    @Provides
    @Singleton
    fun provideServerDao(database: SpeedTestDatabase): ServerDao {
        return database.serverDao()
    }
}
