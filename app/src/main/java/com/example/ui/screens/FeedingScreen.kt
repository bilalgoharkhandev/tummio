package com.example.ui.screens

import androidx.compose.animation.*
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FeedingType
import com.example.data.model.NursingBreast
import com.example.ui.MainViewModel
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun FeedingScreen(
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Bottle, 1: Breastfeeding, 2: Pumping

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Feeding Tracker",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Tab Selector
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.background,
                contentColor = MaterialTheme.colorScheme.primary,
                divider = { HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant) }
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Bottle Feed", style = MaterialTheme.typography.labelLarge) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Breastfeeding", style = MaterialTheme.typography.labelLarge) }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("Pumping", style = MaterialTheme.typography.labelLarge) }
                )
            }

            when (selectedTab) {
                0 -> BottleFeedingView(viewModel)
                1 -> BreastfeedingTimerView(viewModel)
                2 -> PumpingLogView(viewModel)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun BottleFeedingView(viewModel: MainViewModel) {
    val activeBaby by viewModel.activeBaby.collectAsState()
    val comparison by viewModel.guidelineComparison.collectAsState()

    var feedingType by remember { mutableStateOf(FeedingType.FORMULA) }
    var preparedMl by remember { mutableDoubleStateOf(120.0) }
    var remainingMl by remember { mutableDoubleStateOf(0.0) }
    var formulaBrand by remember { mutableStateOf("") }
    var caregiver by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var showSuccessSnackbar by remember { mutableStateOf(false) }

    val consumedMl = (preparedMl - remainingMl).coerceAtLeast(0.0)
    val presets = listOf(30.0, 60.0, 90.0, 120.0, 150.0, 180.0)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Daily Intake Guideline Banner based on Weight & Age
        if (comparison?.weightKgUsed != null) {
            item {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            Icons.Outlined.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Column {
                            Text(
                                text = "Guideline for ${activeBaby?.name ?: "Baby"} (${comparison!!.weightKgUsed} kg • ${comparison!!.selectedGuidelineName}):",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            val lower = comparison!!.referenceRangeLowerMl.roundToInt()
                            val upper = comparison!!.referenceRangeUpperMl.roundToInt()
                            val perFeedLower = (lower / 8.0).roundToInt()
                            val perFeedUpper = (upper / 8.0).roundToInt()
                            Text(
                                text = "$lower–$upper mL/day • ~$perFeedLower–$perFeedUpper mL/feed (~8 feeds/day)",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }

        // Milk Type Selector
        item {
            Text(
                "Milk Type",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MilkTypeChip(
                    label = "Formula",
                    selected = feedingType == FeedingType.FORMULA,
                    modifier = Modifier.weight(1f),
                    onClick = { feedingType = FeedingType.FORMULA }
                )
                MilkTypeChip(
                    label = "Expressed Milk",
                    selected = feedingType == FeedingType.EXPRESSED_BREAST_MILK,
                    modifier = Modifier.weight(1f),
                    onClick = { feedingType = FeedingType.EXPRESSED_BREAST_MILK }
                )
                MilkTypeChip(
                    label = "Mixed",
                    selected = feedingType == FeedingType.MIXED_BOTTLE,
                    modifier = Modifier.weight(1f),
                    onClick = { feedingType = FeedingType.MIXED_BOTTLE }
                )
            }
        }

        // Bottle Volume Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Prepared Quantity",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "${preparedMl.roundToInt()} mL",
                            style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilledTonalIconButton(onClick = { preparedMl = (preparedMl - 10.0).coerceAtLeast(10.0) }) {
                                Icon(Icons.Default.Remove, contentDescription = "Decrease 10mL")
                            }
                            FilledTonalIconButton(onClick = { preparedMl = (preparedMl + 10.0).coerceAtMost(360.0) }) {
                                Icon(Icons.Default.Add, contentDescription = "Increase 10mL")
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Preset buttons
                    Text(
                        "Quick Presets",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        presets.forEach { preset ->
                            SuggestionChip(
                                onClick = { preparedMl = preset },
                                label = { Text("${preset.roundToInt()} mL", style = MaterialTheme.typography.labelSmall) },
                                colors = SuggestionChipDefaults.suggestionChipColors(
                                    containerColor = if (preparedMl == preset) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Spacer(modifier = Modifier.height(14.dp))

                    // Remaining / Leftover milk
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                "Remaining / Wasted",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                "${remainingMl.roundToInt()} mL leftover",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf(0.0, 10.0, 20.0, 30.0).forEach { r ->
                                FilterChip(
                                    selected = remainingMl == r,
                                    onClick = { remainingMl = r },
                                    label = { Text("${r.roundToInt()}m") }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Actually Consumed Highlight
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(14.dp)
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Net Intake Consumed:",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                "${consumedMl.roundToInt()} mL",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }

        // Details (Brand, Notes)
        item {
            OutlinedTextField(
                value = formulaBrand,
                onValueChange = { formulaBrand = it },
                label = { Text("Formula Brand / Product (Optional)") },
                placeholder = { Text("e.g. Kendamil, Similac, Aptamil") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
        }

        item {
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Feeding Notes (Optional)") },
                placeholder = { Text("e.g. Burped well, calm feed") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
        }

        // Save Button
        item {
            Button(
                onClick = {
                    viewModel.logBottleFeed(
                        type = feedingType,
                        preparedMl = preparedMl,
                        remainingMl = remainingMl,
                        brand = formulaBrand,
                        caregiver = caregiver,
                        notes = notes
                    )
                    showSuccessSnackbar = true
                    remainingMl = 0.0
                    notes = ""
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("save_bottle_feed_button"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Save Bottle Feed (${consumedMl.roundToInt()} mL)", style = MaterialTheme.typography.titleSmall)
            }

            if (showSuccessSnackbar) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "✓ Bottle feed successfully recorded to timeline & daily totals.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun BreastfeedingTimerView(viewModel: MainViewModel) {
    val timerState by viewModel.nursingTimerState.collectAsState()
    var sessionNotes by remember { mutableStateOf("") }

    val leftMins = timerState.leftSeconds / 60
    val leftSecs = timerState.leftSeconds % 60
    val rightMins = timerState.rightSeconds / 60
    val rightSecs = timerState.rightSeconds % 60
    val totalMins = (timerState.leftSeconds + timerState.rightSeconds) / 60
    val totalSecs = (timerState.leftSeconds + timerState.rightSeconds) % 60

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // Last used breast indicator
        item {
            if (timerState.lastUsedBreast != null) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Outlined.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Last breast nursed: ${timerState.lastUsedBreast!!.name} breast. Start with the opposite breast if alternating.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        // Dual Breast Timers
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Left Breast Card
                BreastTimerCard(
                    title = "Left Breast",
                    minutes = leftMins,
                    seconds = leftSecs,
                    isActive = timerState.isRunning && timerState.activeBreast == NursingBreast.LEFT,
                    modifier = Modifier.weight(1f),
                    onToggle = {
                        if (timerState.isRunning && timerState.activeBreast == NursingBreast.LEFT) {
                            viewModel.pauseNursingTimer()
                        } else {
                            viewModel.startNursingTimer(NursingBreast.LEFT)
                        }
                    }
                )

                // Right Breast Card
                BreastTimerCard(
                    title = "Right Breast",
                    minutes = rightMins,
                    seconds = rightSecs,
                    isActive = timerState.isRunning && timerState.activeBreast == NursingBreast.RIGHT,
                    modifier = Modifier.weight(1f),
                    onToggle = {
                        if (timerState.isRunning && timerState.activeBreast == NursingBreast.RIGHT) {
                            viewModel.pauseNursingTimer()
                        } else {
                            viewModel.startNursingTimer(NursingBreast.RIGHT)
                        }
                    }
                )
            }
        }

        // Total Elapsed & Finish Controls
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Total Nursing Duration",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = String.format("%02d:%02d", totalMins, totalSecs),
                        style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = sessionNotes,
                        onValueChange = { sessionNotes = it },
                        label = { Text("Session Notes (Latch, pauses)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (timerState.isRunning) {
                            OutlinedButton(
                                onClick = { viewModel.pauseNursingTimer() },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Pause, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Pause")
                            }
                        }

                        Button(
                            onClick = {
                                viewModel.finishAndSaveNursingSession(sessionNotes)
                                sessionNotes = ""
                            },
                            enabled = timerState.leftSeconds > 0 || timerState.rightSeconds > 0,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        ) {
                            Icon(Icons.Default.Done, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Finish & Save")
                        }
                    }
                }
            }
        }

        // Clinical transparency note
        item {
            Text(
                text = "Clinical note: Nursing duration in minutes measures latch time and is never converted to an estimated mL volume, honoring authentic pediatric standards.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun BreastTimerCard(
    title: String,
    minutes: Long,
    seconds: Long,
    isActive: Boolean,
    modifier: Modifier = Modifier,
    onToggle: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface
        ),
        border = androidx.compose.foundation.BorderStroke(
            if (isActive) 2.dp else 1.dp,
            if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
        ),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                title,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = String.format("%02d:%02d", minutes, seconds),
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(12.dp))
            FilledIconButton(
                onClick = onToggle,
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = if (isActive) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                ),
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = if (isActive) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isActive) "Pause $title" else "Start $title"
                )
            }
        }
    }
}

@Composable
private fun PumpingLogView(viewModel: MainViewModel) {
    var leftOutput by remember { mutableDoubleStateOf(60.0) }
    var rightOutput by remember { mutableDoubleStateOf(60.0) }
    var durationMinutes by remember { mutableIntStateOf(15) }
    var storage by remember { mutableStateOf("Fridge") }
    var notes by remember { mutableStateOf("") }
    var savedMessage by remember { mutableStateOf(false) }

    val total = leftOutput + rightOutput

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Expressed Output (mL)",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Left Breast", style = MaterialTheme.typography.labelSmall)
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("${leftOutput.roundToInt()} mL", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                                Spacer(modifier = Modifier.weight(1f))
                                IconButton(onClick = { leftOutput = (leftOutput + 10.0).coerceAtMost(250.0) }) {
                                    Icon(Icons.Default.Add, contentDescription = null)
                                }
                            }
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Right Breast", style = MaterialTheme.typography.labelSmall)
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("${rightOutput.roundToInt()} mL", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                                Spacer(modifier = Modifier.weight(1f))
                                IconButton(onClick = { rightOutput = (rightOutput + 10.0).coerceAtMost(250.0) }) {
                                    Icon(Icons.Default.Add, contentDescription = null)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Total Expressed:", style = MaterialTheme.typography.bodyMedium)
                            Text("${total.roundToInt()} mL", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                        }
                    }
                }
            }
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Fridge", "Freezer", "Room Temp").forEach { loc ->
                    FilterChip(
                        selected = storage == loc,
                        onClick = { storage = loc },
                        label = { Text(loc) }
                    )
                }
            }
        }

        item {
            Button(
                onClick = {
                    viewModel.logPumping(leftOutput, rightOutput, durationMinutes, storage, notes)
                    savedMessage = true
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text("Save Pumping Session (${total.roundToInt()} mL)")
            }

            if (savedMessage) {
                Spacer(modifier = Modifier.height(8.dp))
                Text("✓ Pumping inventory updated.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
private fun MilkTypeChip(
    label: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
        ),
        modifier = modifier.height(44.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal),
                color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
