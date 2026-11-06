package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.guidelines.FeedingGuidelineCalculator
import com.example.ui.MainViewModel
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InsightsScreen(
    viewModel: MainViewModel,
    onNavigateToEducation: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val summary by viewModel.dailySummary.collectAsState()
    val comparison by viewModel.guidelineComparison.collectAsState()
    val useAap by viewModel.useAap.collectAsState()
    val activeBaby by viewModel.activeBaby.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    // Per-feed Planning Calculator State
    var targetDailyMlText by remember { mutableStateOf("720") }
    var plannedFeedsCountText by remember { mutableStateOf("8") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Feeding Guidance & Insights", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            // 1. Feeding Guidance & Comparison Card (NHS / AAP)
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            "Pediatric Feeding Reference",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(
                                selected = !useAap,
                                onClick = { coroutineScope.launch { viewModel.preferences.setUseAap(false) } },
                                label = { Text("NHS (UK)") }
                            )
                            FilterChip(
                                selected = useAap,
                                onClick = { coroutineScope.launch { viewModel.preferences.setUseAap(true) } },
                                label = { Text("AAP (US)") }
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        if (comparison != null && comparison!!.weightKgUsed != null) {
                            val c = comparison!!
                            val lower = c.referenceRangeLowerMl.roundToInt()
                            val upper = c.referenceRangeUpperMl.roundToInt()
                            val actual = c.actualRecordedBottleMl.roundToInt()

                            Surface(
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(
                                        text = "${c.selectedGuidelineName} Reference for ${c.weightKgUsed} kg weight:",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "$lower – $upper mL / 24 hours",
                                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Actual recorded bottle intake: $actual mL in last 24h",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    if (c.intakePerKgMl != null && c.intakePerKgMl > 0) {
                                        Text(
                                            text = "Measured rate: ${c.intakePerKgMl.roundToInt()} mL/kg/day",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = c.statusSummary,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        } else {
                            Text(
                                text = "Please log baby's weight in the Growth tab to calculate personalized NHS/AAP weight-based reference ranges.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (useAap) FeedingGuidelineCalculator.AAP_SOURCE_INFO else FeedingGuidelineCalculator.NHS_SOURCE_INFO,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            // 2. Per-Feed Planning Arithmetic Calculator
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            "Per-Feed Planning Calculator",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            "Arithmetic division tool to plan bottle portions across daily feeds. Never force a bottle amount on an infant.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedTextField(
                                value = targetDailyMlText,
                                onValueChange = { targetDailyMlText = it },
                                label = { Text("Daily Target (mL)") },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = plannedFeedsCountText,
                                onValueChange = { plannedFeedsCountText = it },
                                label = { Text("Planned Feeds") },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            )
                        }

                        val daily = targetDailyMlText.toDoubleOrNull() ?: 0.0
                        val feeds = plannedFeedsCountText.toIntOrNull() ?: 1
                        val perFeed = FeedingGuidelineCalculator.calculatePerFeedAmount(daily, feeds)

                        Spacer(modifier = Modifier.height(12.dp))
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp).fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Planning Estimate per Bottle:", style = MaterialTheme.typography.bodyMedium)
                                Text("${perFeed.roundToInt()} mL / feed", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            }

            // 3. Cross-Feature Summary (Diapers & Sleep)
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            "24-Hour Diaper & Sleep Balance",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Wet Diapers", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("${summary.wetDiapersCount}", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                                Text("Adequate hydration: 6+ / day", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Column {
                                Text("Dirty Diapers", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("${summary.dirtyDiapersCount}", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                                Text("Stool consistency tracked", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Column {
                                Text("Total Sleep", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                val h = summary.sleepTotalMinutes / 60
                                val m = summary.sleepTotalMinutes % 60
                                Text("${h}h ${m}m", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                                Text("Naps & nighttime", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }

            // 4. Authoritative Information Center Link
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToEducation() }
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Icon(
                            Icons.Outlined.MenuBook,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Child Health & Feeding Guide",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                "WHO, AAP, NHS and CDC guidance on hunger cues, responsive feeding, and growth percentiles.",
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
