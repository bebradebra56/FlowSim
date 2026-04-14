package com.flowsim.flosasms.ui.screens.onboarding

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.flowsim.flosasms.data.AppPreferences
import com.flowsim.flosasms.ui.components.GradientButton
import com.flowsim.flosasms.ui.components.OutlineButton
import com.flowsim.flosasms.ui.theme.*
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import kotlin.math.sin

private data class OnboardingPage(
    val title: String,
    val subtitle: String,
    val accentColor: Color
)

private val pages = listOf(
    OnboardingPage(
        title = "Drop and Simulate",
        subtitle = "Watch violet spheres fall through pins to distribute your decisions across categories",
        accentColor = Violet500
    ),
    OnboardingPage(
        title = "Distribute Resources Visually",
        subtitle = "Tasks, budget, and time — see how they split using realistic physics simulation",
        accentColor = BallBlue
    ),
    OnboardingPage(
        title = "See Multiple Outcomes",
        subtitle = "Run hundreds of simulations to discover probability distributions and smart insights",
        accentColor = BallPink
    )
)

@Composable
fun OnboardingScreen(onFinished: () -> Unit) {
    val prefs: AppPreferences = koinInject()
    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(BgDarkest, BgLight)))
    ) {
        HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
            OnboardingPageContent(page = pages[page])
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                repeat(pages.size) { i ->
                    val isSelected = i == pagerState.currentPage
                    Box(
                        modifier = Modifier
                            .background(
                                color = if (isSelected) Violet400 else DividerColor,
                                shape = CircleShape
                            )
                            .size(if (isSelected) 24.dp else 8.dp, 8.dp)
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            if (pagerState.currentPage < pages.size - 1) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlineButton(
                        text = "Skip",
                        onClick = {
                            scope.launch {
                                prefs.setOnboardingCompleted()
                                onFinished()
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                    GradientButton(
                        text = "Next",
                        onClick = {
                            scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            } else {
                GradientButton(
                    text = "Start Simulating",
                    onClick = {
                        scope.launch {
                            prefs.setOnboardingCompleted()
                            onFinished()
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun OnboardingPageContent(page: OnboardingPage) {
    val infinite = rememberInfiniteTransition(label = "anim")
    val float by infinite.animateFloat(
        initialValue = 0f, targetValue = 2f * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(tween(4000, easing = LinearEasing)),
        label = "float"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cx = size.width / 2f
            val cy = size.height * 0.40f
            val baseR = 70.dp.toPx()
            val offsetY = sin(float) * 12.dp.toPx()

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(page.accentColor.copy(0.35f), Color.Transparent),
                    center = Offset(cx, cy + offsetY), radius = baseR * 2f
                ),
                radius = baseR * 2f, center = Offset(cx, cy + offsetY)
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(page.accentColor.copy(0.8f), page.accentColor, BallPurpleShadow),
                    center = Offset(cx - 18f, cy + offsetY - 18f), radius = baseR
                ),
                radius = baseR, center = Offset(cx, cy + offsetY)
            )

            // Mini pins
            val pins = listOf(-90f to -55f, 90f to -55f, -50f to 10f, 50f to 10f, 0f to 60f)
            pins.forEach { (dx, dy) ->
                drawCircle(color = PinNormal, radius = 4.dp.toPx(), center = Offset(cx + dx, cy + dy))
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(horizontal = 32.dp)
                .padding(bottom = 160.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = page.title,
                style = MaterialTheme.typography.displaySmall,
                color = TextPrimary,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = page.subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )
        }
    }
}
