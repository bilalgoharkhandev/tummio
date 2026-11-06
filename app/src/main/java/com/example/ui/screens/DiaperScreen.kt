package com.example.ui.screens

import androidx.compose.foundation.background
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
import com.example.data.model.DiaperType
import com.example.data.model.StoolConsistency
import com.example.ui.MainViewModel
import com.example.ui.theme.DiaperPeach
import com.example.ui.theme.DiaperPeachContainer
import com.example.ui.theme.OnDiaperPeachContainer

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun DiaperScreen(
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit
) {
    var diaperType by remember { mutableStateOf(DiaperType.WET) }
    var stoolColor by remember { mutableStateOf("Mustard Yellow") }
    var consistency by remember { mutableStateOf<StoolConsistency?>(StoolConsistency.SOFT) }
    var amount by remember { mutableStateOf("Normal") }
    var hasMucusOrBlood by remember { mutableStateOf(false) }
    var notes by remember { mutableStateOf("") }
    var savedMessage by remember { mutableStateOf(false) }

    val summary by viewModel.dailySummary.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Diaper & Stool Tracker", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
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
            // 24h Summary Card with Peach Container
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = DiaperPeachContainer.copy(alpha = 0.65f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(18.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Past 24h Wet", style = MaterialTheme.typography.labelSmall, color = OnDiaperPeachContainer)
                            Text("${summary.wetDiapersCount}", style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold), color = DiaperPeach)
                            Text("Target: 6+ / day", style = MaterialTheme.typography.labelSmall, color = OnDiaperPeachContainer.copy(alpha = 0.8f))
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Past 24h Dirty", style = MaterialTheme.typography.labelSmall, color = OnDiaperPeachContainer)
                            Text("${summary.dirtyDiapersCount}", style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold), color = DiaperPeach)
                            Text("Variable by feeding", style = MaterialTheme.typography.labelSmall, color = OnDiaperPeachContainer.copy(alpha = 0.8f))
                        }
                    }
                }
            }

            // Diaper Type Selector
            item {
                Text(
                    "Diaper Type",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        DiaperType.WET to "Wet",
                        DiaperType.DIRTY to "Dirty",
                        DiaperType.BOTH to "Both",
                        DiaperType.DRY to "Dry"
                    ).forEach { (type, label) ->
                        FilterChip(
                            selected = diaperType == type,
                            onClick = { diaperType = type },
                            label = { Text(label) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = DiaperPeach,
                                selectedLabelColor = androidx.compose.ui.graphics.Color.White
                            )
                        )
                    }
                }
            }

            // Stool Details (if Dirty or Both)
            if (diaperType == DiaperType.DIRTY || diaperType == DiaperType.BOTH) {
                item {
                    Text(
                        "Stool Consistency",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    FlowRow(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf(
                            StoolConsistency.SOFT to "Soft",
                            StoolConsistency.LOOSE to "Loose",
                            StoolConsistency.FORMED to "Formed",
                            StoolConsistency.HARD to "Hard"
                        ).forEach { (c, label) ->
                            FilterChip(
                                selected = consistency == c,
                                onClick = { consistency = c },
                                label = { Text(label, style = MaterialTheme.typography.labelSmall) },
                                shape = RoundedCornerShape(10.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = DiaperPeach,
                                    selectedLabelColor = androidx.compose.ui.graphics.Color.White
                                )
                            )
                        }
                    }
                }

                item {
                    Text(
                        "Stool Color",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("Mustard Yellow", "Greenish", "Brown", "Tan").forEach { color ->
                            FilterChip(
                                selected = stoolColor == color,
                                onClick = { stoolColor = color },
                                label = { Text(color, style = MaterialTheme.typography.labelSmall) },
                                shape = RoundedCornerShape(10.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = DiaperPeach,
                                    selectedLabelColor = androidx.compose.ui.graphics.Color.White
                                )
                            )
                        }
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Mucus or Blood Observation", style = MaterialTheme.typography.bodyMedium)
                        Switch(
                            checked = hasMucusOrBlood,
                            onCheckedChange = { hasMucusOrBlood = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = androidx.compose.ui.graphics.Color.White,
                                checkedTrackColor = DiaperPeach
                            )
                        )
                    }
                }
            }

            // Notes
            item {
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Observations & Notes (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                )
            }

            // Save Button
            item {
                Button(
                    onClick = {
                        viewModel.logDiaper(
                            type = diaperType,
                            stoolColor = if (diaperType != DiaperType.WET) stoolColor else "",
                            consistency = if (diaperType != DiaperType.WET) consistency else null,
                            amount = amount,
                            hasMucusOrBlood = hasMucusOrBlood,
                            notes = notes
                        )
                        savedMessage = true
                        notes = ""
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp).testTag("save_diaper_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DiaperPeach,
                        contentColor = androidx.compose.ui.graphics.Color.White
                    )
                ) {
                    Text("Save Diaper Entry", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                }

                if (savedMessage) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("✓ Diaper entry logged to history.", style = MaterialTheme.typography.bodySmall, color = DiaperPeach)
                }
            }
        }
    }
}
