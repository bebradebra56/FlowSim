package com.flowsim.flosasms.ui.screens.scenarios

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.flowsim.flosasms.data.db.ScenarioEntity
import com.flowsim.flosasms.data.db.SimulationResultEntity
import com.flowsim.flosasms.data.repository.SimulationRepository
import com.flowsim.flosasms.ui.components.*
import com.flowsim.flosasms.ui.theme.*
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ScenariosScreen(
    onCompare: (Long, Long) -> Unit,
    onBack: () -> Unit
) {
    val vm: ScenariosViewModel = koinViewModel()
    val scenarios by vm.scenarios.collectAsState()
    var showCreateDialog by remember { mutableStateOf(false) }
    var compareMode by remember { mutableStateOf(false) }
    var selectedForCompare by remember { mutableStateOf<List<Long>>(emptyList()) }

    Column(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
        TopBarWithBack(title = "Scenarios", onBack = onBack) {
            if (scenarios.size >= 2) {
                TextButton(onClick = { compareMode = !compareMode; selectedForCompare = emptyList() }) {
                    Text(if (compareMode) "Cancel" else "Compare", color = Violet400)
                }
            }
            IconButton(onClick = { showCreateDialog = true }) {
                Icon(Icons.Rounded.Add, "New", tint = Violet400)
            }
        }

        if (compareMode && selectedForCompare.size == 2) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                GradientButton(
                    "Compare Selected",
                    onClick = { onCompare(selectedForCompare[0], selectedForCompare[1]) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        if (scenarios.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                EmptyState("No scenarios yet", "Create scenarios to compare different simulation results")
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(scenarios) { scenario ->
                    ScenarioCard(
                        scenario = scenario,
                        compareMode = compareMode,
                        isSelectedForCompare = scenario.id in selectedForCompare,
                        onSelect = {
                            if (scenario.id in selectedForCompare) {
                                selectedForCompare = selectedForCompare - scenario.id
                            } else if (selectedForCompare.size < 2) {
                                selectedForCompare = selectedForCompare + scenario.id
                            }
                        },
                        onDelete = { vm.deleteScenario(scenario) }
                    )
                }
                item { Spacer(Modifier.height(16.dp)) }
            }
        }
    }

    if (showCreateDialog) {
        CreateScenarioDialog(
            onDismiss = { showCreateDialog = false },
            onCreate = { name, desc -> vm.createScenario(name, desc, emptyList()); showCreateDialog = false }
        )
    }
}

@Composable
private fun ScenarioCard(
    scenario: ScenarioEntity,
    compareMode: Boolean,
    isSelectedForCompare: Boolean,
    onSelect: () -> Unit,
    onDelete: () -> Unit
) {
    val fmt = SimpleDateFormat("MMM d, yyyy", Locale.ENGLISH)
    val simCount = if (scenario.simulationIds.isNotEmpty())
        scenario.simulationIds.split(",").size else 0

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (isSelectedForCompare) Violet700.copy(0.2f) else SurfaceDark,
                RoundedCornerShape(14.dp)
            )
            .clickable(onClick = if (compareMode) onSelect else ({}))
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (compareMode) {
            Checkbox(
                checked = isSelectedForCompare,
                onCheckedChange = { onSelect() },
                colors = CheckboxDefaults.colors(checkedColor = Violet500, uncheckedColor = DividerColor)
            )
            Spacer(Modifier.width(6.dp))
        } else {
            Box(
                modifier = Modifier.size(40.dp).background(Violet700.copy(0.15f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.AutoMirrored.Rounded.CompareArrows, null, tint = Violet400, modifier = Modifier.size(22.dp))
            }
            Spacer(Modifier.width(12.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(scenario.name, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold), color = TextPrimary)
            if (scenario.description.isNotEmpty()) {
                Text(scenario.description, style = MaterialTheme.typography.bodySmall, color = TextSecondary, maxLines = 1)
            }
            Text(
                "${fmt.format(Date(scenario.createdAt))}${if (simCount > 0) " · $simCount simulations" else ""}",
                style = MaterialTheme.typography.labelSmall, color = TextInactive
            )
        }
        if (!compareMode) {
            IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Rounded.Delete, null, tint = TextInactive, modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
private fun CreateScenarioDialog(
    onDismiss: () -> Unit,
    onCreate: (String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var nameError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceDark,
        title = { Text("New Scenario", color = TextPrimary) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name, onValueChange = { name = it; nameError = false },
                    label = { Text("Scenario Name") }, isError = nameError,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary,
                        focusedBorderColor = Violet500, unfocusedBorderColor = DividerColor,
                        focusedContainerColor = SurfaceLight, unfocusedContainerColor = SurfaceLight,
                        focusedLabelColor = Violet400, unfocusedLabelColor = TextSecondary
                    ), shape = RoundedCornerShape(10.dp)
                )
                OutlinedTextField(
                    value = description, onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary,
                        focusedBorderColor = Violet500, unfocusedBorderColor = DividerColor,
                        focusedContainerColor = SurfaceLight, unfocusedContainerColor = SurfaceLight,
                        focusedLabelColor = Violet400, unfocusedLabelColor = TextSecondary
                    ), shape = RoundedCornerShape(10.dp)
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (name.isBlank()) { nameError = true; return@TextButton }
                onCreate(name.trim(), description.trim())
            }) { Text("Create", color = Violet400) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", color = TextSecondary) } }
    )
}

@Composable
fun ScenarioComparisonScreen(simId1: Long, simId2: Long, onBack: () -> Unit) {
    val repo: SimulationRepository = koinInject()
    var results1 by remember { mutableStateOf<List<SimulationResultEntity>>(emptyList()) }
    var results2 by remember { mutableStateOf<List<SimulationResultEntity>>(emptyList()) }
    var name1 by remember { mutableStateOf("Simulation A") }
    var name2 by remember { mutableStateOf("Simulation B") }

    LaunchedEffect(simId1, simId2) {
        repo.getById(simId1)?.let { name1 = it.name }
        repo.getById(simId2)?.let { name2 = it.name }
        launch {
            repo.getResultsFlow(simId1).collectLatest { r ->
                val maxRun = r.maxOfOrNull { it.runNumber } ?: 1
                results1 = r.filter { it.runNumber == maxRun }
            }
        }
        launch {
            repo.getResultsFlow(simId2).collectLatest { r ->
                val maxRun = r.maxOfOrNull { it.runNumber } ?: 1
                results2 = r.filter { it.runNumber == maxRun }
            }
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().statusBarsPadding(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item { TopBarWithBack(title = "Compare Scenarios", onBack = onBack) }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ScenarioCompareCard(name1, results1, Violet500, Modifier.weight(1f))
                ScenarioCompareCard(name2, results2, BallBlue, Modifier.weight(1f))
            }
        }

        if (results1.isNotEmpty() || results2.isNotEmpty()) {
            item {
                GlassCard {
                    Text("Side-by-Side Distribution", style = MaterialTheme.typography.titleSmall, color = TextSecondary)
                    Spacer(Modifier.height(12.dp))
                    val entries1 = results1.map { ChartEntry(it.categoryName, it.count.toFloat(), parseColor(it.categoryColorHex)) }
                    val entries2 = results2.map { ChartEntry(it.categoryName, it.count.toFloat(), BallBlue) }
                    if (entries1.isNotEmpty()) {
                        Text(name1, style = MaterialTheme.typography.labelSmall, color = Violet400)
                        BarChart(entries1, modifier = Modifier.fillMaxWidth().height(100.dp))
                    }
                    if (entries2.isNotEmpty()) {
                        Spacer(Modifier.height(8.dp))
                        Text(name2, style = MaterialTheme.typography.labelSmall, color = BallBlue)
                        BarChart(entries2, modifier = Modifier.fillMaxWidth().height(100.dp))
                    }
                }
            }
        }

        item { Spacer(Modifier.height(16.dp)) }
    }
}

@Composable
private fun ScenarioCompareCard(
    name: String,
    results: List<SimulationResultEntity>,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    GlassCard(modifier = modifier) {
        Text(name, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = color, maxLines = 1)
        Spacer(Modifier.height(8.dp))
        if (results.isEmpty()) {
            Text("No data", style = MaterialTheme.typography.labelSmall, color = TextInactive)
        } else {
            Text("${results.sumOf { it.count }} balls", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
            Spacer(Modifier.height(6.dp))
            val entries = results.map { ChartEntry(it.categoryName, it.count.toFloat(), parseColor(it.categoryColorHex)) }
            PieChart(entries, modifier = Modifier.fillMaxWidth().height(100.dp))
        }
    }
}
