package com.flowsim.simfwlos.ui.screens.analytics

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.CompareArrows
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.flowsim.simfwlos.data.db.SimulationResultEntity
import com.flowsim.simfwlos.data.repository.SimulationRepository
import com.flowsim.simfwlos.ui.components.*
import com.flowsim.simfwlos.ui.navigation.Screen
import com.flowsim.simfwlos.ui.theme.*
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

@Composable
fun AnalyticsScreen(
    onVisualStats: () -> Unit,
    navController: NavController
) {
    val vm: AnalyticsViewModel = koinViewModel()
    val state by vm.state.collectAsState()

    val typeColors = mapOf(
        "TASKS" to BallBlue, "BUDGET" to ColorWarning,
        "TIME" to BallTeal, "CUSTOM" to Violet400
    )
    val typeChartEntries = state.typeDistribution.map { (type, count) ->
        ChartEntry(type.lowercase().replaceFirstChar { it.uppercaseChar() }, count.toFloat(), typeColors[type] ?: Violet400)
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().statusBarsPadding(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("Analytics", style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold), color = TextPrimary)
            Text("Insights from your simulations", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatBox("Total Sims", state.totalSimulations.toString(), Violet500, Icons.Rounded.Science, Modifier.weight(1f))
                StatBox("Completed", state.completedSimulations.size.toString(), ColorSuccess, Icons.Rounded.CheckCircle, Modifier.weight(1f))
                StatBox("Types", state.typeDistribution.size.toString(), BallBlue, Icons.Rounded.Category, Modifier.weight(1f))
            }
        }

        if (typeChartEntries.isNotEmpty()) {
            item {
                GlassCard {
                    Text("By Simulation Type", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold), color = TextPrimary)
                    Spacer(Modifier.height(16.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        PieChart(typeChartEntries, modifier = Modifier.size(140.dp))
                        Spacer(Modifier.width(20.dp))
                        ChartLegend(typeChartEntries, modifier = Modifier.weight(1f))
                    }
                }
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                AnalyticsActionCard("Visual Stats", "Charts & graphs", Icons.Rounded.BarChart, Violet500, Modifier.weight(1f)) {
                    onVisualStats()
                }
                AnalyticsActionCard("History", "Past simulations", Icons.Rounded.History, BallBlue, Modifier.weight(1f)) {
                    navController.navigate(Screen.History.route)
                }
            }
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                AnalyticsActionCard("Saved", "Bookmarked results", Icons.Rounded.Bookmark, ColorWarning, Modifier.weight(1f)) {
                    navController.navigate(Screen.SavedResults.route)
                }
                AnalyticsActionCard("Scenarios", "Compare scenarios", Icons.AutoMirrored.Rounded.CompareArrows, BallTeal, Modifier.weight(1f)) {
                    navController.navigate(Screen.Scenarios.route)
                }
            }
        }

        if (state.completedSimulations.isNotEmpty()) {
            item { SectionHeader("Recent Completed") }
            items(state.completedSimulations.take(5)) { sim ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SurfaceDark, RoundedCornerShape(12.dp))
                        .clickable { navController.navigate(Screen.VisualStats.route(sim.id)) }
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Rounded.Science, null, tint = Violet400, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(10.dp))
                    Text(sim.name, style = MaterialTheme.typography.bodyMedium, color = TextPrimary, modifier = Modifier.weight(1f))
                    TypeChip(sim.type)
                    Spacer(Modifier.width(6.dp))
                    Icon(Icons.Rounded.ChevronRight, null, tint = TextInactive, modifier = Modifier.size(18.dp))
                }
            }
        }

        item { Spacer(Modifier.height(16.dp)) }
    }
}

@Composable
private fun StatBox(
    label: String,
    value: String,
    color: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.background(SurfaceDark, RoundedCornerShape(14.dp)).padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(modifier = Modifier.size(34.dp).background(color.copy(alpha = 0.15f), RoundedCornerShape(10.dp)), contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = color, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.height(6.dp))
        Text(value, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = TextPrimary)
        Text(label, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
    }
}

@Composable
private fun AnalyticsActionCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier
            .background(SurfaceDark, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Box(modifier = Modifier.size(40.dp).background(color.copy(alpha = 0.15f), RoundedCornerShape(10.dp)), contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = color, modifier = Modifier.size(22.dp))
        }
        Spacer(Modifier.height(10.dp))
        Text(title, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold), color = TextPrimary)
        Text(subtitle, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
    }
}

