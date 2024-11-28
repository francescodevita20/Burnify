package com.example.burnify.model
import SensorDataManager
import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import com.example.burnify.processor.AccelerometerDataProcessor
import com.example.burnify.retrieveProcessedDataFromDatabase
import com.example.burnify.saveProcessedDataToDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AccelerometerSample() : Parcelable {
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

    fun getSampleValues(): Triple<Float, Float, Float> {
        return Triple(x, y, z)
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

    companion object CREATOR : Parcelable.Creator<AccelerometerSample> {
        override fun createFromParcel(parcel: Parcel): AccelerometerSample {
            return AccelerometerSample(parcel)
        }

        override fun newArray(size: Int): Array<AccelerometerSample?> {
            return arrayOfNulls(size)
        }
    }
}


class AccelerometerMeasurements : Parcelable {
    private val samples = mutableListOf<AccelerometerSample>()
    private var samplesCount = 0
    private var maxSize = 500
    private val accelerometerDataProcessor = AccelerometerDataProcessor()

    constructor()

    private constructor(parcel: Parcel) {
        samplesCount = parcel.readInt()
        maxSize = parcel.readInt()
        parcel.readList(samples, AccelerometerSample::class.java.classLoader)
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(samplesCount)
        parcel.writeInt(maxSize)
        parcel.writeList(samples)
    }

    override fun describeContents(): Int = 0

    fun addSample(context: Context, sample: AccelerometerSample) {
        samples.add(sample)
        samplesCount += 1

        // If the list is full, process the data and save it to the database
        if (isFull()) {
            println("Processing data...")

            try {
                // Process the data
                // DO NOT DELETE THIS CODE, IT'S IMPORTANT.
                //val processedData = accelerometerDataProcessor.processMeasurementsToEntity(this)
                //println("Processed data: $processedData")
                // Save processed data to the database
                //saveProcessedDataToDatabase(context, processedData)
                // retrieveProcessedDataFromDatabase(context, "accelerometer")
                SensorDataManager.accelerometerIsFilled = true
                SensorDataManager.setAccelerometerMeasurements(this)

                // Retrieve processed data from the database

            } catch (e: Exception) {
                println("Error during processing: ${e.message}")
            }

            // Clear the list of samples
            samples.clear()
            samplesCount = 0
        }
    }

    fun getSamples(): List<FloatArray> {
        return samples.map { it.getValues() }
    }

    fun isFull(): Boolean = samples.size >= maxSize

    fun clear() {
        samples.clear()
        samplesCount = 0
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
