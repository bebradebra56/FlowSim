package com.flowsim.simfwlos.ui.screens.simulator

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.flowsim.simfwlos.data.repository.SimulationRepository
import com.flowsim.simfwlos.domain.PhysicsEngine
import com.flowsim.simfwlos.ui.components.*
import com.flowsim.simfwlos.ui.navigation.Screen
import com.flowsim.simfwlos.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.sin

@Composable
fun SimulatorHubScreen(navController: NavController) {
    val repo: SimulationRepository = koinInject()
    var sims by remember { mutableStateOf(listOf<com.flowsim.simfwlos.data.db.SimulationEntity>()) }

    LaunchedEffect(Unit) {
        repo.getAllSimulations().collectLatest { sims = it }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().statusBarsPadding(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Simulator", style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold), color = TextPrimary)
                    Text("Physics-based distribution", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                }
                FloatingActionButton(
                    onClick = { navController.navigate(Screen.CreateSimulation.route()) },
                    containerColor = Violet700,
                    contentColor = TextPrimary,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(Icons.Rounded.Add, "New simulation")
                }
            }
        }

        // Demo plinko preview
        item {
            DemoPlinkoCard()
        }

        // Type cards
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                SimTypeCard("Tasks", Icons.Rounded.CheckCircle, BallBlue, Modifier.weight(1f)) {
                    navController.navigate(Screen.CreateSimulation.route())
                }
                SimTypeCard("Budget", Icons.Rounded.AccountBalanceWallet, ColorWarning, Modifier.weight(1f)) {
                    navController.navigate(Screen.CreateSimulation.route())
                }
            }
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                SimTypeCard("Time", Icons.Rounded.Schedule, BallTeal, Modifier.weight(1f)) {
                    navController.navigate(Screen.CreateSimulation.route())
                }
                SimTypeCard("Custom", Icons.Rounded.Tune, BallPink, Modifier.weight(1f)) {
                    navController.navigate(Screen.CreateSimulation.route())
                }
            }
        }

        if (sims.isNotEmpty()) {
            item { SectionHeader("All Simulations") }
            items(sims) { sim ->
                SimListItem(
                    sim = sim,
                    onClick = {
                        if (sim.isCompleted) navController.navigate(Screen.ResultDistribution.route(sim.id))
                        else navController.navigate(Screen.AddCategories.route(sim.id))
                    }
                )
            }
        }

        item { Spacer(Modifier.height(16.dp)) }
    }
}

@Composable
private fun DemoPlinkoCard() {
    val engine = remember {
        PhysicsEngine(boardWidth = 400f, boardHeight = 200f, numBuckets = 4, gravity = 0.4f, bounce = 0.5f, spread = 0.5f)
    }
    var ballStates by remember { mutableStateOf(engine.balls) }
    var frameCount by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        while (true) {
            if (frameCount % 45 == 0 && engine.activeBallCount < 4) {
                val colors = listOf(0xFF8B5CF6L, 0xFF22D3EEL, 0xFFF472B6L, 0xFFFACC15L)
                engine.launchBall(colors[frameCount / 45 % 4])
            }
            if (engine.getResults().values.sum() > 12) engine.reset()
            engine.tick()
            ballStates = engine.balls
            frameCount++
            delay(16)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .background(SurfaceDark, RoundedCornerShape(20.dp))
            .clip(RoundedCornerShape(20.dp))
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val scaleX = size.width / 400f
            val scaleY = size.height / 200f

            engine.pins.forEach { pin ->
                drawCircle(color = PinNormal, radius = 3.dp.toPx(),
                    center = Offset(pin.x * scaleX, pin.y * scaleY))
            }
            ballStates.filter { it.active }.forEach { ball ->
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(ball.colorLong).copy(0.5f), Color.Transparent),
                        center = Offset(ball.x * scaleX, ball.y * scaleY), radius = 14.dp.toPx()
                    ),
                    radius = 14.dp.toPx(), center = Offset(ball.x * scaleX, ball.y * scaleY)
                )
                drawCircle(
                    color = Color(ball.colorLong),
                    radius = 7.dp.toPx(), center = Offset(ball.x * scaleX, ball.y * scaleY)
                )
            }
        }
        Text(
            "Live Demo", style = MaterialTheme.typography.labelSmall, color = TextInactive,
            modifier = Modifier.align(Alignment.TopStart).padding(10.dp)
        )
    }
}

@Composable
private fun SimTypeCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier
            .background(SurfaceDark, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(color.copy(0.15f), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = color, modifier = Modifier.size(24.dp))
        }
        Spacer(Modifier.height(8.dp))
        Text(title, style = MaterialTheme.typography.titleSmall, color = TextPrimary)
    }
}

@Composable
private fun SimListItem(
    sim: com.flowsim.simfwlos.data.db.SimulationEntity,
    onClick: () -> Unit
) {
    val fmt = SimpleDateFormat("MMM d, yyyy", Locale.ENGLISH)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(SurfaceDark, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(
                    if (sim.isCompleted) Violet700.copy(0.2f) else TextInactive.copy(0.1f),
                    RoundedCornerShape(10.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                if (sim.isCompleted) Icons.Rounded.CheckCircle else Icons.Rounded.PlayCircle,
                null,
                tint = if (sim.isCompleted) Violet400 else TextInactive,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(sim.name, style = MaterialTheme.typography.titleSmall, color = TextPrimary)
            Text(
                "${fmt.format(Date(sim.createdAt))} · ${sim.ballCount} balls",
                style = MaterialTheme.typography.bodySmall, color = TextSecondary
            )
        }
        TypeChip(sim.type)
        Spacer(Modifier.width(6.dp))
        Icon(Icons.Rounded.ChevronRight, null, tint = TextInactive, modifier = Modifier.size(18.dp))
    }
}
