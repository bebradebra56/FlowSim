package com.flowsim.flosasms.gokrp.data.shar

import android.content.Context
import androidx.core.content.edit

class FlowSimSharedPreference(context: Context) {
    private val flowSimPrefs = context.getSharedPreferences("flowSimSharedPrefsAb", Context.MODE_PRIVATE)

    var flowSimSavedUrl: String
        get() = flowSimPrefs.getString(FLOW_SIM_SAVED_URL, "") ?: ""
        set(value) = flowSimPrefs.edit { putString(FLOW_SIM_SAVED_URL, value) }

    var flowSimExpired : Long
        get() = flowSimPrefs.getLong(FLOW_SIM_EXPIRED, 0L)
        set(value) = flowSimPrefs.edit { putLong(FLOW_SIM_EXPIRED, value) }

    var flowSimAppState: Int
        get() = flowSimPrefs.getInt(FLOW_SIM_APPLICATION_STATE, 0)
        set(value) = flowSimPrefs.edit { putInt(FLOW_SIM_APPLICATION_STATE, value) }

    var flowSimNotificationRequest: Long
        get() = flowSimPrefs.getLong(FLOW_SIM_NOTIFICAITON_REQUEST, 0L)
        set(value) = flowSimPrefs.edit { putLong(FLOW_SIM_NOTIFICAITON_REQUEST, value) }


    var flowSimNotificationState:Int
        get() = flowSimPrefs.getInt(FLOW_SIM_NOTIFICATION_STATE, 0)
        set(value) = flowSimPrefs.edit { putInt(FLOW_SIM_NOTIFICATION_STATE, value) }

    companion object {
        private const val FLOW_SIM_NOTIFICATION_STATE = "flowSimNotificationState"
        private const val FLOW_SIM_SAVED_URL = "flowSimSavedUrl"
        private const val FLOW_SIM_EXPIRED = "flowSimExpired"
        private const val FLOW_SIM_APPLICATION_STATE = "flowSimApplicationState"
        private const val FLOW_SIM_NOTIFICAITON_REQUEST = "flowSimNotificationRequest"
    }
}