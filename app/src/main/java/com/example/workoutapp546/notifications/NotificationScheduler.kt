package com.example.workoutapp546.notifications

import android.content.Context
import android.os.Build
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.TimeUnit

object NotificationScheduler {
    private const val WORK_NAME = "daily_motivation_notification"

    private fun getDateFormat(): SimpleDateFormat {
        return SimpleDateFormat("MMM d h:mm a", Locale.getDefault()).apply {
            timeZone = TimeZone.getDefault()
        }
    }

    fun scheduleDailyNotification(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(context, "motivational_messages")
        }

        val randomHour = (9..19).random()
        val randomMinute = (0..59).random()

        val calendar = Calendar.getInstance().apply {
            timeZone = TimeZone.getDefault()
            set(Calendar.HOUR_OF_DAY, randomHour)
            set(Calendar.MINUTE, randomMinute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)

            // reschedule for tomorrow if passed
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }
        val initialDelay = calendar.timeInMillis - System.currentTimeMillis()

        val notificationWork = PeriodicWorkRequestBuilder<NotificationWorker>(
            1, TimeUnit.DAYS
        )
            .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            notificationWork
        )
    }

    fun getNextScheduledTime(context: Context, callback: (String) -> Unit) {
        WorkManager.getInstance(context)
            .getWorkInfosForUniqueWorkLiveData(WORK_NAME)
            .observeForever { workInfos ->
                workInfos.firstOrNull()?.let { info ->
                    if (info.state == WorkInfo.State.ENQUEUED) {
                        val nextRunTime = info.nextScheduleTimeMillis
                        val dateString = getDateFormat().format(nextRunTime)
                        callback("Next notification: $dateString")
                    } else {
                        callback("No scheduled notifications")
                    }
                } ?: run {
                    callback("No scheduled notifications")
                }
            }
    }
}