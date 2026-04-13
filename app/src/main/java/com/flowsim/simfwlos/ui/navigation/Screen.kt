package com.flowsim.simfwlos.ui.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Onboarding : Screen("onboarding")

    // Bottom nav
    object Dashboard : Screen("dashboard")
    object Projects : Screen("projects")
    object Budget : Screen("budget")
    object Analytics : Screen("analytics")
    object Settings : Screen("settings")

    // Simulator flow
    object SimulatorHub : Screen("simulator_hub")
    object CreateSimulation : Screen("create_simulation/{projectId}") {
        fun route(projectId: Long = 0L) = "create_simulation/$projectId"
    }
    object AddCategories : Screen("add_categories/{simId}") {
        fun route(simId: Long) = "add_categories/$simId"
    }
    object PhysicsSettings : Screen("physics_settings/{simId}") {
        fun route(simId: Long) = "physics_settings/$simId"
    }
    object LiveSimulation : Screen("live_simulation/{simId}") {
        fun route(simId: Long) = "live_simulation/$simId"
    }
    object ResultDistribution : Screen("result_distribution/{simId}") {
        fun route(simId: Long) = "result_distribution/$simId"
    }
    object MultiRun : Screen("multi_run/{simId}") {
        fun route(simId: Long) = "multi_run/$simId"
    }
    object AverageResult : Screen("average_result/{simId}") {
        fun route(simId: Long) = "average_result/$simId"
    }

    // Projects
    object ProjectDetails : Screen("project_details/{projectId}") {
        fun route(projectId: Long) = "project_details/$projectId"
    }

    // Tasks / Budget
    object Tasks : Screen("tasks")
    object BudgetMode : Screen("budget_mode/{simId}") {
        fun route(simId: Long) = "budget_mode/$simId"
    }
    object TimeMode : Screen("time_mode/{simId}") {
        fun route(simId: Long) = "time_mode/$simId"
    }
    object TasksMode : Screen("tasks_mode/{simId}") {
        fun route(simId: Long) = "tasks_mode/$simId"
    }

    // Scenarios
    object Scenarios : Screen("scenarios")
    object ScenarioComparison : Screen("scenario_comparison/{id1}/{id2}") {
        fun route(id1: Long, id2: Long) = "scenario_comparison/$id1/$id2"
    }

    // Analytics
    object VisualStats : Screen("visual_stats/{simId}") {
        fun route(simId: Long) = "visual_stats/$simId"
    }
    object ProbabilityMap : Screen("probability_map/{simId}") {
        fun route(simId: Long) = "probability_map/$simId"
    }

    // History
    object History : Screen("history")
    object SavedResults : Screen("saved_results")
    object ActivityHistory : Screen("activity_history")
    object SmartSuggestions : Screen("smart_suggestions/{simId}") {
        fun route(simId: Long) = "smart_suggestions/$simId"
    }

    // Profile
    object Profile : Screen("profile")
}

val bottomNavScreens = listOf(
    Screen.Dashboard,
    Screen.SimulatorHub,
    Screen.Projects,
    Screen.Budget,
    Screen.Settings
)

val noBottomBarRoutes = setOf(
    Screen.Splash.route,
    Screen.Onboarding.route,
    Screen.LiveSimulation.route,
    Screen.CreateSimulation.route
)
