package com.example.workoutapp546.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.workoutapp546.MainActivity
import com.example.workoutapp546.R

fun createNotification(context: Context, message: String) {
    val channelId = "motivational_messages"
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
        createNotificationChannel(context, channelId)
    }

    val notificationBuilder = NotificationCompat.Builder(context, channelId)
        .setContentTitle("Workout Motivation")
        .setContentText(message)
        .setSmallIcon(R.drawable.ic_notification)
        .setContentIntent(createPendingIntent(context))
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setAutoCancel(true)

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        if (ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) != android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
    }

    with(NotificationManagerCompat.from(context)) {
        notify(System.currentTimeMillis().toInt(), notificationBuilder.build())
    }
}

private fun createPendingIntent(context: Context): PendingIntent {
    val intent = Intent(context, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
    }
    val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE

    return PendingIntent.getActivity(
        context,
        System.currentTimeMillis().toInt(),
        intent,
        flags
    )
}

fun createNotificationChannel(
    context: Context,
    channelId: String
) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val name = "Motivational Messages"
        val descriptionText = "Daily workout motivation notifications"
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(channelId, name, importance).apply {
            description = descriptionText
        }

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
}