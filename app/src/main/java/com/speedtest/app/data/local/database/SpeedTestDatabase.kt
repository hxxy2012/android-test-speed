package com.speedtest.app.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.speedtest.app.data.local.dao.ServerDao
import com.speedtest.app.data.local.dao.SpeedTestResultDao
import com.speedtest.app.data.local.entity.Server
import com.speedtest.app.data.local.entity.SpeedTestResult

/**
 * Room database for SpeedTest application
 */
@Database(
    entities = [
        SpeedTestResult::class,
        Server::class
    ],
    version = 1,
    exportSchema = true
)
abstract class SpeedTestDatabase : RoomDatabase() {

    abstract fun speedTestResultDao(): SpeedTestResultDao
    abstract fun serverDao(): ServerDao

    companion object {
        const val DATABASE_NAME = "speedtest_database"
    }
}
