package com.example.burnify.service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {
            val serviceAccelerometerIntent = Intent(context, AccelerometerService::class.java)
            context.startForegroundService(serviceAccelerometerIntent)
            val serviceGyroscopeIntent = Intent(context, GyroscopeService::class.java)
            context.startForegroundService(serviceGyroscopeIntent)
            val serviceMagnetometerIntent = Intent(context, MagnetometerService::class.java)
            context.startForegroundService(serviceMagnetometerIntent)
        }
    }
}
