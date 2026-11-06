package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.FeedingType
import com.example.guidelines.GuidelineComparison
import com.example.ui.DailySummary
import com.example.ui.MainViewModel
import com.example.ui.theme.*
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
    val guidelineComparison by viewModel.guidelineComparison.collectAsState()
    val nowTicker by viewModel.currentTicker.collectAsState()
    val dateFormatter = remember { SimpleDateFormat("MMM d", Locale.getDefault()) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier
                            .clickable { onNavigate("profile") }
                            .padding(vertical = 4.dp)
                    ) {
                        // Baby avatar from attachment
                        Image(
                            painter = painterResource(R.drawable.app_hero_avatar),
                            contentDescription = "Baby Profile",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .border(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), CircleShape)
                        )
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    text = activeBaby?.name ?: "LittleRoutine",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                // Playful heart icon
                                Icon(
                                    imageVector = Icons.Default.Favorite,
                                    contentDescription = null,
                                    tint = PlayfulAccentPink,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
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
                        Icon(Icons.Outlined.People, contentDescription = "Switch Baby Profile", tint = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(
                        onClick = { onNavigate("settings") },
                        modifier = Modifier.testTag("action_settings")
                    ) {
                        Icon(Icons.Outlined.Settings, contentDescription = "Settings", tint = MaterialTheme.colorScheme.primary)
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
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(top = 8.dp, bottom = 28.dp)
        ) {
            // 1. Next Planned Feeding Card with Lavender Bliss Gradient
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

            // 2. Playful Semantic Quick Action Grid (Lavender for Feeding, Peach for Diapers, Sky Blue for Sleep, Mint for Growth)
            item {
                Text(
                    text = "Quick Actions",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(10.dp))
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    PlayfulQuickButton(
                        icon = Icons.Outlined.LocalDrink,
                        label = "Bottle",
                        containerColor = FeedingContainer,
                        contentColor = OnFeedingContainer,
                        iconColor = FeedingColor,
                        modifier = Modifier.weight(1f).testTag("quick_action_bottle"),
                        onClick = { onNavigate("feeding") }
                    )
                    PlayfulQuickButton(
                        icon = Icons.Outlined.Timer,
                        label = "Nursing",
                        containerColor = FeedingContainer,
                        contentColor = OnFeedingContainer,
                        iconColor = FeedingColor,
                        modifier = Modifier.weight(1f).testTag("quick_action_nursing"),
                        onClick = { onNavigate("feeding") }
                    )
                    PlayfulQuickButton(
                        icon = Icons.Outlined.BabyChangingStation,
                        label = "Diaper",
                        containerColor = DiaperPeachContainer,
                        contentColor = OnDiaperPeachContainer,
                        iconColor = DiaperPeach,
                        modifier = Modifier.weight(1f).testTag("quick_action_diaper"),
                        onClick = { onNavigate("diaper") }
                    )
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    PlayfulQuickButton(
                        icon = Icons.Outlined.Bedtime,
                        label = "Sleep",
                        containerColor = SleepSkyBlueContainer,
                        contentColor = OnSleepSkyBlueContainer,
                        iconColor = SleepSkyBlue,
                        modifier = Modifier.weight(1f).testTag("quick_action_sleep"),
                        onClick = { onNavigate("sleep") }
                    )
                    PlayfulQuickButton(
                        icon = Icons.Outlined.Straighten,
                        label = "Growth",
                        containerColor = GrowthMintContainer,
                        contentColor = OnGrowthMintContainer,
                        iconColor = GrowthMint,
                        modifier = Modifier.weight(1f).testTag("quick_action_growth"),
                        onClick = { onNavigate("growth") }
                    )
                    }
                }
            }

            // 3. Daily Nutrition & Intake Snapshot (Lavender Bliss styling)
            item {
                DailyNutritionCard(
                    summary = dailySummary,
                    comparison = guidelineComparison,
                    onViewDetails = { onNavigate("insights") }
                )
            }

            // 4. Growth Snapshot Card (Mint Accent for Growth)
            item {
                Card(
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, GrowthMintContainer),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigate("growth") }
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(GrowthMintContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Outlined.Straighten,
                                        contentDescription = null,
                                        tint = GrowthMint,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Text(
                                    text = "Growth Snapshot",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Surface(
                                color = GrowthMintContainer,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "WHO Standards",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                    color = OnGrowthMintContainer,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        if (latestGrowth != null) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                GrowthMetricColumn(
                                    title = "Weight",
                                    value = "${latestGrowth!!.weightKg ?: "--"} kg",
                                    subtext = latestWhoAssessment?.let { "~${it.percentile.roundToInt()}th %ile" } ?: "Logged",
                                    accentColor = GrowthMint
                                )
                                GrowthMetricColumn(
                                    title = "Length",
                                    value = "${latestGrowth!!.lengthCm ?: "--"} cm",
                                    subtext = if (latestGrowth!!.isRecumbent) "Recumbent" else "Standing",
                                    accentColor = GrowthMint
                                )
                                GrowthMetricColumn(
                                    title = "Head Circ.",
                                    value = "${latestGrowth!!.headCircumferenceCm ?: "--"} cm",
                                    subtext = dateFormatter.format(Date(latestGrowth!!.timestamp)),
                                    accentColor = GrowthMint
                                )
                            }
                        } else {
                            Text(
                                text = "No growth measurements recorded yet. Tap to record baby's weight and length against WHO curves.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // 5. Formula Management & CDC Bottle Safety Card
            item {
                Card(
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigate("formula") }
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(LavenderPastelSurface),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Outlined.Shield,
                                contentDescription = null,
                                tint = LavenderPrimary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Formula Management & Safety",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "CDC preparation guidelines, scoop ratio calculator, and safety countdown.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Icon(
                            Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = LavenderPrimary
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

    // Soft Lavender to Lilac gradient
    val cardGradient = Brush.linearGradient(
        colors = listOf(LavenderPrimary, LavenderSecondary)
    )

    Card(
        shape = RoundedCornerShape(26.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(26.dp))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(cardGradient)
                .padding(22.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "NEXT PLANNED FEED",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color.White.copy(alpha = 0.85f),
                            letterSpacing = 1.2.sp
                        )
                        // Playful heart badge
                        Surface(
                            color = PlayfulAccentPink,
                            shape = CircleShape,
                            modifier = Modifier.size(16.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.Favorite,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(10.dp)
                                )
                            }
                        }
                    }

                    Surface(
                        color = Color.White.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = timeFormatter.format(Date(nextFeedTimestamp)),
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

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
                    style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.ExtraBold),
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(6.dp))

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
                    color = Color.White.copy(alpha = 0.9f)
                )

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onLogFeed,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = LavenderPrimary
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.weight(1f).height(46.dp).testTag("home_button_record_feed")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Record Feed", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
                    }

                    OutlinedButton(
                        onClick = onSnooze,
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color.White
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, Color.White.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.height(46.dp).testTag("home_button_snooze")
                    ) {
                        Text("+15m Snooze", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold))
                    }
                }
            }
        }
    }
}

