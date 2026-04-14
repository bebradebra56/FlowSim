package com.flowsim.flosasms.gokrp.presentation.app

import android.app.Application
import android.util.Log
import android.view.WindowManager
import com.appsflyer.AppsFlyerConversionListener
import com.appsflyer.AppsFlyerLib
import com.appsflyer.attribution.AppsFlyerRequestListener
import com.appsflyer.deeplink.DeepLink
import com.appsflyer.deeplink.DeepLinkListener
import com.appsflyer.deeplink.DeepLinkResult
import com.flowsim.flosasms.di.appModule
import com.flowsim.flosasms.gokrp.presentation.di.flowSimModule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.awaitResponse
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query


sealed interface FlowSimAppsFlyerState {
    data object FlowSimDefault : FlowSimAppsFlyerState
    data class FlowSimSuccess(val flowSimData: MutableMap<String, Any>?) :
        FlowSimAppsFlyerState

    data object FlowSimError : FlowSimAppsFlyerState
}

interface FlowSimAppsApi {
    @Headers("Content-Type: application/json")
    @GET(FLOW_SIM_LIN)
    fun flowSimGetClient(
        @Query("devkey") devkey: String,
        @Query("device_id") deviceId: String,
    ): Call<MutableMap<String, Any>?>
}

private const val FLOW_SIM_APP_DEV = "gXaSyFYZUxTSsUuxMc3ekF"
private const val FLOW_SIM_LIN = "com.flowsim.simfwlos"

class FlowSimApplication : Application() {

    private var flowSimIsResumed = false
    ///////
    private var flowSimConversionTimeoutJob: Job? = null
    private var flowSimDeepLinkData: MutableMap<String, Any>? = null

    override fun onCreate() {
        super.onCreate()

        val appsflyer = AppsFlyerLib.getInstance()
        flowSimSetDebufLogger(appsflyer)
        flowSimMinTimeBetween(appsflyer)

        AppsFlyerLib.getInstance().subscribeForDeepLink(object : DeepLinkListener {
            override fun onDeepLinking(p0: DeepLinkResult) {
                when (p0.status) {
                    DeepLinkResult.Status.FOUND -> {
                        flowSimExtractDeepMap(p0.deepLink)
                        Log.d(FLOW_SIM_MAIN_TAG, "onDeepLinking found: ${p0.deepLink}")

                    }

                    DeepLinkResult.Status.NOT_FOUND -> {
                        Log.d(FLOW_SIM_MAIN_TAG, "onDeepLinking not found: ${p0.deepLink}")
                    }

                    DeepLinkResult.Status.ERROR -> {
                        Log.d(FLOW_SIM_MAIN_TAG, "onDeepLinking error: ${p0.error}")
                    }
                }
            }

        })


        appsflyer.init(
            FLOW_SIM_APP_DEV,
            object : AppsFlyerConversionListener {
                override fun onConversionDataSuccess(p0: MutableMap<String, Any>?) {
                    //////////
                    flowSimConversionTimeoutJob?.cancel()
                    Log.d(FLOW_SIM_MAIN_TAG, "onConversionDataSuccess: $p0")

                    val afStatus = p0?.get("af_status")?.toString() ?: "null"
                    if (afStatus == "Organic") {
                        CoroutineScope(Dispatchers.IO).launch {
                            try {
                                delay(5000)
                                val api = flowSimGetApi(
                                    "https://gcdsdk.appsflyer.com/install_data/v4.0/",
                                    null
                                )
                                val response = api.flowSimGetClient(
                                    devkey = FLOW_SIM_APP_DEV,
                                    deviceId = flowSimGetAppsflyerId()
                                ).awaitResponse()

                                val resp = response.body()
                                Log.d(FLOW_SIM_MAIN_TAG, "After 5s: $resp")
                                if (resp?.get("af_status") == "Organic" || resp?.get("af_status") == null) {
                                    flowSimResume(
                                        FlowSimAppsFlyerState.FlowSimError
                                    )
                                } else {
                                    flowSimResume(
                                        FlowSimAppsFlyerState.FlowSimSuccess(
                                            resp
                                        )
                                    )
                                }
                            } catch (d: Exception) {
                                Log.d(FLOW_SIM_MAIN_TAG, "Error: ${d.message}")
                                flowSimResume(FlowSimAppsFlyerState.FlowSimError)
                            }
                        }
                    } else {
                        flowSimResume(
                            FlowSimAppsFlyerState.FlowSimSuccess(
                                p0
                            )
                        )
                    }
                }

                override fun onConversionDataFail(p0: String?) {
                    /////////
                    flowSimConversionTimeoutJob?.cancel()
                    Log.d(FLOW_SIM_MAIN_TAG, "onConversionDataFail: $p0")
                    flowSimResume(FlowSimAppsFlyerState.FlowSimError)
                }

                override fun onAppOpenAttribution(p0: MutableMap<String, String>?) {
                    Log.d(FLOW_SIM_MAIN_TAG, "onAppOpenAttribution")
                }

                override fun onAttributionFailure(p0: String?) {
                    Log.d(FLOW_SIM_MAIN_TAG, "onAttributionFailure: $p0")
                }
            },
            this
        )

        appsflyer.start(this, FLOW_SIM_APP_DEV, object :
            AppsFlyerRequestListener {
            override fun onSuccess() {
                Log.d(FLOW_SIM_MAIN_TAG, "AppsFlyer started")
            }

            override fun onError(p0: Int, p1: String) {
                Log.d(FLOW_SIM_MAIN_TAG, "AppsFlyer start error: $p0 - $p1")
            }
        })
        ///////////
        flowSimStartConversionTimeout()
        startKoin {
            androidLogger(Level.DEBUG)
            androidContext(this@FlowSimApplication)
            modules(
                listOf(
                    flowSimModule, appModule
                )
            )
        }
    }

