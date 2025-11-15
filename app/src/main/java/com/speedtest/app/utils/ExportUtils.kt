package com.speedtest.app.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.speedtest.app.data.local.entity.SpeedTestResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*

/**
 * Utility for exporting speed test data
 */
object ExportUtils {

    /**
     * Export data to CSV format
     */
    suspend fun exportToCsv(
        context: Context,
        results: List<SpeedTestResult>
    ): Result<Uri> = withContext(Dispatchers.IO) {
        try {
            val fileName = "speedtest_export_${getTimestamp()}.csv"
            val file = File(context.cacheDir, fileName)

            FileWriter(file).use { writer ->
                // Write header
                writer.append("Timestamp,Date,Download (Mbps),Upload (Mbps),Ping (ms),Jitter (ms),Network Type,Operator,Server,Location\n")

                // Write data
                results.forEach { result ->
                    val date = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                        .format(Date(result.timestamp))

                    writer.append("${result.timestamp},")
                    writer.append("\"$date\",")
                    writer.append("${result.downloadSpeed},")
                    writer.append("${result.uploadSpeed},")
                    writer.append("${result.ping},")
                    writer.append("${result.jitter},")
                    writer.append("\"${result.networkType}\",")
                    writer.append("\"${result.operator}\",")
                    writer.append("\"${result.serverName}\",")
                    writer.append("\"${result.serverLocation}\"\n")
                }
            }

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            Result.success(uri)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Export data to JSON format
     */
    suspend fun exportToJson(
        context: Context,
        results: List<SpeedTestResult>
    ): Result<Uri> = withContext(Dispatchers.IO) {
        try {
            val fileName = "speedtest_export_${getTimestamp()}.json"
            val file = File(context.cacheDir, fileName)

            val gson = GsonBuilder()
                .setPrettyPrinting()
                .create()

            val exportData = ExportData(
                exportDate = System.currentTimeMillis(),
                totalTests = results.size,
                results = results.map { result ->
                    ExportResult(
                        timestamp = result.timestamp,
                        date = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                            .format(Date(result.timestamp)),
                        downloadSpeed = result.downloadSpeed,
                        uploadSpeed = result.uploadSpeed,
                        peakDownloadSpeed = result.peakDownloadSpeed,
                        peakUploadSpeed = result.peakUploadSpeed,
                        ping = result.ping,
                        jitter = result.jitter,
                        minPing = result.minPing,
                        maxPing = result.maxPing,
                        avgPing = result.avgPing,
                        packetLoss = result.packetLoss,
                        networkType = result.networkType,
                        operator = result.operator,
                        ipAddress = result.ipAddress,
                        serverName = result.serverName,
                        serverLocation = result.serverLocation,
                        bytesDownloaded = result.bytesDownloaded,
                        bytesUploaded = result.bytesUploaded
                    )
                }
            )

            FileWriter(file).use { writer ->
                gson.toJson(exportData, writer)
            }

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            Result.success(uri)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Share exported file
     */
    fun shareFile(context: Context, uri: Uri, mimeType: String) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        val chooserIntent = Intent.createChooser(shareIntent, "Share Speed Test Data").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        context.startActivity(chooserIntent)
    }

    private fun getTimestamp(): String {
        return SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
            .format(Date())
    }

    data class ExportData(
        val exportDate: Long,
        val totalTests: Int,
        val results: List<ExportResult>
    )

    data class ExportResult(
        val timestamp: Long,
        val date: String,
        val downloadSpeed: Double,
        val uploadSpeed: Double,
        val peakDownloadSpeed: Double,
        val peakUploadSpeed: Double,
        val ping: Int,
        val jitter: Double,
        val minPing: Int,
        val maxPing: Int,
        val avgPing: Int,
        val packetLoss: Double,
        val networkType: String,
        val operator: String,
        val ipAddress: String,
        val serverName: String,
        val serverLocation: String,
        val bytesDownloaded: Long,
        val bytesUploaded: Long
    )
}
