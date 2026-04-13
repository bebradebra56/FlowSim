package com.flowsim.simfwlos.ui.screens.tasks

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Sort
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.flowsim.simfwlos.data.db.TaskEntity
import com.flowsim.simfwlos.ui.components.*
import com.flowsim.simfwlos.ui.theme.*
import org.koin.androidx.compose.koinViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun TasksScreen(onBack: () -> Unit) {
    val vm: TasksViewModel = koinViewModel()
    val state by vm.state.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
        TopBarWithBack(title = "Tasks", onBack = onBack) {
            IconButton(onClick = { vm.toggleSort() }) {
                Icon(
                    if (state.sortByPriority) Icons.AutoMirrored.Rounded.Sort else Icons.Rounded.UnfoldMore,
                    "Sort", tint = if (state.sortByPriority) Violet400 else TextInactive
                )
            }
            IconButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Rounded.Add, "Add task", tint = Violet400)
            }
        }

        // Filter chips
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TaskFilter.entries.forEach { filter ->
                FilterChip(
                    selected = state.filter == filter,
                    onClick = { vm.setFilter(filter) },
                    label = {
                        Text(
                            filter.name.lowercase().replaceFirstChar { it.uppercaseChar() },
                            color = if (state.filter == filter) Violet300 else TextSecondary
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Violet700.copy(0.2f),
                        selectedLabelColor = Violet400
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        borderColor = DividerColor,
                        selectedBorderColor = Violet500,
                        enabled = true,
                        selected = state.filter == filter
                    )
                )
            }
        }

        if (state.tasks.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                EmptyState("No tasks", "Tap + to add your first task")
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(state.tasks, key = { it.id }) { task ->
                    TaskCard(
                        task = task,
                        onToggle = { vm.toggleCompletion(task) },
                        onDelete = { vm.deleteTask(task) }
                    )
                }
                item { Spacer(Modifier.height(16.dp)) }
            }
        }
    }

    if (showAddDialog) {
        AddTaskDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { title, desc, priority, category, hours ->
                vm.addTask(title, desc, priority, category, hours)
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun TaskCard(
    task: TaskEntity,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    val fmt = SimpleDateFormat("MMM d", Locale.ENGLISH)
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(SurfaceDark, RoundedCornerShape(14.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Checkbox(
            checked = task.isCompleted,
            onCheckedChange = { onToggle() },
            colors = CheckboxDefaults.colors(
                checkedColor = Violet500,
                uncheckedColor = DividerColor,
                checkmarkColor = TextPrimary
            ),
            modifier = Modifier.padding(top = 2.dp)
        )
        Spacer(Modifier.width(6.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                task.title,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Medium),
                color = if (task.isCompleted) TextInactive else TextPrimary,
                textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null
            )
            if (task.description.isNotEmpty()) {
                Spacer(Modifier.height(2.dp))
                Text(
                    task.description, style = MaterialTheme.typography.bodySmall,
                    color = TextInactive, maxLines = 2
                )
            }
            Spacer(Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                PriorityChip(task.priority)
                if (task.category.isNotEmpty()) {
                    Surface(color = BallBlue.copy(0.1f), shape = RoundedCornerShape(6.dp)) {
                        Text(task.category, style = MaterialTheme.typography.labelSmall, color = BallBlue,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                    }
                }
                if (task.dueDate != null) {
                    Text(
                        fmt.format(Date(task.dueDate)),
                        style = MaterialTheme.typography.labelSmall, color = TextInactive
                    )
                }
                Text(
                    "${task.estimatedHours}h",
                    style = MaterialTheme.typography.labelSmall, color = TextInactive
                )
            }
        }
        IconButton(onClick = { showDeleteConfirm = true }, modifier = Modifier.size(32.dp)) {
            Icon(Icons.Rounded.Close, null, tint = TextInactive, modifier = Modifier.size(16.dp))
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            containerColor = SurfaceDark,
            title = { Text("Delete Task", color = TextPrimary) },
            text = { Text("Delete \"${task.title}\"?", color = TextSecondary) },
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
private fun AddTaskDialog(
    onDismiss: () -> Unit,
    onAdd: (String, String, String, String, Float) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf("MEDIUM") }
    var category by remember { mutableStateOf("") }
    var hours by remember { mutableStateOf(1f) }
    var titleError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceDark,
        title = { Text("New Task", color = TextPrimary) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = title, onValueChange = { title = it; titleError = false },
                    label = { Text("Title") }, isError = titleError,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary,
                        focusedBorderColor = Violet500, unfocusedBorderColor = DividerColor,
                        focusedContainerColor = SurfaceLight, unfocusedContainerColor = SurfaceLight,
                        focusedLabelColor = Violet400, unfocusedLabelColor = TextSecondary
                    ),
                    shape = RoundedCornerShape(10.dp)
                )
                OutlinedTextField(
                    value = category, onValueChange = { category = it },
                    label = { Text("Category") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary,
                        focusedBorderColor = Violet500, unfocusedBorderColor = DividerColor,
                        focusedContainerColor = SurfaceLight, unfocusedContainerColor = SurfaceLight,
                        focusedLabelColor = Violet400, unfocusedLabelColor = TextSecondary
                    ),
                    shape = RoundedCornerShape(10.dp)
                )
                Text("Priority", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("LOW", "MEDIUM", "HIGH").forEach { p ->
                        FilterChip(
                            selected = priority == p, onClick = { priority = p },
                            label = { Text(p.lowercase().replaceFirstChar { it.uppercaseChar() }, style = MaterialTheme.typography.labelSmall) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = when (p) {
                                    "HIGH" -> ColorError.copy(0.2f)
                                    "LOW" -> ColorSuccess.copy(0.2f)
                                    else -> ColorWarning.copy(0.2f)
                                }
                            )
                        )
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Est. Hours: ${String.format("%.1f", hours)}", style = MaterialTheme.typography.labelMedium, color = TextSecondary, modifier = Modifier.weight(1f))
                    Slider(
                        value = hours, onValueChange = { hours = it },
                        valueRange = 0.5f..12f,
                        modifier = Modifier.weight(1f),
                        colors = SliderDefaults.colors(thumbColor = Violet400, activeTrackColor = Violet500, inactiveTrackColor = DividerColor)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (title.isBlank()) { titleError = true; return@TextButton }
                onAdd(title.trim(), description.trim(), priority, category.trim(), hours)
            }) { Text("Add", color = Violet400) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", color = TextSecondary) } }
    )
}
