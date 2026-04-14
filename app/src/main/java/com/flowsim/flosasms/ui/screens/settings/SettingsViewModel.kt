package com.flowsim.flosasms.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flowsim.flosasms.data.AppPreferences
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class SettingsState(
    val defaultGravity: Float = 0.4f,
    val defaultBounce: Float = 0.5f,
    val defaultSpread: Float = 0.5f,
    val showAnimations: Boolean = true,
    val defaultBallCount: Int = 20
)

class SettingsViewModel(private val prefs: AppPreferences) : ViewModel() {

    val state: StateFlow<SettingsState> = combine(
        prefs.defaultGravity,
        prefs.defaultBounce,
        prefs.defaultSpread,
        prefs.showAnimations,
        prefs.defaultBallCount
    ) { gravity, bounce, spread, anim, balls ->
        SettingsState(gravity, bounce, spread, anim, balls)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SettingsState())

    fun setGravity(v: Float) { viewModelScope.launch { prefs.setDefaultGravity(v) } }
    fun setBounce(v: Float) { viewModelScope.launch { prefs.setDefaultBounce(v) } }
    fun setSpread(v: Float) { viewModelScope.launch { prefs.setDefaultSpread(v) } }
    fun setShowAnimations(v: Boolean) { viewModelScope.launch { prefs.setShowAnimations(v) } }
    fun setBallCount(v: Int) { viewModelScope.launch { prefs.setDefaultBallCount(v) } }
}
