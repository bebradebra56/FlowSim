package com.flowsim.simfwlos.gokrp.domain.model

import com.google.gson.annotations.SerializedName


private const val FLOW_SIM_A = "com.flowsim.simfwlos"
private const val FLOW_SIM_B = "flowsim-d93a5"
data class FlowSimParam (
    @SerializedName("af_id")
    val flowSimAfId: String,
    @SerializedName("bundle_id")
    val flowSimBundleId: String = FLOW_SIM_A,
    @SerializedName("os")
    val flowSimOs: String = "Android",
    @SerializedName("store_id")
    val flowSimStoreId: String = FLOW_SIM_A,
    @SerializedName("locale")
    val flowSimLocale: String,
    @SerializedName("push_token")
    val flowSimPushToken: String,
    @SerializedName("firebase_project_id")
    val flowSimFirebaseProjectId: String = FLOW_SIM_B,

    )