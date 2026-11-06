package com.example.ui.screens

import android.content.Context
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.export.DataExportManager
import com.example.ui.MainViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val isMetric by viewModel.isMetric.collectAsState()
    val useAap by viewModel.useAap.collectAsState()
    val activeBaby by viewModel.activeBaby.collectAsState()
    val feedings by viewModel.feedings.collectAsState()
    val growthList by viewModel.growthList.collectAsState()
    val diapers by viewModel.diapers.collectAsState()
    val symptoms by viewModel.symptoms.collectAsState()

    var showReportDialog by remember { mutableStateOf(false) }
    var reportText by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings & Exports", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)) },
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
            // Preferences & Units
            item {
                Text(
                    "Preferences",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Measurement Units", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                                Text(if (isMetric) "Metric (mL, kg, cm)" else "US Customary (fl oz, lb, in)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Switch(
                                checked = isMetric,
                                onCheckedChange = { coroutineScope.launch { viewModel.preferences.setMetric(it) } }
                            )
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Guideline Calculation Standard", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                                Text(if (useAap) "AAP US (2.5 oz/lb/day)" else "NHS UK (150–200 mL/kg/day)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            FilterChip(
                                selected = useAap,
                                onClick = { coroutineScope.launch { viewModel.preferences.setUseAap(!useAap) } },
                                label = { Text(if (useAap) "AAP" else "NHS") }
                            )
                        }
                    }
                }
            }

            // Export & Sharing
            item {
                Text(
                    "Clinical Reports & Exports",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        // Pediatrician Report
                        Button(
                            onClick = {
                                if (activeBaby != null) {
                                    val r = DataExportManager.generatePediatricianReport(
                                        baby = activeBaby!!,
                                        feedings = feedings,
                                        growths = growthList,
                                        diapers = diapers,
                                        symptoms = symptoms
                                    )
                                    reportText = r
                                    showReportDialog = true
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(48.dp).testTag("btn_pediatrician_report"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        ) {
                            Icon(Icons.Outlined.Assignment, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Generate Pediatrician Report")
                        }

                        // CSV Feeding Export
                        OutlinedButton(
                            onClick = {
                                val csv = DataExportManager.generateFeedingCsv(feedings)
                                DataExportManager.shareText(
                                    context = context,
                                    text = csv,
                                    title = "LittleRoutine_Feeding_Export.csv"
                                )
                            },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Outlined.FileDownload, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Export Feeding History (CSV)")
                        }

                        // CSV Growth Export
                        OutlinedButton(
                            onClick = {
                                if (activeBaby != null) {
                                    val csv = DataExportManager.generateGrowthCsv(growthList, activeBaby!!)
                                    DataExportManager.shareText(
                                        context = context,
                                        text = csv,
                                        title = "LittleRoutine_Growth_Export.csv"
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Outlined.Timeline, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Export Growth History (CSV)")
                        }
                    }
                }
            }

            // References & Privacy
            item {
                Text(
                    "Authoritative Standards & Privacy",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("• WHO Child Growth Standards (2006 MGRS): Published LMS Box-Cox reference datasets for boys and girls from birth to 5 years.", style = MaterialTheme.typography.labelSmall)
                        Text("• UK NHS Infant Nutrition Guidance: ~150–200 mL formula per kg/day reference.", style = MaterialTheme.typography.labelSmall)
                        Text("• AAP Pediatric Nutrition Guidelines: ~2.5 fl oz/lb/day formula reference.", style = MaterialTheme.typography.labelSmall)
                        Text("• US CDC Infant Food Safety: 2h room temp, 1h feeding, 24h refrigerated bottle safety guidelines.", style = MaterialTheme.typography.labelSmall)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("100% Offline-First: All health logs, growth data, and reminders remain private on your device.", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold), color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }

    if (showReportDialog) {
        AlertDialog(
            onDismissRequest = { showReportDialog = false },
            title = { Text("Pediatrician Clinical Report") },
            text = {
                LazyColumn(modifier = Modifier.fillMaxWidth().height(320.dp)) {
                    item {
                        Text(
                            text = reportText,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        DataExportManager.shareText(
                            context = context,
                            text = reportText,
                            title = "LittleRoutine_Pediatrician_Report"
                        )
                        showReportDialog = false
                    }
                ) {
                    Icon(Icons.Default.Share, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Share / Print Report")
                }
            },
            dismissButton = {
                TextButton(onClick = { showReportDialog = false }) {
                    Text("Close")
                }
            }
        )
    }
}
