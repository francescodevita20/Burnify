package com.example.burnify.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * A BroadcastReceiver that listens for the BOOT_COMPLETED event.
 * It starts the necessary foreground services after the device has finished booting.
 */
class BootReceiver : BroadcastReceiver() {

    /**
     * Called when the broadcast is received. This method is triggered after the device finishes booting.
     * It starts the foreground services for accelerometer, gyroscope, and magnetometer.
     */
    override fun onReceive(context: Context, intent: Intent?) {
        // Check if the received intent is for BOOT_COMPLETED
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {
            // Start the AccelerometerService as a foreground service
            val serviceAccelerometerIntent = Intent(context, AccelerometerService::class.java)
            context.startForegroundService(serviceAccelerometerIntent)

            // Start the GyroscopeService as a foreground service
            val serviceGyroscopeIntent = Intent(context, GyroscopeService::class.java)
            context.startForegroundService(serviceGyroscopeIntent)

            // Start the MagnetometerService as a foreground service
            val serviceMagnetometerIntent = Intent(context, MagnetometerService::class.java)
            context.startForegroundService(serviceMagnetometerIntent)
        }
    }
}
