package com.speedtest.app.utils

import java.text.SimpleDateFormat
import java.util.*

/**
 * Utility functions for formatting data
 */
object FormatUtils {

    /**
     * Format speed value based on unit
     */
    fun formatSpeed(mbps: Double, unit: String = "Mbps"): String {
        val value = if (unit == "MB/s") mbps / 8.0 else mbps
        return String.format("%.2f %s", value, unit)
    }

    /**
     * Format ping/latency value
     */
    fun formatPing(ping: Int): String {
        return "$ping ms"
    }

    /**
     * Format jitter value
     */
    fun formatJitter(jitter: Double): String {
        return String.format("%.1f ms", jitter)
    }

    /**
     * Format timestamp to readable date
     */
    fun formatDate(timestamp: Long, pattern: String = "MMM dd, yyyy HH:mm"): String {
        val dateFormat = SimpleDateFormat(pattern, Locale.getDefault())
        return dateFormat.format(Date(timestamp))
    }

    /**
     * Format bytes to human-readable format
     */
    fun formatBytes(bytes: Long): String {
        val kb = bytes / 1024.0
        val mb = kb / 1024.0
        val gb = mb / 1024.0

        return when {
            gb >= 1 -> String.format("%.2f GB", gb)
            mb >= 1 -> String.format("%.2f MB", mb)
            kb >= 1 -> String.format("%.2f KB", kb)
            else -> "$bytes B"
        }
    }

    /**
     * Format duration in milliseconds to readable format
     */
    fun formatDuration(millis: Long): String {
        val seconds = millis / 1000
        val minutes = seconds / 60
        val hours = minutes / 60

        return when {
            hours > 0 -> String.format("%d:%02d:%02d", hours, minutes % 60, seconds % 60)
            minutes > 0 -> String.format("%d:%02d", minutes, seconds % 60)
            else -> String.format("%d sec", seconds)
        }
    }

    /**
     * Format distance in kilometers
     */
    fun formatDistance(km: Double): String {
        return if (km < 1) {
            String.format("%.0f m", km * 1000)
        } else {
            String.format("%.1f km", km)
        }
    }

    /**
     * Get speed quality description
     */
    fun getSpeedQuality(mbps: Double): String {
        return when {
            mbps >= 100 -> "Excellent"
            mbps >= 50 -> "Very Good"
            mbps >= 25 -> "Good"
            mbps >= 10 -> "Fair"
            mbps >= 5 -> "Poor"
            else -> "Very Poor"
        }
    }

    /**
     * Get ping quality description
     */
    fun getPingQuality(ping: Int): String {
        return when {
            ping < 20 -> "Excellent"
            ping < 50 -> "Good"
            ping < 100 -> "Fair"
            ping < 200 -> "Poor"
            else -> "Very Poor"
        }
    }
}
