package com.flowsim.simfwlos.gokrp.presentation.notificiation

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.os.bundleOf
import com.flowsim.simfwlos.FlowSimActivity
import com.flowsim.simfwlos.R
import com.flowsim.simfwlos.gokrp.presentation.app.FlowSimApplication
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

private const val FLOW_SIM_CHANNEL_ID = "flow_sim_notifications"
private const val FLOW_SIM_CHANNEL_NAME = "FlowSim Notifications"
private const val FLOW_SIM_NOT_TAG = "FlowSim"

class FlowSimPushService : FirebaseMessagingService(){
    override fun onNewToken(token: String) {
        super.onNewToken(token)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        // Обработка notification payload
        remoteMessage.notification?.let {
            if (remoteMessage.data.contains("url")) {
                flowSimShowNotification(it.title ?: FLOW_SIM_NOT_TAG, it.body ?: "", data = remoteMessage.data["url"])
            } else {
                flowSimShowNotification(it.title ?: FLOW_SIM_NOT_TAG, it.body ?: "", data = null)
            }
        }

        // Обработка data payload
        if (remoteMessage.data.isNotEmpty()) {
            flowSimHandleDataPayload(remoteMessage.data)
        }
    }

    private fun flowSimShowNotification(title: String, message: String, data: String?) {
        val flowSimNotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Создаем канал уведомлений для Android 8+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                FLOW_SIM_CHANNEL_ID,
                FLOW_SIM_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            )
            flowSimNotificationManager.createNotificationChannel(channel)
        }

        val flowSimIntent = Intent(this, FlowSimActivity::class.java).apply {
            putExtras(bundleOf(
                "url" to data
            ))
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        val flowSimPendingIntent = PendingIntent.getActivity(
            this,
            0,
            flowSimIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val flowSimNotification = NotificationCompat.Builder(this, FLOW_SIM_CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(R.drawable.flow_sim_noti_ic)
            .setAutoCancel(true)
            .setContentIntent(flowSimPendingIntent)
            .build()

        flowSimNotificationManager.notify(System.currentTimeMillis().toInt(), flowSimNotification)
    }

    private fun flowSimHandleDataPayload(data: Map<String, String>) {
        data.forEach { (key, value) ->
            Log.d(FlowSimApplication.FLOW_SIM_MAIN_TAG, "Data key=$key value=$value")
        }
    }
}