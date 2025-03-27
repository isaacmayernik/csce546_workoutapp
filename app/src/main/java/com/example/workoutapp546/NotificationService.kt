package com.example.workoutapp546

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.edit
import java.util.Calendar
import java.util.TimeZone
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
        )
    }

    // Function that will send a daily notification if conditions are met
    fun showNotification() {
        if (!areNotificationsEnabled()) return

        // did user already receive a notification today
        val lastNotificationTime = prefs.getLong(PREF_LAST_NOTIFICATION_TIME, 0)
        val now = System.currentTimeMillis()
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = now

        // convert user's timezone to Eastern Time
        val timeZone = TimeZone.getDefault()
        calendar.timeZone = timeZone

        // check if between 9am-7pm
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        if (hour < 9 || hour >= 19) return

        val lastCalendar = Calendar.getInstance()
        lastCalendar.timeInMillis = lastNotificationTime
        lastCalendar.timeZone = timeZone

        if (lastCalendar.get(Calendar.DAY_OF_YEAR) == calendar.get(Calendar.DAY_OF_YEAR) &&
            lastCalendar.get(Calendar.YEAR) == calendar.get(Calendar.YEAR)) {
            return
        }

        // Create and show notification
        val randomMessage = motivationalMessages.random()
        val activityIntent = Intent(context, MainActivity::class.java)
        val activityPendingIntent = PendingIntent.getActivity(
            context,
            1,
            activityIntent,
            PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Workout App")
            .setContentText(randomMessage)
            .setContentIntent(activityPendingIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
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
//        val randomMessage = motivationalMessages.random()
//        val activityIntent = Intent(context, MainActivity::class.java)
//        val activityPendingIntent = PendingIntent.getActivity(
//            context,
//            1,
//            activityIntent,
//            PendingIntent.FLAG_IMMUTABLE
//        )
//        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
//            .setSmallIcon(R.drawable.ic_notification)
//            .setContentTitle("Workout App")
//            .setContentText(randomMessage)
//            .setContentIntent(activityPendingIntent)
//            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
//            .setAutoCancel(true)
//            .build()
//
//        notificationManager.notify(NOTIFICATION_ID, notification)
//    }
}