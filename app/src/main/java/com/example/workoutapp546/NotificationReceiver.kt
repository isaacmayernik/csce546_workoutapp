package com.example.workoutapp546

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class NotificationReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val service = NotificationService(context)
        if (service.getNotificationPreference()) {
            service.showNotification()
        }
    }
}