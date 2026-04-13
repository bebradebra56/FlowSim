package com.flowsim.simfwlos.ui.screens.projects

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flowsim.simfwlos.data.db.ProjectEntity
import com.flowsim.simfwlos.data.db.SimulationEntity
import com.flowsim.simfwlos.data.db.TaskEntity
import com.flowsim.simfwlos.data.repository.ProjectRepository
import com.flowsim.simfwlos.data.repository.SimulationRepository
import com.flowsim.simfwlos.data.repository.TaskRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ProjectsViewModel(
    private val projectRepo: ProjectRepository,
    private val simRepo: SimulationRepository
) : ViewModel() {

    val projects: StateFlow<List<ProjectEntity>> = projectRepo.getAllProjects()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun createProject(name: String, description: String, colorHex: String) {
        viewModelScope.launch {
            projectRepo.insert(ProjectEntity(name = name, description = description, colorHex = colorHex))
        }
    }

    fun deleteProject(project: ProjectEntity) {
        viewModelScope.launch {
            projectRepo.delete(project)
        }
    }

    fun updateProject(project: ProjectEntity) {
        viewModelScope.launch {
            projectRepo.update(project)
        }
    }
}

class ProjectDetailsViewModel(
    private val projectId: Long,
    private val projectRepo: ProjectRepository,
    private val simRepo: SimulationRepository,
    private val taskRepo: TaskRepository
) : ViewModel() {

    val project: StateFlow<ProjectEntity?> = flow {
        emit(projectRepo.getById(projectId))
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val simulations: StateFlow<List<SimulationEntity>> = simRepo.getSimulationsByProject(projectId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val tasks: StateFlow<List<TaskEntity>> = taskRepo.getTasksByProject(projectId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun toggleTaskCompletion(task: TaskEntity) {
        viewModelScope.launch {
            taskRepo.update(task.copy(isCompleted = !task.isCompleted))
        }
    }

    fun deleteTask(task: TaskEntity) {
        viewModelScope.launch {
            taskRepo.delete(task)
        }
    }

    fun addTask(title: String, priority: String = "MEDIUM") {
        if (title.isBlank()) return
        viewModelScope.launch {
            taskRepo.insert(TaskEntity(projectId = projectId, title = title.trim(), priority = priority))
        }
    }

    fun deleteSimulation(sim: SimulationEntity) {
        viewModelScope.launch {
            simRepo.delete(sim)
        }
    }
}
