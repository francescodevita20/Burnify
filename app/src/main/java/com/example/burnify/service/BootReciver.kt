package com.example.burnify.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * A BroadcastReceiver that listens for the BOOT_COMPLETED event.
 * It starts the necessary unified foreground service after the device has finished booting.
 */
class BootReceiver : BroadcastReceiver() {

    /**
     * Called when the broadcast is received. This method is triggered after the device finishes booting.
     * It starts the UnifiedSensorService.
     */
    override fun onReceive(context: Context, intent: Intent?) {
        // Check if the received intent is for BOOT_COMPLETED
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {
            // Start the UnifiedSensorService as a foreground service
            val unifiedServiceIntent = Intent(context, UnifiedSensorService::class.java)
            context.startForegroundService(unifiedServiceIntent)
        }
    }
}
