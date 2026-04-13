package com.flowsim.simfwlos.ui.screens.projects

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
import androidx.navigation.NavController
import com.flowsim.simfwlos.ui.components.*
import com.flowsim.simfwlos.ui.theme.*
import org.koin.androidx.compose.koinViewModel
import java.text.SimpleDateFormat
import java.util.*

private val projectColors = listOf(
    "#7C3AED", "#22D3EE", "#2DD4BF", "#FACC15",
    "#F472B6", "#FB7185", "#38BDF8", "#A78BFA"
)

@Composable
fun ProjectsScreen(
    onProjectClick: (Long) -> Unit,
    navController: NavController
) {
    val vm: ProjectsViewModel = koinViewModel()
    val projects by vm.projects.collectAsState()
    var showCreateDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize().statusBarsPadding(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Projects", style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold), color = TextPrimary)
                    Text("${projects.size} projects", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                }
                FloatingActionButton(
                    onClick = { showCreateDialog = true },
                    containerColor = Violet700,
                    contentColor = TextPrimary,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(Icons.Rounded.Add, "New project")
                }
            }
        }

        if (projects.isEmpty()) {
            item {
                EmptyState(
                    title = "No projects yet",
                    subtitle = "Create a project to organize your simulations and tasks"
                )
            }
        } else {
            items(projects) { project ->
                ProjectCard(
                    project = project,
                    onClick = { onProjectClick(project.id) },
                    onDelete = { vm.deleteProject(project) }
                )
            }
        }

        item { Spacer(Modifier.height(16.dp)) }
    }

    if (showCreateDialog) {
        CreateProjectDialog(
            onDismiss = { showCreateDialog = false },
            onCreate = { name, desc, color ->
                vm.createProject(name, desc, color)
                showCreateDialog = false
            }
        )
    }
}

@Composable
private fun ProjectCard(
    project: com.flowsim.simfwlos.data.db.ProjectEntity,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val fmt = SimpleDateFormat("MMM d, yyyy", Locale.ENGLISH)
    val color = runCatching { Color(android.graphics.Color.parseColor(project.colorHex)) }.getOrDefault(Violet500)

    var showDeleteConfirm by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(SurfaceDark, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(color.copy(0.15f), RoundedCornerShape(14.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Rounded.Folder, null, tint = color, modifier = Modifier.size(26.dp))
        }
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(project.name, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold), color = TextPrimary)
            if (project.description.isNotEmpty()) {
                Text(project.description, style = MaterialTheme.typography.bodySmall, color = TextSecondary, maxLines = 1)
            }
            Spacer(Modifier.height(4.dp))
            Text(fmt.format(Date(project.createdAt)), style = MaterialTheme.typography.labelSmall, color = TextInactive)
        }
        IconButton(onClick = { showDeleteConfirm = true }) {
            Icon(Icons.Rounded.MoreVert, null, tint = TextInactive, modifier = Modifier.size(18.dp))
        }
        Icon(Icons.Rounded.ChevronRight, null, tint = TextInactive, modifier = Modifier.size(18.dp))
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            containerColor = SurfaceDark,
            title = { Text("Delete Project", color = TextPrimary) },
            text = { Text("Are you sure you want to delete \"${project.name}\"?", color = TextSecondary) },
            confirmButton = {
                TextButton(onClick = { onDelete(); showDeleteConfirm = false }) {
                    Text("Delete", color = ColorError)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }
}

@Composable
private fun CreateProjectDialog(
    onDismiss: () -> Unit,
    onCreate: (String, String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf(projectColors[0]) }
    var nameError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceDark,
        title = { Text("New Project", color = TextPrimary) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it; nameError = false },
                    label = { Text("Project Name") },
                    isError = nameError,
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
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary,
                        focusedBorderColor = Violet500, unfocusedBorderColor = DividerColor,
                        focusedContainerColor = SurfaceLight, unfocusedContainerColor = SurfaceLight,
                        focusedLabelColor = Violet400, unfocusedLabelColor = TextSecondary
                    ),
                    shape = RoundedCornerShape(10.dp)
                )
                Text("Color", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    projectColors.forEach { hex ->
                        val c = runCatching { Color(android.graphics.Color.parseColor(hex)) }.getOrDefault(Violet500)
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .background(c, CircleShape)
                                .then(if (hex == selectedColor) Modifier.border(2.dp, Color.White, CircleShape) else Modifier)
                                .clickable { selectedColor = hex }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (name.isBlank()) { nameError = true; return@TextButton }
                onCreate(name.trim(), description.trim(), selectedColor)
            }) { Text("Create", color = Violet400) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = TextSecondary) }
        }
    )
}
