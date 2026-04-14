package com.flowsim.flosasms.ui.screens.simulator

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.flowsim.flosasms.data.db.SimulationResultEntity
import com.flowsim.flosasms.data.repository.SimulationRepository
import com.flowsim.flosasms.ui.components.*
import com.flowsim.flosasms.ui.theme.*
import kotlinx.coroutines.flow.collectLatest
import org.koin.compose.koinInject

@Composable
fun TasksModeScreen(simId: Long, onBack: () -> Unit) {
    ModeScreen(
        simId = simId,
        title = "Tasks Distribution",
        icon = Icons.Rounded.CheckCircle,
        accentColor = BallBlue,
        unitLabel = "tasks",
        onBack = onBack
    )
}

@Composable
fun BudgetModeScreen(simId: Long, onBack: () -> Unit) {
    ModeScreen(
        simId = simId,
        title = "Budget Distribution",
        icon = Icons.Rounded.AccountBalanceWallet,
        accentColor = ColorWarning,
        unitLabel = "units",
        onBack = onBack
    )
}

@Composable
fun TimeModeScreen(simId: Long, onBack: () -> Unit) {
    ModeScreen(
        simId = simId,
        title = "Time Distribution",
        icon = Icons.Rounded.Schedule,
        accentColor = BallTeal,
        unitLabel = "hours",
        onBack = onBack
    )
}

@Composable
private fun ModeScreen(
    simId: Long,
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: Color,
    unitLabel: String,
    onBack: () -> Unit
) {
    val repo: SimulationRepository = koinInject()
    var results by remember { mutableStateOf<List<SimulationResultEntity>>(emptyList()) }
    var simName by remember { mutableStateOf("") }
    var totalBalls by remember { mutableStateOf(0) }

    LaunchedEffect(simId) {
        repo.getById(simId)?.let { simName = it.name; totalBalls = it.ballCount }
        repo.getResultsFlow(simId).collectLatest { r ->
            val maxRun = r.maxOfOrNull { it.runNumber } ?: 1
            results = r.filter { it.runNumber == maxRun }
        }
    }

    val chartEntries = results.map { r ->
        ChartEntry(r.categoryName, r.count.toFloat(), parseColor(r.categoryColorHex))
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().statusBarsPadding(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                TopBarWithBack(title = title, onBack = onBack, modifier = Modifier.weight(1f))
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(accentColor.copy(0.15f), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, null, tint = accentColor, modifier = Modifier.size(22.dp))
                }
                Spacer(Modifier.width(16.dp))
            }
        }

        item {
            GlassCard {
                Text(simName, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = TextPrimary)
                Text("$totalBalls $unitLabel distributed", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                if (chartEntries.isNotEmpty()) {
                    Spacer(Modifier.height(20.dp))
                    BarChart(chartEntries, modifier = Modifier.fillMaxWidth().height(160.dp))
                }
            }
        }

        if (chartEntries.isNotEmpty()) {
            item {
                GlassCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        PieChart(chartEntries, modifier = Modifier.size(140.dp))
                        Spacer(Modifier.width(20.dp))
                        ChartLegend(chartEntries, modifier = Modifier.weight(1f))
                    }
                }
            }

            items(results) { r ->
                val color = parseColor(r.categoryColorHex)
                val total = results.sumOf { it.count }.coerceAtLeast(1)
                val pct = r.count * 100f / total

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SurfaceDark, RoundedCornerShape(12.dp))
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.size(10.dp).background(color, androidx.compose.foundation.shape.CircleShape))
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(r.categoryName, style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
                        Text(
                            "${r.count} $unitLabel (${String.format("%.1f", pct)}%)",
                            style = MaterialTheme.typography.labelSmall, color = TextSecondary
                        )
                    }
                    // Mini bar
                    Box(
                        modifier = Modifier
                            .width(60.dp)
                            .height(6.dp)
                            .background(DividerColor, RoundedCornerShape(3.dp))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(pct / 100f)
                                .background(color, RoundedCornerShape(3.dp))
                        )
                    }
                }
            }
        } else {
            item { EmptyState("No data yet", "Run the simulation first") }
        }

        item { Spacer(Modifier.height(16.dp)) }
    }
}

