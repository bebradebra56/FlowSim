package com.flowsim.flosasms.ui.screens.simulator

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.flowsim.flosasms.data.db.CategoryEntity
import com.flowsim.flosasms.data.repository.SimulationRepository
import com.flowsim.flosasms.ui.components.*
import com.flowsim.flosasms.ui.theme.*
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

private val presetColors = listOf(
    "#8B5CF6", "#22D3EE", "#2DD4BF", "#FACC15",
    "#F472B6", "#FB7185", "#38BDF8", "#A78BFA"
)

private val defaultCategoryTemplates = mapOf(
    "TASKS" to listOf("Development" to "#8B5CF6", "Design" to "#F472B6", "Research" to "#22D3EE", "Review" to "#2DD4BF"),
    "BUDGET" to listOf("Housing" to "#FACC15", "Food" to "#FB7185", "Transport" to "#38BDF8", "Entertainment" to "#8B5CF6"),
    "TIME" to listOf("Work" to "#8B5CF6", "Personal" to "#2DD4BF", "Health" to "#22D3EE", "Family" to "#FACC15"),
    "CUSTOM" to listOf("Category A" to "#8B5CF6", "Category B" to "#22D3EE", "Category C" to "#F472B6", "Category D" to "#FACC15")
)

@Composable
fun AddCategoriesScreen(
    simId: Long,
    onNext: () -> Unit,
    onBack: () -> Unit
) {
    val repo: SimulationRepository = koinInject()
    val scope = rememberCoroutineScope()
    val categories = remember { mutableStateListOf<CategoryDraft>() }
    var showAddDialog by remember { mutableStateOf(false) }
    var simType by remember { mutableStateOf("CUSTOM") }

    LaunchedEffect(simId) {
        val sim = repo.getById(simId)
        simType = sim?.type ?: "CUSTOM"
        repo.getCategoriesFlow(simId).collectLatest { cats ->
            if (categories.isEmpty()) {
                if (cats.isEmpty()) {
                    val templates = defaultCategoryTemplates[simType] ?: defaultCategoryTemplates["CUSTOM"]!!
                    categories.addAll(templates.mapIndexed { i, (name, color) ->
                        CategoryDraft(name = name, colorHex = color, weight = 1f)
                    })
                } else {
                    categories.addAll(cats.map { CategoryDraft(it.name, it.weight, it.colorHex, it.id) })
                }
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
        TopBarWithBack(title = "Add Categories", onBack = onBack) {
            IconButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Rounded.Add, "Add", tint = Violet400)
            }
        }

        Text(
            "Define the distribution buckets. Weights affect probability.",
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
        )

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            itemsIndexed(categories) { index, cat ->
                CategoryItem(
                    category = cat,
                    onNameChange = { categories[index] = categories[index].copy(name = it) },
                    onWeightChange = { categories[index] = categories[index].copy(weight = it) },
                    onColorChange = { categories[index] = categories[index].copy(colorHex = it) },
                    onDelete = if (categories.size > 2) {{ categories.removeAt(index) }} else null
                )
            }
        }

        Column(modifier = Modifier.padding(16.dp)) {
            GradientButton(
                text = "Continue to Physics",
                enabled = categories.size >= 2,
                onClick = {
                    scope.launch {
                        val entities = categories.mapIndexed { i, c ->
                            CategoryEntity(
                                simulationId = simId,
                                name = c.name.ifEmpty { "Category ${i + 1}" },
                                weight = c.weight.coerceIn(0.1f, 10f),
                                colorHex = c.colorHex,
                                position = i
                            )
                        }
                        repo.replaceCategories(simId, entities)
                        onNext()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }

    if (showAddDialog) {
        var newName by remember { mutableStateOf("") }
        var newColor by remember { mutableStateOf(presetColors[categories.size % presetColors.size]) }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            containerColor = SurfaceDark,
            title = { Text("New Category", color = TextPrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = newName,
                        onValueChange = { newName = it },
                        label = { Text("Category Name") },
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
                    presetColors.forEach { hex ->
                        val c = runCatching { Color(android.graphics.Color.parseColor(hex)) }.getOrDefault(Violet500)
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .background(c, CircleShape)
                                .then(if (hex == newColor) Modifier.border(2.dp, Color.White, CircleShape) else Modifier)
                                .clickable { newColor = hex }
                        )
                    }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    categories.add(CategoryDraft(
                        name = newName.ifEmpty { "Category ${categories.size + 1}" },
                        colorHex = newColor,
                        weight = 1f
                    ))
                    showAddDialog = false
                }) { Text("Add", color = Violet400) }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) { Text("Cancel", color = TextSecondary) }
            }
        )
    }
}

@Composable
private fun CategoryItem(
    category: CategoryDraft,
    onNameChange: (String) -> Unit,
    onWeightChange: (Float) -> Unit,
    onColorChange: (String) -> Unit,
    onDelete: (() -> Unit)?
) {
    val color = runCatching { Color(android.graphics.Color.parseColor(category.colorHex)) }.getOrDefault(Violet500)

    GlassCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(14.dp).background(color, CircleShape))
            Spacer(Modifier.width(10.dp))
            OutlinedTextField(
                value = category.name,
                onValueChange = onNameChange,
                modifier = Modifier.weight(1f),
                placeholder = { Text("Name", color = TextInactive) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary,
                    focusedBorderColor = Violet500, unfocusedBorderColor = DividerColor,
                    focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent
                ),
                singleLine = true, shape = RoundedCornerShape(8.dp)
            )
            if (onDelete != null) {
                IconButton(onClick = onDelete) {
                    Icon(Icons.Rounded.Delete, null, tint = ColorError.copy(0.7f), modifier = Modifier.size(18.dp))
                }
            }
        }

        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Weight: ${String.format("%.1f", category.weight)}", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
            Spacer(Modifier.width(8.dp))
            Slider(
                value = category.weight,
                onValueChange = onWeightChange,
                valueRange = 0.1f..5f,
                modifier = Modifier.weight(1f),
                colors = SliderDefaults.colors(thumbColor = color, activeTrackColor = color, inactiveTrackColor = DividerColor)
            )
        }

        Spacer(Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                presetColors.forEach { hex ->
                    val c = runCatching { Color(android.graphics.Color.parseColor(hex)) }.getOrDefault(Violet500)
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .background(c, CircleShape)
                            .then(if (hex == category.colorHex) Modifier.border(1.5.dp, Color.White, CircleShape) else Modifier)
                            .clickable { onColorChange(hex) }
                    )
                }
        }
    }
}
