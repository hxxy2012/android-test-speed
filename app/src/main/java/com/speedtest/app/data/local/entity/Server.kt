package com.speedtest.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room Entity for storing test servers
 */
@Entity(tableName = "servers")
data class Server(
    @PrimaryKey
    val id: String,

    val name: String,
    val country: String,
    val city: String,
    val host: String,
    val port: Int = 8080,

    // Location coordinates
    val latitude: Double,
    val longitude: Double,

    // Server provider/sponsor
    val sponsor: String,

    // Server metrics
    val lastPing: Int = -1,
    val distance: Double = 0.0,  // Distance from user in km

    // Status
    val isActive: Boolean = true,
    val lastChecked: Long = 0L
)
