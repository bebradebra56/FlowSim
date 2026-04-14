package com.flowsim.flosasms.ui.screens.history

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.flowsim.flosasms.data.db.ActivityLogEntity
import com.flowsim.flosasms.data.db.SimulationEntity
import com.flowsim.flosasms.ui.components.*
import com.flowsim.flosasms.ui.theme.*
import org.koin.androidx.compose.koinViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HistoryScreen(
    onSimulationClick: (Long) -> Unit,
    onBack: () -> Unit
) {
    val vm: HistoryViewModel = koinViewModel()
    val sims by vm.allSimulations.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize().statusBarsPadding(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item { TopBarWithBack(title = "History", onBack = onBack) }

        if (sims.isEmpty()) {
            item { EmptyState("No simulation history", "Complete simulations to see them here") }
        } else {
            items(sims) { sim ->
                HistorySimItem(
                    sim = sim,
                    onClick = { onSimulationClick(sim.id) },
                    onSave = { vm.toggleSave(sim) },
                    onDelete = { vm.deleteSimulation(sim) }
                )
            }
        }
        item { Spacer(Modifier.height(16.dp)) }
    }
}

@Composable
fun SavedResultsScreen(
    onSimulationClick: (Long) -> Unit,
    onBack: () -> Unit
) {
    val vm: HistoryViewModel = koinViewModel()
    val sims by vm.savedSimulations.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize().statusBarsPadding(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item { TopBarWithBack(title = "Saved Results", onBack = onBack) }

        if (sims.isEmpty()) {
            item { EmptyState("No saved results", "Bookmark simulations to save them here") }
        } else {
            items(sims) { sim ->
                HistorySimItem(
                    sim = sim,
                    onClick = { onSimulationClick(sim.id) },
                    onSave = { vm.toggleSave(sim) },
                    onDelete = { vm.deleteSimulation(sim) }
                )
            }
        }
        item { Spacer(Modifier.height(16.dp)) }
    }
}

@Composable
fun ActivityHistoryScreen(onBack: () -> Unit) {
    val vm: HistoryViewModel = koinViewModel()
    val activity by vm.activityLog.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize().statusBarsPadding(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item { TopBarWithBack(title = "Activity History", onBack = onBack) }

        if (activity.isEmpty()) {
            item { EmptyState("No activity yet", "Your actions will appear here") }
        } else {
            items(activity) { log ->
                ActivityLogItem(log = log)
            }
        }
        item { Spacer(Modifier.height(16.dp)) }
    }
}

@Composable
private fun HistorySimItem(
    sim: SimulationEntity,
    onClick: () -> Unit,
    onSave: () -> Unit,
    onDelete: () -> Unit
) {
    val fmt = SimpleDateFormat("MMM d, yyyy · HH:mm", Locale.ENGLISH)
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(SurfaceDark, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(40.dp).background(
                if (sim.isCompleted) Violet700.copy(alpha = 0.2f) else SurfaceLight,
                RoundedCornerShape(10.dp)
            ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                if (sim.isCompleted) Icons.Rounded.CheckCircle else Icons.Rounded.Pending,
                null,
                tint = if (sim.isCompleted) Violet400 else TextInactive,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(sim.name, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Medium), color = TextPrimary)
            Text(
                "${fmt.format(Date(sim.createdAt))} · ${sim.ballCount} balls",
                style = MaterialTheme.typography.labelSmall, color = TextInactive
            )
        }
        TypeChip(sim.type)
        Spacer(Modifier.width(4.dp))
        IconButton(onClick = onSave, modifier = Modifier.size(32.dp)) {
            Icon(
                if (sim.isSaved) Icons.Rounded.Bookmark else Icons.Rounded.BookmarkBorder,
                null, tint = if (sim.isSaved) ColorWarning else TextInactive, modifier = Modifier.size(18.dp)
            )
        }
        IconButton(onClick = { showDeleteConfirm = true }, modifier = Modifier.size(32.dp)) {
            Icon(Icons.Rounded.Delete, null, tint = TextInactive, modifier = Modifier.size(16.dp))
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            containerColor = SurfaceDark,
            title = { Text("Delete", color = TextPrimary) },
            text = { Text("Delete \"${sim.name}\"?", color = TextSecondary) },
            confirmButton = {
                TextButton(onClick = { onDelete(); showDeleteConfirm = false }) {
                    Text("Delete", color = ColorError)
                }
            },
            dismissButton = { TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancel", color = TextSecondary) } }
        )
    }
}

@Composable
private fun ActivityLogItem(log: ActivityLogEntity) {
    val fmt = SimpleDateFormat("MMM d · HH:mm", Locale.ENGLISH)
    val (color, icon) = when {
        log.action.contains("SIMULATION") -> Violet400 to Icons.Rounded.Science
        log.action.contains("TASK") -> BallBlue to Icons.Rounded.CheckCircle
        log.action.contains("BUDGET") -> ColorWarning to Icons.Rounded.AccountBalanceWallet
        log.action.contains("PROJECT") -> BallTeal to Icons.Rounded.Folder
        else -> TextSecondary to Icons.Rounded.Info
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(SurfaceDark, RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(34.dp).background(color.copy(alpha = 0.12f), RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = color, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(log.description, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            Text(fmt.format(Date(log.timestamp)), style = MaterialTheme.typography.labelSmall, color = TextInactive)
        }
    }
}
