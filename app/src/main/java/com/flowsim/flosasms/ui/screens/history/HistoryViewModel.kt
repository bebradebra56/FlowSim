package com.flowsim.flosasms.ui.screens.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flowsim.flosasms.data.db.ActivityLogEntity
import com.flowsim.flosasms.data.db.SimulationEntity
import com.flowsim.flosasms.data.repository.ActivityRepository
import com.flowsim.flosasms.data.repository.SimulationRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HistoryViewModel(
    private val simRepo: SimulationRepository,
    private val activityRepo: ActivityRepository
) : ViewModel() {

    val allSimulations: StateFlow<List<SimulationEntity>> = simRepo.getAllSimulations()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val savedSimulations: StateFlow<List<SimulationEntity>> = simRepo.getSaved()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activityLog: StateFlow<List<ActivityLogEntity>> = activityRepo.getRecentActivity()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun deleteSimulation(sim: SimulationEntity) {
        viewModelScope.launch { simRepo.delete(sim) }
    }

    fun toggleSave(sim: SimulationEntity) {
        viewModelScope.launch { simRepo.update(sim.copy(isSaved = !sim.isSaved)) }
    }
}
