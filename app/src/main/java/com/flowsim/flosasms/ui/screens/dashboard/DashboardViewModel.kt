package com.flowsim.flosasms.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flowsim.flosasms.data.AppPreferences
import com.flowsim.flosasms.data.db.ProjectEntity
import com.flowsim.flosasms.data.db.SimulationEntity
import com.flowsim.flosasms.data.repository.ProjectRepository
import com.flowsim.flosasms.data.repository.SimulationRepository
import com.flowsim.flosasms.data.repository.TaskRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class DashboardState(
    val recentSimulations: List<SimulationEntity> = emptyList(),
    val projects: List<ProjectEntity> = emptyList(),
    val completedSimulationsCount: Int = 0,
    val pendingTasksCount: Int = 0,
    val userName: String = ""
)

class DashboardViewModel(
    private val simRepo: SimulationRepository,
    private val projectRepo: ProjectRepository,
    private val taskRepo: TaskRepository,
    private val prefs: AppPreferences
) : ViewModel() {

    private val _state = MutableStateFlow(DashboardState())
    val state: StateFlow<DashboardState> = _state

    init {
        load()
    }

    private fun load() {
        viewModelScope.launch {
            simRepo.getRecentCompleted().collectLatest { sims ->
                _state.value = _state.value.copy(recentSimulations = sims.take(5))
            }
        }
        viewModelScope.launch {
            projectRepo.getAllProjects().collectLatest { projects ->
                _state.value = _state.value.copy(projects = projects.take(4))
            }
        }
        viewModelScope.launch {
            prefs.userName.collectLatest { name ->
                _state.value = _state.value.copy(userName = name)
            }
        }
        viewModelScope.launch {
            val simCount = simRepo.getCompletedCount()
            val taskCount = taskRepo.getPendingCount()
            _state.value = _state.value.copy(
                completedSimulationsCount = simCount,
                pendingTasksCount = taskCount
            )
        }
    }
}
