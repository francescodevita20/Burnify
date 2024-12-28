package com.example.burnify.util

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.burnify.R

class NotificationHelper(private val context: Context) {

    companion object {
        const val GROUP_KEY = "com.example.burnify.services"
        const val CHANNEL_ID = "ServicesChannel"
    }

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Burnify Services",
                NotificationManager.IMPORTANCE_DEFAULT // Changed from LOW to DEFAULT
            ).apply {
                description = "Notifications for Burnify's active services"
            }
            notificationManager.createNotificationChannel(channel)
            android.util.Log.d("NotificationHelper", "NotificationChannel $CHANNEL_ID created")
        }
    }

    fun createGroupNotification(): Notification {
        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("Active Services")
            .setContentText("You have active services running.")
            .setSmallIcon(R.drawable.ic_notification) // Replace with custom icon if available
            .setPriority(NotificationCompat.PRIORITY_DEFAULT) // Ensure visibility
            .setGroup(GROUP_KEY)
            .setGroupSummary(true)
            .build()
    }

    fun createServiceNotification(serviceName: String): Notification {
        return NotificationCompat.Builder(context, CHANNEL_ID) // Use NotificationCompat.Builder
            .setContentTitle("$serviceName is running")
            .setContentText("This service is active.") // Add content text
            .setSmallIcon(R.drawable.ic_notification) // Replace with custom icon if available
            .setPriority(NotificationCompat.PRIORITY_DEFAULT) // Ensure visibility
            .setGroup(GROUP_KEY)
            .build()
    }

    fun notify(notificationId: Int, notification: Notification) {
        android.util.Log.d("NotificationHelper", "Notifying with ID $notificationId")
        notificationManager.notify(notificationId, notification)
    }

    fun cancel(notificationId: Int) {
        android.util.Log.d("NotificationHelper", "Canceling notification with ID $notificationId")
        notificationManager.cancel(notificationId)
    }
}
