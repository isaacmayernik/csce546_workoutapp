package com.example.workoutapp546

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import java.util.Calendar

class NotificationWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {
    override suspend fun doWork(): Result {
        val notificationService = NotificationService(applicationContext)

        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)

        if (hour in 9..18) { // 9am-7pm
            notificationService.showNotification()
        }

        return Result.success()
    }

    companion object {
        const val WORK_NAME = "daily_notification_work"
    }
}