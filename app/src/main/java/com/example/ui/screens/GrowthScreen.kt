package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.example.data.model.GrowthEntry
import com.example.ui.MainViewModel
import com.example.ui.components.ChartType
import com.example.ui.components.RechartsGrowthChartCard
import com.example.ui.components.WhoGrowthChart
import com.example.who.WhoGrowthCalculator
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GrowthScreen(
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit
) {
    val activeBaby by viewModel.activeBaby.collectAsState()
    val growthList by viewModel.growthList.collectAsState()
    var selectedChart by remember { mutableStateOf(ChartType.WEIGHT_FOR_AGE) }
    var showAddDialog by remember { mutableStateOf(false) }
    val dateFormatter = remember { SimpleDateFormat("MMM d, yyyy", Locale.getDefault()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Growth & WHO Standards",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddDialog = true },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Log Measurement") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("fab_log_growth")
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            // WHO Growth Standards Recharts & Native Chart Visualization
            item {
                if (activeBaby != null) {
                    RechartsGrowthChartCard(
                        baby = activeBaby!!,
                        measurements = growthList
                    )
                }
            }

            // Growth Velocity & Trend Card (if 2 or more measurements)
            if (growthList.size >= 2 && activeBaby != null) {
                item {
                    val prev = growthList[growthList.size - 2]
                    val current = growthList.last()
                    val velocity = WhoGrowthCalculator.calculateVelocity(
                        prevTimestamp = prev.timestamp,
                        prevWeightKg = prev.weightKg,
                        prevLengthCm = prev.lengthCm,
                        prevHeadCm = prev.headCircumferenceCm,
                        currentTimestamp = current.timestamp,
                        currentWeightKg = current.weightKg,
                        currentLengthCm = current.lengthCm,
                        currentHeadCm = current.headCircumferenceCm
                    )

                    if (velocity != null) {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    "Growth Velocity (Last ${velocity.daysBetween} Days)",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text("Weight Gain", style = MaterialTheme.typography.labelSmall)
                                        Text(
                                            "${if (velocity.weightGainGrams >= 0) "+" else ""}${"%.0f".format(velocity.weightGainGrams)} g",
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                        )
                                    }
                                    Column {
                                        Text("Daily Average", style = MaterialTheme.typography.labelSmall)
                                        Text(
                                            "${"%.1f".format(velocity.gramsPerDay)} g/day",
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                        )
                                    }
                                    Column {
                                        Text("Weekly Average", style = MaterialTheme.typography.labelSmall)
                                        Text(
                                            "${"%.0f".format(velocity.gramsPerWeek)} g/week",
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Measurement History Header
            item {
                Text(
                    "Measurement History (${growthList.size})",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Measurement Items
            items(growthList.reversed()) { growth ->
                val ageMonths = activeBaby?.let { WhoGrowthCalculator.calculateAgeMonths(it.birthTimestamp, growth.timestamp) } ?: 0.0
                val assessment = if (activeBaby != null && growth.weightKg != null) {
                    WhoGrowthCalculator.assessWeightForAge(growth.weightKg, ageMonths, activeBaby!!.sex)
                } else null

                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .padding(14.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = dateFormatter.format(Date(growth.timestamp)),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = listOfNotNull(
                                    growth.weightKg?.let { "$it kg" },
                                    growth.lengthCm?.let { "$it cm" },
                                    growth.headCircumferenceCm?.let { "HC: $it cm" }
                                ).joinToString(" • "),
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (growth.notes.isNotBlank()) {
                                Text(
                                    text = growth.notes,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        if (assessment != null) {
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "~${assessment.percentile.roundToInt()}th %ile",
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "Z: ${"%.2f".format(assessment.zScore)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        IconButton(onClick = { viewModel.deleteItem(growth) }) {
                            Icon(Icons.Outlined.Delete, contentDescription = "Delete measurement", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddGrowthDialog(
            onDismiss = { showAddDialog = false },
            onSave = { w, l, hc, src, n ->
                viewModel.logGrowth(w, l, hc, src, n)
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun AddGrowthDialog(
    onDismiss: () -> Unit,
    onSave: (weight: Double?, length: Double?, head: Double?, source: String, notes: String) -> Unit
) {
    var weightText by remember { mutableStateOf("") }
    var lengthText by remember { mutableStateOf("") }
    var headText by remember { mutableStateOf("") }
    var source by remember { mutableStateOf("Home") }
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Log Growth Measurement") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = weightText,
                    onValueChange = { weightText = it },
                    label = { Text("Weight (kg)") },
                    placeholder = { Text("e.g. 5.4") },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().testTag("input_weight")
                )
                OutlinedTextField(
                    value = lengthText,
                    onValueChange = { lengthText = it },
                    label = { Text("Length / Height (cm)") },
                    placeholder = { Text("e.g. 58.5") },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().testTag("input_length")
                )
                OutlinedTextField(
                    value = headText,
                    onValueChange = { headText = it },
                    label = { Text("Head Circumference (cm)") },
                    placeholder = { Text("e.g. 39.2") },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().testTag("input_head")
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Home", "Clinic", "Pediatrician").forEach { s ->
                        FilterChip(
                            selected = source == s,
                            onClick = { source = s },
                            label = { Text(s) }
                        )
                    }
                }
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes") },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val w = weightText.toDoubleOrNull()
                    val l = lengthText.toDoubleOrNull()
                    val hc = headText.toDoubleOrNull()
                    onSave(w, l, hc, source, notes)
                },
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
