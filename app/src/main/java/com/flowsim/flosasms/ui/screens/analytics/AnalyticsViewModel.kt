package com.flowsim.flosasms.ui.screens.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flowsim.flosasms.data.db.SimulationEntity
import com.flowsim.flosasms.data.repository.SimulationRepository
import com.flowsim.flosasms.data.repository.ProjectRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class AnalyticsState(
    val totalSimulations: Int = 0,
    val completedSimulations: List<SimulationEntity> = emptyList(),
    val typeDistribution: Map<String, Int> = emptyMap()
)

class AnalyticsViewModel(
    private val simRepo: SimulationRepository,
    private val projectRepo: ProjectRepository
) : ViewModel() {

    private val _state = MutableStateFlow(AnalyticsState())
    val state: StateFlow<AnalyticsState> = _state

    init {
        viewModelScope.launch {
            simRepo.getAllSimulations().collectLatest { sims ->
                val completed = sims.filter { it.isCompleted }
                val typeDist = completed.groupBy { it.type }.mapValues { it.value.size }
                _state.value = _state.value.copy(
                    totalSimulations = sims.size,
                    completedSimulations = completed,
                    typeDistribution = typeDist
                )
            }
        }
    }
}
