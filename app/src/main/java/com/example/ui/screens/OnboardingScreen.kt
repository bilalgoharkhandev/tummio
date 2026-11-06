package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChildCare
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.model.FeedingMethod
import com.example.data.model.SexCategory
import com.example.ui.MainViewModel
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OnboardingScreen(
    viewModel: MainViewModel,
    onComplete: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var sex by remember { mutableStateOf(SexCategory.BOY) }
    var birthWeightText by remember { mutableStateOf("3.4") }
    var birthLengthText by remember { mutableStateOf("50.0") }
    var gestWeeksText by remember { mutableStateOf("") }
    var feedingMethod by remember { mutableStateOf(FeedingMethod.MIXED) }
    var reminderInterval by remember { mutableIntStateOf(180) }

    val birthTimestamp = remember { System.currentTimeMillis() - TimeUnit.DAYS.toMillis(30) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(top = 36.dp, bottom = 40.dp)
        ) {
            item {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .border(3.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(R.drawable.app_hero_avatar),
                        contentDescription = "Welcome Baby",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            item {
                Text(
                    text = "Welcome to LittleRoutine",
                    style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Baby feeding, nursing, diaper, and growth tracking powered by authentic World Health Organization (WHO) Child Growth Standards.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }

            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            "Baby Profile Details",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )

                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Baby's Name") },
                            placeholder = { Text("e.g. Leo") },
                            modifier = Modifier.fillMaxWidth().testTag("onboarding_input_name"),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Column {
                            Text(
                                "Sex Reference (for WHO 2006 Standards)",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                FilterChip(
                                    selected = sex == SexCategory.BOY,
                                    onClick = { sex = SexCategory.BOY },
                                    label = { Text("Boy") },
                                    modifier = Modifier.weight(1f)
                                )
                                FilterChip(
                                    selected = sex == SexCategory.GIRL,
                                    onClick = { sex = SexCategory.GIRL },
                                    label = { Text("Girl") },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedTextField(
                                value = birthWeightText,
                                onValueChange = { birthWeightText = it },
                                label = { Text("Birth Weight (kg)") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )
                            OutlinedTextField(
                                value = birthLengthText,
                                onValueChange = { birthLengthText = it },
                                label = { Text("Birth Length (cm)") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }

                        OutlinedTextField(
                            value = gestWeeksText,
                            onValueChange = { gestWeeksText = it },
                            label = { Text("Gestational Weeks at Birth (if premature)") },
                            placeholder = { Text("e.g. 35 (leave blank if full term)") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Column {
                            Text(
                                "Primary Feeding Routine",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(6.dp))
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

                        Column {
                            Text(
                                "Feeding Reminder Interval",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            FlowRow(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                listOf(150 to "2.5h", 180 to "3h", 210 to "3.5h", 240 to "4h").forEach { (interval, label) ->
                                    FilterChip(
                                        selected = reminderInterval == interval,
                                        onClick = { reminderInterval = interval },
                                        label = { Text(label, style = MaterialTheme.typography.labelSmall) }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                Button(
                    onClick = {
                        val babyName = if (name.isBlank()) "Leo" else name
                        viewModel.createBaby(
                            name = babyName,
                            birthTimestamp = birthTimestamp,
                            sex = sex,
                            birthWeightKg = birthWeightText.toDoubleOrNull() ?: 3.4,
                            birthLengthCm = birthLengthText.toDoubleOrNull() ?: 50.0,
                            birthHeadCircumferenceCm = 34.5,
                            gestationalWeeks = gestWeeksText.toIntOrNull(),
                            gestationalDays = null,
                            feedingMethod = feedingMethod,
                            reminderInterval = reminderInterval
                        )
                        // Seed an initial birth growth point
                        viewModel.logGrowth(
                            weightKg = birthWeightText.toDoubleOrNull() ?: 3.4,
                            lengthCm = birthLengthText.toDoubleOrNull() ?: 50.0,
                            headCircumferenceCm = 34.5,
                            source = "Clinic",
                            notes = "Birth measurements",
                            timestamp = birthTimestamp
                        )
                        onComplete()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("onboarding_start_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text("Begin LittleRoutine", style = MaterialTheme.typography.titleSmall)
                }
            }

            item {
                Text(
                    "All data is stored strictly locally on your device. No cloud account or internet connection required.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
