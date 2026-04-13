package com.flowsim.simfwlos.gokrp

import android.app.Activity
import android.graphics.Rect
import android.view.View
import android.widget.FrameLayout
import com.flowsim.simfwlos.gokrp.presentation.app.FlowSimApplication

class FlowSimGlobalLayoutUtil {

    private var flowSimMChildOfContent: View? = null
    private var flowSimUsableHeightPrevious = 0

    fun flowSimAssistActivity(activity: Activity) {
        val content = activity.findViewById<FrameLayout>(android.R.id.content)
        flowSimMChildOfContent = content.getChildAt(0)

        flowSimMChildOfContent?.viewTreeObserver?.addOnGlobalLayoutListener {
            possiblyResizeChildOfContent(activity)
        }
    }

    private fun possiblyResizeChildOfContent(activity: Activity) {
        val flowSimUsableHeightNow = flowSimComputeUsableHeight()
        if (flowSimUsableHeightNow != flowSimUsableHeightPrevious) {
            val flowSimUsableHeightSansKeyboard = flowSimMChildOfContent?.rootView?.height ?: 0
            val flowSimHeightDifference = flowSimUsableHeightSansKeyboard - flowSimUsableHeightNow

            if (flowSimHeightDifference > (flowSimUsableHeightSansKeyboard / 4)) {
                activity.window.setSoftInputMode(FlowSimApplication.flowSimInputMode)
            } else {
                activity.window.setSoftInputMode(FlowSimApplication.flowSimInputMode)
            }
//            mChildOfContent?.requestLayout()
            flowSimUsableHeightPrevious = flowSimUsableHeightNow
        }
    }

    private fun flowSimComputeUsableHeight(): Int {
        val r = Rect()
        flowSimMChildOfContent?.getWindowVisibleDisplayFrame(r)
        return r.bottom - r.top  // Visible height без status bar
    }
}