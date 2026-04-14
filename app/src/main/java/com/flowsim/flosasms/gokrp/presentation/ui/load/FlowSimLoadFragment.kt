package com.flowsim.flosasms.gokrp.presentation.ui.load

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.flowsim.flosasms.MainActivity
import com.flowsim.flosasms.R
import com.flowsim.flosasms.databinding.FragmentLoadFlowSimBinding
import com.flowsim.flosasms.gokrp.data.shar.FlowSimSharedPreference
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel


class FlowSimLoadFragment : Fragment(R.layout.fragment_load_flow_sim) {
    private lateinit var flowSimLoadBinding: FragmentLoadFlowSimBinding

    private val flowSimLoadViewModel by viewModel<FlowSimLoadViewModel>()

    private val flowSimSharedPreference by inject<FlowSimSharedPreference>()

    private var flowSimUrl = ""

    private val flowSimRequestNotificationPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        flowSimSharedPreference.flowSimNotificationState = 2
        flowSimNavigateToSuccess(flowSimUrl)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        flowSimLoadBinding = FragmentLoadFlowSimBinding.bind(view)

        flowSimLoadBinding.flowSimGrandButton.setOnClickListener {
            val flowSimPermission = Manifest.permission.POST_NOTIFICATIONS
            flowSimRequestNotificationPermission.launch(flowSimPermission)
        }

        flowSimLoadBinding.flowSimSkipButton.setOnClickListener {
            flowSimSharedPreference.flowSimNotificationState = 1
            flowSimSharedPreference.flowSimNotificationRequest =
                (System.currentTimeMillis() / 1000) + 259200
            flowSimNavigateToSuccess(flowSimUrl)
        }

        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                flowSimLoadViewModel.flowSimHomeScreenState.collect {
                    when (it) {
                        is FlowSimLoadViewModel.FlowSimHomeScreenState.FlowSimLoading -> {

                        }

                        is FlowSimLoadViewModel.FlowSimHomeScreenState.FlowSimError -> {
                            requireActivity().startActivity(
                                Intent(
                                    requireContext(),
                                    MainActivity::class.java
                                )
                            )
                            requireActivity().finish()
                        }

                        is FlowSimLoadViewModel.FlowSimHomeScreenState.FlowSimSuccess -> {
                            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.S_V2) {
                                val flowSimNotificationState = flowSimSharedPreference.flowSimNotificationState
                                when (flowSimNotificationState) {
                                    0 -> {
                                        flowSimLoadBinding.flowSimNotiGroup.visibility = View.VISIBLE
                                        flowSimLoadBinding.flowSimLoadingGroup.visibility = View.GONE
                                        flowSimUrl = it.data
                                    }
                                    1 -> {
                                        if (System.currentTimeMillis() / 1000 > flowSimSharedPreference.flowSimNotificationRequest) {
                                            flowSimLoadBinding.flowSimNotiGroup.visibility = View.VISIBLE
                                            flowSimLoadBinding.flowSimLoadingGroup.visibility = View.GONE
                                            flowSimUrl = it.data
                                        } else {
                                            flowSimNavigateToSuccess(it.data)
                                        }
                                    }
                                    2 -> {
                                        flowSimNavigateToSuccess(it.data)
                                    }
                                }
                            } else {
                                flowSimNavigateToSuccess(it.data)
                            }
                        }

                        FlowSimLoadViewModel.FlowSimHomeScreenState.FlowSimNotInternet -> {
                            flowSimLoadBinding.flowSimStateGroup.visibility = View.VISIBLE
                            flowSimLoadBinding.flowSimLoadingGroup.visibility = View.GONE
                        }
                    }
                }
            }
        }
    }


    private fun flowSimNavigateToSuccess(data: String) {
        findNavController().navigate(
            R.id.action_flowSimLoadFragment_to_flowSimV,
            bundleOf(FLOW_SIM_D to data)
        )
    }

    companion object {
        const val FLOW_SIM_D = "flowSimData"
    }
}