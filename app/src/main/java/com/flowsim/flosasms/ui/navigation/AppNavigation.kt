package com.flowsim.flosasms.ui.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.flowsim.flosasms.ui.screens.analytics.AnalyticsScreen
import com.flowsim.flosasms.ui.screens.analytics.ProbabilityMapScreen
import com.flowsim.flosasms.ui.screens.analytics.VisualStatsScreen
import com.flowsim.flosasms.ui.screens.budget.BudgetScreen
import com.flowsim.flosasms.ui.screens.dashboard.DashboardScreen
import com.flowsim.flosasms.ui.screens.history.ActivityHistoryScreen
import com.flowsim.flosasms.ui.screens.history.HistoryScreen
import com.flowsim.flosasms.ui.screens.history.SavedResultsScreen
import com.flowsim.flosasms.ui.screens.onboarding.OnboardingScreen
import com.flowsim.flosasms.ui.screens.profile.ProfileScreen
import com.flowsim.flosasms.ui.screens.projects.ProjectDetailsScreen
import com.flowsim.flosasms.ui.screens.projects.ProjectsScreen
import com.flowsim.flosasms.ui.screens.scenarios.ScenarioComparisonScreen
import com.flowsim.flosasms.ui.screens.scenarios.ScenariosScreen
import com.flowsim.flosasms.ui.screens.settings.SettingsScreen
import com.flowsim.flosasms.ui.screens.simulator.*
import com.flowsim.flosasms.ui.screens.splash.SplashScreen
import com.flowsim.flosasms.ui.screens.tasks.TasksScreen
import com.flowsim.flosasms.ui.theme.*

data class BottomNavItem(val screen: Screen, val label: String, val icon: ImageVector)

val bottomNavItems = listOf(
    BottomNavItem(Screen.Dashboard, "Home", Icons.Rounded.Home),
    BottomNavItem(Screen.SimulatorHub, "Simulate", Icons.Rounded.PlayCircle),
    BottomNavItem(Screen.Projects, "Projects", Icons.Rounded.Folder),
    BottomNavItem(Screen.Budget, "Budget", Icons.Rounded.AccountBalanceWallet),
    BottomNavItem(Screen.Settings, "Settings", Icons.Rounded.Settings)
)

@Composable
fun AppRoot() {
    val navController = rememberNavController()
    val navBackStack by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStack?.destination?.route

    val showBottomBar = currentRoute != null &&
            !noBottomBarRoutes.any { nr ->
                currentRoute == nr || currentRoute.startsWith(nr.substringBefore("{"))
            }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(BgDarkest, BgLight),
                    startY = 0f,
                    endY = Float.POSITIVE_INFINITY
                )
            )
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets(0),
            bottomBar = {
                AnimatedVisibility(
                    visible = showBottomBar,
                    enter = slideInVertically(initialOffsetY = { it }, animationSpec = tween(300)),
                    exit = slideOutVertically(targetOffsetY = { it }, animationSpec = tween(300))
                ) {
                    AppBottomBar(navController)
                }
            }
        ) { padding ->
            AppNavHost(
                navController = navController,
                startDestination = Screen.Splash.route,
                modifier = Modifier.padding(padding)
            )
        }
    }
}

