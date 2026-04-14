package com.flowsim.flosasms.ui.screens.projects

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.flowsim.flosasms.ui.components.*
import com.flowsim.flosasms.ui.theme.*
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ProjectDetailsScreen(
    projectId: Long,
    onSimulationClick: (Long) -> Unit,
    onNewSimulation: () -> Unit,
    onBack: () -> Unit
) {
    val vm: ProjectDetailsViewModel = koinViewModel(parameters = { parametersOf(projectId) })
    val project by vm.project.collectAsState()
    val simulations by vm.simulations.collectAsState()
    val tasks by vm.tasks.collectAsState()
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Simulations", "Tasks")
    var showAddTaskDialog by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
            TopBarWithBack(title = project?.name ?: "Project", onBack = onBack) {
                if (selectedTab == 0) {
                    IconButton(onClick = onNewSimulation) {
                        Icon(Icons.Rounded.Add, "New simulation", tint = Violet400)
                    }
                } else {
                    IconButton(onClick = { showAddTaskDialog = true }) {
                        Icon(Icons.Rounded.Add, "New task", tint = Violet400)
                    }
                }
            }

            project?.let { p ->
                val color = runCatching { Color(android.graphics.Color.parseColor(p.colorHex)) }.getOrDefault(Violet500)
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.size(10.dp).background(color, CircleShape))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        if (p.description.isNotEmpty()) p.description else "No description",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
            }

            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(color = Violet700.copy(0.15f), shape = RoundedCornerShape(10.dp)) {
                    Row(
                        modifier = Modifier.padding(10.dp, 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Rounded.Science, null, tint = Violet400, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("${simulations.size} sims", style = MaterialTheme.typography.labelMedium, color = Violet400)
                    }
                }
                Surface(color = BallBlue.copy(0.15f), shape = RoundedCornerShape(10.dp)) {
                    Row(
                        modifier = Modifier.padding(10.dp, 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Rounded.CheckCircle, null, tint = BallBlue, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("${tasks.size} tasks", style = MaterialTheme.typography.labelMedium, color = BallBlue)
                    }
                }
            }

            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = SurfaceDark,
                contentColor = Violet400,
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                tabs.forEachIndexed { i, title ->
                    Tab(
                        selected = selectedTab == i,
                        onClick = { selectedTab = i },
                        text = { Text(title, color = if (selectedTab == i) Violet400 else TextSecondary) }
                    )
                }
            }

            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                if (selectedTab == 0) {
                    if (simulations.isEmpty()) {
                        item {
                            EmptyState(
                                title = "No simulations",
                                subtitle = "Tap + in the top bar to create a simulation for this project"
                            )
                        }
                    } else {
                        items(simulations) { sim ->
                            SimulationItem(
                                sim = sim,
                                onClick = { onSimulationClick(sim.id) },
                                onDelete = { vm.deleteSimulation(sim) }
                            )
                        }
                    }
                } else {
                    if (tasks.isEmpty()) {
                        item {
                            EmptyState(
                                title = "No tasks",
                                subtitle = "Tap + in the top bar to add a task to this project"
                            )
                        }
                    } else {
                        items(tasks) { task ->
                            TaskItem(
                                task = task,
                                onToggle = { vm.toggleTaskCompletion(task) },
                                onDelete = { vm.deleteTask(task) }
                            )
                        }
                    }
                }
                item { Spacer(Modifier.height(80.dp)) }
            }
        }

        // FAB
        FloatingActionButton(
            onClick = { if (selectedTab == 0) onNewSimulation() else showAddTaskDialog = true },
            containerColor = Violet700,
            contentColor = TextPrimary,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .navigationBarsPadding()
                .padding(16.dp)
        ) {
            Icon(
                if (selectedTab == 0) Icons.Rounded.Science else Icons.Rounded.Add,
                contentDescription = if (selectedTab == 0) "New simulation" else "New task"
            )
        }
    }

    if (showAddTaskDialog) {
        AddProjectTaskDialog(
            onDismiss = { showAddTaskDialog = false },
            onAdd = { title, priority ->
                vm.addTask(title, priority)
                showAddTaskDialog = false
            }
        )
    }
}

