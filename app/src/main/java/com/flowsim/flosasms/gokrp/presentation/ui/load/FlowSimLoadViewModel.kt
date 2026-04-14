package com.flowsim.flosasms.gokrp.presentation.ui.load

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flowsim.flosasms.gokrp.data.shar.FlowSimSharedPreference
import com.flowsim.flosasms.gokrp.data.utils.FlowSimSystemService
import com.flowsim.flosasms.gokrp.domain.usecases.FlowSimGetAllUseCase
import com.flowsim.flosasms.gokrp.presentation.app.FlowSimAppsFlyerState
import com.flowsim.flosasms.gokrp.presentation.app.FlowSimApplication
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FlowSimLoadViewModel(
    private val flowSimGetAllUseCase: FlowSimGetAllUseCase,
    private val flowSimSharedPreference: FlowSimSharedPreference,
    private val flowSimSystemService: FlowSimSystemService
) : ViewModel() {

    private val _flowSimHomeScreenState: MutableStateFlow<FlowSimHomeScreenState> =
        MutableStateFlow(FlowSimHomeScreenState.FlowSimLoading)
    val flowSimHomeScreenState = _flowSimHomeScreenState.asStateFlow()

    private var flowSimGetApps = false


    init {
        viewModelScope.launch {
            when (flowSimSharedPreference.flowSimAppState) {
                0 -> {
                    if (flowSimSystemService.flowSimIsOnline()) {
                        FlowSimApplication.flowSimConversionFlow.collect {
                            when(it) {
                                FlowSimAppsFlyerState.FlowSimDefault -> {}
                                FlowSimAppsFlyerState.FlowSimError -> {
                                    flowSimSharedPreference.flowSimAppState = 2
                                    _flowSimHomeScreenState.value =
                                        FlowSimHomeScreenState.FlowSimError
                                    flowSimGetApps = true
                                }
                                is FlowSimAppsFlyerState.FlowSimSuccess -> {
                                    if (!flowSimGetApps) {
                                        flowSimGetData(it.flowSimData)
                                        flowSimGetApps = true
                                    }
                                }
                            }
                        }
                    } else {
                        _flowSimHomeScreenState.value =
                            FlowSimHomeScreenState.FlowSimNotInternet
                    }
                }
                1 -> {
                    if (flowSimSystemService.flowSimIsOnline()) {
                        if (FlowSimApplication.FLOW_SIM_FB_LI != null) {
                            _flowSimHomeScreenState.value =
                                FlowSimHomeScreenState.FlowSimSuccess(
                                    FlowSimApplication.FLOW_SIM_FB_LI.toString()
                                )
                        } else if (System.currentTimeMillis() / 1000 > flowSimSharedPreference.flowSimExpired) {
                            Log.d(FlowSimApplication.FLOW_SIM_MAIN_TAG, "Current time more then expired, repeat request")
                            FlowSimApplication.flowSimConversionFlow.collect {
                                when(it) {
                                    FlowSimAppsFlyerState.FlowSimDefault -> {}
                                    FlowSimAppsFlyerState.FlowSimError -> {
                                        _flowSimHomeScreenState.value =
                                            FlowSimHomeScreenState.FlowSimSuccess(
                                                flowSimSharedPreference.flowSimSavedUrl
                                            )
                                        flowSimGetApps = true
                                    }
                                    is FlowSimAppsFlyerState.FlowSimSuccess -> {
                                        if (!flowSimGetApps) {
                                            flowSimGetData(it.flowSimData)
                                            flowSimGetApps = true
                                        }
                                    }
                                }
                            }
                        } else {
                            Log.d(FlowSimApplication.FLOW_SIM_MAIN_TAG, "Current time less then expired, use saved url")
                            _flowSimHomeScreenState.value =
                                FlowSimHomeScreenState.FlowSimSuccess(
                                    flowSimSharedPreference.flowSimSavedUrl
                                )
                        }
                    } else {
                        _flowSimHomeScreenState.value =
                            FlowSimHomeScreenState.FlowSimNotInternet
                    }
                }
                2 -> {
                    _flowSimHomeScreenState.value =
                        FlowSimHomeScreenState.FlowSimError
                }
            }
        }
    }


    private suspend fun flowSimGetData(conversation: MutableMap<String, Any>?) {
        val flowSimData = flowSimGetAllUseCase.invoke(conversation)
        if (flowSimSharedPreference.flowSimAppState == 0) {
            if (flowSimData == null) {
                flowSimSharedPreference.flowSimAppState = 2
                _flowSimHomeScreenState.value =
                    FlowSimHomeScreenState.FlowSimError
            } else {
                flowSimSharedPreference.flowSimAppState = 1
                flowSimSharedPreference.apply {
                    flowSimExpired = flowSimData.flowSimExpires
                    flowSimSavedUrl = flowSimData.flowSimUrl
                }
                _flowSimHomeScreenState.value =
                    FlowSimHomeScreenState.FlowSimSuccess(flowSimData.flowSimUrl)
            }
        } else  {
            if (flowSimData == null) {
                _flowSimHomeScreenState.value =
                    FlowSimHomeScreenState.FlowSimSuccess(
                        flowSimSharedPreference.flowSimSavedUrl
                    )
            } else {
                flowSimSharedPreference.apply {
                    flowSimExpired = flowSimData.flowSimExpires
                    flowSimSavedUrl = flowSimData.flowSimUrl
                }
                _flowSimHomeScreenState.value =
                    FlowSimHomeScreenState.FlowSimSuccess(flowSimData.flowSimUrl)
            }
        }
    }


    sealed class FlowSimHomeScreenState {
        data object FlowSimLoading : FlowSimHomeScreenState()
        data object FlowSimError : FlowSimHomeScreenState()
        data class FlowSimSuccess(val data: String) : FlowSimHomeScreenState()
        data object FlowSimNotInternet: FlowSimHomeScreenState()
    }
}