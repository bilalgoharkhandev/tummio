package com.example.reminder

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.R

object NotificationHelper {
    const val CHANNEL_ID = "tummio_smart_reminders"
    const val CHANNEL_NAME = "Baby care reminders"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(NotificationChannel(
                CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Custom feeding, sleep and diaper check reminders"
                enableVibration(true)
            })
        }
    }

    fun showReminder(context: Context, babyId: Long, babyName: String, kind: ReminderKind) {
        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) return
        createNotificationChannel(context)
        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("navigate_to", kind.destination)
        }
        val open = PendingIntent.getActivity(
            context, SmartReminderManager.notificationId(babyId, kind),
            openIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val snoozeIntent = Intent(context, FeedingReminderReceiver::class.java).apply {
            action = SmartReminderManager.ACTION_SNOOZE
            data = android.net.Uri.parse("tummio://snooze/${babyId}/${kind.name.lowercase()}")
            putExtra(SmartReminderManager.EXTRA_BABY_ID, babyId)
            putExtra(SmartReminderManager.EXTRA_KIND, kind.name)
            putExtra(SmartReminderManager.EXTRA_SNOOZE, 15)
        }
        val snooze = PendingIntent.getBroadcast(
            context, SmartReminderManager.notificationId(babyId, kind),
            snoozeIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val (title, message) = when (kind) {
            ReminderKind.FEED -> "Feeding check for $babyName" to "Check hunger cues or record a feeding."
            ReminderKind.SLEEP -> "Sleep check for $babyName" to "Review your baby's rest routine."
            ReminderKind.DIAPER -> "Diaper check for $babyName" to "Check the diaper and log a change if needed."
        }
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(open)
            .addAction(R.mipmap.ic_launcher, "Open tracker", open)
            .addAction(R.mipmap.ic_launcher, "Snooze 15 min", snooze)
            .build()
        (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
            .notify(SmartReminderManager.notificationId(babyId, kind), notification)
    }

    fun cancel(context: Context, babyId: Long, kind: ReminderKind) {
        (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
            .cancel(SmartReminderManager.notificationId(babyId, kind))
    }

    // Compatibility with the original feeding notification call sites.
    fun cancelNotification(context: Context) {
        (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).cancel(1001)
    }

    fun showFeedingNotification(context: Context, babyName: String, elapsedTimeString: String, lastFeedTimeString: String) {
        createNotificationChannel(context)
        // Old single-baby notification pathway preserved for compatibility.
        val open = PendingIntent.getActivity(context, 0,
            Intent(context, MainActivity::class.java).putExtra("navigate_to", "feeding"),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) return
        (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).notify(
            1001, NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Feeding check for $babyName")
                .setContentText("Last feed: $lastFeedTimeString · $elapsedTimeString ago")
                .setContentIntent(open)
                .setAutoCancel(true)
                .build()
        )
    }
}
