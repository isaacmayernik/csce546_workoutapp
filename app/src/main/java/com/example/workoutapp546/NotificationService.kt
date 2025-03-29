package com.example.workoutapp546

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.edit
import java.util.Calendar
import kotlin.jvm.java

class NotificationService(private val context: Context) {
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

    companion object {
        const val CHANNEL_ID = "workout_motivation_channel"
        const val NOTIFICATION_ID = 1
        const val PREF_NOTIFICATION_REQUESTED = "notification_requested"
        const val PREF_NOTIFICATION_ENABLED = "notification_enabled"
        const val PREF_LAST_NOTIFICATION_TIME = "last_notification_time"

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
            "You are never alone.",
        )
    }

    // Function that will send a daily notification if conditions are met
    fun showNotification() {
        if (!areNotificationsEnabled()) return

        val now = System.currentTimeMillis()
        val calendar = Calendar.getInstance().apply {
            timeInMillis = now
        }

        // Check if between 9am-7pm LOCAL TIME
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        if (hour < 9 || hour >= 19) {
            return
        }

        val lastNotificationTime = prefs.getLong(PREF_LAST_NOTIFICATION_TIME, 0)
        if (lastNotificationTime > 0) {
            val lastCalendar = Calendar.getInstance().apply {
                timeInMillis = lastNotificationTime
            }
            if (lastCalendar.get(Calendar.DAY_OF_YEAR) == calendar.get(Calendar.DAY_OF_YEAR) &&
                lastCalendar.get(Calendar.YEAR) == calendar.get(Calendar.YEAR)) {
                return
            }
        }

        // Create and show notification
        val randomMessage = motivationalMessages.random()
        val activityIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val activityPendingIntent = PendingIntent.getActivity(
            context,
            1,
            activityIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Workout App")
            .setContentText(randomMessage)
            .setContentIntent(activityPendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setWhen(System.currentTimeMillis())
            .setShowWhen(true)

        builder.setVibrate(longArrayOf(0, 200, 100, 200))

        notificationManager.notify(NOTIFICATION_ID, builder.build())
        prefs.edit { putLong(PREF_LAST_NOTIFICATION_TIME, now) }
    }

    // check if system settings notifications are enabled for app
    fun areNotificationsEnabled(): Boolean {
        return NotificationManagerCompat.from(context).areNotificationsEnabled()
    }

    // mark that notification permissions have been requested
    fun setNotificationsRequested(requested: Boolean) {
        prefs.edit { putBoolean(PREF_NOTIFICATION_REQUESTED, requested).apply() }
    }

    // check if notification perms have been previously requested
    fun wasNotificationRequested(): Boolean {
        return prefs.getBoolean(PREF_NOTIFICATION_REQUESTED, false)
    }

    // set user's notification pref in app
    fun setNotificationPreference(enabled: Boolean) {
        prefs.edit { putBoolean(PREF_NOTIFICATION_ENABLED, enabled).apply() }
    }

    // get user's notification pref in app
    fun getNotificationPreference(): Boolean {
        return prefs.getBoolean(PREF_NOTIFICATION_ENABLED, false)
    }

//    // Function for testing if notifications get sent
//    fun showTestNotification() {
//        if (!areNotificationsEnabled()) {
//            // Optional: You might want to show a toast or log here
//            return
//        }
//
//        val randomMessage = motivationalMessages.random()
//        val activityIntent = Intent(context, MainActivity::class.java)
//        val activityPendingIntent = PendingIntent.getActivity(
//            context,
//            2,  // Different request code than regular notifications
//            activityIntent,
//            PendingIntent.FLAG_IMMUTABLE
//        )
//
//        // Create the notification builder with proper backward compatibility
//        val builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            // Oreo and above - use channel ID
//            NotificationCompat.Builder(context, CHANNEL_ID)
//        } else {
//            // Pre-Oreo - use empty string as channel ID
//            NotificationCompat.Builder(context, "").apply {
//                setPriority(NotificationCompat.PRIORITY_DEFAULT)
//            }
//        }
//
//        builder.setSmallIcon(R.drawable.ic_notification)
//            .setContentTitle("Workout App - Test")
//            .setContentText(randomMessage)
//            .setContentIntent(activityPendingIntent)
//            .setAutoCancel(true)
//
//        // Use a different notification ID so it doesn't replace regular notifications
//        notificationManager.notify(NOTIFICATION_ID + 1, builder.build())
//    }
}