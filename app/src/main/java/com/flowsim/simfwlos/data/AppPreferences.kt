package com.flowsim.simfwlos.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "flowsim_prefs")

class AppPreferences(context: Context) {
    private val ds = context.dataStore

    companion object {
        val KEY_ONBOARDING = booleanPreferencesKey("onboarding_done")
        val KEY_USER_NAME = stringPreferencesKey("user_name")
        val KEY_GRAVITY = floatPreferencesKey("default_gravity")
        val KEY_BOUNCE = floatPreferencesKey("default_bounce")
        val KEY_SPREAD = floatPreferencesKey("default_spread")
        val KEY_ANIMATIONS = booleanPreferencesKey("show_animations")
        val KEY_BALL_COUNT = intPreferencesKey("default_ball_count")
    }

    val isOnboardingCompleted: Flow<Boolean> = ds.data.map { it[KEY_ONBOARDING] ?: false }
    val userName: Flow<String> = ds.data.map { it[KEY_USER_NAME] ?: "" }
    val defaultGravity: Flow<Float> = ds.data.map { it[KEY_GRAVITY] ?: 0.4f }
    val defaultBounce: Flow<Float> = ds.data.map { it[KEY_BOUNCE] ?: 0.5f }
    val defaultSpread: Flow<Float> = ds.data.map { it[KEY_SPREAD] ?: 0.5f }
    val showAnimations: Flow<Boolean> = ds.data.map { it[KEY_ANIMATIONS] ?: true }
    val defaultBallCount: Flow<Int> = ds.data.map { it[KEY_BALL_COUNT] ?: 20 }

    suspend fun setOnboardingCompleted() = ds.edit { it[KEY_ONBOARDING] = true }
    suspend fun setUserName(name: String) = ds.edit { it[KEY_USER_NAME] = name }
    suspend fun setDefaultGravity(v: Float) = ds.edit { it[KEY_GRAVITY] = v }
    suspend fun setDefaultBounce(v: Float) = ds.edit { it[KEY_BOUNCE] = v }
    suspend fun setDefaultSpread(v: Float) = ds.edit { it[KEY_SPREAD] = v }
    suspend fun setShowAnimations(v: Boolean) = ds.edit { it[KEY_ANIMATIONS] = v }
    suspend fun setDefaultBallCount(v: Int) = ds.edit { it[KEY_BALL_COUNT] = v }
}
