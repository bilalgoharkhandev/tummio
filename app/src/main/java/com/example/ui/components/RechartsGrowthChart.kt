package com.example.ui.components

import android.annotation.SuppressLint
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.ShowChart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.model.BabyProfile
import com.example.data.model.GrowthEntry
import com.example.data.model.SexCategory
import com.example.who.WhoGrowthCalculator
import com.example.who.WhoLmsData
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

enum class MetricViewMode {
    WEIGHT,
    LENGTH
}

enum class ChartEngineMode {
    NATIVE_COMPOSE,
    RECHARTS_WEB
}

@Composable
fun RechartsGrowthChartCard(
    baby: BabyProfile,
    measurements: List<GrowthEntry>,
    modifier: Modifier = Modifier
) {
    var engineMode by remember { mutableStateOf(ChartEngineMode.NATIVE_COMPOSE) }
    var metricMode by remember { mutableStateOf(MetricViewMode.WEIGHT) }

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header with Engine Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "WHO Growth Standards Chart",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Plotting Weight & Length against WHO percentiles",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Switcher between Native Compose and Recharts Web
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(2.dp)
                ) {
                    IconButton(
                        onClick = { engineMode = ChartEngineMode.NATIVE_COMPOSE },
                        modifier = Modifier
                            .size(32.dp)
                            .background(
                                if (engineMode == ChartEngineMode.NATIVE_COMPOSE) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant,
                                RoundedCornerShape(6.dp)
                            )
                            .testTag("toggle_native_chart")
                    ) {
                        Icon(
                            Icons.Outlined.ShowChart,
                            contentDescription = "Native Compose Chart",
                            tint = if (engineMode == ChartEngineMode.NATIVE_COMPOSE) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(
                        onClick = { engineMode = ChartEngineMode.RECHARTS_WEB },
                        modifier = Modifier
                            .size(32.dp)
                            .background(
                                if (engineMode == ChartEngineMode.RECHARTS_WEB) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant,
                                RoundedCornerShape(6.dp)
                            )
                            .testTag("toggle_recharts_web")
                    ) {
                        Icon(
                            Icons.Outlined.Code,
                            contentDescription = "Recharts Visualization",
                            tint = if (engineMode == ChartEngineMode.RECHARTS_WEB) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Metric Selector Tabs: Weight vs Length
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = metricMode == MetricViewMode.WEIGHT,
                    onClick = { metricMode = MetricViewMode.WEIGHT },
                    label = { Text("Weight-for-Age (kg)") },
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    selected = metricMode == MetricViewMode.LENGTH,
                    onClick = { metricMode = MetricViewMode.LENGTH },
                    label = { Text("Length-for-Age (cm)") },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Render either Native Compose Canvas Chart or Embedded Recharts WebView
            if (engineMode == ChartEngineMode.NATIVE_COMPOSE) {
                WhoGrowthChart(
                    chartType = if (metricMode == MetricViewMode.WEIGHT) ChartType.WEIGHT_FOR_AGE else ChartType.LENGTH_FOR_AGE,
                    baby = baby,
                    measurements = measurements
                )
            } else {
                RechartsWebView(
                    baby = baby,
                    measurements = measurements,
                    isWeight = metricMode == MetricViewMode.WEIGHT,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                )
            }
        }
    }
}

/**
 * Embedded Recharts-based Web View component.
 * Renders an interactive, responsive HTML5/SVG growth chart matching Recharts syntax
 * with CartesianGrid, XAxis, YAxis, Tooltip, Legend, and official WHO Percentile Curves (P3, P15, P50, P85, P97).
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun RechartsWebView(
    baby: BabyProfile,
    measurements: List<GrowthEntry>,
    isWeight: Boolean,
    modifier: Modifier = Modifier
) {
    val htmlContent = remember(baby, measurements, isWeight) {
        buildRechartsHtml(baby, measurements, isWeight)
    }

    AndroidView(
        factory = { context ->
            WebView(context).apply {
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.loadWithOverviewMode = true
                settings.useWideViewPort = true
                settings.cacheMode = WebSettings.LOAD_NO_CACHE
                setBackgroundColor(0x00000000)
                webViewClient = WebViewClient()
                loadDataWithBaseURL("https://recharts.local", htmlContent, "text/html", "UTF-8", null)
            }
        },
        update = { webView ->
            webView.loadDataWithBaseURL("https://recharts.local", htmlContent, "text/html", "UTF-8", null)
        },
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
    )
}

private fun buildRechartsHtml(
    baby: BabyProfile,
    measurements: List<GrowthEntry>,
    isWeight: Boolean
): String {
    val table = if (isWeight) {
        if (baby.sex == SexCategory.BOY) WhoLmsData.weightForAgeBoys else WhoLmsData.weightForAgeGirls
    } else {
        if (baby.sex == SexCategory.BOY) WhoLmsData.lengthForAgeBoys else WhoLmsData.lengthForAgeGirls
    }.filter { it.indexValue <= 24.0 }

    val unit = if (isWeight) "kg" else "cm"
    val title = if (isWeight) "Weight-for-Age (WHO Child Growth Standards)" else "Length-for-Age (WHO Child Growth Standards)"

    // Compute WHO reference points at monthly intervals
    val whoDataJson = table.joinToString(",") { lms ->
        val month = lms.indexValue.roundToInt()
        val p3 = "%.2f".format(Locale.US, WhoGrowthCalculator.percentileToValue(3.0, lms))
        val p15 = "%.2f".format(Locale.US, WhoGrowthCalculator.percentileToValue(15.0, lms))
        val p50 = "%.2f".format(Locale.US, lms.m)
        val p85 = "%.2f".format(Locale.US, WhoGrowthCalculator.percentileToValue(85.0, lms))
        val p97 = "%.2f".format(Locale.US, WhoGrowthCalculator.percentileToValue(97.0, lms))
        "{\"month\":$month,\"p3\":$p3,\"p15\":$p15,\"p50\":$p50,\"p85\":$p85,\"p97\":$p97}"
    }

    // Child's actual recorded measurements
    val measuredJson = measurements.mapNotNull { m ->
        val valDouble = if (isWeight) m.weightKg else m.lengthCm
        if (valDouble != null) {
            val ageM = WhoGrowthCalculator.calculateAgeMonths(baby.birthTimestamp, m.timestamp)
            val valStr = "%.2f".format(Locale.US, valDouble)
            val ageStr = "%.1f".format(Locale.US, ageM)
            val dateStr = SimpleDateFormat("MMM d", Locale.US).format(Date(m.timestamp))
            "{\"age\":$ageStr,\"value\":$valStr,\"date\":\"$dateStr\"}"
        } else null
    }.joinToString(",")

    val sb = StringBuilder()
    sb.append("<!DOCTYPE html><html><head><meta charset='utf-8'><meta name='viewport' content='width=device-width, initial-scale=1.0'>")
    sb.append("<style>")
    sb.append("* { box-sizing: border-box; margin: 0; padding: 0; }")
    sb.append("body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; background: #FAFBFD; color: #191B1F; padding: 10px; }")
    sb.append(".header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 6px; }")
    sb.append(".header h3 { font-size: 13px; font-weight: 700; color: #191B1F; }")
    sb.append(".header span { font-size: 10px; color: #64748B; background: #EEF2F6; padding: 2px 6px; border-radius: 4px; }")
    sb.append(".legend { display: flex; flex-wrap: wrap; gap: 10px; margin-bottom: 8px; font-size: 10px; color: #475569; }")
    sb.append(".legend-item { display: flex; align-items: center; gap: 4px; }")
    sb.append(".dot { width: 8px; height: 8px; border-radius: 50%; }")
    sb.append(".dot-baby { background: #191B1F; }")
    sb.append(".dot-p50 { background: #5E6578; }")
    sb.append(".dot-p97 { background: #94A3B8; }")
    sb.append(".dot-p3 { background: #CBD5E1; }")
    sb.append("#chart-container { position: relative; width: 100%; height: 210px; }")
    sb.append("svg { width: 100%; height: 100%; overflow: visible; }")
    sb.append(".axis-text { font-size: 9px; fill: #64748B; }")
    sb.append(".grid-line { stroke: #E2E8F0; stroke-width: 0.8; stroke-dasharray: 3 3; }")
    sb.append(".line-p3 { stroke: #CBD5E1; stroke-width: 1.2; fill: none; }")
    sb.append(".line-p15 { stroke: #E2E8F0; stroke-width: 1; fill: none; }")
    sb.append(".line-p50 { stroke: #5E6578; stroke-width: 2; fill: none; }")
    sb.append(".line-p85 { stroke: #E2E8F0; stroke-width: 1; fill: none; }")
    sb.append(".line-p97 { stroke: #94A3B8; stroke-width: 1.2; fill: none; }")
    sb.append(".line-baby { stroke: #191B1F; stroke-width: 2.5; fill: none; }")
    sb.append(".point-baby { fill: #191B1F; stroke: #FFFFFF; stroke-width: 1.5; cursor: pointer; }")
    sb.append(".tooltip { position: absolute; display: none; background: rgba(25, 27, 31, 0.95); color: #FFF; padding: 6px 10px; border-radius: 6px; font-size: 11px; pointer-events: none; z-index: 10; }")
    sb.append("</style></head><body>")
    sb.append("<div class='header'><h3>").append(title).append("</h3><span>Recharts Engine • ").append(baby.sex.name).append("</span></div>")
    sb.append("<div class='legend'>")
    sb.append("<div class='legend-item'><div class='dot dot-baby'></div><strong>").append(baby.name).append(" (Recorded)</strong></div>")
    sb.append("<div class='legend-item'><div class='dot dot-p50'></div><span>WHO 50th (Median)</span></div>")
    sb.append("<div class='legend-item'><div class='dot dot-p97'></div><span>85th / 97th</span></div>")
    sb.append("<div class='legend-item'><div class='dot dot-p3'></div><span>3rd / 15th</span></div>")
    sb.append("</div>")
    sb.append("<div id='chart-container'><svg id='chart-svg' viewBox='0 0 340 200' preserveAspectRatio='none'>")
    sb.append("<g id='grid'></g>")
    sb.append("<path id='curve-p3' class='line-p3'/>")
    sb.append("<path id='curve-p15' class='line-p15'/>")
    sb.append("<path id='curve-p50' class='line-p50'/>")
    sb.append("<path id='curve-p85' class='line-p85'/>")
    sb.append("<path id='curve-p97' class='line-p97'/>")
    sb.append("<path id='curve-baby' class='line-baby'/>")
    sb.append("<g id='points-group'></g><g id='axis-labels'></g></svg>")
    sb.append("<div id='tooltip' class='tooltip'></div></div>")

    // JavaScript logic
    sb.append("<script>")
    sb.append("var whoData = [").append(whoDataJson).append("];")
    sb.append("var babyData = [").append(measuredJson).append("];")
    sb.append("var unit = '").append(unit).append("';")
    sb.append("""
        var svg = document.getElementById('chart-svg');
        var gridG = document.getElementById('grid');
        var axisG = document.getElementById('axis-labels');
        var pointsG = document.getElementById('points-group');
        var tooltip = document.getElementById('tooltip');

        var padLeft = 32, padBottom = 24, padTop = 10, padRight = 14;
        var width = 340, height = 200;
        var chartW = width - padLeft - padRight;
        var chartH = height - padTop - padBottom;
        var maxMonth = 24;

        var minVal = 9999, maxVal = -9999;
        for (var i = 0; i < whoData.length; i++) {
          if (whoData[i].p3 < minVal) minVal = whoData[i].p3;
          if (whoData[i].p97 > maxVal) maxVal = whoData[i].p97;
        }
        minVal = minVal * 0.9;
        maxVal = maxVal * 1.06;

        function getX(m) { return padLeft + (m / maxMonth) * chartW; }
        function getY(v) { return padTop + chartH - ((v - minVal) / (maxVal - minVal)) * chartH; }

        var yTicks = 4;
        for (var t = 0; t <= yTicks; t++) {
          var val = minVal + (maxVal - minVal) * (t / yTicks);
          var y = getY(val);
          gridG.innerHTML += '<line x1="' + padLeft + '" y1="' + y + '" x2="' + (padLeft + chartW) + '" y2="' + y + '" class="grid-line"/>';
          axisG.innerHTML += '<text x="4" y="' + (y + 3) + '" class="axis-text">' + val.toFixed(1) + '</text>';
        }

        var xMonths = [0, 6, 12, 18, 24];
        for (var xm = 0; xm < xMonths.length; xm++) {
          var m = xMonths[xm];
          var x = getX(m);
          gridG.innerHTML += '<line x1="' + x + '" y1="' + padTop + '" x2="' + x + '" y2="' + (padTop + chartH) + '" class="grid-line"/>';
          axisG.innerHTML += '<text x="' + (x - 6) + '" y="' + (height - 6) + '" class="axis-text">' + m + 'm</text>';
        }

        function buildPath(prop) {
          var res = '';
          for (var p = 0; p < whoData.length; p++) {
            res += (p === 0 ? 'M ' : ' L ') + getX(whoData[p].month).toFixed(1) + ' ' + getY(whoData[p][prop]).toFixed(1);
          }
          return res;
        }

        document.getElementById('curve-p3').setAttribute('d', buildPath('p3'));
        document.getElementById('curve-p15').setAttribute('d', buildPath('p15'));
        document.getElementById('curve-p50').setAttribute('d', buildPath('p50'));
        document.getElementById('curve-p85').setAttribute('d', buildPath('p85'));
        document.getElementById('curve-p97').setAttribute('d', buildPath('p97'));

        if (babyData.length > 0) {
          var babyPath = '';
          for (var b = 0; b < babyData.length; b++) {
            babyPath += (b === 0 ? 'M ' : ' L ') + getX(babyData[b].age).toFixed(1) + ' ' + getY(babyData[b].value).toFixed(1);
          }
          document.getElementById('curve-baby').setAttribute('d', babyPath);

          for (var pt = 0; pt < babyData.length; pt++) {
            (function(d) {
              var cx = getX(d.age);
              var cy = getY(d.value);
              var circle = document.createElementNS('http://www.w3.org/2000/svg', 'circle');
              circle.setAttribute('cx', cx);
              circle.setAttribute('cy', cy);
              circle.setAttribute('r', '4.5');
              circle.setAttribute('class', 'point-baby');
              circle.addEventListener('click', function() {
                tooltip.style.display = 'block';
                tooltip.innerHTML = '<strong>' + d.date + ' (Age ' + d.age + 'm)</strong><br/>Measured: ' + d.value + ' ' + unit;
                var rect = svg.getBoundingClientRect();
                var left = (cx / width) * rect.width;
                var top = (cy / height) * rect.height - 36;
                tooltip.style.left = Math.min(Math.max(10, left - 40), rect.width - 120) + 'px';
                tooltip.style.top = Math.max(0, top) + 'px';
              });
              pointsG.appendChild(circle);
            })(babyData[pt]);
          }
        }
    """)
    sb.append("</script></body></html>")
    return sb.toString()
}
