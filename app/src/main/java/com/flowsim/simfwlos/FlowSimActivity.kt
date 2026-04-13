package com.flowsim.simfwlos

import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import com.flowsim.simfwlos.gokrp.FlowSimGlobalLayoutUtil
import com.flowsim.simfwlos.gokrp.flowSimSetupSystemBars
import com.flowsim.simfwlos.gokrp.presentation.app.FlowSimApplication
import com.flowsim.simfwlos.gokrp.presentation.pushhandler.FlowSimPushHandler
import org.koin.android.ext.android.inject

class FlowSimActivity : AppCompatActivity() {

    private val flowSimPushHandler by inject<FlowSimPushHandler>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        flowSimSetupSystemBars()
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContentView(R.layout.activity_flow_sim)

        val flowSimRootView = findViewById<View>(android.R.id.content)
        FlowSimGlobalLayoutUtil().flowSimAssistActivity(this)
        ViewCompat.setOnApplyWindowInsetsListener(flowSimRootView) { flowSimView, flowSimInsets ->
            val flowSimSystemBars = flowSimInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            val flowSimDisplayCutout = flowSimInsets.getInsets(WindowInsetsCompat.Type.displayCutout())
            val flowSimIme = flowSimInsets.getInsets(WindowInsetsCompat.Type.ime())


            val flowSimTopPadding = maxOf(flowSimSystemBars.top, flowSimDisplayCutout.top)
            val flowSimLeftPadding = maxOf(flowSimSystemBars.left, flowSimDisplayCutout.left)
            val flowSimRightPadding = maxOf(flowSimSystemBars.right, flowSimDisplayCutout.right)
            window.setSoftInputMode(FlowSimApplication.flowSimInputMode)

            if (window.attributes.softInputMode == WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN) {
                Log.d(FlowSimApplication.FLOW_SIM_MAIN_TAG, "ADJUST PUN")
                val flowSimBottomInset = maxOf(flowSimSystemBars.bottom, flowSimDisplayCutout.bottom)

                flowSimView.setPadding(flowSimLeftPadding, flowSimTopPadding, flowSimRightPadding, 0)

                flowSimView.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                    bottomMargin = flowSimBottomInset
                }
            } else {
                Log.d(FlowSimApplication.FLOW_SIM_MAIN_TAG, "ADJUST RESIZE")

                val flowSimBottomInset = maxOf(flowSimSystemBars.bottom, flowSimDisplayCutout.bottom, flowSimIme.bottom)

                flowSimView.setPadding(flowSimLeftPadding, flowSimTopPadding, flowSimRightPadding, 0)

                flowSimView.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                    bottomMargin = flowSimBottomInset
                }
            }



            WindowInsetsCompat.CONSUMED
        }
        Log.d(FlowSimApplication.FLOW_SIM_MAIN_TAG, "Activity onCreate()")
        flowSimPushHandler.flowSimHandlePush(intent.extras)
    }
    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            flowSimSetupSystemBars()
        }
    }

    override fun onResume() {
        super.onResume()
        flowSimSetupSystemBars()
    }
}