package com.flowsim.simfwlos.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        ProjectEntity::class,
        SimulationEntity::class,
        CategoryEntity::class,
        SimulationResultEntity::class,
        TaskEntity::class,
        BudgetItemEntity::class,
        ScenarioEntity::class,
        ActivityLogEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun projectDao(): ProjectDao
    abstract fun simulationDao(): SimulationDao
    abstract fun categoryDao(): CategoryDao
    abstract fun simulationResultDao(): SimulationResultDao
    abstract fun taskDao(): TaskDao
    abstract fun budgetItemDao(): BudgetItemDao
    abstract fun scenarioDao(): ScenarioDao
    abstract fun activityLogDao(): ActivityLogDao

    companion object {
        fun build(context: Context): AppDatabase =
            Room.databaseBuilder(context, AppDatabase::class.java, "flowsim.db")
                .fallbackToDestructiveMigration()
                .build()
    }
}
