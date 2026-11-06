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
import com.example.data.model.BottleSafetyEntry
import com.example.data.model.FormulaProduct
import com.example.guidelines.FeedingGuidelineCalculator
import com.example.ui.MainViewModel
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormulaScreen(
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit
) {
    val formulas by viewModel.formulas.collectAsState()
    val activeBottles by viewModel.activeBottles.collectAsState()
    val nowTicker by viewModel.currentTicker.collectAsState()

    var showAddFormulaDialog by remember { mutableStateOf(false) }
    var prepWaterMlText by remember { mutableStateOf("90") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Formula & Bottle Safety", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showAddFormulaDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Add Formula Product")
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
            // 1. Preparation Calculator
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Outlined.Calculate, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Text(
                                "Preparation Calculator",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            "Calculate exact level scoops based on manufacturer instructions (Standard: 1 level scoop per 30 mL water).",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedTextField(
                                value = prepWaterMlText,
                                onValueChange = { prepWaterMlText = it },
                                label = { Text("Water Volume (mL)") },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            )

                            val water = prepWaterMlText.toDoubleOrNull() ?: 0.0
                            val activeProduct = formulas.firstOrNull { it.isActive }
                            val waterRatio = activeProduct?.waterQuantityPerUnitMl ?: 30.0
                            val scoopsRatio = activeProduct?.scoopsPerUnit ?: 1.0
                            val calculatedScoops = (water / waterRatio) * scoopsRatio

                            Surface(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f).height(56.dp)
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalArrangement = Arrangement.Center,
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "${"%.1f".format(calculatedScoops)} Level Scoops",
                                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Note: Always add water first, then level scoops of powder without packing or extra dilution.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // 2. CDC Bottle Safety Timers
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(Icons.Outlined.Timer, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Text(
                                    "CDC Bottle Safety Timer",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            TextButton(onClick = { viewModel.logBottleSafety(isRefrigerated = false, "Room temp bottle") }) {
                                Text("+ Start Timer")
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "CDC Guidelines: Use within 2h unrefrigerated, 1h once feeding begins, or 24h refrigerated. Discard leftovers.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        if (activeBottles.isEmpty()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "No active bottles being tracked right now. Tap '+ Start Timer' when preparing a bottle.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            Spacer(modifier = Modifier.height(12.dp))
                            activeBottles.forEach { bottle ->
                                val status = FeedingGuidelineCalculator.calculateBottleSafety(
                                    preparedTimestamp = bottle.preparedTimestamp,
                                    feedingStartedTimestamp = bottle.feedingStartedTimestamp,
                                    isRefrigerated = bottle.isRefrigerated,
                                    nowTimestamp = nowTicker
                                )

                                Surface(
                                    color = if (status.isExpired) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surfaceVariant,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp).fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = if (status.isExpired) "EXPIRED – Discard immediately" else "Safe for: ${status.remainingMinutes} minutes",
                                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                                color = if (status.isExpired) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = status.ruleDescription,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = if (status.isExpired) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }

                                        Row {
                                            if (bottle.feedingStartedTimestamp == null) {
                                                IconButton(onClick = { viewModel.markBottleFeedingStarted(bottle) }) {
                                                    Icon(Icons.Default.PlayArrow, contentDescription = "Feeding Started")
                                                }
                                            }
                                            IconButton(onClick = { viewModel.discardBottle(bottle) }) {
                                                Icon(Icons.Default.Delete, contentDescription = "Discard bottle")
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 3. Formula Products & Powder Inventory
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Formula Inventory (${formulas.size})",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    TextButton(onClick = { showAddFormulaDialog = true }) {
                        Text("+ Add Tin")
                    }
                }
            }

            items(formulas) { product ->
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "${product.manufacturer} – ${product.productName}",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${product.stage} • ${product.tinSizeGrams.roundToInt()}g tin",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (product.isActive) {
                                SuggestionChip(
                                    onClick = {},
                                    label = { Text("Active") },
                                    colors = SuggestionChipDefaults.suggestionChipColors(
                                        containerColor = MaterialTheme.colorScheme.primaryContainer
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "Ratio: 1 scoop / ${product.waterQuantityPerUnitMl.roundToInt()} mL",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (product.price > 0) {
                                Text(
                                    "Price: ${product.currency}${product.price}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddFormulaDialog) {
        AddFormulaDialog(
            onDismiss = { showAddFormulaDialog = false },
            onSave = { mfg, name, stage, size, price, ratio, scoops, mass ->
                viewModel.addFormulaProduct(mfg, name, stage, size, price, ratio, scoops, mass)
                showAddFormulaDialog = false
            }
        )
    }
}

@Composable
private fun AddFormulaDialog(
    onDismiss: () -> Unit,
    onSave: (mfg: String, name: String, stage: String, size: Double, price: Double, ratio: Double, scoops: Double, mass: Double) -> Unit
) {
    var mfg by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var stage by remember { mutableStateOf("Stage 1 (0-6m)") }
    var sizeText by remember { mutableStateOf("800") }
    var priceText by remember { mutableStateOf("28.0") }
    var ratioText by remember { mutableStateOf("30") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Formula Product") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = mfg,
                    onValueChange = { mfg = it },
                    label = { Text("Manufacturer / Brand") },
                    placeholder = { Text("e.g. Kendamil, Similac") },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Product Name") },
                    placeholder = { Text("e.g. Organic First Infant Milk") },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = sizeText,
                    onValueChange = { sizeText = it },
                    label = { Text("Tin Size (grams)") },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = ratioText,
                    onValueChange = { ratioText = it },
                    label = { Text("Water per Scoop (mL)") },
                    placeholder = { Text("30") },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val s = sizeText.toDoubleOrNull() ?: 800.0
                    val p = priceText.toDoubleOrNull() ?: 0.0
                    val r = ratioText.toDoubleOrNull() ?: 30.0
                    onSave(mfg, name, stage, s, p, r, 1.0, 4.3)
                },
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Save Product")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
