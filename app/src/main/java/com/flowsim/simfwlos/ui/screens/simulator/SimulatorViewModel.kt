package com.flowsim.simfwlos.ui.screens.simulator

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flowsim.simfwlos.data.db.*
import com.flowsim.simfwlos.data.repository.SimulationRepository
import com.flowsim.simfwlos.domain.BallState
import com.flowsim.simfwlos.domain.PhysicsEngine
import com.flowsim.simfwlos.domain.PinState
import com.flowsim.simfwlos.ui.theme.ballColors
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

enum class SimulationType { TASKS, BUDGET, TIME, CUSTOM }
enum class SimulationStep { CREATE, CATEGORIES, PHYSICS, LIVE, DONE }

data class CategoryDraft(
    val name: String = "",
    val weight: Float = 1f,
    val colorHex: String = "#8B5CF6",
    val id: Long = 0
)

class SimulatorViewModel(
    private val simId: Long,
    private val repo: SimulationRepository,
    private val appPrefs: com.flowsim.simfwlos.data.AppPreferences
) : ViewModel() {

    var simulation by mutableStateOf<SimulationEntity?>(null)
        private set
    var categories by mutableStateOf<List<CategoryEntity>>(emptyList())
        private set
    var ballStates by mutableStateOf<List<BallState>>(emptyList())
        private set
    var pins by mutableStateOf<List<PinState>>(emptyList())
        private set
    var isRunning by mutableStateOf(false)
        private set
    var isCompleted by mutableStateOf(false)
        private set
    var results by mutableStateOf<Map<Int, Int>>(emptyMap())
        private set
    var engine: PhysicsEngine? = null
        private set
    var launchedCount by mutableStateOf(0)
        private set

    private var tickJob: Job? = null
    private var isAutoLaunching = false

    // Ticks between each auto-launched ball (~480ms at 16ms/tick)
    private val BALL_LAUNCH_INTERVAL = 30

    init {
        if (simId > 0) loadSimulation()
    }

    private fun loadSimulation() {
        viewModelScope.launch {
            simulation = repo.getById(simId)
            repo.getCategoriesFlow(simId).collectLatest { cats ->
                categories = cats
            }
        }
    }

    fun initEngine(width: Float, height: Float) {
        val sim = simulation ?: return
        val e = PhysicsEngine(
            boardWidth = width,
            boardHeight = height,
            numBuckets = categories.size.coerceAtLeast(2),
            gravity = sim.gravity,
            bounce = sim.bounce,
            spread = sim.spread
        )
        engine = e
        pins = e.pins
        ballStates = emptyList()
        launchedCount = 0
        isCompleted = false
        isRunning = false
        isAutoLaunching = false
        results = emptyMap()
        tickJob?.cancel()
    }

    /**
     * Launch one ball manually. Starts the tick loop if not already running.
     */
    fun launchBall() {
        val sim = simulation ?: return
        val e = engine ?: return
        if (launchedCount >= sim.ballCount) return

        val colorLong = ballColors[launchedCount % ballColors.size].value.toLong()
        e.launchBall(colorLong)
        launchedCount++

        if (tickJob?.isActive != true) {
            startTickLoop(autoMode = false)
        }
    }

    /**
     * Auto-launch all remaining balls with spacing, running the full simulation to completion.
     */
    fun startAutoLaunch() {
        if (isRunning) return
        isRunning = true
        isAutoLaunching = true
        startTickLoop(autoMode = true)
    }

    private fun startTickLoop(autoMode: Boolean) {
        tickJob?.cancel()
        tickJob = viewModelScope.launch {
            val sim = simulation ?: run { isRunning = false; isAutoLaunching = false; return@launch }
            val e = engine ?: run { isRunning = false; isAutoLaunching = false; return@launch }
            var launchTimer = 0

            while (isActive) {
                // Auto-launch logic: space balls out by BALL_LAUNCH_INTERVAL ticks
                if (autoMode && isAutoLaunching && launchedCount < sim.ballCount) {
                    if (launchTimer <= 0) {
                        val colorLong = ballColors[launchedCount % ballColors.size].value.toLong()
                        e.launchBall(colorLong)
                        launchedCount++
                        launchTimer = BALL_LAUNCH_INTERVAL
                    } else {
                        launchTimer--
                    }
                }

                e.tick()
                ballStates = e.balls
                results = e.getResults()

                val noActiveBalls = e.activeBallCount == 0
                val allLaunched = launchedCount >= sim.ballCount

                if (noActiveBalls) {
                    when {
                        autoMode && allLaunched -> {
                            // All balls launched and settled → simulation complete
                            isAutoLaunching = false
                            isRunning = false
                            isCompleted = true
                            saveResults(1)
                            break
                        }
                        !autoMode -> {
                            // Single-ball mode: stop ticking until user launches another
                            break
                        }
                        // autoMode but still more balls to launch → keep looping
                    }
                }

                delay(16L)
            }
        }
    }

    private suspend fun saveResults(runNumber: Int) {
        val e = engine ?: return
        val cats = categories
        val rawResults = e.getResults()
        val toSave = cats.mapIndexed { i, cat ->
            SimulationResultEntity(
                simulationId = simId,
                runNumber = runNumber,
                categoryId = cat.id,
                categoryName = cat.name,
                categoryColorHex = cat.colorHex,
                count = rawResults[i] ?: 0
            )
        }
        repo.saveResults(toSave)
        simulation?.let { s ->
            repo.update(s.copy(isCompleted = true))
            simulation = s.copy(isCompleted = true)
        }
        repo.log("SIMULATION_COMPLETE", "Simulation '${simulation?.name}' completed", "simulation", simId)
    }

    fun stopSimulation() {
        isAutoLaunching = false
        tickJob?.cancel()
        isRunning = false
    }

    suspend fun runMultipleSims(count: Int, ballCount: Int): List<Map<String, Int>> {
        val cats = categories
        if (cats.isEmpty()) return emptyList()
        val allResults = mutableListOf<Map<String, Int>>()
        val maxRun = repo.getMaxRunNumber(simId) ?: 0

        withContext(Dispatchers.Default) {
            for (run in 1..count) {
                val e = PhysicsEngine(
                    boardWidth = 400f,
                    boardHeight = 600f,
                    numBuckets = cats.size,
                    gravity = simulation?.gravity ?: 0.4f,
                    bounce = simulation?.bounce ?: 0.5f,
                    spread = simulation?.spread ?: 0.5f
                )
                val raw = e.simulateInstant(ballCount)
                val named = cats.mapIndexed { i, c -> c.name to (raw[i] ?: 0) }.toMap()
                allResults.add(named)

                val runNum = maxRun + run
                val toSave = cats.mapIndexed { i, cat ->
                    SimulationResultEntity(
                        simulationId = simId,
                        runNumber = runNum,
                        categoryId = cat.id,
                        categoryName = cat.name,
                        categoryColorHex = cat.colorHex,
                        count = raw[i] ?: 0
                    )
                }
                withContext(Dispatchers.Main) { repo.saveResults(toSave) }
            }
        }
        return allResults
    }

    override fun onCleared() {
        super.onCleared()
        tickJob?.cancel()
    }
}
