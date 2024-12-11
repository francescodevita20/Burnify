package com.example.burnify.model

import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import com.example.burnify.util.SensorDataManager

/**
 * Data model representing an individual accelerometer sample with x, y, z values and a timestamp.
 */
class AccelerometerSample() : Parcelable {
    var x: Float = 0f
    var y: Float = 0f
    var z: Float = 0f
    var timestamp: Long = 0L // Timestamp in milliseconds since epoch

    constructor(x: Float, y: Float, z: Float, timestamp: Long) : this() {
        this.x = x
        this.y = y
        this.z = z
        this.timestamp = timestamp
    }

    fun setSample(x: Float, y: Float, z: Float, timestamp: Long) {
        this.x = x
        this.y = y
        this.z = z
        this.timestamp = timestamp
    }

    fun getValues(): FloatArray {
        return floatArrayOf(x, y, z)
    }

    private constructor(parcel: Parcel) : this() {
        x = parcel.readFloat()
        y = parcel.readFloat()
        z = parcel.readFloat()
        timestamp = parcel.readLong()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeFloat(x)
        parcel.writeFloat(y)
        parcel.writeFloat(z)
        parcel.writeLong(timestamp)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<AccelerometerSample> {
        override fun createFromParcel(parcel: Parcel): AccelerometerSample {
            return AccelerometerSample(parcel)
        }

        override fun newArray(size: Int): Array<AccelerometerSample?> {
            return arrayOfNulls(size)
        }
    }
}

/**
 * Data model representing a collection of accelerometer measurements.
 */
class AccelerometerMeasurements : Parcelable {
    private val samples = mutableListOf<AccelerometerSample>()
    private var maxSize = 500

    constructor()

    private constructor(parcel: Parcel) {
        maxSize = parcel.readInt()
        parcel.readList(samples, AccelerometerSample::class.java.classLoader)
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(maxSize)
        parcel.writeList(samples)
    }

    override fun describeContents(): Int = 0

    /**
     * Adds an accelerometer sample and updates the SensorDataManager.
     * @param context The context for interacting with system resources.
     * @param sample The accelerometer sample to add.
     */
    fun addSample(context: Context, sample: AccelerometerSample) {
        samples.add(sample)

        if (isFull()) {
            // Convert each FloatArray to a List<Float>, then flatten the result
            val accelerometerData = getSamples().map { it.toList() }.flatten()
            SensorDataManager.updateAccelerometerData(accelerometerData, context)

            // Clear the samples after processing
            clear()
        }
    }

    fun getSamples(): List<FloatArray> = samples.map { it.getValues() }

    fun isFull(): Boolean = samples.size >= maxSize

    fun clear() {
        samples.clear()
    }

    companion object CREATOR : Parcelable.Creator<AccelerometerMeasurements> {
        override fun createFromParcel(parcel: Parcel): AccelerometerMeasurements {
            return AccelerometerMeasurements(parcel)
        }

        override fun newArray(size: Int): Array<AccelerometerMeasurements?> {
            return arrayOfNulls(size)
        }
    }
}
