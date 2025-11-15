package com.speedtest.app.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.speedtest.app.data.local.datastore.PreferencesManager
import com.speedtest.app.domain.usecase.ExecuteSpeedTestUseCase
import com.speedtest.app.utils.NotificationHelper
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.lastOrNull

/**
 * Worker for automatic scheduled speed tests
 */
@HiltWorker
class AutoSpeedTestWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val executeSpeedTestUseCase: ExecuteSpeedTestUseCase,
    private val preferencesManager: PreferencesManager
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            // Check if auto test is enabled
            val isEnabled = preferencesManager.autoTestEnabled.first()
            if (!isEnabled) {
                return Result.success()
            }

            // Check if notifications are enabled
            val notificationsEnabled = preferencesManager.notificationEnabled.first()

            // Execute speed test
            val result = executeSpeedTestUseCase(serverId = null).lastOrNull()

            // Show notification if enabled and test completed
            if (notificationsEnabled && result != null && result.isCompleted) {
                NotificationHelper.showTestCompletionNotification(
                    applicationContext,
                    downloadSpeed = result.avgDownloadSpeed,
                    uploadSpeed = result.avgUploadSpeed,
                    ping = result.avgPing
                )
            }

            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }

    companion object {
        const val WORK_NAME = "auto_speed_test"
    }
}
