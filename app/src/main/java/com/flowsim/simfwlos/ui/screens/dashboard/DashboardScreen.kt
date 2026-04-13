package com.flowsim.simfwlos.ui.screens.dashboard

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.flowsim.simfwlos.ui.components.*
import com.flowsim.simfwlos.ui.navigation.Screen
import com.flowsim.simfwlos.ui.theme.*
import org.koin.androidx.compose.koinViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun DashboardScreen(navController: NavController) {
    val vm: DashboardViewModel = koinViewModel()
    val state by vm.state.collectAsState()

    val infinite = rememberInfiniteTransition(label = "glow")
    val glowAlpha by infinite.animateFloat(
        0.4f, 0.8f,
        infiniteRepeatable(tween(2000), RepeatMode.Reverse),
        label = "glow"
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize().statusBarsPadding(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    val greeting = when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
                        in 5..11 -> "Good morning"
                        in 12..17 -> "Good afternoon"
                        else -> "Good evening"
                    }
                    Text(text = greeting, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                    Text(
                        text = if (state.userName.isNotEmpty()) state.userName else "Simulator",
                        style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimary
                    )
                }
                IconButton(onClick = { navController.navigate(Screen.Profile.route) }) {
                    Icon(Icons.Rounded.Person, contentDescription = "Profile", tint = Violet400)
                }
            }
        }

        // Stats row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    label = "Simulations",
                    value = state.completedSimulationsCount.toString(),
                    icon = Icons.Rounded.PlayCircle,
                    color = Violet500,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    label = "Projects",
                    value = state.projects.size.toString(),
                    icon = Icons.Rounded.Folder,
                    color = BallBlue,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    label = "Tasks",
                    value = state.pendingTasksCount.toString(),
                    icon = Icons.Rounded.CheckCircle,
                    color = BallTeal,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Quick Start
        item {
            GlassCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Quick Start", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold), color = TextPrimary)
                        Spacer(Modifier.height(4.dp))
                        Text("Run a new simulation", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    }
                    GradientButton(
                        text = "New Sim",
                        onClick = { navController.navigate(Screen.CreateSimulation.route()) },
                        modifier = Modifier.width(110.dp)
                    )
                }

                Spacer(Modifier.height(12.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    QuickTypeChip("Tasks", BallBlue) { navController.navigate(Screen.Tasks.route) }
                    QuickTypeChip("Budget", ColorWarning) { navController.navigate(Screen.Budget.route) }
                    QuickTypeChip("Analytics", BallPink) { navController.navigate(Screen.Analytics.route) }
                    QuickTypeChip("Scenarios", BallTeal) { navController.navigate(Screen.Scenarios.route) }
                }
            }
        }

        // Recent Simulations
        if (state.recentSimulations.isNotEmpty()) {
            item { SectionHeader("Recent Simulations") }
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(state.recentSimulations) { sim ->
                        SimulationCard(
                            sim = sim,
                            onClick = { navController.navigate(Screen.ResultDistribution.route(sim.id)) }
                        )
                    }
                }
            }
        } else {
            item {
                EmptyState(
                    title = "No simulations yet",
                    subtitle = "Create your first simulation to get started"
                )
            }
        }

        // Projects
        if (state.projects.isNotEmpty()) {
            item { SectionHeader("Projects") }
            items(state.projects) { project ->
                ProjectRow(
                    project = project,
                    onClick = { navController.navigate(Screen.ProjectDetails.route(project.id)) }
                )
            }
        }

        item { Spacer(Modifier.height(16.dp)) }
    }
}

@Composable
private fun StatCard(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(SurfaceDark, RoundedCornerShape(16.dp))
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(color.copy(0.15f), RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.height(6.dp))
        Text(value, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = TextPrimary)
        Text(label, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
    }
}

@Composable
private fun SimulationCard(
    sim: com.flowsim.simfwlos.data.db.SimulationEntity,
    onClick: () -> Unit
) {
    val fmt = SimpleDateFormat("MMM d", Locale.ENGLISH)
    Column(
        modifier = Modifier
            .width(160.dp)
            .background(SurfaceLight, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(14.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(Violet700.copy(0.2f), RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Rounded.Science, null, tint = Violet400, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.height(10.dp))
        Text(sim.name, style = MaterialTheme.typography.titleSmall, color = TextPrimary, maxLines = 1)
        Spacer(Modifier.height(4.dp))
        TypeChip(sim.type)
        Spacer(Modifier.height(6.dp))
        Text(
            fmt.format(Date(sim.createdAt)),
            style = MaterialTheme.typography.labelSmall,
            color = TextInactive
        )
    }
}

@Composable
private fun ProjectRow(
    project: com.flowsim.simfwlos.data.db.ProjectEntity,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(SurfaceDark, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ColorDot(project.colorHex, size = 16.dp)
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(project.name, style = MaterialTheme.typography.titleSmall, color = TextPrimary)
            if (project.description.isNotEmpty())
                Text(project.description, style = MaterialTheme.typography.bodySmall, color = TextSecondary, maxLines = 1)
        }
        Icon(Icons.Rounded.ChevronRight, null, tint = TextInactive, modifier = Modifier.size(20.dp))
    }
}

@Composable
private fun QuickTypeChip(
    label: String,
    color: Color,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        color = color.copy(0.12f),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        )
    }
}
