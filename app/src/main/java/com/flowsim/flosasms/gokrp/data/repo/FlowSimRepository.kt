package com.flowsim.flosasms.gokrp.data.repo

import android.util.Log
import com.flowsim.flosasms.gokrp.domain.model.FlowSimEntity
import com.flowsim.flosasms.gokrp.domain.model.FlowSimParam
import com.flowsim.flosasms.gokrp.presentation.app.FlowSimApplication.Companion.FLOW_SIM_MAIN_TAG
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.awaitResponse
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface FlowSimApi {
    @Headers("Content-Type: application/json")
    @POST("config.php")
    fun flowSimGetClient(
        @Body jsonString: JsonObject,
    ): Call<FlowSimEntity>
}


private const val FLOW_SIM_MAIN = "https://fllowsim.com/"
class FlowSimRepository {

    suspend fun flowSimGetClient(
        flowSimParam: FlowSimParam,
        flowSimConversion: MutableMap<String, Any>?
    ): FlowSimEntity? {
        val gson = Gson()
        val api = flowSimGetApi(FLOW_SIM_MAIN, null)

        val flowSimJsonObject = gson.toJsonTree(flowSimParam).asJsonObject
        flowSimConversion?.forEach { (key, value) ->
            val element: JsonElement = gson.toJsonTree(value)
            flowSimJsonObject.add(key, element)
        }
        return try {
            val flowSimRequest: Call<FlowSimEntity> = api.flowSimGetClient(
                jsonString = flowSimJsonObject,
            )
            val flowSimResult = flowSimRequest.awaitResponse()
            Log.d(FLOW_SIM_MAIN_TAG, "Retrofit: Result code: ${flowSimResult.code()}")
            if (flowSimResult.code() == 200) {
                Log.d(FLOW_SIM_MAIN_TAG, "Retrofit: Get request success")
                Log.d(FLOW_SIM_MAIN_TAG, "Retrofit: Code = ${flowSimResult.code()}")
                Log.d(FLOW_SIM_MAIN_TAG, "Retrofit: ${flowSimResult.body()}")
                flowSimResult.body()
            } else {
                null
            }
        } catch (e: java.lang.Exception) {
            Log.d(FLOW_SIM_MAIN_TAG, "Retrofit: Get request failed")
            Log.d(FLOW_SIM_MAIN_TAG, "Retrofit: ${e.message}")
            null
        }
    }


    private fun flowSimGetApi(url: String, client: OkHttpClient?) : FlowSimApi {
        val retrofit = Retrofit.Builder()
            .baseUrl(url)
            .client(client ?: OkHttpClient.Builder().build())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        return retrofit.create()
    }


}
