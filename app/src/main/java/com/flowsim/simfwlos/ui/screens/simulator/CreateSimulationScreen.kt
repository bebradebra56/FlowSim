package com.flowsim.simfwlos.ui.screens.simulator

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.flowsim.simfwlos.data.db.SimulationEntity
import com.flowsim.simfwlos.data.repository.SimulationRepository
import com.flowsim.simfwlos.ui.components.*
import com.flowsim.simfwlos.ui.theme.*
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Composable
fun CreateSimulationScreen(
    projectId: Long = 0L,
    onCreated: (Long) -> Unit,
    onBack: () -> Unit
) {
    val repo: SimulationRepository = koinInject()
    val scope = rememberCoroutineScope()

    var name by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("CUSTOM") }
    var ballCount by remember { mutableStateOf(20) }
    var isLoading by remember { mutableStateOf(false) }
    var nameError by remember { mutableStateOf(false) }

    val types = listOf("TASKS" to BallBlue, "BUDGET" to ColorWarning, "TIME" to BallTeal, "CUSTOM" to Violet400)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
    ) {
        TopBarWithBack(title = "New Simulation", onBack = onBack)

        Column(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            GlassCard {
                Text("Simulation Name", style = MaterialTheme.typography.titleSmall, color = TextSecondary)
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it; nameError = false },
                    placeholder = { Text("e.g. Weekly Budget Split", color = TextInactive) },
                    isError = nameError,
                    supportingText = if (nameError) {{ Text("Name is required", color = ColorError) }} else null,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = Violet500,
                        unfocusedBorderColor = DividerColor,
                        cursorColor = Violet400,
                        focusedContainerColor = SurfaceLight,
                        unfocusedContainerColor = SurfaceLight
                    ),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    leadingIcon = {
                        Icon(Icons.Rounded.Science, null, tint = Violet400)
                    }
                )
            }

            GlassCard {
                Text("Simulation Type", style = MaterialTheme.typography.titleSmall, color = TextSecondary)
                Spacer(Modifier.height(12.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    types.chunked(2).forEach { row ->
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            row.forEach { (t, color) ->
                                TypeSelectCard(
                                    label = t,
                                    color = color,
                                    isSelected = type == t,
                                    onClick = { type = t },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            if (row.size < 2) Spacer(Modifier.weight(1f))
                        }
                    }
                }
            }

            GlassCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Ball Count", style = MaterialTheme.typography.titleSmall, color = TextSecondary)
                        Text(
                            "$ballCount balls",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimary
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { if (ballCount > 5) ballCount -= 5 }) {
                            Icon(Icons.Rounded.Remove, null, tint = Violet400)
                        }
                        Text(
                            ballCount.toString(),
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                        IconButton(onClick = { if (ballCount < 200) ballCount += 5 }) {
                            Icon(Icons.Rounded.Add, null, tint = Violet400)
                        }
                    }
                }
                Slider(
                    value = ballCount.toFloat(),
                    onValueChange = { ballCount = it.toInt() },
                    valueRange = 5f..200f,
                    steps = 38,
                    colors = SliderDefaults.colors(
                        thumbColor = Violet400,
                        activeTrackColor = Violet500,
                        inactiveTrackColor = DividerColor
                    )
                )
            }

            GradientButton(
                text = if (isLoading) "Creating…" else "Create Simulation",
                enabled = !isLoading,
                onClick = {
                    if (name.isBlank()) { nameError = true; return@GradientButton }
                    isLoading = true
                    scope.launch {
                        val id = repo.insert(
                            SimulationEntity(
                                name = name.trim(),
                                type = type,
                                ballCount = ballCount,
                                projectId = if (projectId > 0L) projectId else null
                            )
                        )
                        repo.log("SIMULATION_CREATED", "Created simulation '$name'", "simulation", id)
                        isLoading = false
                        onCreated(id)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun TypeSelectCard(
    label: String,
    color: Color,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val icon = when (label) {
        "TASKS" -> Icons.Rounded.CheckCircle
        "BUDGET" -> Icons.Rounded.AccountBalanceWallet
        "TIME" -> Icons.Rounded.Schedule
        else -> Icons.Rounded.Tune
    }
    Box(
        modifier = modifier
            .background(
                if (isSelected) color.copy(0.15f) else SurfaceLight,
                RoundedCornerShape(12.dp)
            )
            .border(
                width = if (isSelected) 1.5.dp else 1.dp,
                color = if (isSelected) color else DividerColor,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick)
            .padding(12.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Icon(icon, null, tint = if (isSelected) color else TextInactive, modifier = Modifier.size(22.dp))
            Spacer(Modifier.height(4.dp))
            Text(
                text = label.lowercase().replaceFirstChar { it.uppercaseChar() },
                style = MaterialTheme.typography.labelMedium,
                color = if (isSelected) color else TextSecondary
            )
        }
    }
}
