package com.speedtest.app.data.repository

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import com.speedtest.app.domain.model.NetworkInfo
import com.speedtest.app.domain.model.NetworkType
import com.speedtest.app.domain.repository.NetworkRepository
import com.speedtest.app.utils.NetworkUtils
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of NetworkRepository
 */
@Singleton
class NetworkRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : NetworkRepository {

    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    override fun getNetworkInfo(): Flow<NetworkInfo> = callbackFlow {
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                trySend(getCurrentNetworkInfoInternal())
            }

            override fun onLost(network: Network) {
                trySend(NetworkInfo(isConnected = false))
            }

            override fun onCapabilitiesChanged(
                network: Network,
                networkCapabilities: NetworkCapabilities
            ) {
                trySend(getCurrentNetworkInfoInternal())
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            connectivityManager.registerDefaultNetworkCallback(callback)
        } else {
            val request = NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build()
            connectivityManager.registerNetworkCallback(request, callback)
        }

        // Send initial state
        trySend(getCurrentNetworkInfoInternal())

        awaitClose {
            connectivityManager.unregisterNetworkCallback(callback)
        }
    }

    override suspend fun getCurrentNetworkInfo(): NetworkInfo {
        return getCurrentNetworkInfoInternal()
    }

    private suspend fun getCurrentNetworkInfoInternal(): NetworkInfo {
        val isConnected = NetworkUtils.isNetworkConnected(context)
        val networkType = NetworkUtils.getNetworkType(context)
        val operator = NetworkUtils.getNetworkOperator(context)
        val ipAddress = getExternalIpAddress()
        val isMetered = NetworkUtils.isNetworkMetered(context)

        return NetworkInfo(
            networkType = networkType,
            operator = operator,
            ipAddress = ipAddress,
            isConnected = isConnected,
            isMetered = isMetered
        )
    }

    override suspend fun isConnected(): Boolean {
        return NetworkUtils.isNetworkConnected(context)
    }

    override suspend fun getExternalIpAddress(): String = withContext(Dispatchers.IO) {
        try {
            // Use multiple IP check services as fallback
            val services = listOf(
                "https://api.ipify.org",
                "https://icanhazip.com",
                "https://ifconfig.me/ip"
            )

            for (service in services) {
                try {
                    val request = Request.Builder()
                        .url(service)
                        .build()

                    val response = httpClient.newCall(request).execute()
                    if (response.isSuccessful) {
                        val ip = response.body?.string()?.trim()
                        if (!ip.isNullOrBlank()) {
                            return@withContext ip
                        }
                    }
                } catch (e: Exception) {
                    // Try next service
                    continue
                }
            }
            "0.0.0.0"
        } catch (e: Exception) {
            "0.0.0.0"
        }
    }
}
