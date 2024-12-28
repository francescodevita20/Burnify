package com.example.burnify.model

import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import com.example.burnify.util.SensorDataManager

/**
 * Data model representing an individual gyroscope sample with x, y, z values.
 */
class GyroscopeSample() : Parcelable {
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

    companion object CREATOR : Parcelable.Creator<GyroscopeSample> {
        override fun createFromParcel(parcel: Parcel): GyroscopeSample {
            return GyroscopeSample(parcel)
        }

        override fun newArray(size: Int): Array<GyroscopeSample?> {
            return arrayOfNulls(size)
        }
    }
}

/**
 * Data model representing a collection of gyroscope samples.
 */
class GyroscopeMeasurements : Parcelable {
    private val samples = mutableListOf<GyroscopeSample>()
    private var maxSize = 500

    constructor()

    private constructor(parcel: Parcel) {
        maxSize = parcel.readInt()
        parcel.readList(samples, GyroscopeSample::class.java.classLoader)
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(maxSize)
        parcel.writeList(samples)
    }

    override fun describeContents(): Int = 0

    /**
     * Adds a sample and processes data when the list is full.
     * @param context The context for interacting with system resources.
     * @param sample The gyroscope sample to add.
     */
    fun addSample(context: Context, sample: GyroscopeSample) {
        samples.add(sample)

        if (isFull()) {
            // Convert samples to a flattened List<Float>
            val gyroscopeData = getSamples().map { it.toList() }.flatten()

            // Send gyroscope data to SensorDataManager
            SensorDataManager.updateGyroscopeData(gyroscopeData, context)

            // Clear samples after processing
            clear()
        }
    }

    /**
     * Returns the list of gyroscope samples as FloatArrays.
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

    companion object CREATOR : Parcelable.Creator<GyroscopeMeasurements> {
        override fun createFromParcel(parcel: Parcel): GyroscopeMeasurements {
            return GyroscopeMeasurements(parcel)
        }

        override fun newArray(size: Int): Array<GyroscopeMeasurements?> {
            return arrayOfNulls(size)
        }
    }
}