@Composable
fun VisualStatsScreen(simId: Long, onBack: () -> Unit) {
    val repo: SimulationRepository = koinInject()
    var results by remember { mutableStateOf<List<SimulationResultEntity>>(emptyList()) }
    var allResults by remember { mutableStateOf<List<SimulationResultEntity>>(emptyList()) }
    var simName by remember { mutableStateOf("") }
    var maxRun by remember { mutableStateOf(1) }

    LaunchedEffect(simId) {
        if (simId > 0) {
            repo.getById(simId)?.let { simName = it.name }
            repo.getResultsFlow(simId).collectLatest { r ->
                allResults = r
                val max = r.maxOfOrNull { it.runNumber } ?: 1
                maxRun = max
                results = r.filter { it.runNumber == max }
            }
        }
    }

    val chartEntries = results.map { r ->
        ChartEntry(r.categoryName, r.count.toFloat(), parseColor(r.categoryColorHex))
    }
    val runTotals = (1..maxRun).map { run ->
        allResults.filter { it.runNumber == run }.sumOf { it.count }.toFloat()
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().statusBarsPadding(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item { TopBarWithBack(title = "Visual Stats${if (simName.isNotEmpty()) ": $simName" else ""}", onBack = onBack) }

        if (chartEntries.isNotEmpty()) {
            item {
                GlassCard {
                    Text("Distribution Pie", style = MaterialTheme.typography.titleSmall, color = TextSecondary)
                    Spacer(Modifier.height(12.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        PieChart(chartEntries, modifier = Modifier.size(160.dp))
                        Spacer(Modifier.width(20.dp))
                        ChartLegend(chartEntries, modifier = Modifier.weight(1f))
                    }
                }
            }
            item {
                GlassCard {
                    Text("Bar Distribution", style = MaterialTheme.typography.titleSmall, color = TextSecondary)
                    Spacer(Modifier.height(12.dp))
                    BarChart(chartEntries, modifier = Modifier.fillMaxWidth().height(160.dp))
                }
            }
            if (maxRun > 1 && runTotals.isNotEmpty()) {
                item {
                    GlassCard {
                        Text("Ball Count per Run", style = MaterialTheme.typography.titleSmall, color = TextSecondary)
                        Spacer(Modifier.height(12.dp))
                        LineChart(
                            dataPoints = runTotals.mapIndexed { i, v -> "Run ${i + 1}" to v },
                            lineColor = Violet500,
                            modifier = Modifier.fillMaxWidth().height(120.dp)
                        )
                    }
                }
            }
        } else {
            item { EmptyState("No data available", "Run a simulation to see visual stats") }
        }

        item { Spacer(Modifier.height(16.dp)) }
    }
}

@Composable
fun ProbabilityMapScreen(simId: Long, onBack: () -> Unit) {
    val repo: SimulationRepository = koinInject()
    var allResults by remember { mutableStateOf<List<SimulationResultEntity>>(emptyList()) }
    var simName by remember { mutableStateOf("") }

    LaunchedEffect(simId) {
        if (simId > 0) {
            repo.getById(simId)?.let { simName = it.name }
            repo.getResultsFlow(simId).collectLatest { allResults = it }
        }
    }

    val categories = allResults.map { it.categoryName }.distinct()
    val probabilities = categories.map { cat ->
        val allCounts = allResults.filter { it.categoryName == cat }.map { it.count }
        val avgCount = if (allCounts.isNotEmpty()) allCounts.average().toFloat() else 0f
        val total = allResults.groupBy { it.runNumber }
            .values.mapNotNull { run -> run.sumOf { it.count }.toFloat().takeIf { it > 0 } }.average().toFloat()
        cat to if (total > 0) avgCount / total else 0f
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().statusBarsPadding(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item { TopBarWithBack(title = "Probability Map", onBack = onBack) }
        item {
            GlassCard {
                Text(
                    "Probability Distribution",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = TextPrimary
                )
                Text("Based on ${allResults.map { it.runNumber }.distinct().size} runs", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                Spacer(Modifier.height(20.dp))
                probabilities.forEach { (cat, prob) ->
                    ProbabilityRow(category = cat, probability = prob)
                    Spacer(Modifier.height(8.dp))
                }
            }
        }
        item { Spacer(Modifier.height(16.dp)) }
    }
}

@Composable
private fun ProbabilityRow(category: String, probability: Float) {
    val pct = (probability * 100).toInt()
    val color = when {
        probability > 0.4f -> ColorError
        probability > 0.25f -> ColorWarning
        probability > 0.15f -> ColorSuccess
        else -> BallBlue
    }
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(category, style = MaterialTheme.typography.bodyMedium, color = TextPrimary, modifier = Modifier.weight(1f))
            Text("$pct%", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold), color = color)
        }
        Spacer(Modifier.height(4.dp))
        Box(
            modifier = Modifier.fillMaxWidth().height(8.dp).background(DividerColor, RoundedCornerShape(4.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(probability.coerceIn(0f, 1f))
                    .fillMaxHeight()
                    .background(
                        androidx.compose.ui.graphics.Brush.horizontalGradient(listOf(color, color.copy(alpha = 0.6f))),
                        RoundedCornerShape(4.dp)
                    )
            )
        }
    }
}
