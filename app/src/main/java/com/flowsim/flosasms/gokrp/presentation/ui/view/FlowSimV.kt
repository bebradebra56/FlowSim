package com.flowsim.flosasms.gokrp.presentation.ui.view

import android.content.DialogInterface
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.ValueCallback
import android.widget.FrameLayout
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.flowsim.flosasms.gokrp.presentation.app.FlowSimApplication
import com.flowsim.flosasms.gokrp.presentation.ui.load.FlowSimLoadFragment
import org.koin.android.ext.android.inject

class FlowSimV : Fragment(){

    private lateinit var flowSimPhoto: Uri
    private var flowSimFilePathFromChrome: ValueCallback<Array<Uri>>? = null

    private val flowSimTakeFile: ActivityResultLauncher<PickVisualMediaRequest> = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) {
        flowSimFilePathFromChrome?.onReceiveValue(arrayOf(it ?: Uri.EMPTY))
        flowSimFilePathFromChrome = null
    }

    private val flowSimTakePhoto: ActivityResultLauncher<Uri> = registerForActivityResult(ActivityResultContracts.TakePicture()) {
        if (it) {
            flowSimFilePathFromChrome?.onReceiveValue(arrayOf(flowSimPhoto))
            flowSimFilePathFromChrome = null
        } else {
            flowSimFilePathFromChrome?.onReceiveValue(null)
            flowSimFilePathFromChrome = null
        }
    }

    private val flowSimDataStore by activityViewModels<FlowSimDataStore>()


    private val flowSimViFun by inject<FlowSimViFun>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(FlowSimApplication.FLOW_SIM_MAIN_TAG, "Fragment onCreate")
        CookieManager.getInstance().setAcceptCookie(true)
        requireActivity().onBackPressedDispatcher.addCallback(this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (flowSimDataStore.flowSimView.canGoBack()) {
                        flowSimDataStore.flowSimView.goBack()
                        Log.d(FlowSimApplication.FLOW_SIM_MAIN_TAG, "WebView can go back")
                    } else if (flowSimDataStore.flowSimViList.size > 1) {
                        Log.d(FlowSimApplication.FLOW_SIM_MAIN_TAG, "WebView can`t go back")
                        flowSimDataStore.flowSimViList.removeAt(flowSimDataStore.flowSimViList.lastIndex)
                        Log.d(FlowSimApplication.FLOW_SIM_MAIN_TAG, "WebView list size ${flowSimDataStore.flowSimViList.size}")
                        flowSimDataStore.flowSimView.destroy()
                        val previousWebView = flowSimDataStore.flowSimViList.last()
                        flowSimAttachWebViewToContainer(previousWebView)
                        flowSimDataStore.flowSimView = previousWebView
                    }
                }

            })
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        if (flowSimDataStore.flowSimIsFirstCreate) {
            flowSimDataStore.flowSimIsFirstCreate = false
            flowSimDataStore.flowSimContainerView = FrameLayout(requireContext()).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                id = View.generateViewId()
            }
            return flowSimDataStore.flowSimContainerView
        } else {
            return flowSimDataStore.flowSimContainerView
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.d(FlowSimApplication.FLOW_SIM_MAIN_TAG, "onViewCreated")
        if (flowSimDataStore.flowSimViList.isEmpty()) {
            flowSimDataStore.flowSimView = FlowSimVi(requireContext(), object :
                FlowSimCallBack {
                override fun flowSimHandleCreateWebWindowRequest(flowSimVi: FlowSimVi) {
                    flowSimDataStore.flowSimViList.add(flowSimVi)
                    Log.d(FlowSimApplication.FLOW_SIM_MAIN_TAG, "WebView list size = ${flowSimDataStore.flowSimViList.size}")
                    Log.d(FlowSimApplication.FLOW_SIM_MAIN_TAG, "CreateWebWindowRequest")
                    flowSimDataStore.flowSimView = flowSimVi
                    flowSimVi.flowSimSetFileChooserHandler { callback ->
                        flowSimHandleFileChooser(callback)
                    }
                    flowSimAttachWebViewToContainer(flowSimVi)
                }

            }, flowSimWindow = requireActivity().window).apply {
                flowSimSetFileChooserHandler { callback ->
                    flowSimHandleFileChooser(callback)
                }
            }
            flowSimDataStore.flowSimView.flowSimFLoad(arguments?.getString(
                FlowSimLoadFragment.FLOW_SIM_D) ?: "")
//            ejvview.fLoad("www.google.com")
            flowSimDataStore.flowSimViList.add(flowSimDataStore.flowSimView)
            flowSimAttachWebViewToContainer(flowSimDataStore.flowSimView)
        } else {
            flowSimDataStore.flowSimViList.forEach { webView ->
                webView.flowSimSetFileChooserHandler { callback ->
                    flowSimHandleFileChooser(callback)
                }
            }
            flowSimDataStore.flowSimView = flowSimDataStore.flowSimViList.last()

            flowSimAttachWebViewToContainer(flowSimDataStore.flowSimView)
        }
        Log.d(FlowSimApplication.FLOW_SIM_MAIN_TAG, "WebView list size = ${flowSimDataStore.flowSimViList.size}")
    }

    private fun flowSimHandleFileChooser(callback: ValueCallback<Array<Uri>>?) {
        Log.d(FlowSimApplication.FLOW_SIM_MAIN_TAG, "handleFileChooser called, callback: ${callback != null}")

        flowSimFilePathFromChrome = callback

        val listItems: Array<out String> = arrayOf("Select from file", "To make a photo")
        val listener = DialogInterface.OnClickListener { _, which ->
            when (which) {
                0 -> {
                    Log.d(FlowSimApplication.FLOW_SIM_MAIN_TAG, "Launching file picker")
                    flowSimTakeFile.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                }
                1 -> {
                    Log.d(FlowSimApplication.FLOW_SIM_MAIN_TAG, "Launching camera")
                    flowSimPhoto = flowSimViFun.flowSimSavePhoto()
                    flowSimTakePhoto.launch(flowSimPhoto)
                }
            }
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Choose a method")
            .setItems(listItems, listener)
            .setCancelable(true)
            .setOnCancelListener {
                Log.d(FlowSimApplication.FLOW_SIM_MAIN_TAG, "File chooser canceled")
                callback?.onReceiveValue(null)
                flowSimFilePathFromChrome = null
            }
            .create()
            .show()
    }

    private fun flowSimAttachWebViewToContainer(w: FlowSimVi) {
        flowSimDataStore.flowSimContainerView.post {
            (w.parent as? ViewGroup)?.removeView(w)
            flowSimDataStore.flowSimContainerView.removeAllViews()
            flowSimDataStore.flowSimContainerView.addView(w)
        }
    }


}