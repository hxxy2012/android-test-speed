package com.speedtest.app.worker

import android.content.Context
import androidx.work.*
import java.util.concurrent.TimeUnit

/**
 * Scheduler for WorkManager tasks
 */
object WorkManagerScheduler {

    /**
     * Schedule automatic speed test
     */
    fun scheduleAutoSpeedTest(context: Context, intervalHours: Int) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val workRequest = PeriodicWorkRequestBuilder<AutoSpeedTestWorker>(
            intervalHours.toLong(), TimeUnit.HOURS
        )
            .setConstraints(constraints)
            .addTag(AutoSpeedTestWorker.WORK_NAME)
            .setBackoffCriteria(
                BackoffPolicy.LINEAR,
                PeriodicWorkRequest.MIN_BACKOFF_MILLIS,
                TimeUnit.MILLISECONDS
            )
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            AutoSpeedTestWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            workRequest
        )
    }

    /**
     * Cancel automatic speed test
     */
    fun cancelAutoSpeedTest(context: Context) {
        WorkManager.getInstance(context)
            .cancelUniqueWork(AutoSpeedTestWorker.WORK_NAME)
    }

    /**
     * Check if auto test is scheduled
     */
    fun isAutoTestScheduled(context: Context): Boolean {
        val workInfos = WorkManager.getInstance(context)
            .getWorkInfosForUniqueWork(AutoSpeedTestWorker.WORK_NAME)
            .get()

        return workInfos.any {
            it.state == WorkInfo.State.ENQUEUED || it.state == WorkInfo.State.RUNNING
        }
    }
}
