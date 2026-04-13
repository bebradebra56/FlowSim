package com.flowsim.simfwlos.ui.screens.simulator

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.flowsim.simfwlos.data.db.SimulationEntity
import com.flowsim.simfwlos.data.repository.SimulationRepository
import com.flowsim.simfwlos.ui.components.*
import com.flowsim.simfwlos.ui.theme.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Composable
fun PhysicsSettingsScreen(
    simId: Long,
    onStart: () -> Unit,
    onBack: () -> Unit
) {
    val repo: SimulationRepository = koinInject()
    val scope = rememberCoroutineScope()
    val prefs: com.flowsim.simfwlos.data.AppPreferences = koinInject()

    var gravity by remember { mutableStateOf(0.4f) }
    var bounce by remember { mutableStateOf(0.5f) }
    var spread by remember { mutableStateOf(0.5f) }

    LaunchedEffect(simId) {
        val sim = repo.getById(simId)
        if (sim != null) {
            gravity = sim.gravity
            bounce = sim.bounce
            spread = sim.spread
        } else {
            gravity = prefs.defaultGravity.first()
            bounce = prefs.defaultBounce.first()
            spread = prefs.defaultSpread.first()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
    ) {
        TopBarWithBack(title = "Physics Settings", onBack = onBack)

        Column(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            GlassCard {
                Text("Simulation Physics", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold), color = TextPrimary)
                Spacer(Modifier.height(4.dp))
                Text("Adjust how balls behave during simulation", style = MaterialTheme.typography.bodySmall, color = TextSecondary)

                Spacer(Modifier.height(20.dp))

                PhysicsSlider(
                    label = "Gravity",
                    icon = Icons.Rounded.ArrowDownward,
                    value = gravity,
                    onValueChange = { gravity = it },
                    valueRange = 0.1f..1.2f,
                    description = "How fast balls fall",
                    color = Violet500
                )

                Spacer(Modifier.height(16.dp))

                PhysicsSlider(
                    label = "Bounce",
                    icon = Icons.Rounded.SwapVert,
                    value = bounce,
                    onValueChange = { bounce = it },
                    valueRange = 0.1f..1.0f,
                    description = "How much balls bounce off pins",
                    color = BallBlue
                )

                Spacer(Modifier.height(16.dp))

                PhysicsSlider(
                    label = "Spread",
                    icon = Icons.Rounded.SwapHoriz,
                    value = spread,
                    onValueChange = { spread = it },
                    valueRange = 0.1f..1.0f,
                    description = "How wide balls spread sideways",
                    color = BallPink
                )
            }

            GlassCard {
                Text("Presets", style = MaterialTheme.typography.titleSmall, color = TextSecondary)
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    PhysicsPreset("Light", 0.2f, 0.3f, 0.3f, Modifier.weight(1f)) {
                        gravity = 0.2f; bounce = 0.3f; spread = 0.3f
                    }
                    PhysicsPreset("Normal", 0.4f, 0.5f, 0.5f, Modifier.weight(1f)) {
                        gravity = 0.4f; bounce = 0.5f; spread = 0.5f
                    }
                    PhysicsPreset("Chaotic", 0.8f, 0.9f, 0.9f, Modifier.weight(1f)) {
                        gravity = 0.8f; bounce = 0.9f; spread = 0.9f
                    }
                }
            }

            GradientButton(
                text = "Launch Simulation",
                onClick = {
                    scope.launch {
                        val sim = repo.getById(simId)
                        if (sim != null) {
                            repo.update(sim.copy(gravity = gravity, bounce = bounce, spread = spread))
                        }
                        onStart()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun PhysicsSlider(
    label: String,
    icon: ImageVector,
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    description: String,
    color: Color
) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = color, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(label, style = MaterialTheme.typography.titleSmall, color = TextPrimary)
                Text(description, style = MaterialTheme.typography.labelSmall, color = TextInactive)
            }
            Text(
                String.format("%.2f", value),
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = color
            )
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            colors = SliderDefaults.colors(
                thumbColor = color,
                activeTrackColor = color,
                inactiveTrackColor = DividerColor
            )
        )
    }
}

@Composable
private fun PhysicsPreset(
    label: String,
    gravity: Float,
    bounce: Float,
    spread: Float,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        color = SurfaceLight,
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(label, style = MaterialTheme.typography.labelLarge, color = TextPrimary)
            Spacer(Modifier.height(4.dp))
            Text("g:${String.format("%.1f", gravity)}", style = MaterialTheme.typography.labelSmall, color = TextInactive)
        }
    }
}
