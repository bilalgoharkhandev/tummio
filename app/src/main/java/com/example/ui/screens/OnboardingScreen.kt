package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FeedingMethod
import com.example.data.model.SexCategory
import com.example.ui.MainViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private data class WelcomeFeature(
    val eyebrow: String,
    val title: String,
    val description: String,
    val icon: ImageVector,
    val accent: Color,
    val bullets: List<Pair<ImageVector, String>>
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun OnboardingScreen(viewModel: MainViewModel, onComplete: () -> Unit) {
    var step by remember { mutableIntStateOf(0) }
    var name by remember { mutableStateOf("") }
    var sex by remember { mutableStateOf<SexCategory?>(null) }
    var feedingMethod by remember { mutableStateOf(FeedingMethod.MIXED) }
    var birthWeightText by remember { mutableStateOf("") }
    var birthLengthText by remember { mutableStateOf("") }
    var selectedBirthDate by remember { mutableStateOf<Long?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }

    val features = remember {
        listOf(
            WelcomeFeature(
                "EVERYDAY CARE", "A little help with the routine",
                "Keep feeding, sleep and diaper notes together, so the small details are easier to remember.",
                Icons.Default.Favorite, Color(0xFF3867A6),
                listOf(Icons.Default.LocalDrink to "Feeds", Icons.Default.Bedtime to "Sleep", Icons.Default.ChildCare to "Diapers")
            ),
            WelcomeFeature(
                "GROWING TOGETHER", "See the story in every measurement",
                "Track weight, length and head circumference over time with WHO growth references.",
                Icons.Default.TrendingUp, Color(0xFF147D73),
                listOf(Icons.Default.MonitorWeight to "Weight", Icons.Default.Straighten to "Length", Icons.Default.ShowChart to "Growth")
            ),
            WelcomeFeature(
                "CALM, USEFUL CONTEXT", "Your notes, made easier to use",
                "Gentle reminders and simple summaries help you spot patterns and share useful notes with your care team.",
                Icons.Default.AutoGraph, Color(0xFF7352A5),
                listOf(Icons.Default.NotificationsActive to "Reminders", Icons.Default.Insights to "Patterns", Icons.Default.Share to "Reports")
            )
        )
    }

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { scaffoldPadding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(scaffoldPadding).padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("tummio", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold), color = MaterialTheme.colorScheme.primary)
                if (step < 3) TextButton(onClick = { step = 3 }) { Text("Skip intro") }
                else TextButton(onClick = { step = 2 }) { Text("Back") }
            }

            if (step < 3) {
                val feature = features[step]
                LazyColumn(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    contentPadding = PaddingValues(top = 22.dp, bottom = 12.dp)
                ) {
                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(7.dp), verticalAlignment = Alignment.CenterVertically) {
                            repeat(3) { index ->
                                Box(Modifier.width(if (step == index) 28.dp else 8.dp).height(8.dp).clip(CircleShape).background(if (step == index) feature.accent else MaterialTheme.colorScheme.outlineVariant))
                            }
                            Spacer(Modifier.width(6.dp))
                            Text("${step + 1} OF 3", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Spacer(Modifier.height(24.dp))
                        FeatureIllustration(feature = feature, page = step)
                        Spacer(Modifier.height(28.dp))
                        Text(feature.eyebrow, style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 1.2.sp), color = feature.accent)
                        Spacer(Modifier.height(10.dp))
                        Text(feature.title, style = MaterialTheme.typography.displayMedium, color = MaterialTheme.colorScheme.onBackground, textAlign = TextAlign.Center)
                        Spacer(Modifier.height(12.dp))
                        Text(feature.description, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
                        Spacer(Modifier.height(20.dp))
                        feature.bullets.forEachIndexed { index, (icon, label) ->
                            Row(
                                Modifier.fillMaxWidth().padding(vertical = 5.dp).clip(RoundedCornerShape(14.dp)).background(MaterialTheme.colorScheme.surface).padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(icon, contentDescription = null, tint = feature.accent, modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(12.dp))
                                Text(label, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold), color = MaterialTheme.colorScheme.onSurface)
                                Spacer(Modifier.weight(1f))
                                Text("0${index + 1}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
                Button(
                    onClick = { if (step < 2) step++ else step = 3 },
                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 24.dp).heightIn(min = 56.dp).testTag("onboarding_continue"),
                    shape = RoundedCornerShape(18.dp)
                ) { Text(if (step == 2) "Create your baby’s profile" else "Continue", style = MaterialTheme.typography.titleSmall) }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    contentPadding = PaddingValues(top = 16.dp, bottom = 12.dp)
                ) {
                    item {
                        Text("Let’s meet your little one", style = MaterialTheme.typography.displayMedium, color = MaterialTheme.colorScheme.onBackground)
                        Spacer(Modifier.height(8.dp))
                        Text("A few details personalize your growth charts and daily summaries.", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    item {
                        FormSection(number = "01", title = "Baby’s name", subtitle = "What would you like to call them?") {
                            OutlinedTextField(
                                value = name, onValueChange = { name = it }, label = { Text("Name") }, placeholder = { Text("e.g. Leo") },
                                singleLine = true, modifier = Modifier.fillMaxWidth().testTag("onboarding_input_name"), shape = RoundedCornerShape(16.dp)
                            )
                        }
                    }
                    item {
                        FormSection(number = "02", title = "Date of birth", subtitle = "Used to show age-appropriate growth context.") {
                            OutlinedButton(onClick = { showDatePicker = true }, modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp), shape = RoundedCornerShape(16.dp)) {
                                Icon(Icons.Default.CalendarMonth, null)
                                Spacer(Modifier.width(10.dp))
                                Text(selectedBirthDate?.let { SimpleDateFormat("MMMM d, yyyy", Locale.getDefault()).format(Date(it)) } ?: "Choose date of birth")
                            }
                        }
                    }
                    item {
                        FormSection(number = "03", title = "Growth reference", subtitle = "Choose the WHO reference used for the chart.") {
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                                SexChoiceCard("Girl", Icons.Default.Face3, sex == SexCategory.GIRL, Modifier.weight(1f)) { sex = SexCategory.GIRL }
                                SexChoiceCard("Boy", Icons.Default.Face, sex == SexCategory.BOY, Modifier.weight(1f)) { sex = SexCategory.BOY }
                            }
                        }
                    }
                    item {
                        FormSection(number = "04", title = "Starting details", subtitle = "Optional. You can add or update these anytime.") {
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                                OutlinedTextField(value = birthWeightText, onValueChange = { birthWeightText = it }, label = { Text("Weight (kg)") }, placeholder = { Text("Optional") }, singleLine = true, modifier = Modifier.weight(1f), shape = RoundedCornerShape(16.dp))
                                OutlinedTextField(value = birthLengthText, onValueChange = { birthLengthText = it }, label = { Text("Length (cm)") }, placeholder = { Text("Optional") }, singleLine = true, modifier = Modifier.weight(1f), shape = RoundedCornerShape(16.dp))
                            }
                        }
                    }
                    item {
                        FormSection(number = "05", title = "Feeding routine", subtitle = "Choose the closest fit for now.") {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                listOf(FeedingMethod.BREASTFEEDING to "Breastfeeding", FeedingMethod.FORMULA to "Formula", FeedingMethod.MIXED to "Combination feeding").forEach { (method, label) ->
                                    SelectableRow(label = label, selected = feedingMethod == method, onClick = { feedingMethod = method })
                                }
                            }
                        }
                    }
                    item {
                        Button(
                            onClick = {
                                val weight = birthWeightText.toDoubleOrNull()
                                val length = birthLengthText.toDoubleOrNull()
                                val date = selectedBirthDate
                                val referenceSex = sex
                                if (name.isNotBlank() && date != null && referenceSex != null) {
                                    viewModel.createBaby(name.trim(), date, referenceSex, weight, length, null, null, null, feedingMethod, 180)
                                    onComplete()
                                }
                            },
                            enabled = name.isNotBlank() && selectedBirthDate != null && sex != null,
                            modifier = Modifier.fillMaxWidth().heightIn(min = 58.dp).testTag("onboarding_start_button"),
                            shape = RoundedCornerShape(18.dp)
                        ) { Text("Create profile", style = MaterialTheme.typography.titleSmall) }
                        Text("Your profile and notes stay on this device.", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(top = 6.dp, bottom = 20.dp))
                    }
                }
            }
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = selectedBirthDate)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = { selectedBirthDate = datePickerState.selectedDateMillis; showDatePicker = false }, enabled = datePickerState.selectedDateMillis != null) { Text("Choose date") }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Cancel") } }
        ) { DatePicker(state = datePickerState, showModeToggle = false) }
    }
}

