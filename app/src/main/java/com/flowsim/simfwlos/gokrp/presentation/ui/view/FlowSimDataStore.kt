package com.flowsim.simfwlos.gokrp.presentation.ui.view

import android.annotation.SuppressLint
import android.widget.FrameLayout
import androidx.lifecycle.ViewModel

class FlowSimDataStore : ViewModel(){
    val flowSimViList: MutableList<FlowSimVi> = mutableListOf()
    var flowSimIsFirstCreate = true
    @SuppressLint("StaticFieldLeak")
    lateinit var flowSimContainerView: FrameLayout
    @SuppressLint("StaticFieldLeak")
    lateinit var flowSimView: FlowSimVi

}