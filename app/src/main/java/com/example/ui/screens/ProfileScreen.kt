package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.model.BabyProfile
import com.example.data.model.FeedingMethod
import com.example.data.model.SexCategory
import com.example.ui.MainViewModel
import com.example.who.WhoGrowthCalculator
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ProfileScreen(
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit
) {
    val allBabies by viewModel.allBabies.collectAsState()
    val activeBaby by viewModel.activeBaby.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    val dateFormatter = remember { SimpleDateFormat("MMMM d, yyyy", Locale.getDefault()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Baby Profiles", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Add Baby Profile")
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
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            item {
                Text(
                    "Select Active Baby",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            items(allBabies) { baby ->
                val isSelected = activeBaby?.id == baby.id
                val now = System.currentTimeMillis()
                val ageDays = WhoGrowthCalculator.calculateAgeDays(baby.birthTimestamp, now)
                val months = ageDays / 30
                val weeks = (ageDays % 30) / 7
                val days = (ageDays % 30) % 7

                val (correctedDays, isPremature) = WhoGrowthCalculator.calculateCorrectedAgeDays(
                    baby.birthTimestamp,
                    now,
                    baby.gestationalAgeWeeks,
                    baby.gestationalAgeDays
                )

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        if (isSelected) 2.dp else 1.dp,
                        if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.selectBaby(baby.id) }
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Image(
                                    painter = painterResource(R.drawable.app_hero_avatar),
                                    contentDescription = baby.name,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                )
                                Column {
                                    Text(
                                        text = baby.name,
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Born ${dateFormatter.format(Date(baby.birthTimestamp))} • ${baby.sex.name} reference",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            if (isSelected) {
                                SuggestionChip(
                                    onClick = {},
                                    label = { Text("Active") },
                                    colors = SuggestionChipDefaults.suggestionChipColors(
                                        containerColor = MaterialTheme.colorScheme.primary,
                                        labelColor = MaterialTheme.colorScheme.onPrimary
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        Spacer(modifier = Modifier.height(10.dp))

                        // Age and measurements
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Chronological Age", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("${months}m ${weeks}w ${days}d ($ageDays days)", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                            }
                            if (isPremature) {
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("Corrected Age", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                                    Text("${correctedDays / 30}m ${(correctedDays % 30) / 7}w ($correctedDays days)", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold), color = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }

                        if (isPremature) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Prematurity note: Born at ${baby.gestationalAgeWeeks}w ${baby.gestationalAgeDays ?: 0}d. Pediatricians evaluate milestones and growth using corrected age.",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Routine: ${baby.primaryFeedingMethod.name.replace("_", " ")} • Reminder interval: ${baby.reminderIntervalMinutes / 60}h ${baby.reminderIntervalMinutes % 60}m",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            item {
                Button(
                    onClick = { showAddDialog = true },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Add Another Baby Profile")
                }
            }
        }
    }

    if (showAddDialog) {
        AddBabyDialog(
            onDismiss = { showAddDialog = false },
            onSave = { name, dob, sex, weight, length, head, gestW, gestD, method, interval ->
                viewModel.createBaby(name, dob, sex, weight, length, head, gestW, gestD, method, interval)
                showAddDialog = false
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddBabyDialog(
    onDismiss: () -> Unit,
    onSave: (
        name: String,
        birthTimestamp: Long,
        sex: SexCategory,
        birthWeightKg: Double?,
        birthLengthCm: Double?,
        birthHeadCm: Double?,
        gestWeeks: Int?,
        gestDays: Int?,
        method: FeedingMethod,
        reminderInterval: Int
    ) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var sex by remember { mutableStateOf(SexCategory.BOY) }
    var weightText by remember { mutableStateOf("3.4") }
    var lengthText by remember { mutableStateOf("50.0") }
    var headText by remember { mutableStateOf("34.5") }
    var gestWeeksText by remember { mutableStateOf("") }
    var feedingMethod by remember { mutableStateOf(FeedingMethod.MIXED) }
    var reminderIntervalMins by remember { mutableIntStateOf(180) }

    // Date of birth (default today or recent)
    var birthTimestamp by remember { mutableLongStateOf(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(45)) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Baby Profile") },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                item {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Baby's Name") },
                        placeholder = { Text("e.g. Leo") },
                        modifier = Modifier.fillMaxWidth().testTag("input_baby_name"),
                        shape = RoundedCornerShape(10.dp)
                    )
                }

                item {
                    Text("Sex Category (Required for WHO Growth Standards):", style = MaterialTheme.typography.labelSmall)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = sex == SexCategory.BOY,
                            onClick = { sex = SexCategory.BOY },
                            label = { Text("Boy (WHO Reference)") },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = sex == SexCategory.GIRL,
                            onClick = { sex = SexCategory.GIRL },
                            label = { Text("Girl (WHO Reference)") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = weightText,
                            onValueChange = { weightText = it },
                            label = { Text("Birth Wt (kg)") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        )
                        OutlinedTextField(
                            value = lengthText,
                            onValueChange = { lengthText = it },
                            label = { Text("Birth Lg (cm)") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                }

                item {
                    OutlinedTextField(
                        value = gestWeeksText,
                        onValueChange = { gestWeeksText = it },
                        label = { Text("Gestational Weeks at Birth (if premature)") },
                        placeholder = { Text("e.g. 34 for 34 weeks") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                }

                item {
                    Text("Primary Feeding Routine:", style = MaterialTheme.typography.labelSmall)
                    FlowRow(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf(
                            FeedingMethod.FORMULA to "Formula",
                            FeedingMethod.BREASTFEEDING to "Nursing",
                            FeedingMethod.MIXED to "Mixed"
                        ).forEach { (m, label) ->
                            FilterChip(
                                selected = feedingMethod == m,
                                onClick = { feedingMethod = m },
                                label = { Text(label, style = MaterialTheme.typography.labelSmall) }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onSave(
                            name,
                            birthTimestamp,
                            sex,
                            weightText.toDoubleOrNull(),
                            lengthText.toDoubleOrNull(),
                            headText.toDoubleOrNull(),
                            gestWeeksText.toIntOrNull(),
                            null,
                            feedingMethod,
                            reminderIntervalMins
                        )
                    }
                },
                enabled = name.isNotBlank(),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Create Profile")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
