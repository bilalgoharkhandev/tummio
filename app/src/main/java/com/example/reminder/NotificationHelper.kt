package com.example.reminder

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R

object NotificationHelper {
    const val CHANNEL_ID = "little_routine_feeding_reminders"
    const val CHANNEL_NAME = "Feeding Reminders"
    const val NOTIFICATION_ID = 1001

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Reminders for scheduled infant feedings"
                enableVibration(true)
                setShowBadge(true)
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    fun showFeedingNotification(
        context: Context,
        babyName: String,
        elapsedTimeString: String,
        lastFeedTimeString: String
    ) {
        createNotificationChannel(context)

        // Open App intent
        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("navigate_to", "feeding")
        }
        val openPendingIntent = PendingIntent.getActivity(
            context,
            0,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Snooze Intent (15 minutes)
        val snoozeIntent = Intent(context, FeedingReminderReceiver::class.java).apply {
            action = FeedingReminderScheduler.ACTION_SNOOZE_REMINDER
            putExtra("snooze_minutes", 15)
            putExtra("baby_name", babyName)
        }
        val snoozePendingIntent = PendingIntent.getBroadcast(
            context,
            1,
            snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Time to feed $babyName")
            .setContentText("Last feeding: $lastFeedTimeString • Elapsed: $elapsedTimeString")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("It has been $elapsedTimeString since $babyName's last feed ($lastFeedTimeString). Check for hunger cues.")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(openPendingIntent)
            .addAction(R.mipmap.ic_launcher, "Record Feed", openPendingIntent)
            .addAction(R.mipmap.ic_launcher, "Snooze 15m", snoozePendingIntent)
            .build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID, notification)
    }

    fun cancelNotification(context: Context) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.cancel(NOTIFICATION_ID)
    }
}
