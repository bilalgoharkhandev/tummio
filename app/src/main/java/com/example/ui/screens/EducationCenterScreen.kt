package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EducationCenterScreen(
    onNavigateBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Child Health & Standards Center", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)) },
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
            // 1. Responsive Feeding
            item {
                EducationCard(
                    title = "Responsive Feeding & Hunger Cues",
                    source = "World Health Organization (WHO) & UNICEF",
                    content = """
                    • Responsive feeding means recognizing and promptly responding to baby's hunger and satiety cues, rather than forcing a strict clock or fixed bottle volume.
                    • Early Hunger Cues: Licking lips, opening mouth, turning head looking for breast or bottle (rooting).
                    • Active Hunger Cues: Sucking on hands, squirming, stretching, breathing faster.
                    • Late Cues: Crying, agitation, turning red. Calm baby first before feeding.
                    • Satiety (Fullness) Cues: Releasing nipple or bottle, turning head away, relaxing hands and unclenching fists, falling asleep calmly.
                    """.trimIndent()
                )
            }

            // 2. Growth Percentiles & Z-scores
            item {
                EducationCard(
                    title = "Understanding WHO Growth Standards & Z-Scores",
                    source = "World Health Organization Multicentre Growth Reference Study (MGRS)",
                    content = """
                    • Percentiles show how a child compares to healthy children worldwide raised in optimal socioeconomic and health conditions.
                    • 50th percentile represents the median: half of healthy children weigh more, half weigh less.
                    • Tracking along a consistent curve matters far more than being at the 50th percentile. A baby tracking on the 15th percentile is typically just as healthy as one on the 85th percentile.
                    • Z-scores measure standard deviations (SD) from the median. A Z-score of 0 is the 50th percentile; -1.88 SD is the 3rd percentile; +1.88 SD is the 97th percentile.
                    • A sudden, sustained drop or rise across multiple percentile curves should be discussed with your pediatrician.
                    """.trimIndent()
                )
            }

            // 3. Formula Preparation & CDC Safety
            item {
                EducationCard(
                    title = "Safe Formula Preparation & Storage",
                    source = "US Centers for Disease Control and Prevention (CDC)",
                    content = """
                    • Always follow exact manufacturer instructions for water-to-powder ratio (usually 1 level scoop per 30 mL water). Never dilute with extra water or add extra powder, which can cause electrolyte imbalance or kidney strain.
                    • Level each scoop with a clean, flat knife; do not pack the powder down tightly.
                    • 2-Hour Room Temp Rule: Use prepared formula within 2 hours of preparation if kept at room temperature.
                    • 1-Hour Feeding Rule: Once baby starts drinking from a bottle, use within 1 hour or discard, as oral bacteria can rapidly multiply in leftover milk.
                    • 24-Hour Refrigeration Rule: Unused freshly prepared bottles can be refrigerated immediately (below 4°C / 40°F) for up to 24 hours.
                    """.trimIndent()
                )
            }

            // 4. Complementary Feeding
            item {
                EducationCard(
                    title = "Complementary Feeding (Around 6 Months)",
                    source = "American Academy of Pediatrics (AAP) & UK NHS",
                    content = """
                    • Around 6 months, infants are developmentally ready for solid foods alongside breast milk or formula.
                    • Signs of readiness: Sitting up with support, good head and neck control, opening mouth when food is offered, bringing objects to mouth.
                    • Breast milk or infant formula remains the primary source of nutrition throughout the first year.
                    """.trimIndent()
                )
            }
        }
    }
}

@Composable
private fun EducationCard(title: String, source: String, content: String) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                title,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                "Source: $source",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                content,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 20.sp
            )
        }
    }
}
