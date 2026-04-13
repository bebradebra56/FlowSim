package com.flowsim.simfwlos.ui.screens.splash

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.flowsim.simfwlos.data.AppPreferences
import com.flowsim.simfwlos.ui.theme.*
import kotlinx.coroutines.delay
import org.koin.compose.koinInject
import kotlin.math.sin

@Composable
fun SplashScreen(
    onNavigateToOnboarding: () -> Unit,
    onNavigateToDashboard: () -> Unit
) {
    val prefs: AppPreferences = koinInject()
    val isOnboarded by prefs.isOnboardingCompleted.collectAsState(initial = null)

    val ballY = remember { Animatable(-0.1f) }
    val textAlpha = remember { Animatable(0f) }
    val glowAlpha = remember { Animatable(0f) }

    LaunchedEffect(isOnboarded) {
        if (isOnboarded == null) return@LaunchedEffect
        glowAlpha.animateTo(1f, tween(400))
        ballY.animateTo(
            targetValue = 0.55f,
            animationSpec = spring(dampingRatio = 0.45f, stiffness = 60f)
        )
        delay(200)
        textAlpha.animateTo(1f, tween(500))
        delay(1600)
        if (isOnboarded == true) onNavigateToDashboard() else onNavigateToOnboarding()
    }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 1.08f,
        animationSpec = infiniteRepeatable(tween(1200, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "pulse"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF0F0A1F), Color(0xFF2A0F4F)))),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize().alpha(glowAlpha.value)) {
            val pinRows = listOf(3, 4, 5, 4, 3)
            val startY = size.height * 0.25f
            val rowSpacing = size.height * 0.07f
            pinRows.forEachIndexed { row, count ->
                val y = startY + row * rowSpacing
                val spacing = size.width * 0.15f
                val startX = size.width / 2f - spacing * (count - 1) / 2f
                for (col in 0 until count) {
                    val x = startX + col * spacing
                    drawCircle(
                        color = PinNormal,
                        radius = 4.dp.toPx(),
                        center = Offset(x, y)
                    )
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(GlowViolet.copy(0.4f), Color.Transparent),
                            center = Offset(x, y),
                            radius = 14.dp.toPx()
                        ),
                        radius = 14.dp.toPx(),
                        center = Offset(x, y)
                    )
                }
            }
        }

        Canvas(modifier = Modifier.fillMaxSize()) {
            val cx = size.width / 2f
            val cy = size.height * ballY.value.coerceIn(0f, 1f)
            val br = 18.dp.toPx() * pulseScale

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(GlowViolet.copy(0.5f), Color.Transparent),
                    center = Offset(cx, cy), radius = br * 3f
                ),
                radius = br * 3f, center = Offset(cx, cy)
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(BallPurpleLight, BallPurpleCenter, BallPurpleShadow),
                    center = Offset(cx - br * 0.25f, cy - br * 0.25f),
                    radius = br
                ),
                radius = br, center = Offset(cx, cy)
            )
        }

        Column(
            modifier = Modifier.alpha(textAlpha.value).padding(top = 160.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Flow Sim",
                style = MaterialTheme.typography.displayLarge.copy(
                    fontWeight = FontWeight.Bold,
                    shadow = Shadow(
                        color = GlowViolet,
                        offset = Offset(0f, 0f),
                        blurRadius = 24f
                    )
                ),
                color = TextPrimary
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Simulate your decisions",
                style = MaterialTheme.typography.bodyLarge,
                color = Violet400
            )
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 48.dp)
                .alpha(textAlpha.value)
        ) {
            val infiniteT = rememberInfiniteTransition(label = "dots")
            val dotOffset by infiniteT.animateFloat(
                initialValue = 0f, targetValue = 1f,
                animationSpec = infiniteRepeatable(tween(1500)),
                label = "dots"
            )
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                repeat(3) { i ->
                    val alpha = ((sin(dotOffset * Math.PI * 2 - i * 1.0) + 1) / 2).toFloat()
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(Violet400.copy(alpha = alpha), shape = androidx.compose.foundation.shape.CircleShape)
                    )
                }
            }
        }
    }
}
