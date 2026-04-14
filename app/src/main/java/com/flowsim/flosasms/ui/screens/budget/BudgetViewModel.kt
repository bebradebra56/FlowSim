package com.flowsim.flosasms.ui.screens.budget

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flowsim.flosasms.data.db.BudgetItemEntity
import com.flowsim.flosasms.data.repository.ActivityRepository
import com.flowsim.flosasms.data.repository.BudgetRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class BudgetState(
    val items: List<BudgetItemEntity> = emptyList(),
    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0,
    val filterType: String = "ALL"
) {
    val balance: Double get() = totalIncome - totalExpense
}

class BudgetViewModel(
    private val budgetRepo: BudgetRepository,
    private val activityRepo: ActivityRepository
) : ViewModel() {

    private val _filterType = MutableStateFlow("ALL")

    val state: StateFlow<BudgetState> = combine(
        budgetRepo.getAllItems(),
        _filterType
    ) { items, filter ->
        val filtered = if (filter == "ALL") items else items.filter { it.type == filter }
        val income = items.filter { it.type == "INCOME" }.sumOf { it.amount }
        val expense = items.filter { it.type == "EXPENSE" }.sumOf { it.amount }
        BudgetState(items = filtered, totalIncome = income, totalExpense = expense, filterType = filter)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), BudgetState())

    fun setFilter(type: String) { _filterType.value = type }

    fun addItem(name: String, amount: Double, category: String, type: String, notes: String) {
        viewModelScope.launch {
            val id = budgetRepo.insert(
                BudgetItemEntity(name = name, amount = amount, category = category, type = type, notes = notes)
            )
            activityRepo.log("BUDGET_ADDED", "Budget item '$name' ($type: $$amount) added", "budget", id)
        }
    }

    fun deleteItem(item: BudgetItemEntity) {
        viewModelScope.launch {
            budgetRepo.delete(item)
            activityRepo.log("BUDGET_DELETED", "Budget item '${item.name}' deleted", "budget", item.id)
        }
    }
}
