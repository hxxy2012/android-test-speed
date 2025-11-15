package com.speedtest.app.domain.model

/**
 * Network connection types
 */
enum class NetworkType(val displayName: String) {
    WIFI("WiFi"),
    ETHERNET("Ethernet"),
    MOBILE_5G("5G"),
    MOBILE_4G("4G"),
    MOBILE_3G("3G"),
    MOBILE_2G("2G"),
    MOBILE("Mobile"),
    UNKNOWN("Unknown"),
    NONE("No Connection");

    companion object {
        fun fromString(value: String): NetworkType {
            return entries.find { it.name.equals(value, ignoreCase = true) } ?: UNKNOWN
        }
    }
}
