package com.flowsim.flosasms.gokrp.presentation.pushhandler

import android.os.Bundle
import android.util.Log
import com.flowsim.flosasms.gokrp.presentation.app.FlowSimApplication

class FlowSimPushHandler {
    fun flowSimHandlePush(extras: Bundle?) {
        Log.d(FlowSimApplication.FLOW_SIM_MAIN_TAG, "Extras from Push = ${extras?.keySet()}")
        if (extras != null) {
            val map = flowSimBundleToMap(extras)
            Log.d(FlowSimApplication.FLOW_SIM_MAIN_TAG, "Map from Push = $map")
            map?.let {
                if (map.containsKey("url")) {
                    FlowSimApplication.FLOW_SIM_FB_LI = map["url"]
                    Log.d(FlowSimApplication.FLOW_SIM_MAIN_TAG, "UrlFromActivity = $map")
                }
            }
        } else {
            Log.d(FlowSimApplication.FLOW_SIM_MAIN_TAG, "Push data no!")
        }
    }

    private fun flowSimBundleToMap(extras: Bundle): Map<String, String?>? {
        val map: MutableMap<String, String?> = HashMap()
        val ks = extras.keySet()
        val iterator: Iterator<String> = ks.iterator()
        while (iterator.hasNext()) {
            val key = iterator.next()
            map[key] = extras.getString(key)
        }
        return map
    }

}