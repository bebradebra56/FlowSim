package com.flowsim.flosasms.ui.screens.simulator

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.flowsim.flosasms.data.db.SimulationResultEntity
import com.flowsim.flosasms.data.repository.SimulationRepository
import com.flowsim.flosasms.ui.components.*
import com.flowsim.flosasms.ui.theme.*
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Composable
fun ResultDistributionScreen(
    simId: Long,
    onMultiRun: () -> Unit,
    onVisualStats: () -> Unit,
    onSuggestions: () -> Unit,
    onDone: () -> Unit
) {
    val repo: SimulationRepository = koinInject()
    var results by remember { mutableStateOf<List<SimulationResultEntity>>(emptyList()) }
    var simName by remember { mutableStateOf("Results") }
    var simType by remember { mutableStateOf("CUSTOM") }
    var isSaved by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(simId) {
        repo.getById(simId)?.let { sim ->
            simName = sim.name
            simType = sim.type
            isSaved = sim.isSaved
        }
        repo.getResultsFlow(simId).collectLatest { r ->
            results = r.filter { it.runNumber == r.maxOfOrNull { it2 -> it2.runNumber } ?: 1 }
        }
    }

    val totalBalls = results.sumOf { it.count }
    val chartEntries = results.map { r ->
        ChartEntry(
            label = r.categoryName,
            value = r.count.toFloat(),
            color = parseColor(r.categoryColorHex)
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().statusBarsPadding(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onDone) {
                    Icon(Icons.Rounded.Close, null, tint = TextPrimary)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(simName, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = TextPrimary)
                    Text("$totalBalls balls distributed", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                }
                TypeChip(simType)
                Spacer(Modifier.width(4.dp))
                IconButton(onClick = {
                    coroutineScope.launch {
                        val sim = repo.getById(simId) ?: return@launch
                        repo.update(sim.copy(isSaved = !sim.isSaved))
                        isSaved = !isSaved
                    }
                }) {
                    Icon(
                        if (isSaved) Icons.Rounded.Bookmark else Icons.Rounded.BookmarkBorder,
                        null, tint = if (isSaved) ColorWarning else TextSecondary
                    )
                }
            }
        }

        if (chartEntries.isNotEmpty()) {
            item {
                GlassCard {
                    Text("Distribution", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold), color = TextPrimary)
                    Spacer(Modifier.height(16.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        PieChart(
                            entries = chartEntries,
                            modifier = Modifier.size(160.dp)
                        )
                        Spacer(Modifier.width(24.dp))
                        ChartLegend(entries = chartEntries, modifier = Modifier.weight(1f))
                    }
                }
            }

            item {
                GlassCard {
                    Text("Bar Chart", style = MaterialTheme.typography.titleSmall, color = TextSecondary)
                    Spacer(Modifier.height(12.dp))
                    BarChart(
                        entries = chartEntries,
                        modifier = Modifier.fillMaxWidth().height(150.dp)
                    )
                }
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    results.forEach { r ->
                        ResultRow(result = r, total = totalBalls)
                    }
                }
            }
        } else {
            item { EmptyState("No results yet", "Run the simulation first") }
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                GradientButton("Run Multiple Times", onClick = onMultiRun, modifier = Modifier.fillMaxWidth())
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlineButton("Visual Stats", onClick = onVisualStats, modifier = Modifier.weight(1f))
                    OutlineButton("Suggestions", onClick = onSuggestions, modifier = Modifier.weight(1f))
                }
            }
        }

        item { Spacer(Modifier.height(16.dp)) }
    }
}

@Composable
private fun ResultRow(result: SimulationResultEntity, total: Int) {
    val color = parseColor(result.categoryColorHex)
    val pct = if (total > 0) result.count * 100f / total else 0f

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(SurfaceDark, RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(10.dp).background(color, androidx.compose.foundation.shape.CircleShape))
        Spacer(Modifier.width(12.dp))
        Text(result.categoryName, style = MaterialTheme.typography.bodyMedium, color = TextPrimary, modifier = Modifier.weight(1f))
        Column(horizontalAlignment = Alignment.End) {
            Text(
                "${result.count} balls",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = color
            )
            Text("${String.format("%.1f", pct)}%", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
        }
    }
}