@Composable
fun SmartSuggestionsScreen(simId: Long, onBack: () -> Unit) {
    val repo: SimulationRepository = koinInject()
    var suggestions by remember { mutableStateOf<List<String>>(emptyList()) }
    var results by remember { mutableStateOf<List<SimulationResultEntity>>(emptyList()) }

    LaunchedEffect(simId) {
        repo.getResultsFlow(simId).collectLatest { r ->
            val maxRun = r.maxOfOrNull { it.runNumber } ?: 1
            val latest = r.filter { it.runNumber == maxRun }
            results = latest
            suggestions = generateSuggestions(latest)
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().statusBarsPadding(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { TopBarWithBack(title = "Smart Suggestions", onBack = onBack) }

        item {
            GlassCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(44.dp).background(Violet700.copy(0.2f), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Rounded.Lightbulb, null, tint = ColorWarning, modifier = Modifier.size(24.dp))
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text("AI Insights", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = TextPrimary)
                        Text("Based on simulation results", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    }
                }
            }
        }

        if (suggestions.isNotEmpty()) {
            items(suggestions.size) { i ->
                SuggestionCard(index = i + 1, text = suggestions[i])
            }
        } else if (results.isEmpty()) {
            item { EmptyState("No data to analyze", "Run the simulation first to get suggestions") }
        } else {
            item { EmptyState("No suggestions available", "Try running more simulations") }
        }

        item { Spacer(Modifier.height(16.dp)) }
    }
}

@Composable
private fun SuggestionCard(index: Int, text: String) {
    val colors = listOf(Violet400, BallBlue, BallTeal, ColorWarning, BallPink)
    val color = colors[(index - 1) % colors.size]

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(SurfaceDark, RoundedCornerShape(14.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .background(color.copy(0.15f), RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(index.toString(), style = MaterialTheme.typography.labelLarge, color = color)
        }
        Spacer(Modifier.width(12.dp))
        Text(text, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
    }
}

private fun generateSuggestions(results: List<SimulationResultEntity>): List<String> {
    if (results.isEmpty()) return emptyList()
    val total = results.sumOf { it.count }.toFloat()
    val avg = total / results.size
    val suggestions = mutableListOf<String>()

    val max = results.maxByOrNull { it.count }
    val min = results.minByOrNull { it.count }

    max?.let {
        suggestions.add("\"${it.categoryName}\" received the most balls (${it.count} — ${String.format("%.0f", it.count / total * 100)}%). Consider reducing its weight if this seems unbalanced.")
    }
    min?.let {
        suggestions.add("\"${it.categoryName}\" received the fewest balls (${it.count}). You may want to increase its weight to get better representation.")
    }

    val overloaded = results.filter { it.count > avg * 1.5f }
    if (overloaded.isNotEmpty()) {
        suggestions.add("Categories ${overloaded.joinToString { "\"${it.categoryName}\"" }} are significantly over-represented. Try rebalancing category weights.")
    }

    val balanced = results.all { kotlin.math.abs(it.count - avg) < avg * 0.2f }
    if (balanced) {
        suggestions.add("Your distribution looks well-balanced! All categories received similar numbers of balls.")
    }

    suggestions.add("Run 50+ simulations using Multi-Run to get more statistically reliable probability distribution data.")

    if (results.size >= 3) {
        val spread = results.maxOf { it.count } - results.minOf { it.count }
        if (spread > avg) {
            suggestions.add("High spread detected (${spread} balls difference). Consider adjusting physics settings: lower spread value for more uniform distribution.")
        }
    }

    return suggestions
}
