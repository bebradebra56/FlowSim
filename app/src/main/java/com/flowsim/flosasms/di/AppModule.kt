package com.flowsim.flosasms.di

import com.flowsim.flosasms.data.AppPreferences
import com.flowsim.flosasms.data.db.AppDatabase
import com.flowsim.flosasms.data.repository.*
import com.flowsim.flosasms.ui.screens.analytics.AnalyticsViewModel
import com.flowsim.flosasms.ui.screens.budget.BudgetViewModel
import com.flowsim.flosasms.ui.screens.dashboard.DashboardViewModel
import com.flowsim.flosasms.ui.screens.history.HistoryViewModel
import com.flowsim.flosasms.ui.screens.profile.ProfileViewModel
import com.flowsim.flosasms.ui.screens.projects.ProjectDetailsViewModel
import com.flowsim.flosasms.ui.screens.projects.ProjectsViewModel
import com.flowsim.flosasms.ui.screens.scenarios.ScenariosViewModel
import com.flowsim.flosasms.ui.screens.settings.SettingsViewModel
import com.flowsim.flosasms.ui.screens.simulator.SimulatorViewModel
import com.flowsim.flosasms.ui.screens.tasks.TasksViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    // Database
    single { AppDatabase.build(get()) }

    // DAOs
    single { get<AppDatabase>().projectDao() }
    single { get<AppDatabase>().simulationDao() }
    single { get<AppDatabase>().categoryDao() }
    single { get<AppDatabase>().simulationResultDao() }
    single { get<AppDatabase>().taskDao() }
    single { get<AppDatabase>().budgetItemDao() }
    single { get<AppDatabase>().scenarioDao() }
    single { get<AppDatabase>().activityLogDao() }

    // Preferences
    single { AppPreferences(get()) }

    // Repositories
    single { ProjectRepository(get()) }
    single { SimulationRepository(get(), get(), get(), get()) }
    single { TaskRepository(get()) }
    single { BudgetRepository(get()) }
    single { ScenarioRepository(get()) }
    single { ActivityRepository(get()) }

    // ViewModels
    viewModel { DashboardViewModel(get(), get(), get(), get()) }
    viewModel { (simId: Long) -> SimulatorViewModel(simId, get(), get()) }
    viewModel { ProjectsViewModel(get(), get()) }
    viewModel { (projectId: Long) -> ProjectDetailsViewModel(projectId, get(), get(), get()) }
    viewModel { TasksViewModel(get(), get()) }
    viewModel { BudgetViewModel(get(), get()) }
    viewModel { ScenariosViewModel(get(), get()) }
    viewModel { AnalyticsViewModel(get(), get()) }
    viewModel { HistoryViewModel(get(), get()) }
    viewModel { SettingsViewModel(get()) }
    viewModel { ProfileViewModel(get()) }
}
