package com.flowsim.flosasms.ui.screens.profile

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.flowsim.flosasms.data.repository.SimulationRepository
import com.flowsim.flosasms.data.repository.TaskRepository
import com.flowsim.flosasms.ui.components.GlassCard
import com.flowsim.flosasms.ui.components.GradientButton
import com.flowsim.flosasms.ui.components.TopBarWithBack
import com.flowsim.flosasms.ui.theme.*
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

@Composable
fun ProfileScreen(onBack: () -> Unit) {
    val vm: ProfileViewModel = koinViewModel()
    val simRepo: SimulationRepository = koinInject()
    val taskRepo: TaskRepository = koinInject()

    val userName by vm.userName.collectAsState()
    var editMode by remember { mutableStateOf(false) }
    var nameInput by remember { mutableStateOf("") }
    var totalSims by remember { mutableStateOf(0) }
    var pendingTasks by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        totalSims = simRepo.getCompletedCount()
        pendingTasks = taskRepo.getPendingCount()
    }

    val infinite = rememberInfiniteTransition(label = "avatar")
    val glowAlpha by infinite.animateFloat(
        0.5f, 1f,
        infiniteRepeatable(tween(2000), RepeatMode.Reverse),
        label = "glow"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
    ) {
        TopBarWithBack(title = "Profile", onBack = onBack) {
            IconButton(onClick = {
                if (editMode) { vm.updateName(nameInput); editMode = false }
                else { nameInput = userName; editMode = true }
            }) {
                Icon(
                    if (editMode) Icons.Rounded.Check else Icons.Rounded.Edit,
                    null, tint = Violet400
                )
            }
        }

        Column(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(Modifier.height(8.dp))

            // Avatar
            Box(
                modifier = Modifier.size(100.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(GlowViolet.copy(alpha = glowAlpha * 0.6f), Color.Transparent),
                            center = center, radius = size.minDimension / 2f * 1.4f
                        ),
                        radius = size.minDimension / 2f * 1.4f
                    )
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Violet300, Violet500, BallPurpleShadow),
                            center = Offset(center.x - 12f, center.y - 12f),
                            radius = size.minDimension / 2f
                        ),
                        radius = size.minDimension / 2f
                    )
                }
                Text(
                    text = if (userName.isNotEmpty()) userName.first().uppercaseChar().toString() else "?",
                    style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary
                )
            }

            if (editMode) {
                OutlinedTextField(
                    value = nameInput,
                    onValueChange = { nameInput = it },
                    label = { Text("Your Name") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary,
                        focusedBorderColor = Violet500, unfocusedBorderColor = DividerColor,
                        focusedContainerColor = SurfaceLight, unfocusedContainerColor = SurfaceLight,
                        focusedLabelColor = Violet400, unfocusedLabelColor = TextSecondary
                    ),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Rounded.Person, null, tint = Violet400) }
                )
                GradientButton(
                    text = "Save Changes",
                    onClick = { vm.updateName(nameInput); editMode = false },
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                Text(
                    text = if (userName.isNotEmpty()) userName else "Anonymous Simulator",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary
                )
                Text(
                    "Flow Sim User",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Violet400
                )
            }

            Spacer(Modifier.height(4.dp))

            // Stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ProfileStat("Simulations", totalSims.toString(), Icons.Rounded.Science, Violet500, Modifier.weight(1f))
                ProfileStat("Pending Tasks", pendingTasks.toString(), Icons.Rounded.CheckCircle, BallBlue, Modifier.weight(1f))
            }

            // Info cards
            GlassCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(40.dp).background(Violet700.copy(alpha = 0.2f), RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Rounded.EmojiEvents, null, tint = ColorWarning, modifier = Modifier.size(22.dp))
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text("Achievement", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold), color = TextPrimary)
                        Text(
                            when {
                                totalSims >= 50 -> "Simulation Master 🏆"
                                totalSims >= 20 -> "Experienced Simulator ⭐"
                                totalSims >= 5 -> "Growing Simulator 🌱"
                                else -> "Simulation Beginner 🎯"
                            },
                            style = MaterialTheme.typography.bodySmall, color = TextSecondary
                        )
                    }
                }
            }

            GlassCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Rounded.Info, null, tint = BallBlue, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(10.dp))
                    Text(
                        "Flow Sim uses physics simulation to help you make better decisions about resource distribution",
                        style = MaterialTheme.typography.bodySmall, color = TextSecondary
                    )
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ProfileStat(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(SurfaceDark, RoundedCornerShape(14.dp))
            .padding(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(icon, null, tint = color, modifier = Modifier.size(22.dp))
        Spacer(Modifier.height(6.dp))
        Text(value, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = TextPrimary)
        Text(label, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
    }
}
