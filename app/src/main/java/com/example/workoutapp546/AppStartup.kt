package com.example.workoutapp546

import android.content.Context
import android.util.Log
import androidx.startup.Initializer
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

@Suppress("unused")
class AppStartup : Initializer<Unit> {
    override fun create(context: Context) {
        Log.d("AppStartup", "Initializing notification worker")
        val notificationWork = PeriodicWorkRequestBuilder<NotificationWorker>(
            24,
            TimeUnit.HOURS,
        ).setInitialDelay(1, TimeUnit.HOURS)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            NotificationWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            notificationWork
        )
    }

    override fun dependencies(): List<Class<out Initializer<*>>> = emptyList()
}