package com.example.workoutapp546.notifications

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import java.util.Random

class NotificationWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {
    override fun doWork(): Result {
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

        val randomMessage = motivationalMessages[Random().nextInt(motivationalMessages.size)]
        createNotification(applicationContext, randomMessage)

        return Result.success()
    }
}