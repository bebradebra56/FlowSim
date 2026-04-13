package com.flowsim.simfwlos.data.repository

import com.flowsim.simfwlos.data.db.*
import kotlinx.coroutines.flow.Flow

class ProjectRepository(private val dao: ProjectDao) {
    fun getAllProjects(): Flow<List<ProjectEntity>> = dao.getAllProjects()
    suspend fun getById(id: Long): ProjectEntity? = dao.getProjectById(id)
    suspend fun insert(project: ProjectEntity): Long = dao.insertProject(project)
    suspend fun update(project: ProjectEntity) = dao.updateProject(project)
    suspend fun delete(project: ProjectEntity) = dao.deleteProject(project)
    suspend fun getCount(): Int = dao.getProjectCount()
}

class SimulationRepository(
    private val simDao: SimulationDao,
    private val catDao: CategoryDao,
    private val resultDao: SimulationResultDao,
    private val logDao: ActivityLogDao
) {
    fun getAllSimulations(): Flow<List<SimulationEntity>> = simDao.getAllSimulations()
    fun getSimulationsByProject(projectId: Long) = simDao.getSimulationsByProject(projectId)
    fun getRecentCompleted() = simDao.getRecentCompletedSimulations()
    fun getSaved() = simDao.getSavedSimulations()
    suspend fun getById(id: Long): SimulationEntity? = simDao.getSimulationById(id)
    suspend fun insert(sim: SimulationEntity): Long = simDao.insertSimulation(sim)
    suspend fun update(sim: SimulationEntity) = simDao.updateSimulation(sim)
    suspend fun delete(sim: SimulationEntity) = simDao.deleteSimulation(sim)
    suspend fun getCompletedCount(): Int = simDao.getCompletedCount()

    fun getCategoriesFlow(simId: Long) = catDao.getCategoriesForSimulation(simId)
    suspend fun getCategoriesSync(simId: Long) = catDao.getCategoriesForSimulationSync(simId)
    suspend fun insertCategory(cat: CategoryEntity): Long = catDao.insertCategory(cat)
    suspend fun updateCategory(cat: CategoryEntity) = catDao.updateCategory(cat)
    suspend fun deleteCategory(cat: CategoryEntity) = catDao.deleteCategory(cat)
    suspend fun deleteAllCategories(simId: Long) = catDao.deleteCategoriesForSimulation(simId)
    suspend fun replaceCategories(simId: Long, cats: List<CategoryEntity>) {
        catDao.deleteCategoriesForSimulation(simId)
        catDao.insertCategories(cats.mapIndexed { i, c -> c.copy(simulationId = simId, position = i) })
    }

    fun getResultsFlow(simId: Long) = resultDao.getResultsForSimulation(simId)
    suspend fun getResultsForRun(simId: Long, run: Int) = resultDao.getResultsForRun(simId, run)
    suspend fun getMaxRunNumber(simId: Long) = resultDao.getMaxRunNumber(simId)
    suspend fun getAverageResults(simId: Long) = resultDao.getAverageResults(simId)
    suspend fun saveResults(results: List<SimulationResultEntity>) = resultDao.insertResults(results)

    suspend fun log(action: String, desc: String, type: String = "", id: Long = 0) {
        logDao.insertLog(ActivityLogEntity(action = action, description = desc, entityType = type, entityId = id))
    }
}

class TaskRepository(private val dao: TaskDao) {
    fun getAllTasks(): Flow<List<TaskEntity>> = dao.getAllTasks()
    fun getTasksByProject(projectId: Long) = dao.getTasksByProject(projectId)
    fun getPendingTasks() = dao.getPendingTasks()
    suspend fun insert(task: TaskEntity): Long = dao.insertTask(task)
    suspend fun update(task: TaskEntity) = dao.updateTask(task)
    suspend fun delete(task: TaskEntity) = dao.deleteTask(task)
    suspend fun getPendingCount(): Int = dao.getPendingTaskCount()
}

class BudgetRepository(private val dao: BudgetItemDao) {
    fun getAllItems(): Flow<List<BudgetItemEntity>> = dao.getAllBudgetItems()
    fun getItemsByProject(projectId: Long) = dao.getBudgetItemsByProject(projectId)
    suspend fun getTotalIncome(): Double = dao.getTotalIncome() ?: 0.0
    suspend fun getTotalExpense(): Double = dao.getTotalExpense() ?: 0.0
    suspend fun insert(item: BudgetItemEntity): Long = dao.insertBudgetItem(item)
    suspend fun update(item: BudgetItemEntity) = dao.updateBudgetItem(item)
    suspend fun delete(item: BudgetItemEntity) = dao.deleteBudgetItem(item)
}

class ScenarioRepository(private val dao: ScenarioDao) {
    fun getAllScenarios(): Flow<List<ScenarioEntity>> = dao.getAllScenarios()
    suspend fun getById(id: Long): ScenarioEntity? = dao.getScenarioById(id)
    suspend fun insert(scenario: ScenarioEntity): Long = dao.insertScenario(scenario)
    suspend fun update(scenario: ScenarioEntity) = dao.updateScenario(scenario)
    suspend fun delete(scenario: ScenarioEntity) = dao.deleteScenario(scenario)
}

class ActivityRepository(private val dao: ActivityLogDao) {
    fun getRecentActivity() = dao.getRecentActivity()
    suspend fun log(action: String, desc: String, type: String = "", id: Long = 0) =
        dao.insertLog(ActivityLogEntity(action = action, description = desc, entityType = type, entityId = id))
}
