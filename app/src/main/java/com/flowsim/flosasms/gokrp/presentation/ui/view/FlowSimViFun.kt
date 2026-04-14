package com.flowsim.flosasms.gokrp.presentation.ui.view

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FlowSimViFun(private val context: Context) {
    fun flowSimSavePhoto() : Uri {
        val sdf = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US)
        val df = sdf.format(Date())
        val dir = context.filesDir.absoluteFile
        if (!dir.exists()) {
            dir.mkdir()
        }
        return FileProvider.getUriForFile(
            context,
            "com.flowsim.simfwlossssssssss",
            File(dir, "/$df.jpg")
        )
    }

}