@Composable
private fun FeatureIllustration(feature: WelcomeFeature, page: Int) {
    Box(
        modifier = Modifier.fillMaxWidth().height(250.dp).clip(RoundedCornerShape(32.dp)).background(feature.accent.copy(alpha = 0.09f)),
        contentAlignment = Alignment.Center
    ) {
        Box(Modifier.size(148.dp).clip(CircleShape).background(feature.accent.copy(alpha = 0.13f)), contentAlignment = Alignment.Center) {
            Box(Modifier.size(104.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surface).border(1.dp, feature.accent.copy(alpha = 0.15f), CircleShape), contentAlignment = Alignment.Center) {
                Icon(feature.icon, contentDescription = null, tint = feature.accent, modifier = Modifier.size(50.dp))
            }
        }
        val placements = when (page) {
            0 -> listOf(Alignment.TopStart, Alignment.TopEnd, Alignment.BottomCenter)
            1 -> listOf(Alignment.CenterStart, Alignment.CenterEnd, Alignment.BottomCenter)
            else -> listOf(Alignment.TopStart, Alignment.CenterEnd, Alignment.BottomStart)
        }
        feature.bullets.forEachIndexed { index, (icon, label) ->
            Row(
                Modifier.align(placements[index]).padding(18.dp).clip(RoundedCornerShape(15.dp)).background(MaterialTheme.colorScheme.surface).border(1.dp, feature.accent.copy(alpha = 0.15f), RoundedCornerShape(15.dp)).padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(icon, null, tint = feature.accent, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(7.dp))
                Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface)
            }
        }
    }
}

