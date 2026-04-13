package com.flowsim.simfwlos.ui.screens.budget

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.TrendingDown
import androidx.compose.material.icons.automirrored.rounded.TrendingUp
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.flowsim.simfwlos.data.db.BudgetItemEntity
import com.flowsim.simfwlos.ui.components.*
import com.flowsim.simfwlos.ui.theme.*
import org.koin.androidx.compose.koinViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun BudgetScreen(navController: NavController) {
    val vm: BudgetViewModel = koinViewModel()
    val state by vm.state.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var addType by remember { mutableStateOf("EXPENSE") }

    val categoryData = state.items.groupBy { it.category }
        .mapValues { it.value.sumOf { i -> i.amount } }
        .entries.sortedByDescending { it.value }

    LazyColumn(
        modifier = Modifier.fillMaxSize().statusBarsPadding(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Budget", style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold), color = TextPrimary)
                FloatingActionButton(
                    onClick = { showAddDialog = true },
                    containerColor = Violet700, contentColor = TextPrimary, modifier = Modifier.size(48.dp)
                ) { Icon(Icons.Rounded.Add, null) }
            }
        }

        // Balance card
        item {
            GlassCard {
                Text("Balance Overview", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold), color = TextPrimary)
                Spacer(Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    BudgetStat("Income", state.totalIncome, ColorSuccess, Modifier.weight(1f))
                    BudgetStat("Expense", state.totalExpense, ColorError, Modifier.weight(1f))
                    BudgetStat("Balance", state.balance, if (state.balance >= 0) ColorSuccess else ColorError, Modifier.weight(1f))
                }
            }
        }

        // Category chart
        if (categoryData.isNotEmpty()) {
            item {
                GlassCard {
                    Text("By Category", style = MaterialTheme.typography.titleSmall, color = TextSecondary)
                    Spacer(Modifier.height(12.dp))
                    val colors = listOf(Violet500, BallBlue, BallTeal, ColorWarning, BallPink, BallRed)
                    val chartEntries = categoryData.mapIndexed { i, (name, amt) ->
                        ChartEntry(name, amt.toFloat(), colors[i % colors.size])
                    }
                    BarChart(chartEntries, modifier = Modifier.fillMaxWidth().height(130.dp))
                }
            }
        }

        // Filter
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                listOf("ALL", "INCOME", "EXPENSE").forEach { type ->
                    FilterChip(
                        selected = state.filterType == type,
                        onClick = { vm.setFilter(type) },
                        label = {
                            Text(
                                type.lowercase().replaceFirstChar { it.uppercaseChar() },
                                color = if (state.filterType == type) Violet300 else TextSecondary,
                                style = MaterialTheme.typography.labelMedium
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Violet700.copy(0.2f)),
                        border = FilterChipDefaults.filterChipBorder(
                            borderColor = DividerColor, selectedBorderColor = Violet500,
                            enabled = true, selected = state.filterType == type
                        )
                    )
                }
            }
        }

        if (state.items.isEmpty()) {
            item { EmptyState("No budget items", "Tap + to add income or expense") }
        } else {
            items(state.items, key = { it.id }) { item ->
                BudgetItemCard(item = item, onDelete = { vm.deleteItem(item) })
            }
        }

        item { Spacer(Modifier.height(16.dp)) }
    }

    if (showAddDialog) {
        AddBudgetDialog(
            defaultType = addType,
            onDismiss = { showAddDialog = false },
            onAdd = { name, amount, category, type, notes ->
                vm.addItem(name, amount, category, type, notes)
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun BudgetStat(label: String, amount: Double, color: androidx.compose.ui.graphics.Color, modifier: Modifier = Modifier) {
    Column(modifier = modifier.background(color.copy(alpha = 0.08f), RoundedCornerShape(12.dp)).padding(10.dp)) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = TextSecondary, maxLines = 1)
        Spacer(Modifier.height(4.dp))
        Text(
            "$${String.format("%.2f", amount)}",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = color,
            maxLines = 1,
            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun BudgetItemCard(item: BudgetItemEntity, onDelete: () -> Unit) {
    val fmt = SimpleDateFormat("MMM d", Locale.ENGLISH)
    val isIncome = item.type == "INCOME"
    val color = if (isIncome) ColorSuccess else ColorError

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(SurfaceDark, RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(36.dp).background(color.copy(0.15f), RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                if (isIncome) Icons.AutoMirrored.Rounded.TrendingUp else Icons.AutoMirrored.Rounded.TrendingDown,
                null, tint = color, modifier = Modifier.size(18.dp)
            )
        }
        Spacer(Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                item.name,
                style = MaterialTheme.typography.titleSmall,
                color = TextPrimary,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
            Text(
                buildString {
                    if (item.category.isNotEmpty()) append("${item.category} · ")
                    append(fmt.format(Date(item.date)))
                },
                style = MaterialTheme.typography.labelSmall,
                color = TextInactive,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
        }
        Spacer(Modifier.width(8.dp))
        Text(
            "${if (isIncome) "+" else "-"}$${String.format("%.2f", item.amount)}",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = color,
            maxLines = 1
        )
        IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
            Icon(Icons.Rounded.Close, null, tint = TextInactive, modifier = Modifier.size(16.dp))
        }
    }
}

@Composable
private fun AddBudgetDialog(
    defaultType: String,
    onDismiss: () -> Unit,
    onAdd: (String, Double, String, String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var type by remember { mutableStateOf(defaultType) }
    var notes by remember { mutableStateOf("") }
    var nameError by remember { mutableStateOf(false) }
    var amountError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceDark,
        title = { Text("Add Budget Item", color = TextPrimary) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("INCOME", "EXPENSE").forEach { t ->
                        FilterChip(
                            selected = type == t, onClick = { type = t },
                            label = { Text(t.lowercase().replaceFirstChar { it.uppercaseChar() }, style = MaterialTheme.typography.labelSmall) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = if (t == "INCOME") ColorSuccess.copy(0.2f) else ColorError.copy(0.2f)
                            )
                        )
                    }
                }
                OutlinedTextField(
                    value = name, onValueChange = { name = it; nameError = false },
                    label = { Text("Name") }, isError = nameError,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary,
                        focusedBorderColor = Violet500, unfocusedBorderColor = DividerColor,
                        focusedContainerColor = SurfaceLight, unfocusedContainerColor = SurfaceLight,
                        focusedLabelColor = Violet400, unfocusedLabelColor = TextSecondary
                    ), shape = RoundedCornerShape(10.dp)
                )
                OutlinedTextField(
                    value = amount, onValueChange = { amount = it; amountError = false },
                    label = { Text("Amount") }, isError = amountError,
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary,
                        focusedBorderColor = Violet500, unfocusedBorderColor = DividerColor,
                        focusedContainerColor = SurfaceLight, unfocusedContainerColor = SurfaceLight,
                        focusedLabelColor = Violet400, unfocusedLabelColor = TextSecondary
                    ), shape = RoundedCornerShape(10.dp)
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
                    ), shape = RoundedCornerShape(10.dp)
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (name.isBlank()) { nameError = true; return@TextButton }
                val amt = amount.toDoubleOrNull()
                if (amt == null || amt <= 0) { amountError = true; return@TextButton }
                onAdd(name.trim(), amt, category.trim(), type, notes.trim())
            }) { Text("Add", color = Violet400) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", color = TextSecondary) } }
    )
}

