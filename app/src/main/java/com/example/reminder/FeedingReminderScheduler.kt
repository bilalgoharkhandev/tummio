package com.example.reminder

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.data.db.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

object FeedingReminderScheduler {
    const val ACTION_FEEDING_REMINDER = "com.example.ACTION_FEEDING_REMINDER"
    const val ACTION_SNOOZE_REMINDER = "com.example.ACTION_SNOOZE_REMINDER"
    private const val REQUEST_CODE = 2001

    fun scheduleNextReminder(
        context: Context,
        triggerTimestamp: Long,
        babyName: String,
        lastFeedTimestamp: Long
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(context, FeedingReminderReceiver::class.java).apply {
            action = ACTION_FEEDING_REMINDER
            putExtra("baby_name", babyName)
            putExtra("last_feed_time", lastFeedTimestamp)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTimestamp,
                    pendingIntent
                )
            } else {
                alarmManager.set(
                    AlarmManager.RTC_WAKEUP,
                    triggerTimestamp,
                    pendingIntent
                )
            }
        } catch (e: SecurityException) {
            // Fallback for exact alarm permission constraints
            alarmManager.set(
                AlarmManager.RTC_WAKEUP,
                triggerTimestamp,
                pendingIntent
            )
        }
    }

    fun snoozeReminder(context: Context, snoozeMinutes: Int, babyName: String) {
        val snoozeTime = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(snoozeMinutes.toLong())
        scheduleNextReminder(context, snoozeTime, babyName, System.currentTimeMillis() - TimeUnit.HOURS.toMillis(3))
        NotificationHelper.cancelNotification(context)
    }

    fun cancelReminder(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, FeedingReminderReceiver::class.java).apply {
            action = ACTION_FEEDING_REMINDER
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }
}

class FeedingReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            FeedingReminderScheduler.ACTION_FEEDING_REMINDER -> {
                val babyName = intent.getStringExtra("baby_name") ?: "Baby"
                val lastFeedTime = intent.getLongExtra("last_feed_time", System.currentTimeMillis() - TimeUnit.HOURS.toMillis(3))

                val elapsedMillis = System.currentTimeMillis() - lastFeedTime
                val elapsedHours = TimeUnit.MILLISECONDS.toHours(elapsedMillis)
                val elapsedMins = TimeUnit.MILLISECONDS.toMinutes(elapsedMillis) % 60
                val elapsedStr = if (elapsedHours > 0) "${elapsedHours}h ${elapsedMins}m" else "${elapsedMins}m"

                val timeFormatter = SimpleDateFormat("h:mm a", Locale.getDefault())
                val lastTimeStr = timeFormatter.format(Date(lastFeedTime))

                NotificationHelper.showFeedingNotification(
                    context = context,
                    babyName = babyName,
                    elapsedTimeString = elapsedStr,
                    lastFeedTimeString = lastTimeStr
                )
            }
            FeedingReminderScheduler.ACTION_SNOOZE_REMINDER -> {
                val snoozeMinutes = intent.getIntExtra("snooze_minutes", 15)
                val babyName = intent.getStringExtra("baby_name") ?: "Baby"
                FeedingReminderScheduler.snoozeReminder(context, snoozeMinutes, babyName)
            }
            Intent.ACTION_BOOT_COMPLETED -> {
                // Re-evaluate pending reminder from database after reboot
                CoroutineScope(Dispatchers.IO).launch {
                    val db = AppDatabase.getDatabase(context)
                    val defaultBaby = db.babyDao().getAllBabies()
                    // Notification helper re-init
                    NotificationHelper.createNotificationChannel(context)
                }
            }
        }
    }
}
