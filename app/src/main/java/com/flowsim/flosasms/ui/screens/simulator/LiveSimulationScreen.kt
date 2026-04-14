package com.flowsim.flosasms.ui.screens.simulator

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.flowsim.flosasms.ui.components.parseColor
import com.flowsim.flosasms.ui.theme.*
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun LiveSimulationScreen(
    simId: Long,
    onResults: () -> Unit,
    onBack: () -> Unit
) {
    val vm: SimulatorViewModel = koinViewModel(parameters = { parametersOf(simId) })
    var canvasSize by remember { mutableStateOf(IntSize.Zero) }
    var engineInitialized by remember { mutableStateOf(false) }

    // Wait for BOTH simulation AND categories before initialising the engine.
    // If categories are not yet loaded, numBuckets defaults to 2 which produces
    // a completely wrong bucket-width in the physics engine vs the canvas display.
    LaunchedEffect(canvasSize, vm.simulation, vm.categories) {
        if (canvasSize != IntSize.Zero &&
            vm.simulation != null &&
            vm.categories.isNotEmpty() &&
            !engineInitialized
        ) {
            vm.initEngine(canvasSize.width.toFloat(), canvasSize.height.toFloat())
            engineInitialized = true
        }
    }

    LaunchedEffect(vm.isCompleted) {
        if (vm.isCompleted) {
            kotlinx.coroutines.delay(800)
            onResults()
        }
    }

    val results = vm.results
    val categories = vm.categories

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(BgDarkest, BgMain)))
    ) {
        // Top bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { vm.stopSimulation(); onBack() }) {
                Icon(Icons.Rounded.Close, "Stop", tint = TextPrimary)
            }
            Text(
                vm.simulation?.name ?: "Simulation",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = TextPrimary, modifier = Modifier.weight(1f)
            )
            Text(
                "${vm.launchedCount}/${vm.simulation?.ballCount ?: 0}",
                style = MaterialTheme.typography.labelLarge, color = Violet400
            )
        }

        // Physics board
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
                .background(ChartBg)
                .onSizeChanged { canvasSize = it }
        ) {
            if (engineInitialized) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val balls = vm.ballStates
                    val pins = vm.pins
                    val numBuckets = categories.size.coerceAtLeast(2)
                    val bw = size.width / numBuckets

                    // Bucket dividers
                    for (i in 1 until numBuckets) {
                        drawLine(
                            color = DividerColor,
                            start = Offset(i * bw, size.height * 0.85f),
                            end = Offset(i * bw, size.height),
                            strokeWidth = 1.5f
                        )
                    }

                    // Bucket labels background
                    drawRect(
                        color = SurfaceLight.copy(0.6f),
                        topLeft = Offset(0f, size.height - 36.dp.toPx()),
                        size = Size(size.width, 36.dp.toPx())
                    )

                    // Pins
                    pins.forEach { pin ->
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(GlowViolet.copy(0.3f), Color.Transparent),
                                center = Offset(pin.x, pin.y), radius = 10.dp.toPx()
                            ),
                            radius = 10.dp.toPx(), center = Offset(pin.x, pin.y)
                        )
                        drawCircle(color = PinNormal, radius = 4.dp.toPx(), center = Offset(pin.x, pin.y))
                    }

                    // Balls
                    balls.forEach { ball ->
                        if (!ball.active && ball.bucketIndex >= 0) return@forEach
                        val cx = ball.x
                        val cy = ball.y
                        val r = 9.dp.toPx()
                        val ballColor = Color(ball.colorLong)

                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(ballColor.copy(0.4f), Color.Transparent),
                                center = Offset(cx, cy), radius = r * 2.5f
                            ),
                            radius = r * 2.5f, center = Offset(cx, cy)
                        )
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    ballColor.copy(0.9f),
                                    ballColor,
                                    BallPurpleShadow
                                ),
                                center = Offset(cx - r * 0.25f, cy - r * 0.25f), radius = r
                            ),
                            radius = r, center = Offset(cx, cy)
                        )
                    }
                }

                // Category labels
                if (categories.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                            .height(36.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        categories.forEach { cat ->
                            val catColor = parseColor(cat.colorHex)
                            Column(
                                modifier = Modifier.weight(1f),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    cat.name,
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                    color = catColor,
                                    maxLines = 1
                                )
                                val count = results[categories.indexOf(cat)] ?: 0
                                if (count > 0) {
                                    Text(
                                        count.toString(),
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp),
                                        color = TextPrimary
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Violet400)
                }
            }
        }

        // Controls
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            if (vm.simulation != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Progress
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Balls launched: ${vm.launchedCount}/${vm.simulation?.ballCount ?: 0}",
                            style = MaterialTheme.typography.labelMedium,
                            color = TextSecondary
                        )
                        Spacer(Modifier.height(4.dp))
                        LinearProgressIndicator(
                            progress = { (vm.launchedCount.toFloat() / (vm.simulation?.ballCount ?: 1).toFloat()) },
                            modifier = Modifier.fillMaxWidth(),
                            color = Violet500,
                            trackColor = DividerColor
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (!vm.isRunning && !vm.isCompleted && engineInitialized) {
                        Button(
                            onClick = { vm.launchBall() },
                            colors = ButtonDefaults.buttonColors(containerColor = SurfaceLight),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Rounded.ArrowDownward, null, tint = Violet400, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("One Ball", color = TextPrimary)
                        }
                        Button(
                            onClick = { vm.startAutoLaunch() },
                            colors = ButtonDefaults.buttonColors(containerColor = Violet700),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Rounded.PlayArrow, null, tint = TextPrimary, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Auto Launch", color = TextPrimary)
                        }
                    } else if (vm.isRunning) {
                        Button(
                            onClick = { vm.stopSimulation() },
                            colors = ButtonDefaults.buttonColors(containerColor = ColorError.copy(0.8f)),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Rounded.Stop, null, tint = TextPrimary, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Stop", color = TextPrimary)
                        }
                    } else if (vm.isCompleted) {
                        Button(
                            onClick = onResults,
                            colors = ButtonDefaults.buttonColors(containerColor = Violet700),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Rounded.BarChart, null, tint = TextPrimary, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("View Results", color = TextPrimary)
                        }
                    }
                }
            }
        }
    }
}