@Composable
private fun FormSection(number: String, title: String, subtitle: String, content: @Composable ColumnScope.() -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), shape = RoundedCornerShape(22.dp), border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
        Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(number, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.primary, modifier = Modifier.clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer).padding(horizontal = 10.dp, vertical = 6.dp))
                Column {
                    Text(title, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
                    Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            content()
        }
    }
}

@Composable
private fun SexChoiceCard(label: String, icon: ImageVector, selected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val stroke = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
    Surface(onClick = onClick, modifier = modifier.heightIn(min = 96.dp), shape = RoundedCornerShape(18.dp), color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface, border = androidx.compose.foundation.BorderStroke(if (selected) 2.dp else 1.dp, stroke)) {
        Column(Modifier.fillMaxWidth().padding(vertical = 14.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Icon(icon, contentDescription = null, tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(26.dp))
            Text(label, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold), color = MaterialTheme.colorScheme.onSurface)
            Text("WHO reference", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun SelectableRow(label: String, selected: Boolean, onClick: () -> Unit) {
    Surface(onClick = onClick, shape = RoundedCornerShape(14.dp), color = if (selected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f) else MaterialTheme.colorScheme.surface, border = androidx.compose.foundation.BorderStroke(1.dp, if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant)) {
        Row(Modifier.fillMaxWidth().heightIn(min = 52.dp).padding(horizontal = 14.dp), verticalAlignment = Alignment.CenterVertically) {
            RadioButton(selected = selected, onClick = onClick)
            Spacer(Modifier.width(8.dp))
            Text(label, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium), color = MaterialTheme.colorScheme.onSurface)
        }
    }
}
