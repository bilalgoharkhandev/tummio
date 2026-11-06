package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.BabyProfile
import com.example.reminder.ReminderKind
import com.example.reminder.ReminderRule
import com.example.reminder.SmartReminderManager
import com.example.ui.MainViewModel
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmartReminderScreen(viewModel: MainViewModel, onNavigateBack: () -> Unit) {
    val baby by viewModel.activeBaby.collectAsState()
    val context = LocalContext.current

    Scaffold(topBar = {
        TopAppBar(
            title = { Text("Smart Reminders", style = MaterialTheme.typography.titleLarge) },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.Outlined.ArrowBack, contentDescription = "Back")
                }
            }
        )
    }) { inset ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(inset),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 36.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    "Plan check-ins around your family's routine",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    "Choose an interval or times of day for each activity. These are optional caregiver reminders, not medical requirements. Respond to your baby's cues and follow your clinician's advice.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            val selectedBaby = baby
            if (selectedBaby != null) {
                items(ReminderKind.entries.size) { index ->
                    ReminderEditor(selectedBaby, ReminderKind.entries[index])
                }
            } else {
                item { Text("Create or choose a baby profile to configure reminders.") }
            }
            item {
                Text(
                    "Android may delay alerts under power saving or when precise alarm access is unavailable. Enable app notifications in Android Settings.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ReminderEditor(baby: BabyProfile, kind: ReminderKind) {
    val context = LocalContext.current
    var rule by remember(baby.id, kind) {
        mutableStateOf(SmartReminderManager.getRule(context, baby.id, kind))
    }
    var intervalText by remember(baby.id, kind) { mutableStateOf(rule.intervalMinutes.toString()) }
    var clockTimes by remember(baby.id, kind) { mutableStateOf(rule.fixedTimes) }
    var validation by remember(baby.id, kind) { mutableStateOf<String?>(null) }
    var saved by remember(baby.id, kind) { mutableStateOf(false) }
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }

    LaunchedEffect(baby.id, kind) {
        while (true) {
            now = System.currentTimeMillis()
            delay(30_000)
        }
    }

    val ageDays = TimeUnit.MILLISECONDS.toDays((now - baby.birthTimestamp).coerceAtLeast(0))
    val suggested = SmartReminderManager.ageStartingPoint(ageDays, kind)
    val descriptions = when (kind) {
        ReminderKind.FEED -> "Check feeding cues and log milk or nursing"
        ReminderKind.SLEEP -> "Review naps, bedtime or sleep"
        ReminderKind.DIAPER -> "Check if a change is needed"
    }
    val timeFormat = remember { SimpleDateFormat("EEE, h:mm a", Locale.getDefault()) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            Modifier.fillMaxWidth().padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.NotificationsActive, contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text(kind.title, style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold)
                    Text(descriptions, style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Switch(
                    checked = rule.enabled,
                    onCheckedChange = {
                        val next = rule.copy(enabled = it)
                        SmartReminderManager.saveRule(context, baby, kind, next)
                        rule = SmartReminderManager.getRule(context, baby.id, kind)
                        saved = true
                    }
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Text("Repeat every (minutes)", style = MaterialTheme.typography.labelLarge)
            OutlinedTextField(
                value = intervalText,
                onValueChange = { intervalText = it.filter(Char::isDigit); saved = false },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text("Interval: 30–1440 minutes") },
                supportingText = { Text("Used when no fixed times are entered") },
                shape = RoundedCornerShape(14.dp)
            )
            OutlinedButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = { intervalText = suggested.toString(); clockTimes = ""; saved = false }
            ) {
                Text("Try age-based planning preset: ${suggested / 60}h ${suggested % 60}m")
            }
            OutlinedTextField(
                value = clockTimes,
                onValueChange = { clockTimes = it; saved = false },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Optional fixed clock times") },
                placeholder = { Text("08:00, 12:00, 18:30") },
                supportingText = { Text("Use 24-hour HH:mm, separated by commas") },
                minLines = 1,
                maxLines = 3,
                shape = RoundedCornerShape(14.dp)
            )

            validation?.let {
                Text(it, color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall)
            }
            if (rule.enabled && rule.nextDueAt > 0) {
                Text(
                    "Next check: ${timeFormat.format(Date(rule.nextDueAt))}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Button(
                modifier = Modifier.fillMaxWidth().heightIn(min = 50.dp),
                onClick = {
                    val minutes = intervalText.toIntOrNull()
                    validation = when {
                        minutes == null || minutes !in 30..1440 ->
                            "Choose an interval between 30 and 1440 minutes."
                        !SmartReminderManager.validateTimes(clockTimes) ->
                            "Enter times in HH:mm format, such as 08:00, 14:30."
                        else -> null
                    }
                    if (validation == null) {
                        SmartReminderManager.saveRule(
                            context, baby, kind,
                            rule.copy(intervalMinutes = minutes!!, fixedTimes = clockTimes)
                        )
                        rule = SmartReminderManager.getRule(context, baby.id, kind)
                        saved = true
                    }
                }
            ) { Text(if (saved) "Saved ✓" else "Save ${kind.title} Reminder") }
        }
    }
}