@Composable
private fun PlayfulQuickButton(
    icon: ImageVector,
    label: String,
    containerColor: Color,
    contentColor: Color,
    iconColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(18.dp),
        color = containerColor,
        border = androidx.compose.foundation.BorderStroke(1.dp, iconColor.copy(alpha = 0.25f)),
        modifier = modifier.heightIn(min = 84.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = iconColor,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = contentColor
            )
        }
    }
}

@Composable
private fun DailyNutritionCard(
    summary: DailySummary,
    comparison: GuidelineComparison?,
    onViewDetails: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(LavenderPastelSurface),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Outlined.LocalDrink,
                            contentDescription = null,
                            tint = LavenderPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Text(
                        text = "Past 24 Hours Intake",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Text(
                    text = "View Insights →",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable { onViewDetails() }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                NutritionColumn("Measurable Bottle", "${summary.totalBottleIntakeMl.roundToInt()} mL",
                    "${summary.bottleSessionsCount} feeds", LavenderPrimary)
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                NutritionColumn("Formula / EBM",
                    "${summary.formulaConsumedMl.roundToInt()} / ${summary.ebmConsumedMl.roundToInt()} mL",
                    "Intake split", LavenderSecondary)
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                NutritionColumn("Direct Nursing", "${summary.nursingTotalMinutes} mins",
                    "${summary.nursingSessionsCount} sessions", PlayfulAccentPink)
            }

            // Required Intake based on Weight & Age
            if (comparison?.weightKgUsed != null) {
                Spacer(modifier = Modifier.height(14.dp))
                Surface(
                    color = LavenderPastelSurface.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth().clickable { onViewDetails() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Reference for ${comparison.weightKgUsed} kg (${comparison.selectedGuidelineName}):",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${comparison.referenceRangeLowerMl.roundToInt()} – ${comparison.referenceRangeUpperMl.roundToInt()} mL / 24 hours",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = LavenderPrimary
                            )
                        }
                        SuggestionChip(
                            onClick = onViewDetails,
                            label = { Text("Info", style = MaterialTheme.typography.labelMedium) },
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = LavenderPastelSurface,
                                labelColor = LavenderPrimary
                            ),
                            border = null
                        )
                    }
                }
            }

            if (summary.averageBottleMl > 0) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(modifier = Modifier.height(10.dp))
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Avg bottle: ${summary.averageBottleMl.roundToInt()} mL",
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
private fun NutritionColumn(title: String, value: String, subtext: String, badgeColor: Color) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(text = subtext, style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Text(text = value, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = badgeColor)
    }
}

@Composable
private fun GrowthMetricColumn(title: String, value: String, subtext: String, accentColor: Color) {
    Column {
        Text(text = title, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(3.dp))
        Text(text = value, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold), color = accentColor)
        Text(text = subtext, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
