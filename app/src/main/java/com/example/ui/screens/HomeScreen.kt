package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FeedingType
import com.example.ui.DailySummary
import com.example.ui.MainViewModel
import com.example.who.WhoGrowthCalculator
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    onNavigate: (String) -> Unit
) {
    val activeBaby by viewModel.activeBaby.collectAsState()
    val lastFeed by viewModel.lastFeeding.collectAsState()
    val dailySummary by viewModel.dailySummary.collectAsState()
    val latestGrowth by viewModel.latestGrowth.collectAsState()
    val latestWhoAssessment by viewModel.latestWhoAssessment.collectAsState()
    val nowTicker by viewModel.currentTicker.collectAsState()
    val timeFormatter = remember { SimpleDateFormat("h:mm a", Locale.getDefault()) }
    val dateFormatter = remember { SimpleDateFormat("MMM d", Locale.getDefault()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier
                            .clickable { onNavigate("profile") }
                            .padding(vertical = 4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ChildCare,
                                contentDescription = "Baby Profile",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Column {
                            Text(
                                text = activeBaby?.name ?: "LittleRoutine",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (activeBaby != null) {
                                val ageDays = WhoGrowthCalculator.calculateAgeDays(activeBaby!!.birthTimestamp, nowTicker)
                                val months = ageDays / 30
                                val remDays = ageDays % 30
                                val ageStr = if (months > 0) "${months}m ${remDays}d" else "${ageDays} days"
                                Text(
                                    text = "Age: $ageStr • ${activeBaby!!.sex.name.lowercase().replaceFirstChar { it.uppercase() }}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = { onNavigate("profile") },
                        modifier = Modifier.testTag("action_switch_profile")
                    ) {
                        Icon(Icons.Outlined.People, contentDescription = "Switch Baby Profile")
                    }
                    IconButton(
                        onClick = { onNavigate("settings") },
                        modifier = Modifier.testTag("action_settings")
                    ) {
                        Icon(Icons.Outlined.Settings, contentDescription = "Settings")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            // 1. Next Planned Feeding Card
            item {
                NextFeedingCard(
                    babyName = activeBaby?.name ?: "Baby",
                    lastFeedTime = lastFeed?.timestamp,
                    lastFeedAmount = lastFeed?.consumedMl,
                    lastFeedType = lastFeed?.type,
                    intervalMinutes = activeBaby?.reminderIntervalMinutes ?: 180,
                    currentTime = nowTicker,
                    onLogFeed = { onNavigate("feeding") },
                    onSnooze = { viewModel.snoozeReminder(15) }
                )
            }

            // 2. Quick Action Grid
            item {
                Text(
                    text = "Quick Actions",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    QuickActionButton(
                        icon = Icons.Outlined.LocalDrink,
                        label = "Bottle",
                        modifier = Modifier.weight(1f).testTag("quick_action_bottle"),
                        onClick = { onNavigate("feeding") }
                    )
                    QuickActionButton(
                        icon = Icons.Outlined.Timer,
                        label = "Nursing",
                        modifier = Modifier.weight(1f).testTag("quick_action_nursing"),
                        onClick = { onNavigate("feeding") }
                    )
                    QuickActionButton(
                        icon = Icons.Outlined.BabyChangingStation,
                        label = "Diaper",
                        modifier = Modifier.weight(1f).testTag("quick_action_diaper"),
                        onClick = { onNavigate("diaper") }
                    )
                    QuickActionButton(
                        icon = Icons.Outlined.Bedtime,
                        label = "Sleep",
                        modifier = Modifier.weight(1f).testTag("quick_action_sleep"),
                        onClick = { onNavigate("sleep") }
                    )
                }
            }

            // 3. Daily Nutrition & Intake Snapshot
            item {
                DailyNutritionCard(
                    summary = dailySummary,
                    onViewDetails = { onNavigate("insights") }
                )
            }

            // 4. Growth Snapshot Card
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigate("growth") }
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(
                                    Icons.Outlined.Straighten,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = "Growth Snapshot",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Text(
                                text = "WHO Standards",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        if (latestGrowth != null) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                GrowthMetricColumn(
                                    title = "Weight",
                                    value = "${latestGrowth!!.weightKg ?: "--"} kg",
                                    subtext = latestWhoAssessment?.let { "~${it.percentile.roundToInt()}th %ile" } ?: "Logged"
                                )
                                GrowthMetricColumn(
                                    title = "Length",
                                    value = "${latestGrowth!!.lengthCm ?: "--"} cm",
                                    subtext = if (latestGrowth!!.isRecumbent) "Recumbent" else "Standing"
                                )
                                GrowthMetricColumn(
                                    title = "Head Circ.",
                                    value = "${latestGrowth!!.headCircumferenceCm ?: "--"} cm",
                                    subtext = dateFormatter.format(Date(latestGrowth!!.timestamp))
                                )
                            }
                        } else {
                            Text(
                                text = "No growth measurements recorded yet. Tap to record baby's weight and length.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // 5. CDC Bottle Safety Alert / Active Bottles (if any)
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigate("formula") }
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            Icons.Outlined.Shield,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Formula Management & Safety",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "CDC preparation guidelines, scoop ratio calculator, and powder inventory.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Icon(
                            Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NextFeedingCard(
    babyName: String,
    lastFeedTime: Long?,
    lastFeedAmount: Double?,
    lastFeedType: FeedingType?,
    intervalMinutes: Int,
    currentTime: Long,
    onLogFeed: () -> Unit,
    onSnooze: () -> Unit
) {
    val intervalMillis = TimeUnit.MINUTES.toMillis(intervalMinutes.toLong())
    val nextFeedTimestamp = (lastFeedTime ?: (currentTime - TimeUnit.HOURS.toMillis(3))) + intervalMillis
    val diffMillis = nextFeedTimestamp - currentTime
    val elapsedSinceLastMillis = if (lastFeedTime != null) currentTime - lastFeedTime else 0L

    val isOverdue = diffMillis <= 0
    val timeFormatter = remember { SimpleDateFormat("h:mm a", Locale.getDefault()) }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "NEXT PLANNED FEED",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f),
                    letterSpacing = 1.sp
                )
                Text(
                    text = timeFormatter.format(Date(nextFeedTimestamp)),
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Main Countdown or Due Notice
            val countdownText = if (isOverdue) {
                val overdueMins = TimeUnit.MILLISECONDS.toMinutes(-diffMillis)
                if (overdueMins > 60) "Due (${overdueMins / 60}h ${overdueMins % 60}m ago)" else "Due (${overdueMins}m ago)"
            } else {
                val remHours = TimeUnit.MILLISECONDS.toHours(diffMillis)
                val remMins = TimeUnit.MILLISECONDS.toMinutes(diffMillis) % 60
                if (remHours > 0) "in ${remHours}h ${remMins}m" else "in ${remMins}m"
            }

            Text(
                text = countdownText,
                style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onPrimary
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Last feed context
            val lastFeedDesc = if (lastFeedTime != null) {
                val elapsedH = TimeUnit.MILLISECONDS.toHours(elapsedSinceLastMillis)
                val elapsedM = TimeUnit.MILLISECONDS.toMinutes(elapsedSinceLastMillis) % 60
                val elapsedStr = if (elapsedH > 0) "${elapsedH}h ${elapsedM}m" else "${elapsedM}m"
                val amountStr = if (lastFeedAmount != null && lastFeedAmount > 0) " • ${lastFeedAmount.roundToInt()} mL" else ""
                val typeStr = lastFeedType?.name?.replace("_", " ")?.lowercase() ?: "feed"
                "Last: $elapsedStr ago ($typeStr$amountStr)"
            } else {
                "No previous feed recorded today"
            }

            Text(
                text = lastFeedDesc,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Action buttons inside card
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onLogFeed,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.onSurface
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f).testTag("home_button_record_feed")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Record Feed", style = MaterialTheme.typography.labelLarge)
                }

                OutlinedButton(
                    onClick = onSnooze,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.3f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("home_button_snooze")
                ) {
                    Text("+15m Snooze", style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}

@Composable
private fun QuickActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = modifier.height(68.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun DailyNutritionCard(
    summary: DailySummary,
    onViewDetails: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Past 24 Hours Intake",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "View Insights",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable { onViewDetails() }
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                NutritionColumn(
                    title = "Measurable Bottle",
                    value = "${summary.totalBottleIntakeMl.roundToInt()} mL",
                    subtext = "${summary.bottleSessionsCount} feeds"
                )
                NutritionColumn(
                    title = "Formula / EBM",
                    value = "${summary.formulaConsumedMl.roundToInt()} / ${summary.ebmConsumedMl.roundToInt()} mL",
                    subtext = "Split"
                )
                NutritionColumn(
                    title = "Direct Nursing",
                    value = "${summary.nursingTotalMinutes} mins",
                    subtext = "${summary.nursingSessionsCount} sessions"
                )
            }

            if (summary.averageBottleMl > 0) {
                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Average bottle volume: ${summary.averageBottleMl.roundToInt()} mL",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (summary.averageIntervalMinutes > 0) {
                        Text(
                            text = "Avg interval: ${summary.averageIntervalMinutes / 60}h ${summary.averageIntervalMinutes % 60}m",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NutritionColumn(title: String, value: String, subtext: String) {
    Column {
        Text(text = title, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = value, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
        Text(text = subtext, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun GrowthMetricColumn(title: String, value: String, subtext: String) {
    Column {
        Text(text = title, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = value, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
        Text(text = subtext, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
