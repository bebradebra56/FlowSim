package com.flowsim.flosasms.ui.screens.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flowsim.flosasms.data.db.TaskEntity
import com.flowsim.flosasms.data.repository.ActivityRepository
import com.flowsim.flosasms.data.repository.TaskRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class TaskFilter { ALL, PENDING, COMPLETED }

data class TasksState(
    val tasks: List<TaskEntity> = emptyList(),
    val filter: TaskFilter = TaskFilter.ALL,
    val sortByPriority: Boolean = false
)

class TasksViewModel(
    private val taskRepo: TaskRepository,
    private val activityRepo: ActivityRepository
) : ViewModel() {

    private val _filter = MutableStateFlow(TaskFilter.ALL)
    private val _sortByPriority = MutableStateFlow(false)

    val state: StateFlow<TasksState> = combine(
        taskRepo.getAllTasks(),
        _filter,
        _sortByPriority
    ) { tasks, filter, sort ->
        val filtered = when (filter) {
            TaskFilter.ALL -> tasks
            TaskFilter.PENDING -> tasks.filter { !it.isCompleted }
            TaskFilter.COMPLETED -> tasks.filter { it.isCompleted }
        }
        val sorted = if (sort) {
            filtered.sortedByDescending { when (it.priority) { "HIGH" -> 3; "MEDIUM" -> 2; else -> 1 } }
        } else filtered
        TasksState(tasks = sorted, filter = filter, sortByPriority = sort)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), TasksState())

    fun setFilter(filter: TaskFilter) { _filter.value = filter }
    fun toggleSort() { _sortByPriority.value = !_sortByPriority.value }

    fun addTask(title: String, description: String, priority: String, category: String, estimatedHours: Float) {
        viewModelScope.launch {
            val id = taskRepo.insert(TaskEntity(
                title = title, description = description,
                priority = priority, category = category, estimatedHours = estimatedHours
            ))
            activityRepo.log("TASK_CREATED", "Task '$title' created", "task", id)
        }
    }

    fun toggleCompletion(task: TaskEntity) {
        viewModelScope.launch {
            taskRepo.update(task.copy(isCompleted = !task.isCompleted))
        }
    }

    fun deleteTask(task: TaskEntity) {
        viewModelScope.launch {
            taskRepo.delete(task)
            activityRepo.log("TASK_DELETED", "Task '${task.title}' deleted", "task", task.id)
        }
    }

    fun updateTask(task: TaskEntity) {
        viewModelScope.launch { taskRepo.update(task) }
    }
}
