package com.example.workoutapp546

import android.content.Context
import android.util.Log
import androidx.startup.Initializer
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.Calendar
import java.util.concurrent.TimeUnit

@Suppress("unused")
class AppStartup : Initializer<Unit> {
    override fun create(context: Context) {
        Log.d("AppStartup", "Initializing notification worker")

        val calendar = Calendar.getInstance()
        val currentHour = calendar.get(Calendar.HOUR_OF_DAY).toLong()
        val currentMinute = calendar.get(Calendar.MINUTE).toLong()

        val windowStart = 9L
        val windowEnd = 18L
        val initialDelay: Long
        val repeatInterval = 24L

        val notificationService = NotificationService(context)
        val lastNotificationTime = notificationService.getLastNotificationTime()
        val hadNotificationToday = lastNotificationTime > 0 &&
                isSameDay(lastNotificationTime, System.currentTimeMillis())

        when {
            // Case 1: Within window and haven't had notification today
            currentHour in windowStart..windowEnd && !hadNotificationToday -> {
                // Schedule between now and 6 PM
                val remainingMinutes = (windowEnd - currentHour) * 60 - currentMinute
                initialDelay = (5L..remainingMinutes.coerceAtLeast(5L)).random()
                Log.d("AppStartup", "Scheduling today in $initialDelay minutes")
            }
            // Case 2: Before window starts today
            currentHour < windowStart -> {
                val minutesUntilWindow = (windowStart - currentHour) * 60 - currentMinute
                initialDelay = minutesUntilWindow
                Log.d("AppStartup", "Scheduling for today at 9 AM ($minutesUntilWindow minutes)")
            }
            // Case 3: After window or already had notification today
            else -> {
                // Schedule for tomorrow at random time between 9 AM and 6 PM
                val minutesUntilTomorrowWindow = (24L - currentHour + windowStart) * 60 - currentMinute
                val randomMinutes = (0L..(windowEnd - windowStart) * 60).random()
                initialDelay = minutesUntilTomorrowWindow + randomMinutes
                Log.d("AppStartup", "Scheduling for tomorrow in ${initialDelay/60}h ${initialDelay%60}m")
            }
        }

        val notificationWork = PeriodicWorkRequestBuilder<NotificationWorker>(
            repeatInterval,
            TimeUnit.HOURS
        ).setInitialDelay(initialDelay, TimeUnit.MINUTES)
            .setConstraints(
                Constraints.Builder()
                    .setRequiresBatteryNotLow(false)
                    .setRequiresCharging(false)
                    .build()
            )
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            NotificationWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            notificationWork
        )
    }

    override fun dependencies(): List<Class<out Initializer<*>>> = emptyList()
}

fun isSameDay(time1: Long, time2: Long): Boolean {
    val cal1 = Calendar.getInstance().apply { timeInMillis = time1 }
    val cal2 = Calendar.getInstance().apply { timeInMillis = time2 }
    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
            cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
}