package com.flowsim.simfwlos.ui.screens.simulator

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.flowsim.simfwlos.ui.components.*
import com.flowsim.simfwlos.ui.theme.*
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject
import org.koin.core.parameter.parametersOf

@Composable
fun MultiRunScreen(
    simId: Long,
    onResults: () -> Unit,
    onBack: () -> Unit
) {
    val vm: SimulatorViewModel = koinViewModel(parameters = { parametersOf(simId) })
    val scope = rememberCoroutineScope()
    var selectedCount by remember { mutableStateOf(10) }
    var isRunning by remember { mutableStateOf(false) }
    var progress by remember { mutableStateOf(0) }
    var errorMsg by remember { mutableStateOf("") }

    val countOptions = listOf(10, 50, 100)

    Column(
        modifier = Modifier.fillMaxSize().statusBarsPadding()
    ) {
        TopBarWithBack(title = "Multi-Run Simulation", onBack = onBack)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {

            GlassCard {
                Text("Run Count", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold), color = TextPrimary)
                Spacer(Modifier.height(4.dp))
                Text("Run the simulation multiple times to get statistical data", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                Spacer(Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    countOptions.forEach { count ->
                        RunCountCard(
                            count = count,
                            isSelected = selectedCount == count,
                            onClick = { selectedCount = count },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            GlassCard {
                Text("Ball Count per Run", style = MaterialTheme.typography.titleSmall, color = TextSecondary)
                Spacer(Modifier.height(6.dp))
                Text(
                    "${vm.simulation?.ballCount ?: 20} balls",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary
                )
                Text("From simulation settings", style = MaterialTheme.typography.labelSmall, color = TextInactive)
            }

            if (isRunning) {
                GlassCard {
                    Text("Running simulations…", style = MaterialTheme.typography.titleSmall, color = TextSecondary)
                    Spacer(Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { progress.toFloat() / selectedCount },
                        modifier = Modifier.fillMaxWidth(),
                        color = Violet500,
                        trackColor = DividerColor
                    )
                    Spacer(Modifier.height(4.dp))
                    Text("$progress / $selectedCount", style = MaterialTheme.typography.labelMedium, color = Violet400)
                }
            }

            if (errorMsg.isNotEmpty()) {
                Text(errorMsg, style = MaterialTheme.typography.bodySmall, color = ColorError)
            }

            Spacer(Modifier.weight(1f))

            GradientButton(
                text = if (isRunning) "Running…" else "Start $selectedCount Runs",
                enabled = !isRunning,
                onClick = {
                    isRunning = true
                    progress = 0
                    scope.launch {
                        try {
                            val ballCount = vm.simulation?.ballCount ?: 20
                            vm.runMultipleSims(selectedCount, ballCount)
                            isRunning = false
                            onResults()
                        } catch (e: Exception) {
                            isRunning = false
                            errorMsg = "Error: ${e.message}"
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun RunCountCard(
    count: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(
                if (isSelected) Violet700.copy(0.2f) else SurfaceLight,
                RoundedCornerShape(14.dp)
            )
            .border(
                1.5.dp,
                if (isSelected) Violet500 else DividerColor,
                RoundedCornerShape(14.dp)
            )
            .clickable(onClick = onClick)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            count.toString(),
            style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Bold),
            color = if (isSelected) Violet300 else TextPrimary
        )
        Text("runs", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
    }
}

@Composable
fun AverageResultScreen(
    simId: Long,
    onBack: () -> Unit
) {
    val repo: com.flowsim.simfwlos.data.repository.SimulationRepository = koinInject()
    var avgResults by remember { mutableStateOf<List<com.flowsim.simfwlos.data.db.AvgResultRow>>(emptyList()) }
    var simName by remember { mutableStateOf("") }
    var totalRuns by remember { mutableStateOf(0) }

    LaunchedEffect(simId) {
        repo.getById(simId)?.let { simName = it.name }
        avgResults = repo.getAverageResults(simId)
        totalRuns = repo.getMaxRunNumber(simId) ?: 0
    }

    val totalAvg = avgResults.sumOf { it.avgCount }
    val chartEntries = avgResults.map { r ->
        ChartEntry(
            label = r.categoryName,
            value = r.avgCount.toFloat(),
            color = parseColor(r.categoryColorHex)
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().statusBarsPadding(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            TopBarWithBack(title = "Average Results", onBack = onBack)
        }
        item {
            GlassCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(simName, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = TextPrimary)
                        Text("Based on $totalRuns runs", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    }
                    Surface(color = Violet700.copy(0.2f), shape = RoundedCornerShape(8.dp)) {
                        Text("$totalRuns runs", style = MaterialTheme.typography.labelMedium, color = Violet400,
                            modifier = Modifier.padding(8.dp, 4.dp))
                    }
                }
                if (chartEntries.isNotEmpty()) {
                    Spacer(Modifier.height(20.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        PieChart(chartEntries, modifier = Modifier.size(150.dp))
                        Spacer(Modifier.width(20.dp))
                        ChartLegend(chartEntries, modifier = Modifier.weight(1f))
                    }
                }
            }
        }
        if (chartEntries.isNotEmpty()) {
            item {
                GlassCard {
                    Text("Average Distribution", style = MaterialTheme.typography.titleSmall, color = TextSecondary)
                    Spacer(Modifier.height(12.dp))
                    BarChart(chartEntries, modifier = Modifier.fillMaxWidth().height(140.dp))
                }
            }
            items(avgResults) { r ->
                val color = parseColor(r.categoryColorHex)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SurfaceDark, RoundedCornerShape(12.dp))
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.size(10.dp).background(color, CircleShape))
                    Spacer(Modifier.width(12.dp))
                    Text(r.categoryName, style = MaterialTheme.typography.bodyMedium, color = TextPrimary, modifier = Modifier.weight(1f))
                    Text(
                        "avg ${String.format("%.1f", r.avgCount)}",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = color
                    )
                }
            }
        }
        item { Spacer(Modifier.height(16.dp)) }
    }
}
