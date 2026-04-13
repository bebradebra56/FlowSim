package com.flowsim.simfwlos.data.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ProjectDao {
    @Query("SELECT * FROM projects ORDER BY createdAt DESC")
    fun getAllProjects(): Flow<List<ProjectEntity>>

    @Query("SELECT * FROM projects WHERE id = :id")
    suspend fun getProjectById(id: Long): ProjectEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProject(project: ProjectEntity): Long

    @Update
    suspend fun updateProject(project: ProjectEntity)

    @Delete
    suspend fun deleteProject(project: ProjectEntity)

    @Query("SELECT COUNT(*) FROM projects")
    suspend fun getProjectCount(): Int
}

@Dao
interface SimulationDao {
    @Query("SELECT * FROM simulations ORDER BY createdAt DESC")
    fun getAllSimulations(): Flow<List<SimulationEntity>>

    @Query("SELECT * FROM simulations WHERE projectId = :projectId ORDER BY createdAt DESC")
    fun getSimulationsByProject(projectId: Long): Flow<List<SimulationEntity>>

    @Query("SELECT * FROM simulations WHERE id = :id")
    suspend fun getSimulationById(id: Long): SimulationEntity?

    @Query("SELECT * FROM simulations WHERE isCompleted = 1 ORDER BY createdAt DESC LIMIT 10")
    fun getRecentCompletedSimulations(): Flow<List<SimulationEntity>>

    @Query("SELECT * FROM simulations WHERE isSaved = 1 ORDER BY createdAt DESC")
    fun getSavedSimulations(): Flow<List<SimulationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSimulation(simulation: SimulationEntity): Long

    @Update
    suspend fun updateSimulation(simulation: SimulationEntity)

    @Delete
    suspend fun deleteSimulation(simulation: SimulationEntity)

    @Query("SELECT COUNT(*) FROM simulations WHERE isCompleted = 1")
    suspend fun getCompletedCount(): Int
}

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories WHERE simulationId = :simulationId ORDER BY position ASC")
    fun getCategoriesForSimulation(simulationId: Long): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories WHERE simulationId = :simulationId ORDER BY position ASC")
    suspend fun getCategoriesForSimulationSync(simulationId: Long): List<CategoryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: CategoryEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategories(categories: List<CategoryEntity>)

    @Update
    suspend fun updateCategory(category: CategoryEntity)

    @Delete
    suspend fun deleteCategory(category: CategoryEntity)

    @Query("DELETE FROM categories WHERE simulationId = :simulationId")
    suspend fun deleteCategoriesForSimulation(simulationId: Long)
}

@Dao
interface SimulationResultDao {
    @Query("SELECT * FROM simulation_results WHERE simulationId = :simulationId ORDER BY runNumber ASC, categoryName ASC")
    fun getResultsForSimulation(simulationId: Long): Flow<List<SimulationResultEntity>>

    @Query("SELECT * FROM simulation_results WHERE simulationId = :simulationId AND runNumber = :runNumber")
    suspend fun getResultsForRun(simulationId: Long, runNumber: Int): List<SimulationResultEntity>

    @Query("SELECT MAX(runNumber) FROM simulation_results WHERE simulationId = :simulationId")
    suspend fun getMaxRunNumber(simulationId: Long): Int?

    @Query("SELECT categoryName, AVG(count) as avgCount, categoryColorHex FROM simulation_results WHERE simulationId = :simulationId GROUP BY categoryName")
    suspend fun getAverageResults(simulationId: Long): List<AvgResultRow>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertResults(results: List<SimulationResultEntity>)

    @Query("DELETE FROM simulation_results WHERE simulationId = :simulationId")
    suspend fun deleteResultsForSimulation(simulationId: Long)
}

data class AvgResultRow(
    val categoryName: String,
    val avgCount: Double,
    val categoryColorHex: String
)

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks ORDER BY createdAt DESC")
    fun getAllTasks(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE projectId = :projectId ORDER BY createdAt DESC")
    fun getTasksByProject(projectId: Long): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE isCompleted = 0 ORDER BY dueDate ASC, priority DESC")
    fun getPendingTasks(): Flow<List<TaskEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity): Long

    @Update
    suspend fun updateTask(task: TaskEntity)

    @Delete
    suspend fun deleteTask(task: TaskEntity)

    @Query("SELECT COUNT(*) FROM tasks WHERE isCompleted = 0")
    suspend fun getPendingTaskCount(): Int
}

@Dao
interface BudgetItemDao {
    @Query("SELECT * FROM budget_items ORDER BY date DESC")
    fun getAllBudgetItems(): Flow<List<BudgetItemEntity>>

    @Query("SELECT * FROM budget_items WHERE projectId = :projectId ORDER BY date DESC")
    fun getBudgetItemsByProject(projectId: Long): Flow<List<BudgetItemEntity>>

    @Query("SELECT SUM(amount) FROM budget_items WHERE type = 'INCOME'")
    suspend fun getTotalIncome(): Double?

    @Query("SELECT SUM(amount) FROM budget_items WHERE type = 'EXPENSE'")
    suspend fun getTotalExpense(): Double?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBudgetItem(item: BudgetItemEntity): Long

    @Update
    suspend fun updateBudgetItem(item: BudgetItemEntity)

    @Delete
    suspend fun deleteBudgetItem(item: BudgetItemEntity)
}

@Dao
interface ScenarioDao {
    @Query("SELECT * FROM scenarios ORDER BY createdAt DESC")
    fun getAllScenarios(): Flow<List<ScenarioEntity>>

    @Query("SELECT * FROM scenarios WHERE id = :id")
    suspend fun getScenarioById(id: Long): ScenarioEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScenario(scenario: ScenarioEntity): Long

    @Update
    suspend fun updateScenario(scenario: ScenarioEntity)

    @Delete
    suspend fun deleteScenario(scenario: ScenarioEntity)
}

@Dao
interface ActivityLogDao {
    @Query("SELECT * FROM activity_log ORDER BY timestamp DESC LIMIT 100")
    fun getRecentActivity(): Flow<List<ActivityLogEntity>>

    @Insert
    suspend fun insertLog(log: ActivityLogEntity)

    @Query("DELETE FROM activity_log WHERE timestamp < :before")
    suspend fun deleteOldLogs(before: Long)
}
