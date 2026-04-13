package com.flowsim.simfwlos.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val description: String = "",
    val colorHex: String = "#7C3AED",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "simulations")
data class SimulationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val projectId: Long? = null,
    val name: String,
    val type: String = "CUSTOM",
    val ballCount: Int = 20,
    val gravity: Float = 0.4f,
    val bounce: Float = 0.5f,
    val spread: Float = 0.5f,
    val isCompleted: Boolean = false,
    val isSaved: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val simulationId: Long,
    val name: String,
    val weight: Float = 1.0f,
    val colorHex: String = "#8B5CF6",
    val position: Int = 0
)

@Entity(tableName = "simulation_results")
data class SimulationResultEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val simulationId: Long,
    val runNumber: Int,
    val categoryId: Long,
    val categoryName: String,
    val categoryColorHex: String,
    val count: Int,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val projectId: Long? = null,
    val title: String,
    val description: String = "",
    val priority: String = "MEDIUM",
    val dueDate: Long? = null,
    val isCompleted: Boolean = false,
    val estimatedHours: Float = 1.0f,
    val category: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "budget_items")
data class BudgetItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val projectId: Long? = null,
    val name: String,
    val amount: Double,
    val category: String = "",
    val type: String = "EXPENSE",
    val date: Long = System.currentTimeMillis(),
    val notes: String = ""
)

@Entity(tableName = "scenarios")
data class ScenarioEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val description: String = "",
    val simulationIds: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "activity_log")
data class ActivityLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val action: String,
    val description: String,
    val entityType: String = "",
    val entityId: Long = 0,
    val timestamp: Long = System.currentTimeMillis()
)
