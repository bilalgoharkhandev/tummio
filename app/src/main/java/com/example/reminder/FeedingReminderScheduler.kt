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
import java.util.concurrent.TimeUnit

/** Backward compatible facade for existing navigation/VM call sites. */
object FeedingReminderScheduler {
    const val ACTION_FEEDING_REMINDER = "com.example.ACTION_FEEDING_REMINDER"
    const val ACTION_SNOOZE_REMINDER = "com.example.ACTION_SNOOZE_REMINDER"

    fun scheduleNextReminder(context: Context, triggerTimestamp: Long, babyName: String, lastFeedTimestamp: Long) {
        // Legacy scheduling retained only for old entry points. New sessions use SmartReminderManager.
        val intent = Intent(context, FeedingReminderReceiver::class.java).apply {
            action = ACTION_FEEDING_REMINDER
            putExtra("baby_name", babyName)
            putExtra("last_feed_time", lastFeedTimestamp)
        }
        val pending = PendingIntent.getBroadcast(context, 2001, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val manager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !manager.canScheduleExactAlarms()) {
                manager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTimestamp, pending)
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                manager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTimestamp, pending)
            } else manager.setExact(AlarmManager.RTC_WAKEUP, triggerTimestamp, pending)
        } catch (_: SecurityException) {
            manager.set(AlarmManager.RTC_WAKEUP, triggerTimestamp, pending)
        }
    }

    fun snoozeReminder(context: Context, snoozeMinutes: Int, babyName: String) {
        scheduleNextReminder(context,
            System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(snoozeMinutes.toLong()),
            babyName, System.currentTimeMillis())
        NotificationHelper.cancelNotification(context)
    }

    fun cancelReminder(context: Context) {
        val intent = Intent(context, FeedingReminderReceiver::class.java).apply { action = ACTION_FEEDING_REMINDER }
        val pending = PendingIntent.getBroadcast(context, 2001, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        (context.getSystemService(Context.ALARM_SERVICE) as AlarmManager).cancel(pending)
    }
}

class FeedingReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            SmartReminderManager.ACTION_FIRE -> {
                val babyId = intent.getLongExtra(SmartReminderManager.EXTRA_BABY_ID, -1L)
                val kind = runCatching {
                    ReminderKind.valueOf(intent.getStringExtra(SmartReminderManager.EXTRA_KIND).orEmpty())
                }.getOrNull() ?: return
                if (babyId < 0) return
                SmartReminderManager.onAlarm(
                    context, babyId, kind,
                    intent.getLongExtra(SmartReminderManager.EXTRA_DUE, -1L),
                    intent.getStringExtra("baby_name") ?: "Baby"
                )
            }
            SmartReminderManager.ACTION_SNOOZE -> {
                val babyId = intent.getLongExtra(SmartReminderManager.EXTRA_BABY_ID, -1L)
                val kind = runCatching {
                    ReminderKind.valueOf(intent.getStringExtra(SmartReminderManager.EXTRA_KIND).orEmpty())
                }.getOrNull() ?: return
                if (babyId < 0) return
                val pending = goAsync()
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val baby = AppDatabase.getDatabase(context).babyDao().getBabyById(babyId)
                        if (baby != null) SmartReminderManager.snooze(
                            context, baby, kind,
                            intent.getIntExtra(SmartReminderManager.EXTRA_SNOOZE, 15))
                    } finally { pending.finish() }
                }
            }
            Intent.ACTION_BOOT_COMPLETED, Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED, "android.app.action.SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED" -> {
                val pending = goAsync()
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        NotificationHelper.createNotificationChannel(context)
                        SmartReminderManager.restoreAll(context)
                    } finally { pending.finish() }
                }
            }
            FeedingReminderScheduler.ACTION_FEEDING_REMINDER -> {
                val name = intent.getStringExtra("baby_name") ?: "Baby"
                NotificationHelper.showFeedingNotification(context, name, "a while", "previously")
            }
            FeedingReminderScheduler.ACTION_SNOOZE_REMINDER -> {
                FeedingReminderScheduler.snoozeReminder(context,
                    intent.getIntExtra("snooze_minutes", 15),
                    intent.getStringExtra("baby_name") ?: "Baby")
            }
        }
    }
}
