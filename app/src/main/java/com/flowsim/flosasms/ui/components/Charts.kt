package com.flowsim.flosasms.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.flowsim.flosasms.ui.theme.*
import kotlin.math.min

data class ChartEntry(val label: String, val value: Float, val color: Color)

@Composable
fun PieChart(
    entries: List<ChartEntry>,
    modifier: Modifier = Modifier
) {
    val animProgress = remember { Animatable(0f) }
    LaunchedEffect(entries) {
        animProgress.snapTo(0f)
        animProgress.animateTo(1f, animationSpec = tween(900, easing = EaseOutCubic))
    }
    val progress by animProgress.asState()
    val total = entries.sumOf { it.value.toDouble() }.toFloat().coerceAtLeast(1f)

    Canvas(modifier = modifier) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val radius = min(size.width, size.height) / 2f * 0.88f
        val innerRadius = radius * 0.48f
        var startAngle = -90f

        entries.forEach { entry ->
            val sweep = (entry.value / total) * 360f * progress
            // Glow
            drawArc(
                brush = Brush.radialGradient(
                    colors = listOf(entry.color.copy(alpha = 0.3f), Color.Transparent),
                    center = center, radius = radius * 1.2f
                ),
                startAngle = startAngle,
                sweepAngle = sweep,
                useCenter = true,
                size = Size(radius * 2.4f, radius * 2.4f),
                topLeft = Offset(center.x - radius * 1.2f, center.y - radius * 1.2f)
            )
            drawArc(
                color = entry.color,
                startAngle = startAngle,
                sweepAngle = sweep - 1f,
                useCenter = true,
                size = Size(radius * 2f, radius * 2f),
                topLeft = Offset(center.x - radius, center.y - radius)
            )
            startAngle += sweep
        }
        // Inner hole
        drawCircle(color = ChartBg, radius = innerRadius, center = center)
        drawCircle(color = DividerColor, radius = innerRadius, center = center, style = Stroke(1.dp.toPx()))
    }
}

@Composable
fun BarChart(
    entries: List<ChartEntry>,
    modifier: Modifier = Modifier,
    showLabels: Boolean = true
) {
    val animProgress = remember { Animatable(0f) }
    LaunchedEffect(entries) {
        animProgress.snapTo(0f)
        animProgress.animateTo(1f, animationSpec = tween(800, easing = EaseOutCubic))
    }
    val progress by animProgress.asState()
    val maxVal = entries.maxOfOrNull { it.value }?.coerceAtLeast(1f) ?: 1f

    Column(modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxWidth().weight(1f)) {
            val barCount = entries.size
            if (barCount == 0) return@Canvas
            val gap = 8.dp.toPx()
            val barWidth = (size.width - gap * (barCount + 1)) / barCount
            val maxBarHeight = size.height - 20.dp.toPx()

            entries.forEachIndexed { i, entry ->
                val barHeight = (entry.value / maxVal) * maxBarHeight * progress
                val left = gap + i * (barWidth + gap)
                val top = size.height - barHeight

                // Glow
                drawRoundRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(entry.color.copy(alpha = 0.4f), Color.Transparent),
                        startY = top,
                        endY = size.height
                    ),
                    topLeft = Offset(left - 4f, top - 4f),
                    size = Size(barWidth + 8f, barHeight + 8f),
                    cornerRadius = CornerRadius(6.dp.toPx())
                )

                drawRoundRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(entry.color.copy(alpha = 0.9f), entry.color),
                        startY = top,
                        endY = size.height
                    ),
                    topLeft = Offset(left, top),
                    size = Size(barWidth, barHeight),
                    cornerRadius = CornerRadius(6.dp.toPx())
                )
            }
        }
        if (showLabels) {
            Row(modifier = Modifier.fillMaxWidth().padding(top = 4.dp)) {
                entries.forEach { entry ->
                    Text(
                        text = entry.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f),
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@Composable
fun LineChart(
    dataPoints: List<Pair<String, Float>>,
    lineColor: Color = ChartLine1,
    modifier: Modifier = Modifier
) {
    val animProgress = remember { Animatable(0f) }
    LaunchedEffect(dataPoints) {
        animProgress.snapTo(0f)
        animProgress.animateTo(1f, animationSpec = tween(1000, easing = EaseOutCubic))
    }
    val progress by animProgress.asState()

    if (dataPoints.isEmpty()) return

    Canvas(modifier = modifier) {
        val maxVal = dataPoints.maxOfOrNull { it.second }?.coerceAtLeast(1f) ?: 1f
        val minVal = dataPoints.minOfOrNull { it.second } ?: 0f
        val range = (maxVal - minVal).coerceAtLeast(1f)
        val xStep = size.width / (dataPoints.size - 1).coerceAtLeast(1)
        val visibleCount = (dataPoints.size * progress).toInt().coerceAtLeast(1)

        val path = Path()
        val fillPath = Path()

        dataPoints.take(visibleCount).forEachIndexed { i, (_, v) ->
            val x = i * xStep
            val y = size.height - ((v - minVal) / range) * size.height * 0.85f - size.height * 0.05f
            if (i == 0) {
                path.moveTo(x, y)
                fillPath.moveTo(x, size.height)
                fillPath.lineTo(x, y)
            } else {
                path.lineTo(x, y)
                fillPath.lineTo(x, y)
            }
        }

        val lastX = (visibleCount - 1) * xStep
        fillPath.lineTo(lastX, size.height)
        fillPath.close()

        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(lineColor.copy(alpha = 0.25f), Color.Transparent)
            )
        )
        drawPath(
            path = path,
            color = lineColor,
            style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
        // Dots
        dataPoints.take(visibleCount).forEachIndexed { i, (_, v) ->
            val x = i * xStep
            val y = size.height - ((v - minVal) / range) * size.height * 0.85f - size.height * 0.05f
            drawCircle(color = lineColor, radius = 4.dp.toPx(), center = Offset(x, y))
            drawCircle(color = ChartBg, radius = 2.5.dp.toPx(), center = Offset(x, y))
        }
    }
}

@Composable
fun ChartLegend(entries: List<ChartEntry>, modifier: Modifier = Modifier) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        entries.forEach { entry ->
            val pct = if (entries.sumOf { it.value.toDouble() } > 0)
                (entry.value / entries.sumOf { it.value.toDouble() } * 100).toInt()
            else 0

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(entry.color, RoundedCornerShape(2.dp))
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = entry.label,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "${entry.value.toInt()} ($pct%)",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextPrimary
                )
            }
        }
    }
}

fun parseColor(hex: String): Color {
    return runCatching { Color(android.graphics.Color.parseColor(hex)) }.getOrDefault(Violet500)
}