@Composable
fun AppBottomBar(navController: NavController) {
    val navBackStack by navController.currentBackStackEntryAsState()
    val currentDest = navBackStack?.destination

    NavigationBar(
        containerColor = SurfaceDark.copy(alpha = 0.95f),
        tonalElevation = 0.dp,
        modifier = Modifier.navigationBarsPadding()
    ) {
        bottomNavItems.forEach { item ->
            val selected = currentDest?.hierarchy?.any { it.route == item.screen.route } == true
            NavigationBarItem(
                selected = selected,
                onClick = {
                    navController.navigate(item.screen.route) {
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label,
                        tint = if (selected) Violet400 else TextInactive
                    )
                },
                label = {
                    Text(
                        text = item.label,
                        color = if (selected) Violet400 else TextInactive,
                        style = MaterialTheme.typography.labelSmall
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = Violet700.copy(alpha = 0.2f),
                    selectedIconColor = Violet400,
                    unselectedIconColor = TextInactive
                )
            )
        }
    }
}

@Composable
fun AppNavHost(
    navController: NavHostController,
    startDestination: String,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
        enterTransition = { fadeIn(tween(300)) + slideInHorizontally(tween(300)) { it / 4 } },
        exitTransition = { fadeOut(tween(200)) },
        popEnterTransition = { fadeIn(tween(300)) },
        popExitTransition = { fadeOut(tween(200)) + slideOutHorizontally(tween(300)) { it / 4 } }
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(
                onNavigateToOnboarding = { navController.navigate(Screen.Onboarding.route) { popUpTo(Screen.Splash.route) { inclusive = true } } },
                onNavigateToDashboard = { navController.navigate(Screen.Dashboard.route) { popUpTo(Screen.Splash.route) { inclusive = true } } }
            )
        }

        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                onFinished = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Dashboard.route) {
            DashboardScreen(navController = navController)
        }

        composable(Screen.SimulatorHub.route) {
            SimulatorHubScreen(navController = navController)
        }

        composable(
            Screen.CreateSimulation.route,
            arguments = listOf(navArgument("projectId") { type = NavType.LongType; defaultValue = 0L })
        ) { back ->
            val projectId = back.arguments?.getLong("projectId") ?: 0L
            CreateSimulationScreen(
                projectId = projectId,
                onCreated = { simId -> navController.navigate(Screen.AddCategories.route(simId)) },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            Screen.AddCategories.route,
            arguments = listOf(navArgument("simId") { type = NavType.LongType })
        ) { back ->
            val simId = back.arguments?.getLong("simId") ?: 0L
            AddCategoriesScreen(
                simId = simId,
                onNext = { navController.navigate(Screen.PhysicsSettings.route(simId)) },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            Screen.PhysicsSettings.route,
            arguments = listOf(navArgument("simId") { type = NavType.LongType })
        ) { back ->
            val simId = back.arguments?.getLong("simId") ?: 0L
            PhysicsSettingsScreen(
                simId = simId,
                onStart = { navController.navigate(Screen.LiveSimulation.route(simId)) },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            Screen.LiveSimulation.route,
            arguments = listOf(navArgument("simId") { type = NavType.LongType })
        ) { back ->
            val simId = back.arguments?.getLong("simId") ?: 0L
            LiveSimulationScreen(
                simId = simId,
                onResults = {
                    navController.navigate(Screen.ResultDistribution.route(simId)) {
                        popUpTo(Screen.LiveSimulation.route) { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            Screen.ResultDistribution.route,
            arguments = listOf(navArgument("simId") { type = NavType.LongType })
        ) { back ->
            val simId = back.arguments?.getLong("simId") ?: 0L
            ResultDistributionScreen(
                simId = simId,
                onMultiRun = { navController.navigate(Screen.MultiRun.route(simId)) },
                onVisualStats = { navController.navigate(Screen.VisualStats.route(simId)) },
                onSuggestions = { navController.navigate(Screen.SmartSuggestions.route(simId)) },
                onDone = { navController.navigate(Screen.Dashboard.route) { popUpTo(Screen.Dashboard.route) { inclusive = false } } }
            )
        }

        composable(
            Screen.MultiRun.route,
            arguments = listOf(navArgument("simId") { type = NavType.LongType })
        ) { back ->
            val simId = back.arguments?.getLong("simId") ?: 0L
            MultiRunScreen(
                simId = simId,
                onResults = { navController.navigate(Screen.AverageResult.route(simId)) },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            Screen.AverageResult.route,
            arguments = listOf(navArgument("simId") { type = NavType.LongType })
        ) { back ->
            val simId = back.arguments?.getLong("simId") ?: 0L
            AverageResultScreen(
                simId = simId,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Projects.route) {
            ProjectsScreen(
                onProjectClick = { navController.navigate(Screen.ProjectDetails.route(it)) },
                navController = navController
            )
        }

        composable(
            Screen.ProjectDetails.route,
            arguments = listOf(navArgument("projectId") { type = NavType.LongType })
        ) { back ->
            val projectId = back.arguments?.getLong("projectId") ?: 0L
            ProjectDetailsScreen(
                projectId = projectId,
                onSimulationClick = { navController.navigate(Screen.ResultDistribution.route(it)) },
                onNewSimulation = { navController.navigate(Screen.CreateSimulation.route(projectId)) },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Budget.route) {
            BudgetScreen(navController = navController)
        }

        composable(Screen.Tasks.route) {
            TasksScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Scenarios.route) {
            ScenariosScreen(
                onCompare = { id1, id2 -> navController.navigate(Screen.ScenarioComparison.route(id1, id2)) },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            Screen.ScenarioComparison.route,
            arguments = listOf(
                navArgument("id1") { type = NavType.LongType },
                navArgument("id2") { type = NavType.LongType }
            )
        ) { back ->
            val id1 = back.arguments?.getLong("id1") ?: 0L
            val id2 = back.arguments?.getLong("id2") ?: 0L
            ScenarioComparisonScreen(simId1 = id1, simId2 = id2, onBack = { navController.popBackStack() })
        }

        composable(Screen.Analytics.route) {
            AnalyticsScreen(
                onVisualStats = { navController.navigate(Screen.VisualStats.route(0L)) },
                navController = navController
            )
        }

        composable(
            Screen.VisualStats.route,
            arguments = listOf(navArgument("simId") { type = NavType.LongType })
        ) { back ->
            val simId = back.arguments?.getLong("simId") ?: 0L
            VisualStatsScreen(simId = simId, onBack = { navController.popBackStack() })
        }

        composable(
            Screen.ProbabilityMap.route,
            arguments = listOf(navArgument("simId") { type = NavType.LongType })
        ) { back ->
            val simId = back.arguments?.getLong("simId") ?: 0L
            ProbabilityMapScreen(simId = simId, onBack = { navController.popBackStack() })
        }

        composable(Screen.History.route) {
            HistoryScreen(
                onSimulationClick = { navController.navigate(Screen.ResultDistribution.route(it)) },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.SavedResults.route) {
            SavedResultsScreen(
                onSimulationClick = { navController.navigate(Screen.ResultDistribution.route(it)) },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.ActivityHistory.route) {
            ActivityHistoryScreen(onBack = { navController.popBackStack() })
        }

        composable(
            Screen.SmartSuggestions.route,
            arguments = listOf(navArgument("simId") { type = NavType.LongType })
        ) { back ->
            val simId = back.arguments?.getLong("simId") ?: 0L
            SmartSuggestionsScreen(simId = simId, onBack = { navController.popBackStack() })
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                onProfile = { navController.navigate(Screen.Profile.route) },
                onHistory = { navController.navigate(Screen.History.route) },
                onActivityLog = { navController.navigate(Screen.ActivityHistory.route) },
                onScenarios = { navController.navigate(Screen.Scenarios.route) },
                navController = navController
            )
        }

        composable(Screen.Profile.route) {
            ProfileScreen(onBack = { navController.popBackStack() })
        }

        composable(
            Screen.BudgetMode.route,
            arguments = listOf(navArgument("simId") { type = NavType.LongType })
        ) { back ->
            val simId = back.arguments?.getLong("simId") ?: 0L
            BudgetModeScreen(simId = simId, onBack = { navController.popBackStack() })
        }

        composable(
            Screen.TimeMode.route,
            arguments = listOf(navArgument("simId") { type = NavType.LongType })
        ) { back ->
            val simId = back.arguments?.getLong("simId") ?: 0L
            TimeModeScreen(simId = simId, onBack = { navController.popBackStack() })
        }

        composable(
            Screen.TasksMode.route,
            arguments = listOf(navArgument("simId") { type = NavType.LongType })
        ) { back ->
            val simId = back.arguments?.getLong("simId") ?: 0L
            TasksModeScreen(simId = simId, onBack = { navController.popBackStack() })
        }
    }
}
