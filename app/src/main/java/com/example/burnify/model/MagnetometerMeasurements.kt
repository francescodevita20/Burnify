package com.example.burnify.model

import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import com.example.burnify.util.SensorDataManager

/**
 * Data model representing an individual magnetometer sample with x, y, z values.
 */
class MagnetometerSample() : Parcelable {
    var x: Float = 0f
    var y: Float = 0f
    var z: Float = 0f

    constructor(x: Float, y: Float, z: Float) : this() {
        this.x = x
        this.y = y
        this.z = z
    }

    fun setSample(x: Float, y: Float, z: Float) {
        this.x = x
        this.y = y
        this.z = z
    }

    fun getValues(): FloatArray {
        return floatArrayOf(x, y, z)
    }

    private constructor(parcel: Parcel) : this() {
        x = parcel.readFloat()
        y = parcel.readFloat()
        z = parcel.readFloat()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeFloat(x)
        parcel.writeFloat(y)
        parcel.writeFloat(z)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<MagnetometerSample> {
        override fun createFromParcel(parcel: Parcel): MagnetometerSample {
            return MagnetometerSample(parcel)
        }

        override fun newArray(size: Int): Array<MagnetometerSample?> {
            return arrayOfNulls(size)
        }
    }
}

/**
 * Data model representing a collection of magnetometer samples.
 */
class MagnetometerMeasurements : Parcelable {
    private val samples = mutableListOf<MagnetometerSample>()
    private var maxSize = 500

    constructor()

    private constructor(parcel: Parcel) {
        maxSize = parcel.readInt()
        parcel.readList(samples, MagnetometerSample::class.java.classLoader)
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(maxSize)
        parcel.writeList(samples)
    }

    override fun describeContents(): Int = 0

    /**
     * Adds a sample and processes data when the list is full.
     * @param context The context for interacting with system resources.
     * @param sample The magnetometer sample to add.
     */
    fun addSample(context: Context, sample: MagnetometerSample) {
        samples.add(sample)

        if (isFull()) {
            // Convert samples to a flattened List<Float>
            val magnetometerData = getSamples().map { it.toList() }.flatten()

            // Send magnetometer data to SensorDataManager
            SensorDataManager.updateMagnetometerData(magnetometerData, context)

            // Clear samples after processing
            clear()
        }
    }

    /**
     * Returns the list of magnetometer samples as FloatArrays.
     */
    fun getSamples(): List<FloatArray> = samples.map { it.getValues() }

    /**
     * Checks if the sample list has reached its maximum size.
     */
    fun isFull(): Boolean = samples.size >= maxSize

    /**
     * Clears the list of samples.
     */
    fun clear() {
        samples.clear()
    }

    companion object CREATOR : Parcelable.Creator<MagnetometerMeasurements> {
        override fun createFromParcel(parcel: Parcel): MagnetometerMeasurements {
            return MagnetometerMeasurements(parcel)
        }

        override fun newArray(size: Int): Array<MagnetometerMeasurements?> {
            return arrayOfNulls(size)
        }
    }
}