@Composable
private fun AddProjectTaskDialog(
    onDismiss: () -> Unit,
    onAdd: (String, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf("MEDIUM") }
    var titleError by remember { mutableStateOf(false) }
    val priorities = listOf("LOW", "MEDIUM", "HIGH")

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceDark,
        title = { Text("Add Task", color = TextPrimary, style = MaterialTheme.typography.titleLarge) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it; titleError = false },
                    placeholder = { Text("Task title", color = TextInactive) },
                    isError = titleError,
                    supportingText = if (titleError) {{ Text("Title is required", color = ColorError) }} else null,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary,
                        focusedBorderColor = Violet500, unfocusedBorderColor = DividerColor,
                        cursorColor = Violet400,
                        focusedContainerColor = SurfaceLight, unfocusedContainerColor = SurfaceLight
                    ),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Rounded.CheckCircle, null, tint = Violet400) }
                )
                Text("Priority", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    priorities.forEach { p ->
                        val isSelected = priority == p
                        val color = when (p) {
                            "HIGH" -> ColorError
                            "LOW" -> ColorSuccess
                            else -> ColorWarning
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(
                                    if (isSelected) color.copy(0.15f) else SurfaceLight,
                                    RoundedCornerShape(10.dp)
                                )
                                .border(1.dp, if (isSelected) color else DividerColor, RoundedCornerShape(10.dp))
                                .clickable { priority = p }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                p,
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal),
                                color = if (isSelected) color else TextSecondary
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isBlank()) { titleError = true; return@Button }
                    onAdd(title, priority)
                },
                colors = ButtonDefaults.buttonColors(containerColor = Violet700)
            ) {
                Text("Add Task", color = TextPrimary)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        }
    )
}

@Composable
private fun SimulationItem(
    sim: com.flowsim.flosasms.data.db.SimulationEntity,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val fmt = SimpleDateFormat("MMM d", Locale.ENGLISH)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(SurfaceDark, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            if (sim.isCompleted) Icons.Rounded.CheckCircle else Icons.Rounded.PlayCircle,
            null,
            tint = if (sim.isCompleted) Violet400 else TextInactive,
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(sim.name, style = MaterialTheme.typography.titleSmall, color = TextPrimary, maxLines = 1)
            Text(
                "${sim.ballCount} balls · ${fmt.format(Date(sim.createdAt))}",
                style = MaterialTheme.typography.labelSmall,
                color = TextInactive
            )
        }
        TypeChip(sim.type)
        Spacer(Modifier.width(4.dp))
        IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
            Icon(Icons.Rounded.Close, null, tint = TextInactive, modifier = Modifier.size(14.dp))
        }
    }
}

@Composable
private fun TaskItem(
    task: com.flowsim.flosasms.data.db.TaskEntity,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(SurfaceDark, RoundedCornerShape(12.dp))
            .padding(start = 4.dp, end = 12.dp, top = 6.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = task.isCompleted,
            onCheckedChange = { onToggle() },
            colors = CheckboxDefaults.colors(
                checkedColor = Violet500,
                uncheckedColor = TextInactive,
                checkmarkColor = TextPrimary
            )
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                task.title,
                style = MaterialTheme.typography.bodyMedium,
                color = if (task.isCompleted) TextInactive else TextPrimary,
                textDecoration = if (task.isCompleted) androidx.compose.ui.text.style.TextDecoration.LineThrough else null,
                maxLines = 2
            )
            if (task.category.isNotEmpty()) {
                Text(task.category, style = MaterialTheme.typography.labelSmall, color = TextInactive)
            }
        }
        Spacer(Modifier.width(8.dp))
        PriorityChip(task.priority)
        Spacer(Modifier.width(4.dp))
        IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
            Icon(Icons.Rounded.Close, null, tint = TextInactive, modifier = Modifier.size(14.dp))
        }
    }
}
