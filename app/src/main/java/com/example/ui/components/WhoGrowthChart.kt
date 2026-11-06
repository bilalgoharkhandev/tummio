package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BabyProfile
import com.example.data.model.GrowthEntry
import com.example.data.model.SexCategory
import com.example.who.WhoGrowthCalculator
import com.example.who.WhoLmsData
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

enum class ChartType {
    WEIGHT_FOR_AGE,
    LENGTH_FOR_AGE,
    HEAD_CIRCUMFERENCE_FOR_AGE
}

@Composable
fun WhoGrowthChart(
    chartType: ChartType,
    baby: BabyProfile,
    measurements: List<GrowthEntry>,
    modifier: Modifier = Modifier
) {
    var selectedIndex by remember { mutableStateOf<Int?>(null) }
    val textMeasurer = rememberTextMeasurer()
    val dateFormatter = remember { SimpleDateFormat("MMM d, yyyy", Locale.getDefault()) }

    val lmsTable = remember(chartType, baby.sex) {
        when (chartType) {
            ChartType.WEIGHT_FOR_AGE -> if (baby.sex == SexCategory.BOY) WhoLmsData.weightForAgeBoys else WhoLmsData.weightForAgeGirls
            ChartType.LENGTH_FOR_AGE -> if (baby.sex == SexCategory.BOY) WhoLmsData.lengthForAgeBoys else WhoLmsData.lengthForAgeGirls
            ChartType.HEAD_CIRCUMFERENCE_FOR_AGE -> if (baby.sex == SexCategory.BOY) WhoLmsData.headCircumferenceBoys else WhoLmsData.headCircumferenceGirls
        }
    }

    val maxAgeMonths = 24.0 // Show 0-24 months for detailed infant view
    val filteredTable = remember(lmsTable) {
        lmsTable.filter { it.indexValue <= maxAgeMonths }
    }

    // Determine Y range from WHO P3 at 0m to P97 at 24m
    val minY = remember(filteredTable) {
        val first = filteredTable.first()
        (WhoGrowthCalculator.percentileToValue(3.0, first) * 0.9).coerceAtLeast(0.0)
    }
    val maxY = remember(filteredTable) {
        val last = filteredTable.last()
        WhoGrowthCalculator.percentileToValue(97.0, last) * 1.08
    }

    val points = remember(measurements, baby) {
        measurements.mapNotNull { entry ->
            val ageMonths = WhoGrowthCalculator.calculateAgeMonths(baby.birthTimestamp, entry.timestamp)
            val value = when (chartType) {
                ChartType.WEIGHT_FOR_AGE -> entry.weightKg
                ChartType.LENGTH_FOR_AGE -> entry.lengthCm
                ChartType.HEAD_CIRCUMFERENCE_FOR_AGE -> entry.headCircumferenceCm
            }
            if (value != null && ageMonths in 0.0..maxAgeMonths) {
                Pair(entry, Pair(ageMonths, value))
            } else null
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        // Legend & Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = when (chartType) {
                    ChartType.WEIGHT_FOR_AGE -> "Weight-for-age (kg)"
                    ChartType.LENGTH_FOR_AGE -> "Length-for-age (cm)"
                    ChartType.HEAD_CIRCUMFERENCE_FOR_AGE -> "Head Circumference (cm)"
                },
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "WHO 2006 Standards",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Legend row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            LegendItem(color = Color(0xFF1E2024), label = "Recorded (Baby)")
            LegendItem(color = Color(0xFF5E6578), label = "50th (Median)")
            LegendItem(color = Color(0xFFA0A5B1), label = "3rd / 97th Percentile")
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Canvas Chart
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(points) {
                        detectTapGestures { tapOffset ->
                            val width = size.width
                            val height = size.height
                            val chartLeft = 36.dp.toPx()
                            val chartBottom = height - 24.dp.toPx()
                            val chartWidth = width - chartLeft - 16.dp.toPx()
                            val chartHeight = chartBottom - 16.dp.toPx()

                            var closestIndex: Int? = null
                            var minDistance = Float.MAX_VALUE

                            points.forEachIndexed { index, (_, point) ->
                                val (age, value) = point
                                val px = chartLeft + (age.toFloat() / maxAgeMonths.toFloat()) * chartWidth
                                val py = chartBottom - ((value.toFloat() - minY.toFloat()) / (maxY.toFloat() - minY.toFloat())) * chartHeight
                                val dist = (Offset(px, py) - tapOffset).getDistance()
                                if (dist < 40.dp.toPx() && dist < minDistance) {
                                    minDistance = dist
                                    closestIndex = index
                                }
                            }
                            selectedIndex = closestIndex
                        }
                    }
            ) {
                val width = size.width
                val height = size.height
                val chartLeft = 36.dp.toPx()
                val chartBottom = height - 24.dp.toPx()
                val chartWidth = width - chartLeft - 16.dp.toPx()
                val chartHeight = chartBottom - 16.dp.toPx()

                // Grid lines & labels
                val gridColor = Color(0xFFEDEDF0)
                val textColor = Color(0xFF717680)

                // Horizontal grid lines (4 lines)
                val ySteps = 4
                for (i in 0..ySteps) {
                    val yVal = minY + (maxY - minY) * (i.toDouble() / ySteps)
                    val py = chartBottom - (i.toFloat() / ySteps) * chartHeight
                    drawLine(
                        color = gridColor,
                        start = Offset(chartLeft, py),
                        end = Offset(chartLeft + chartWidth, py),
                        strokeWidth = 1.dp.toPx()
                    )
                    // Y Label
                    val text = "%.1f".format(yVal)
                    drawText(
                        textMeasurer = textMeasurer,
                        text = text,
                        topLeft = Offset(4.dp.toPx(), py - 8.dp.toPx()),
                        style = TextStyle(fontSize = 10.sp, color = textColor)
                    )
                }

                // Vertical age grid lines (every 6 months: 0, 6, 12, 18, 24)
                for (m in listOf(0, 6, 12, 18, 24)) {
                    val px = chartLeft + (m.toFloat() / maxAgeMonths.toFloat()) * chartWidth
                    drawLine(
                        color = gridColor,
                        start = Offset(px, chartBottom),
                        end = Offset(px, chartBottom - chartHeight),
                        strokeWidth = 1.dp.toPx()
                    )
                    val label = "${m}m"
                    drawText(
                        textMeasurer = textMeasurer,
                        text = label,
                        topLeft = Offset(px - 10.dp.toPx(), chartBottom + 4.dp.toPx()),
                        style = TextStyle(fontSize = 10.sp, color = textColor)
                    )
                }

                // Draw WHO Percentile Reference Curves (P97, P85, P50, P15, P3)
                val percentiles = listOf(
                    Triple(97.0, Color(0xFFB0B5C0), false),
                    Triple(85.0, Color(0xFFCCD0D9), false),
                    Triple(50.0, Color(0xFF5E6578), true),
                    Triple(15.0, Color(0xFFCCD0D9), false),
                    Triple(3.0, Color(0xFFB0B5C0), false)
                )

                percentiles.forEach { (p, col, isMedian) ->
                    val path = Path()
                    filteredTable.forEachIndexed { idx, lms ->
                        val pVal = WhoGrowthCalculator.percentileToValue(p, lms)
                        val px = chartLeft + (lms.indexValue.toFloat() / maxAgeMonths.toFloat()) * chartWidth
                        val py = chartBottom - ((pVal.toFloat() - minY.toFloat()) / (maxY.toFloat() - minY.toFloat())) * chartHeight
                        if (idx == 0) path.moveTo(px, py) else path.lineTo(px, py)
                    }
                    drawPath(
                        path = path,
                        color = col,
                        style = Stroke(width = if (isMedian) 2.dp.toPx() else 1.2.dp.toPx())
                    )
                }

                // Draw Baby's recorded trajectory line & points
                if (points.isNotEmpty()) {
                    val babyPath = Path()
                    points.forEachIndexed { idx, (_, pt) ->
                        val (age, value) = pt
                        val px = chartLeft + (age.toFloat() / maxAgeMonths.toFloat()) * chartWidth
                        val py = chartBottom - ((value.toFloat() - minY.toFloat()) / (maxY.toFloat() - minY.toFloat())) * chartHeight
                        if (idx == 0) babyPath.moveTo(px, py) else babyPath.lineTo(px, py)
                    }
                    drawPath(
                        path = babyPath,
                        color = Color(0xFF191B1F),
                        style = Stroke(width = 2.5.dp.toPx())
                    )

                    // Draw Point dots
                    points.forEachIndexed { idx, (_, pt) ->
                        val (age, value) = pt
                        val px = chartLeft + (age.toFloat() / maxAgeMonths.toFloat()) * chartWidth
                        val py = chartBottom - ((value.toFloat() - minY.toFloat()) / (maxY.toFloat() - minY.toFloat())) * chartHeight
                        val isSelected = selectedIndex == idx

                        drawCircle(
                            color = Color.White,
                            radius = if (isSelected) 7.dp.toPx() else 5.dp.toPx(),
                            center = Offset(px, py)
                        )
                        drawCircle(
                            color = if (isSelected) Color(0xFF383D47) else Color(0xFF191B1F),
                            radius = if (isSelected) 5.dp.toPx() else 3.5.dp.toPx(),
                            center = Offset(px, py)
                        )
                    }
                }
            }
        }

        // Inspection Card for selected or latest point
        val inspectedPair = selectedIndex?.let { points.getOrNull(it) } ?: points.lastOrNull()
        if (inspectedPair != null) {
            val (entry, pt) = inspectedPair
            val (ageMonths, value) = pt
            val assessment = remember(value, ageMonths, baby.sex, chartType) {
                when (chartType) {
                    ChartType.WEIGHT_FOR_AGE -> WhoGrowthCalculator.assessWeightForAge(value, ageMonths, baby.sex)
                    ChartType.LENGTH_FOR_AGE -> WhoGrowthCalculator.assessLengthForAge(value, ageMonths, baby.sex)
                    ChartType.HEAD_CIRCUMFERENCE_FOR_AGE -> WhoGrowthCalculator.assessHeadCircumferenceForAge(value, ageMonths, baby.sex)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .padding(12.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "${dateFormatter.format(Date(entry.timestamp))} (Age ${ageMonths.roundToInt()}m)",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Measured: $value ${if (chartType == ChartType.WEIGHT_FOR_AGE) "kg" else "cm"}",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "~${assessment.percentile.roundToInt()}th Percentile",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Z: ${"%.2f".format(assessment.zScore)} SD",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Tap any recorded point to inspect exact WHO percentile and Z-score.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(color, RoundedCornerShape(2.dp))
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
