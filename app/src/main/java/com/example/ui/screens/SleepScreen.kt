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
import com.example.ui.MainViewModel
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SleepScreen(
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit
) {
    var isSleeping by remember { mutableStateOf(false) }
    var sleepStartTimestamp by remember { mutableLongStateOf(0L) }
    val nowTicker by viewModel.currentTicker.collectAsState()
    val summary by viewModel.dailySummary.collectAsState()

    var isNap by remember { mutableStateOf(true) }
    var quality by remember { mutableStateOf("Restful") }
    var manualDurationMinsText by remember { mutableStateOf("45") }
    var notes by remember { mutableStateOf("") }
    var savedMessage by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sleep Tracker", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)) },
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
            // 24h Sleep Duration Card
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Past 24-Hour Sleep Total", style = MaterialTheme.typography.labelSmall)
                        Spacer(modifier = Modifier.height(4.dp))
                        val h = summary.sleepTotalMinutes / 60
                        val m = summary.sleepTotalMinutes % 60
                        Text(
                            "${h}h ${m}m recorded",
                            style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            "Newborns typically sleep 14–17 hours in 24 hours in short intervals.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Live Sleep Stopwatch
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            if (isSleeping) "Baby is currently sleeping..." else "Live Sleep Timer",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        val elapsedMillis = if (isSleeping) nowTicker - sleepStartTimestamp else 0L
                        val elapsedH = TimeUnit.MILLISECONDS.toHours(elapsedMillis)
                        val elapsedM = TimeUnit.MILLISECONDS.toMinutes(elapsedMillis) % 60
                        val elapsedS = TimeUnit.MILLISECONDS.toSeconds(elapsedMillis) % 60

                        Text(
                            text = String.format("%02d:%02d:%02d", elapsedH, elapsedM, elapsedS),
                            style = MaterialTheme.typography.displayLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        if (!isSleeping) {
                            Button(
                                onClick = {
                                    isSleeping = true
                                    sleepStartTimestamp = System.currentTimeMillis()
                                },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth().height(48.dp)
                            ) {
                                Icon(Icons.Default.Bedtime, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Start Sleep Session")
                            }
                        } else {
                            Button(
                                onClick = {
                                    val end = System.currentTimeMillis()
                                    viewModel.logSleep(sleepStartTimestamp, end, isNap, quality, notes)
                                    isSleeping = false
                                    savedMessage = true
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth().height(48.dp)
                            ) {
                                Icon(Icons.Default.WbSunny, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Baby Woke Up (Finish & Save)")
                            }
                        }
                    }
                }
            }

            // Manual Sleep Entry
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            "Manual Sleep Entry",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
                        )

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(
                                selected = isNap,
                                onClick = { isNap = true },
                                label = { Text("Day Nap") },
                                modifier = Modifier.weight(1f)
                            )
                            FilterChip(
                                selected = !isNap,
                                onClick = { isNap = false },
                                label = { Text("Night Sleep") },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        OutlinedTextField(
                            value = manualDurationMinsText,
                            onValueChange = { manualDurationMinsText = it },
                            label = { Text("Duration (minutes)") },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf("Restful", "Fussy", "Interrupted").forEach { q ->
                                FilterChip(
                                    selected = quality == q,
                                    onClick = { quality = q },
                                    label = { Text(q, style = MaterialTheme.typography.labelSmall) }
                                )
                            }
                        }

                        Button(
                            onClick = {
                                val mins = manualDurationMinsText.toLongOrNull() ?: 45L
                                val end = System.currentTimeMillis()
                                val start = end - TimeUnit.MINUTES.toMillis(mins)
                                viewModel.logSleep(start, end, isNap, quality, notes)
                                savedMessage = true
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().height(48.dp)
                        ) {
                            Text("Save Sleep Record")
                        }
                    }
                }
            }

            if (savedMessage) {
                item {
                    Text("✓ Sleep session saved to timeline.", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}
