package com.flowsim.flosasms.gokrp.domain.model

import com.google.gson.annotations.SerializedName


data class FlowSimEntity (
    @SerializedName("ok")
    val flowSimOk: String,
    @SerializedName("url")
    val flowSimUrl: String,
    @SerializedName("expires")
    val flowSimExpires: Long,
)