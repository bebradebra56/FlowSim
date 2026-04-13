package com.flowsim.simfwlos.ui.screens.settings

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.CompareArrows
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.flowsim.simfwlos.ui.components.GlassCard
import com.flowsim.simfwlos.ui.components.SectionHeader
import com.flowsim.simfwlos.ui.navigation.Screen
import com.flowsim.simfwlos.ui.theme.*
import org.koin.androidx.compose.koinViewModel

@Composable
fun SettingsScreen(
    onProfile: () -> Unit,
    onHistory: () -> Unit,
    onActivityLog: () -> Unit,
    onScenarios: () -> Unit,
    navController: NavController
) {
    val vm: SettingsViewModel = koinViewModel()
    val state by vm.state.collectAsState()
    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier.fillMaxSize().statusBarsPadding(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("Settings", style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold), color = TextPrimary)
        }

        item {
            GlassCard {
                SettingsRow(
                    icon = Icons.Rounded.Person,
                    title = "Profile",
                    subtitle = "Name and preferences",
                    color = Violet400,
                    onClick = onProfile
                )
            }
        }

        item {
            SectionHeader("Physics Defaults")
            GlassCard {
                PhysicsSliderSetting(
                    label = "Gravity",
                    value = state.defaultGravity,
                    onValueChange = vm::setGravity,
                    range = 0.1f..1.2f,
                    color = Violet500
                )
                HorizontalDivider(color = DividerColor, modifier = Modifier.padding(vertical = 12.dp))
                PhysicsSliderSetting(
                    label = "Bounce",
                    value = state.defaultBounce,
                    onValueChange = vm::setBounce,
                    range = 0.1f..1.0f,
                    color = BallBlue
                )
                HorizontalDivider(color = DividerColor, modifier = Modifier.padding(vertical = 12.dp))
                PhysicsSliderSetting(
                    label = "Spread",
                    value = state.defaultSpread,
                    onValueChange = vm::setSpread,
                    range = 0.1f..1.0f,
                    color = BallPink
                )
                HorizontalDivider(color = DividerColor, modifier = Modifier.padding(vertical = 12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Default Ball Count", style = MaterialTheme.typography.titleSmall, color = TextPrimary)
                        Text("${state.defaultBallCount} balls", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { if (state.defaultBallCount > 5) vm.setBallCount(state.defaultBallCount - 5) }) {
                            Icon(Icons.Rounded.Remove, null, tint = Violet400)
                        }
                        Text(state.defaultBallCount.toString(), style = MaterialTheme.typography.titleSmall, color = TextPrimary)
                        IconButton(onClick = { if (state.defaultBallCount < 200) vm.setBallCount(state.defaultBallCount + 5) }) {
                            Icon(Icons.Rounded.Add, null, tint = Violet400)
                        }
                    }
                }
            }
        }

        item {
            SectionHeader("Animation")
            GlassCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.Animation, null, tint = BallTeal, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Show Animations", style = MaterialTheme.typography.titleSmall, color = TextPrimary)
                        Text("Enable ball physics animations", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                    }
                    Switch(
                        checked = state.showAnimations,
                        onCheckedChange = vm::setShowAnimations,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = TextPrimary,
                            checkedTrackColor = Violet500,
                            uncheckedThumbColor = TextInactive,
                            uncheckedTrackColor = DividerColor
                        )
                    )
                }
            }
        }

        item {
            SectionHeader("Data")
            GlassCard {
                SettingsRow(Icons.Rounded.History, "Simulation History", "View all past simulations", BallBlue, onHistory)
                HorizontalDivider(color = DividerColor, modifier = Modifier.padding(vertical = 8.dp))
                SettingsRow(Icons.Rounded.Timeline, "Activity Log", "Your recent actions", BallTeal, onActivityLog)
                HorizontalDivider(color = DividerColor, modifier = Modifier.padding(vertical = 8.dp))
                SettingsRow(Icons.AutoMirrored.Rounded.CompareArrows, "Scenarios", "Manage scenarios", Violet400, onScenarios)
                HorizontalDivider(color = DividerColor, modifier = Modifier.padding(vertical = 8.dp))
                SettingsRow(Icons.Rounded.Bookmark, "Saved Results", "Bookmarked simulations", ColorWarning) {
                    navController.navigate(Screen.SavedResults.route)
                }
                HorizontalDivider(color = DividerColor, modifier = Modifier.padding(vertical = 8.dp))
                SettingsRow(Icons.Rounded.CheckCircle, "Tasks", "Manage your tasks", BallPink) {
                    navController.navigate(Screen.Tasks.route)
                }
            }
        }

        item {
            SectionHeader("About")
            GlassCard {
                SettingsRow(Icons.Rounded.Policy, "Privacy Policy", "Tap to read", BallBlue, {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://fllowsim.com/privacy-policy.html"))
                    context.startActivity(intent)
                })
            }
        }

        item {
            GlassCard {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text("Flow Sim v1.0", style = MaterialTheme.typography.labelMedium, color = TextInactive)
                    Text("Physics-based decision simulation", style = MaterialTheme.typography.labelSmall, color = TextInactive)
                }
            }
        }

        item { Spacer(Modifier.height(16.dp)) }
    }
}

@Composable
private fun SettingsRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    color: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(36.dp).background(color.copy(alpha = 0.12f), RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = color, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleSmall, color = TextPrimary)
            Text(subtitle, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
        }
        Icon(Icons.Rounded.ChevronRight, null, tint = TextInactive, modifier = Modifier.size(18.dp))
    }
}

@Composable
private fun PhysicsSliderSetting(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    range: ClosedFloatingPointRange<Float>,
    color: Color
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(label, style = MaterialTheme.typography.titleSmall, color = TextPrimary, modifier = Modifier.width(70.dp))
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = range,
            modifier = Modifier.weight(1f),
            colors = SliderDefaults.colors(thumbColor = color, activeTrackColor = color, inactiveTrackColor = DividerColor)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            String.format("%.2f", value),
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            color = color,
            modifier = Modifier.width(36.dp)
        )
    }
}
