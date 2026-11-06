package com.example.reminder

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import com.example.data.db.AppDatabase
import com.example.data.model.BabyProfile
import kotlinx.coroutines.flow.first
import java.util.Calendar
import java.util.concurrent.TimeUnit

/**
 * Caregiver-configured planning reminders. These are not clinical minimum feeding,
 * diaper, or sleep frequencies; hunger cues and clinician advice take precedence.
 */
enum class ReminderKind(val title: String, val destination: String) {
    FEED("Feeding", "feeding"),
    SLEEP("Sleep check", "sleep"),
    DIAPER("Diaper check", "diaper")
}

data class ReminderRule(
    val enabled: Boolean,
    val intervalMinutes: Int,
    val fixedTimes: String = "",
    val nextDueAt: Long = 0L
)

object SmartReminderManager {
    const val ACTION_FIRE = "com.example.tummio.reminder.FIRE"
    const val ACTION_SNOOZE = "com.example.tummio.reminder.SNOOZE"
    const val EXTRA_BABY_ID = "baby_id"
    const val EXTRA_KIND = "reminder_kind"
    const val EXTRA_DUE = "reminder_due"
    const val EXTRA_SNOOZE = "snooze_minutes"
    private const val STORE = "tummio_smart_reminders"

    private fun prefs(context: Context) =
        context.applicationContext.getSharedPreferences(STORE, Context.MODE_PRIVATE)

    private fun key(babyId: Long, kind: ReminderKind, suffix: String) =
        "${babyId}_${kind.name}_${suffix}"

    fun getRule(context: Context, babyId: Long, kind: ReminderKind): ReminderRule {
        val p = prefs(context)
        return ReminderRule(
            enabled = p.getBoolean(key(babyId, kind, "enabled"), kind == ReminderKind.FEED),
            intervalMinutes = p.getInt(key(babyId, kind, "interval"), 180).coerceIn(30, 1440),
            fixedTimes = p.getString(key(babyId, kind, "times"), "").orEmpty(),
            nextDueAt = p.getLong(key(babyId, kind, "due"), 0L)
        )
    }

    private fun persist(context: Context, babyId: Long, kind: ReminderKind, rule: ReminderRule) {
        prefs(context).edit()
            .putBoolean(key(babyId, kind, "enabled"), rule.enabled)
            .putInt(key(babyId, kind, "interval"), rule.intervalMinutes)
            .putString(key(babyId, kind, "times"), rule.fixedTimes)
            .putLong(key(babyId, kind, "due"), rule.nextDueAt)
            .apply()
    }

    /**
     * Suggested starting points for an OPTIONAL planning routine, not WHO/AAP
     * medical targets. Parents can override or disable each reminder.
     */
    fun ageStartingPoint(ageDays: Long, kind: ReminderKind): Int = when (kind) {
        ReminderKind.FEED -> when {
            ageDays < 28 -> 150
            ageDays < 180 -> 180
            else -> 240
        }
        ReminderKind.SLEEP -> when {
            ageDays < 90 -> 120
            ageDays < 180 -> 180
            else -> 240
        }
        ReminderKind.DIAPER -> if (ageDays < 180) 180 else 240
    }

    fun validateTimes(text: String): Boolean =
        text.isBlank() || text.split(",").all {
            it.trim().matches(Regex("(?:[01]?\\d|2[0-3]):[0-5]\\d"))
        }

    fun nextFixedTime(text: String, now: Long): Long? {
        if (text.isBlank() || !validateTimes(text)) return null
        return text.split(",").map { it.trim() }.map { time ->
            val parts = time.split(":")
            val cal = Calendar.getInstance().apply {
                timeInMillis = now
                set(Calendar.HOUR_OF_DAY, parts[0].toInt())
                set(Calendar.MINUTE, parts[1].toInt())
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
                if (timeInMillis <= now) add(Calendar.DAY_OF_YEAR, 1)
            }
            cal.timeInMillis
        }.minOrNull()
    }

    fun saveRule(context: Context, baby: BabyProfile, kind: ReminderKind, incoming: ReminderRule) {
        val interval = incoming.intervalMinutes.coerceIn(30, 1440)
        val times = incoming.fixedTimes.trim()
        require(validateTimes(times)) { "Use comma-separated times like 07:00, 12:30" }
        val now = System.currentTimeMillis()
        val due = when {
            !incoming.enabled -> 0L
            times.isNotBlank() -> nextFixedTime(times, now) ?: 0L
            else -> now + TimeUnit.MINUTES.toMillis(interval.toLong())
        }
        val rule = incoming.copy(intervalMinutes = interval, fixedTimes = times, nextDueAt = due)
        persist(context, baby.id, kind, rule)
        schedule(context, baby, kind, rule)
    }

