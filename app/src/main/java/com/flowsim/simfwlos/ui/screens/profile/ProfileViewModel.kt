package com.flowsim.simfwlos.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flowsim.simfwlos.data.AppPreferences
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ProfileViewModel(private val prefs: AppPreferences) : ViewModel() {
    val userName: StateFlow<String> = prefs.userName
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    fun updateName(name: String) {
        viewModelScope.launch { prefs.setUserName(name) }
    }
}
