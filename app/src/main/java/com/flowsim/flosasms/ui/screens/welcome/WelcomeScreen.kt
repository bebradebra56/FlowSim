package com.flowsim.flosasms.ui.screens.welcome

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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.flowsim.flosasms.ui.components.GradientButton
import com.flowsim.flosasms.ui.components.OutlineButton
import com.flowsim.flosasms.ui.theme.*
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun WelcomeScreen(
    onStart: () -> Unit,
    onLogIn: () -> Unit
) {
    val contentAlpha = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        delay(100)
        contentAlpha.animateTo(1f, tween(600))
    }

    val infiniteTransition = rememberInfiniteTransition(label = "orbs")
    val orbAngle by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(12000, easing = LinearEasing)),
        label = "orb"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(BgDarkest, BgLight))),
        contentAlignment = Alignment.Center
    ) {
        // Floating orbs decoration
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cx = size.width / 2f
            val cy = size.height * 0.38f
            val orbs = listOf(
                Triple(0.35f, BallPurpleCenter, 0f),
                Triple(0.28f, BallBlue, 120f),
                Triple(0.22f, BallPink, 240f)
            )
            orbs.forEach { (dist, color, offset) ->
                val angle = Math.toRadians((orbAngle + offset).toDouble())
                val ox = cx + cos(angle).toFloat() * size.width * dist
                val oy = cy + sin(angle).toFloat() * size.height * 0.18f
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(color.copy(0.25f), Color.Transparent),
                        center = Offset(ox.toFloat(), oy.toFloat()),
                        radius = 80.dp.toPx()
                    ),
                    radius = 80.dp.toPx(),
                    center = Offset(ox.toFloat(), oy.toFloat())
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .alpha(contentAlpha.value)
                .padding(horizontal = 32.dp)
                .statusBarsPadding()
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.weight(0.15f))

            // Logo
            Canvas(modifier = Modifier.size(90.dp)) {
                val c = center
                val r = size.minDimension / 2f
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(GlowViolet, Color.Transparent),
                        center = c, radius = r * 1.5f
                    ),
                    radius = r * 1.5f, center = c
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(BallPurpleLight, BallPurpleCenter, BallPurpleShadow),
                        center = Offset(c.x - r * 0.2f, c.y - r * 0.2f), radius = r
                    ),
                    radius = r, center = c
                )
            }

            Spacer(Modifier.height(28.dp))

            Text(
                text = "Welcome to",
                style = MaterialTheme.typography.bodyLarge,
                color = Violet400
            )
            Text(
                text = "Flow Sim",
                style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Bold),
                color = TextPrimary
            )

            Spacer(Modifier.height(16.dp))

            Text(
                text = "Distribute your time, tasks and budget\nthrough falling violet spheres",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.weight(0.5f))

            GradientButton(
                text = "Get Started",
                onClick = onStart,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))

            OutlineButton(
                text = "I Already Have an Account",
                onClick = onLogIn,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(24.dp))

            Text(
                text = "By continuing you agree to our Terms of Service",
                style = MaterialTheme.typography.labelSmall,
                color = TextInactive,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(16.dp))
        }
    }
}
