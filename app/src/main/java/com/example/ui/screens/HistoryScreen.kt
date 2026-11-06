package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.*
import com.example.ui.MainViewModel
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

sealed class TimelineItem(val timestamp: Long, val original: Any) {
    class Feed(val feed: FeedingEntry) : TimelineItem(feed.timestamp, feed)
    class Diaper(val diaper: DiaperEntry) : TimelineItem(diaper.timestamp, diaper)
    class Sleep(val sleep: SleepEntry) : TimelineItem(sleep.startTime, sleep)
    class Growth(val growth: GrowthEntry) : TimelineItem(growth.timestamp, growth)
    class Symptom(val symptom: SymptomEntry) : TimelineItem(symptom.timestamp, symptom)
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun HistoryScreen(
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit
) {
    val feedings by viewModel.feedings.collectAsState()
    val diapers by viewModel.diapers.collectAsState()
    val sleepList by viewModel.sleepList.collectAsState()
    val growthList by viewModel.growthList.collectAsState()
    val symptoms by viewModel.symptoms.collectAsState()

    var filterType by remember { mutableStateOf("ALL") }
    var searchQuery by remember { mutableStateOf("") }
    val timeFormatter = remember { SimpleDateFormat("h:mm a", Locale.getDefault()) }
    val dateFormatter = remember { SimpleDateFormat("EEEE, MMM d", Locale.getDefault()) }

    val combinedTimeline = remember(feedings, diapers, sleepList, growthList, symptoms, filterType, searchQuery) {
        val list = mutableListOf<TimelineItem>()
        if (filterType == "ALL" || filterType == "FEED") {
            list.addAll(feedings.map { TimelineItem.Feed(it) })
        }
        if (filterType == "ALL" || filterType == "DIAPER") {
            list.addAll(diapers.map { TimelineItem.Diaper(it) })
        }
        if (filterType == "ALL" || filterType == "SLEEP") {
            list.addAll(sleepList.map { TimelineItem.Sleep(it) })
        }
        if (filterType == "ALL" || filterType == "GROWTH") {
            list.addAll(growthList.map { TimelineItem.Growth(it) })
        }
        if (filterType == "ALL" || filterType == "SYMPTOM") {
            list.addAll(symptoms.map { TimelineItem.Symptom(it) })
        }

        val filtered = if (searchQuery.isBlank()) {
            list
        } else {
            list.filter { item ->
                when (item) {
                    is TimelineItem.Feed -> item.feed.notes.contains(searchQuery, ignoreCase = true) || item.feed.formulaBrand.contains(searchQuery, ignoreCase = true)
                    is TimelineItem.Diaper -> item.diaper.notes.contains(searchQuery, ignoreCase = true) || item.diaper.stoolColor.contains(searchQuery, ignoreCase = true)
                    is TimelineItem.Sleep -> item.sleep.notes.contains(searchQuery, ignoreCase = true)
                    is TimelineItem.Growth -> item.growth.notes.contains(searchQuery, ignoreCase = true)
                    is TimelineItem.Symptom -> item.symptom.symptomType.contains(searchQuery, ignoreCase = true) || item.symptom.notes.contains(searchQuery, ignoreCase = true)
                }
            }
        }
        filtered.sortedByDescending { it.timestamp }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Activity Timeline", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Search Bar & Filter chips
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search activity or notes...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    listOf(
                        "ALL" to "All",
                        "FEED" to "Feeding",
                        "DIAPER" to "Diapers",
                        "SLEEP" to "Sleep",
                        "GROWTH" to "Growth"
                    ).forEach { (key, label) ->
                        FilterChip(
                            selected = filterType == key,
                            onClick = { filterType = key },
                            label = { Text(label, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }
            }

            if (combinedTimeline.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Outlined.History,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            "No activities recorded yet",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            "Logged feeds, nursing sessions, diapers, and growth will appear in this timeline.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(vertical = 12.dp)
                ) {
                    items(combinedTimeline) { item ->
                        TimelineCard(
                            item = item,
                            timeFormatter = timeFormatter,
                            dateFormatter = dateFormatter,
                            onDelete = { viewModel.deleteItem(item.original) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TimelineCard(
    item: TimelineItem,
    timeFormatter: SimpleDateFormat,
    dateFormatter: SimpleDateFormat,
    onDelete: () -> Unit
) {
    val (icon, title, subtitle, badge) = when (item) {
        is TimelineItem.Feed -> {
            val f = item.feed
            when (f.type) {
                FeedingType.DIRECT_BREASTFEEDING -> {
                    val totalMins = f.nursingLeftMinutes + f.nursingRightMinutes
                    Quad(
                        Icons.Outlined.Timer,
                        "Breastfeeding (${totalMins}m)",
                        "Left: ${f.nursingLeftMinutes}m • Right: ${f.nursingRightMinutes}m",
                        f.lastBreastUsed?.name?.let { "Last: $it" } ?: "Nursing"
                    )
                }
                FeedingType.EXPRESSED_BREAST_MILK -> {
                    Quad(
                        Icons.Outlined.LocalDrink,
                        "Expressed Milk Bottle (${f.consumedMl.roundToInt()} mL)",
                        if (f.remainingMl > 0) "Prepared ${f.preparedMl.roundToInt()} mL, leftover ${f.remainingMl.roundToInt()} mL" else "Consumed ${f.consumedMl.roundToInt()} mL",
                        "EBM"
                    )
                }
                else -> {
                    Quad(
                        Icons.Outlined.LocalDrink,
                        "Formula Bottle (${f.consumedMl.roundToInt()} mL)",
                        if (f.formulaBrand.isNotBlank()) "Brand: ${f.formulaBrand}" else "Prepared ${f.preparedMl.roundToInt()} mL",
                        "Formula"
                    )
                }
            }
        }
        is TimelineItem.Diaper -> {
            val d = item.diaper
            Quad(
                Icons.Outlined.BabyChangingStation,
                "Diaper: ${d.type.name.lowercase().replaceFirstChar { it.uppercase() }}",
                listOfNotNull(d.stoolColor.takeIf { it.isNotBlank() }, d.stoolConsistency?.name?.lowercase(), if (d.hasMucusOrBlood) "Mucus/Blood noted" else null).joinToString(" • "),
                d.type.name
            )
        }
        is TimelineItem.Sleep -> {
            val s = item.sleep
            Quad(
                Icons.Outlined.Bedtime,
                "${if (s.isNap) "Nap" else "Night Sleep"} (${s.durationMinutes} mins)",
                "Quality: ${s.quality}",
                "${s.durationMinutes}m"
            )
        }
        is TimelineItem.Growth -> {
            val g = item.growth
            Quad(
                Icons.Outlined.Straighten,
                "Growth Measurement",
                listOfNotNull(g.weightKg?.let { "$it kg" }, g.lengthCm?.let { "$it cm" }, g.headCircumferenceCm?.let { "HC $it cm" }).joinToString(" • "),
                g.measurementSource
            )
        }
        is TimelineItem.Symptom -> {
            val sym = item.symptom
            Quad(
                Icons.Outlined.Sick,
                "Symptom: ${sym.symptomType}",
                "Severity: ${sym.severity} ${if (sym.formulaBrand.isNotBlank()) "• Formula: ${sym.formulaBrand}" else ""}",
                sym.severity
            )
        }
    }

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
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = timeFormatter.format(Date(item.timestamp)),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                if (subtitle.isNotBlank()) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = dateFormatter.format(Date(item.timestamp)),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }

            IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                Icon(
                    Icons.Outlined.Delete,
                    contentDescription = "Delete item",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

private data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
