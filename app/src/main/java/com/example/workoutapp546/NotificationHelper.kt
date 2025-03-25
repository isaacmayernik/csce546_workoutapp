package com.example.workoutapp546

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import java.util.Calendar
import java.util.concurrent.TimeUnit

object NotificationHelper {
    private const val CHANNEL_ID = "workout_motivation_channel"
    private const val NOTIFICATION_ID = 1
    private const val WORK_TAG = "daily_motivation_work"
    private const val PREF_NOTIFICATION_ENABLED = "notifications_enabled"
    private const val PREF_LAST_MESSAGE_INDEX = "last_message_index"
    private const val PREF_ATTEMPTED_ENABLE = "attempted_enable"

    val motivationalMessages = listOf(
        "Keep up the great work!",
        "Your consistency is paying off!",
        "Keep on fighting. Never give up.",
        "You're stronger than you think.",
        "Time for another workout!",
        "Make sure to get rest!",
        "The only bad workout is the one that didn't happen!",
        "You're one workout closer to your goals!",
        "Keep pushing!",
        "Never forget why you started. You've got this!",
        "The impossible today is possible tomorrow.",
        "It is the courage to continue that counts.",
        "Consistency is key. Never stop!",
        "Always work hard.",
        "If you believe in yourself, anything is possible.",
        "Despite everything, it's still you.",
        "You can always do better, be better. Never forget that.",
        "Be proud of yourself and how far you have come.",
        "Let's get that workout on!",
    )

    fun createNotificationChannel(context: Context) {
        val name = "Workout Motivation"
        val descriptionText = "Daily motivational messages"
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
            description = descriptionText
        }

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    fun showNotification(context: Context, message: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Workout Motivation")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val notificationManager = ContextCompat.getSystemService(context, NotificationManager::class.java)
        notificationManager?.notify(NOTIFICATION_ID, notification)
    }

    fun scheduleDailyNotification(context: Context) {
        cancelExistingWork(context)

        val currentTime = Calendar.getInstance()
        val dueTime = Calendar.getInstance().apply {
            // set time to random hour between 9am-7pm ET -> local time
            set(Calendar.HOUR_OF_DAY, (9..19).random())
            set(Calendar.MINUTE, (0..59).random())
            set(Calendar.SECOND, 0)

            // schedule for tomorrow if time passed
            if (before(currentTime)) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        val delay = dueTime.timeInMillis - currentTime.timeInMillis
        val workRequest = OneTimeWorkRequestBuilder<MotivationWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .addTag(WORK_TAG)
            .build()

        WorkManager.getInstance(context)
            .enqueueUniqueWork(WORK_TAG, ExistingWorkPolicy.REPLACE, workRequest)
    }

    private fun cancelExistingWork(context: Context) {
        WorkManager.getInstance(context).cancelAllWorkByTag(WORK_TAG)
    }

    fun areNotificationsEnabled(context: Context): Boolean {
        val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val appSetting = prefs.getBoolean(PREF_NOTIFICATION_ENABLED, false)
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = notificationManager.getNotificationChannel(CHANNEL_ID)
        return appSetting && channel?.importance != NotificationManager.IMPORTANCE_NONE
    }

    fun setNotificationsEnabled(context: Context, enabled: Boolean) {
        val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        prefs.edit {
            putBoolean(PREF_NOTIFICATION_ENABLED, enabled)
            putBoolean(PREF_ATTEMPTED_ENABLE, enabled)
        }

        if (enabled) {
            createNotificationChannel(context)
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                !notificationManager.areNotificationsEnabled()) {
                val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                    putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                }
                context.startActivity(intent)
            } else {
                scheduleDailyNotification(context)
            }
        } else {
            cancelExistingWork(context)
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.deleteNotificationChannel(CHANNEL_ID)
        }
    }

    fun getRandomMessage(context: Context): String {
        val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val lastIndex = prefs.getInt(PREF_LAST_MESSAGE_INDEX, -1)

        val availableIndices = motivationalMessages.indices.filter { it != lastIndex }
        val randomIndex = if (availableIndices.isNotEmpty()) {
            availableIndices.random()
        } else {
            (motivationalMessages.indices).random()
        }

        prefs.edit { putInt(PREF_LAST_MESSAGE_INDEX, randomIndex) }
        return motivationalMessages[randomIndex]
    }

    class MotivationWorker(context: Context, params: WorkerParameters) : Worker(context, params) {
        override fun doWork(): Result {
            val message = getRandomMessage(applicationContext)
            showNotification(applicationContext, message)

            scheduleDailyNotification(applicationContext)
            return Result.success()
        }
    }
}

@Composable
fun RequestNotificationPermission() {
    val context = LocalContext.current
    var showDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val askedBefore = prefs.getBoolean("notification_permission_asked", false)

        if (!askedBefore && !NotificationHelper.areNotificationsEnabled(context)) {
            showDialog = true
            prefs.edit { putBoolean("notification_permission_asked", true) }
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Enable Notifications") },
            text = {
                Column {
                    Text("Would you like to receive daily motivational messages?")
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        Text("\nYou'll need to enable notifications in system settings.",
                            style = MaterialTheme.typography.bodySmall)
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    NotificationHelper.setNotificationsEnabled(context, true)
                    showDialog = false
                }) {
                    Text("Enable")
                }
            },
            dismissButton = {
                Button(onClick = {
                    NotificationHelper.setNotificationsEnabled(context, false)
                    showDialog = false
                }) {
                    Text("Not Now")
                }
            }
        )
    }
}