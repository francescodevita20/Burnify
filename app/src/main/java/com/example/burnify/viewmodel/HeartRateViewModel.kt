package com.example.burnify.viewmodel
import android.Manifest
import android.annotation.SuppressLint
import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import java.util.UUID

class HeartRateViewModel(application: Application) : AndroidViewModel(application) {

    private val bluetoothAdapter: BluetoothAdapter
    private val bleScanner: BluetoothLeScanner

    // MutableLiveData to hold heart rate data
    private val _heartRateData = MutableLiveData<String>("No data")
    val heartRateData: LiveData<String> = _heartRateData
    private val appContext = application.applicationContext

    init {
        // Initialize Bluetooth Adapter
        val bluetoothManager = application.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter
        bleScanner = bluetoothAdapter.bluetoothLeScanner
    }

    fun startBleScan() {
        val scanCallback = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                if (ContextCompat.checkSelfPermission(appContext, Manifest.permission.BLUETOOTH_CONNECT)
                    == PackageManager.PERMISSION_GRANTED) {
                    val deviceName = result.device.name ?: "Unknown"
                    Log.d("BLE", "Device found: $deviceName, ${result.device.address}")

                    // Here, simulate receiving heart rate data
                    if (deviceName.contains("HeartRateDevice", ignoreCase = true)) {
                        bleScanner.stopScan(this) // Stop scanning after finding the device
                        connectToDevice(result.device) // Initiate GATT connection
                    }
                } else {
                    Log.d("BLE", "Missing BLUETOOTH_CONNECT permission.")
                }

            }
        }

        bleScanner.startScan(scanCallback)
        // bleScanner.stopScan(scanCallback)
    }

    @SuppressLint("MissingPermission")
    private fun connectToDevice(device: BluetoothDevice) {
        val gattCallback = object : BluetoothGattCallback() {
            @SuppressLint("MissingPermission")
            override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    Log.d("GATT", "Connected to GATT server. Discovering services...")
                    gatt.discoverServices() // Discover services after connection
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    Log.d("GATT", "Disconnected from GATT server.")
                    gatt.close() // Ensure the GATT instance is properly closed
                }
            }

            @SuppressLint("MissingPermission")
            override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    val heartRateService = gatt.getService(UUID.fromString("0000180d-0000-1000-8000-00805f9b34fb"))
                    if (heartRateService != null) {
                        val heartRateCharacteristic = heartRateService.getCharacteristic(
                            UUID.fromString("00002a37-0000-1000-8000-00805f9b34fb")
                        )
                        gatt.setCharacteristicNotification(heartRateCharacteristic, true)
                    } else {
                        Log.d("GATT", "Heart Rate Service not found.")
                    }
                } else {
                    Log.d("GATT", "Service discovery failed with status: $status")
                }
            }

            @Deprecated("Deprecated in Java")
            override fun onCharacteristicChanged(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
                if (characteristic.uuid == UUID.fromString("00002a37-0000-1000-8000-00805f9b34fb")) {
                    val heartRate = parseHeartRate(characteristic.value)
                    _heartRateData.postValue("Heart Rate: $heartRate bpm")
                }
            }
        }

        // Call connectGatt with the correct context and callback
        device.connectGatt(appContext, false, gattCallback)
    }

    private fun parseHeartRate(data: ByteArray): Int {
        return data[1].toInt() // Simplified parsing; adjust based on device documentation
    }
}