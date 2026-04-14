package com.flowsim.flosasms.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.flowsim.flosasms.ui.theme.*

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .background(
                color = SurfaceDark.copy(alpha = 0.85f),
                shape = RoundedCornerShape(20.dp)
            )
            .border(
                width = 1.dp,
                color = DividerColor,
                shape = RoundedCornerShape(20.dp)
            )
            .padding(16.dp),
        content = content
    )
}

@Composable
fun GradientButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(52.dp),
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            disabledContainerColor = Color.Transparent
        ),
        contentPadding = PaddingValues(0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = if (enabled)
                        Brush.horizontalGradient(colors = listOf(Violet700, Violet500))
                    else
                        Brush.horizontalGradient(colors = listOf(TextInactive, TextInactive)),
                    shape = RoundedCornerShape(16.dp)
                )
                .clip(RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                color = if (enabled) TextPrimary else TextInactive
            )
        }
    }
}

@Composable
fun OutlineButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(52.dp),
        border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(width = 1.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = GlassEffect,
            contentColor = TextPrimary
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Text(text = text, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
fun TopBarWithBack(
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    actions: @Composable RowScope.() -> Unit = {}
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.Rounded.ArrowBackIosNew,
                contentDescription = "Back",
                tint = TextPrimary
            )
        }
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = TextPrimary,
            modifier = Modifier.weight(1f).padding(start = 4.dp)
        )
        actions()
    }
}

@Composable
fun SectionHeader(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.headlineMedium,
        color = TextPrimary,
        modifier = modifier.padding(vertical = 8.dp)
    )
}

@Composable
fun ColorDot(colorHex: String, size: Dp = 12.dp, modifier: Modifier = Modifier) {
    val color = runCatching { Color(android.graphics.Color.parseColor(colorHex)) }.getOrDefault(Violet500)
    Box(
        modifier = modifier
            .size(size)
            .background(color = color, shape = CircleShape)
    )
}

@Composable
fun EmptyState(
    title: String,
    subtitle: String = "",
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "✦", style = MaterialTheme.typography.displaySmall, color = Violet500)
        Spacer(Modifier.height(12.dp))
        Text(text = title, style = MaterialTheme.typography.titleMedium, color = TextSecondary, textAlign = TextAlign.Center)
        if (subtitle.isNotEmpty()) {
            Spacer(Modifier.height(4.dp))
            Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = TextInactive, textAlign = TextAlign.Center)
        }
    }
}

@Composable
fun PriorityChip(priority: String) {
    val (color, label) = when (priority.uppercase()) {
        "HIGH" -> ColorError to "High"
        "LOW" -> ColorSuccess to "Low"
        else -> ColorWarning to "Medium"
    }
    Surface(
        color = color.copy(alpha = 0.15f),
        shape = RoundedCornerShape(6.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}

@Composable
fun TypeChip(type: String) {
    val (color, label) = when (type.uppercase()) {
        "INCOME" -> ColorSuccess to "Income"
        "EXPENSE" -> ColorError to "Expense"
        "TASKS" -> BallBlue to "Tasks"
        "BUDGET" -> ColorWarning to "Budget"
        "TIME" -> BallTeal to "Time"
        else -> Violet400 to type.replaceFirstChar { it.uppercaseChar() }
    }
    Surface(
        color = color.copy(alpha = 0.15f),
        shape = RoundedCornerShape(6.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}
