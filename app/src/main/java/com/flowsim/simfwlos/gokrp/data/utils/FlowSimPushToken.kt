package com.flowsim.simfwlos.gokrp.data.utils

import android.util.Log
import com.flowsim.simfwlos.gokrp.presentation.app.FlowSimApplication
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.delay
import kotlinx.coroutines.tasks.await
import java.lang.Exception

class FlowSimPushToken {

    suspend fun flowSimGetToken(
        flowSimMaxAttempts: Int = 3,
        flowSimDelayMs: Long = 1500
    ): String {

        repeat(flowSimMaxAttempts - 1) {
            try {
                val flowSimToken = FirebaseMessaging.getInstance().token.await()
                return flowSimToken
            } catch (e: Exception) {
                Log.e(FlowSimApplication.FLOW_SIM_MAIN_TAG, "Token error (attempt ${it + 1}): ${e.message}")
                delay(flowSimDelayMs)
            }
        }

        return try {
            FirebaseMessaging.getInstance().token.await()
        } catch (e: Exception) {
            Log.e(FlowSimApplication.FLOW_SIM_MAIN_TAG, "Token error final: ${e.message}")
            "null"
        }
    }


}