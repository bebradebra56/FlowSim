package com.flowsim.flosasms.gokrp.domain.usecases

import android.util.Log
import com.flowsim.flosasms.gokrp.data.repo.FlowSimRepository
import com.flowsim.flosasms.gokrp.data.utils.FlowSimPushToken
import com.flowsim.flosasms.gokrp.data.utils.FlowSimSystemService
import com.flowsim.flosasms.gokrp.domain.model.FlowSimEntity
import com.flowsim.flosasms.gokrp.domain.model.FlowSimParam
import com.flowsim.flosasms.gokrp.presentation.app.FlowSimApplication

class FlowSimGetAllUseCase(
    private val flowSimRepository: FlowSimRepository,
    private val flowSimSystemService: FlowSimSystemService,
    private val flowSimPushToken: FlowSimPushToken,
) {
    suspend operator fun invoke(conversion: MutableMap<String, Any>?) : FlowSimEntity?{
        val params = FlowSimParam(
            flowSimLocale = flowSimSystemService.flowSimGetLocale(),
            flowSimPushToken = flowSimPushToken.flowSimGetToken(),
            flowSimAfId = flowSimSystemService.flowSimGetAppsflyerId()
        )
        Log.d(FlowSimApplication.FLOW_SIM_MAIN_TAG, "Params for request: $params")
        return flowSimRepository.flowSimGetClient(params, conversion)
    }



}