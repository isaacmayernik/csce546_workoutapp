package com.example.workoutapp546

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.startup.Initializer
import java.util.Calendar

@Suppress("unused")
class AppStartup : Initializer<Unit> {
    override fun create(context: Context) {
        Log.d("AppStartup", "Initializing notification alarms")

        val calendar = Calendar.getInstance()
        val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
        val currentMinute = calendar.get(Calendar.MINUTE)

        val windowStart = 9L
        val windowEnd = 19L
        val initialDelay: Long

        val notificationService = NotificationService(context)
        val lastNotificationTime = notificationService.getLastNotificationTime()
        val hadNotificationToday = lastNotificationTime > 0 &&
                isSameDay(lastNotificationTime, System.currentTimeMillis())

        when {
            // Case 1: Within window and haven't had notification today
            currentHour in windowStart..<windowEnd && !hadNotificationToday -> {
                // Schedule between now and 7 PM
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
                // Schedule for tomorrow at random time between 9 AM and 7 PM
                val minutesUntilTomorrowWindow = (24 - currentHour + windowStart) * 60 - currentMinute
                val randomMinutes = (0L..(windowEnd - windowStart) * 60).random()
                initialDelay = minutesUntilTomorrowWindow + randomMinutes
                Log.d("AppStartup", "Scheduling for tomorrow in ${initialDelay/60}h ${initialDelay%60}m")
            }
        }

        scheduleNotificationAlarm(context, initialDelay * 60 * 1000) // Convert minutes to milliseconds
    }

    private fun scheduleNotificationAlarm(context: Context, delayMillis: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, NotificationReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val triggerTime = System.currentTimeMillis() + delayMillis
        NotificationService(context).setNextAlarmTime(triggerTime)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
        } else {
            alarmManager.set(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
        }
    }

    override fun dependencies(): List<Class<out Initializer<*>>> = emptyList()
}

fun isSameDay(time1: Long, time2: Long): Boolean {
    val cal1 = Calendar.getInstance().apply { timeInMillis = time1 }
    val cal2 = Calendar.getInstance().apply { timeInMillis = time2 }
    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
            cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
}