    /**
     * An actual, recent activity resets an interval schedule.
     * Historical/backdated records must not overwrite live reminders.
     */
    fun onActivityLogged(context: Context, baby: BabyProfile, kind: ReminderKind, timestamp: Long) {
        val now = System.currentTimeMillis()
        if (timestamp < now - TimeUnit.MINUTES.toMillis(15) || timestamp > now + TimeUnit.MINUTES.toMillis(2)) return
        val current = getRule(context, baby.id, kind)
        if (!current.enabled) return
        val due = if (current.fixedTimes.isNotBlank()) {
            current.nextDueAt.takeIf { it > now } ?: nextFixedTime(current.fixedTimes, now) ?: 0L
        } else {
            timestamp + TimeUnit.MINUTES.toMillis(current.intervalMinutes.toLong())
        }
        val updated = current.copy(nextDueAt = due)
        persist(context, baby.id, kind, updated)
        schedule(context, baby, kind, updated)
        NotificationHelper.cancel(context, baby.id, kind)
    }

    fun snooze(context: Context, baby: BabyProfile, kind: ReminderKind, minutes: Int) {
        val rule = getRule(context, baby.id, kind)
        if (!rule.enabled) return
        val updated = rule.copy(nextDueAt = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(minutes.coerceIn(1, 120).toLong()))
        persist(context, baby.id, kind, updated)
        schedule(context, baby, kind, updated)
        NotificationHelper.cancel(context, baby.id, kind)
    }

    fun onAlarm(context: Context, babyId: Long, kind: ReminderKind, expectedDue: Long, babyName: String) {
        val rule = getRule(context, babyId, kind)
        // Stale PendingIntents must not show reminders after a reset/disable.
        if (!rule.enabled || expectedDue <= 0 || expectedDue != rule.nextDueAt) return
        NotificationHelper.showReminder(context, babyId, babyName, kind)

        // Interval reminders fire once per activity, rather than nagging indefinitely.
        // Fixed-clock reminders recur at the next selected clock time.
        val next = if (rule.fixedTimes.isNotBlank()) {
            nextFixedTime(rule.fixedTimes, System.currentTimeMillis() + 1000L) ?: 0L
        } else 0L
        val updated = rule.copy(nextDueAt = next)
        persist(context, babyId, kind, updated)
        if (next > 0) scheduleById(context, babyId, babyName, kind, next)
    }

    fun schedule(context: Context, baby: BabyProfile, kind: ReminderKind, rule: ReminderRule = getRule(context, baby.id, kind)) {
        cancelAlarm(context, baby.id, kind)
        if (rule.enabled && rule.nextDueAt > 0) {
            scheduleById(context, baby.id, baby.name, kind, rule.nextDueAt)
        } else if (!rule.enabled) {
            NotificationHelper.cancel(context, baby.id, kind)
        }
    }

    private fun requestCode(babyId: Long, kind: ReminderKind): Int =
        ((babyId % 100_000).toInt() * 10) + kind.ordinal + 2000

    fun notificationId(babyId: Long, kind: ReminderKind): Int = requestCode(babyId, kind) + 20_000

    private fun fireIntent(context: Context, babyId: Long, kind: ReminderKind): Intent =
        Intent(context, FeedingReminderReceiver::class.java).apply {
            action = ACTION_FIRE
            data = Uri.parse("tummio://reminder/${babyId}/${kind.name.lowercase()}")
        }

    private fun cancelAlarm(context: Context, babyId: Long, kind: ReminderKind) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pending = PendingIntent.getBroadcast(
            context, requestCode(babyId, kind), fireIntent(context, babyId, kind),
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pending != null) {
            alarmManager.cancel(pending)
            pending.cancel()
        }
    }

    private fun scheduleById(context: Context, babyId: Long, babyName: String, kind: ReminderKind, dueAt: Long) {
        val manager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = fireIntent(context, babyId, kind).apply {
            putExtra(EXTRA_BABY_ID, babyId)
            putExtra(EXTRA_KIND, kind.name)
            putExtra(EXTRA_DUE, dueAt)
            putExtra("baby_name", babyName)
        }
        val pending = PendingIntent.getBroadcast(
            context, requestCode(babyId, kind), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val triggerAt = dueAt.coerceAtLeast(System.currentTimeMillis() + 1000L)
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !manager.canScheduleExactAlarms()) {
                manager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pending)
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                manager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pending)
            } else {
                manager.setExact(AlarmManager.RTC_WAKEUP, triggerAt, pending)
            }
        } catch (_: SecurityException) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                manager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pending)
            else manager.set(AlarmManager.RTC_WAKEUP, triggerAt, pending)
        }
    }

    /**
     * Re-registers alarms that Android drops on reboot.
     * Skips expired interval alarms instead of displaying a stale notification.
     */
    suspend fun restoreAll(context: Context) {
        val babies = AppDatabase.getDatabase(context).babyDao().getAllBabies().first()
        val now = System.currentTimeMillis()
        for (baby in babies) {
            for (kind in ReminderKind.entries) {
                val rule = getRule(context, baby.id, kind)
                if (!rule.enabled) continue
                val due = when {
                    rule.fixedTimes.isNotBlank() -> nextFixedTime(rule.fixedTimes, now) ?: 0L
                    rule.nextDueAt > now -> rule.nextDueAt
                    rule.nextDueAt > 0 -> now + TimeUnit.MINUTES.toMillis(rule.intervalMinutes.toLong())
                    else -> 0L
                }
                if (due > 0) {
                    val restored = rule.copy(nextDueAt = due)
                    persist(context, baby.id, kind, restored)
                    schedule(context, baby, kind, restored)
                }
            }
        }
    }
}