    private fun flowSimExtractDeepMap(dl: DeepLink) {
        val map = mutableMapOf<String, Any>()
        dl.deepLinkValue?.let { map["deep_link_value"] = it }
        dl.mediaSource?.let { map["media_source"] = it }
        dl.campaign?.let { map["campaign"] = it }
        dl.campaignId?.let { map["campaign_id"] = it }
        dl.afSub1?.let { map["af_sub1"] = it }
        dl.afSub2?.let { map["af_sub2"] = it }
        dl.afSub3?.let { map["af_sub3"] = it }
        dl.afSub4?.let { map["af_sub4"] = it }
        dl.afSub5?.let { map["af_sub5"] = it }
        dl.matchType?.let { map["match_type"] = it }
        dl.clickHttpReferrer?.let { map["click_http_referrer"] = it }
        dl.getStringValue("timestamp")?.let { map["timestamp"] = it }
        dl.isDeferred?.let { map["is_deferred"] = it }
        for (i in 1..10) {
            val key = "deep_link_sub$i"
            dl.getStringValue(key)?.let {
                if (!map.containsKey(key)) {
                    map[key] = it
                }
            }
        }
        Log.d(FLOW_SIM_MAIN_TAG, "Extracted DeepLink data: $map")
        flowSimDeepLinkData = map
    }
    /////////////////

    private fun flowSimStartConversionTimeout() {
        flowSimConversionTimeoutJob = CoroutineScope(Dispatchers.Main).launch {
            delay(30000)
            if (!flowSimIsResumed) {
                Log.d(FLOW_SIM_MAIN_TAG, "TIMEOUT: No conversion data received in 30s")
                flowSimResume(FlowSimAppsFlyerState.FlowSimError)
            }
        }
    }

    private fun flowSimResume(state: FlowSimAppsFlyerState) {
        ////////////
        flowSimConversionTimeoutJob?.cancel()
        if (state is FlowSimAppsFlyerState.FlowSimSuccess) {
            val convData = state.flowSimData ?: mutableMapOf()
            val deepData = flowSimDeepLinkData ?: mutableMapOf()
            val merged = mutableMapOf<String, Any>().apply {
                putAll(convData)
                for ((key, value) in deepData) {
                    if (!containsKey(key)) {
                        put(key, value)
                    }
                }
            }
            if (!flowSimIsResumed) {
                flowSimIsResumed = true
                flowSimConversionFlow.value =
                    FlowSimAppsFlyerState.FlowSimSuccess(merged)
            }
        } else {
            if (!flowSimIsResumed) {
                flowSimIsResumed = true
                flowSimConversionFlow.value = state
            }
        }
    }

    private fun flowSimGetAppsflyerId(): String {
        val appsflyrid = AppsFlyerLib.getInstance().getAppsFlyerUID(this) ?: ""
        Log.d(FLOW_SIM_MAIN_TAG, "AppsFlyer: AppsFlyer Id = $appsflyrid")
        return appsflyrid
    }

    private fun flowSimSetDebufLogger(appsflyer: AppsFlyerLib) {
        appsflyer.setDebugLog(true)
    }

    private fun flowSimMinTimeBetween(appsflyer: AppsFlyerLib) {
        appsflyer.setMinTimeBetweenSessions(0)
    }

    private fun flowSimGetApi(url: String, client: OkHttpClient?): FlowSimAppsApi {
        val retrofit = Retrofit.Builder()
            .baseUrl(url)
            .client(client ?: OkHttpClient.Builder().build())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        return retrofit.create()
    }

    companion object {
        var flowSimInputMode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN
        val flowSimConversionFlow: MutableStateFlow<FlowSimAppsFlyerState> = MutableStateFlow(
            FlowSimAppsFlyerState.FlowSimDefault
        )
        var FLOW_SIM_FB_LI: String? = null
        const val FLOW_SIM_MAIN_TAG = "FlowSimMainTag"
    }
}