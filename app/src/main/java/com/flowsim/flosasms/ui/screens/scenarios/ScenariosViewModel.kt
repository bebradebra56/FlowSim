package com.flowsim.flosasms.ui.screens.scenarios

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flowsim.flosasms.data.db.ScenarioEntity
import com.flowsim.flosasms.data.repository.ActivityRepository
import com.flowsim.flosasms.data.repository.ScenarioRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ScenariosViewModel(
    private val scenarioRepo: ScenarioRepository,
    private val activityRepo: ActivityRepository
) : ViewModel() {

    val scenarios: StateFlow<List<ScenarioEntity>> = scenarioRepo.getAllScenarios()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun createScenario(name: String, description: String, simulationIds: List<Long>) {
        viewModelScope.launch {
            val id = scenarioRepo.insert(
                ScenarioEntity(
                    name = name,
                    description = description,
                    simulationIds = simulationIds.joinToString(",")
                )
            )
            activityRepo.log("SCENARIO_CREATED", "Scenario '$name' created", "scenario", id)
        }
    }

    fun deleteScenario(scenario: ScenarioEntity) {
        viewModelScope.launch {
            scenarioRepo.delete(scenario)
        }
    }
}
