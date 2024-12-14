package com.example.burnify.model

import com.example.burnify.util.SensorDataManager
import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import com.example.burnify.processor.AccelerometerDataProcessor

/**
 * Data model representing an individual accelerometer sample with x, y, z values.
 */
class AccelerometerSample() : Parcelable {
    var x: Float = 0f
    var y: Float = 0f
    var z: Float = 0f

    // Primary constructor to initialize x, y, and z values.
    constructor(x: Float, y: Float, z: Float) : this() {
        this.x = x
        this.y = y
        this.z = z
    }

    /**
     * Sets the x, y, z values for this accelerometer sample.
     */
    fun setSample(x: Float, y: Float, z: Float) {
        this.x = x
        this.y = y
        this.z = z
    }

    /**
     * Returns the accelerometer values as a Triple of x, y, z.
     */
    fun getSampleValues(): Triple<Float, Float, Float> {
        return Triple(x, y, z)
    }

    /**
     * Returns the accelerometer values as a FloatArray (x, y, z).
     */
    fun getValues(): FloatArray {
        return floatArrayOf(x, y, z)
    }

    // Parcelable constructor to read the data from a Parcel.
    private constructor(parcel: Parcel) : this() {
        x = parcel.readFloat()
        y = parcel.readFloat()
        z = parcel.readFloat()
    }

    // Write the accelerometer values to the parcel.
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeFloat(x)
        parcel.writeFloat(y)
        parcel.writeFloat(z)
    }

    // Describe the contents (no special objects in this class).
    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<AccelerometerSample> {
        // Creates an instance of AccelerometerSample from a Parcel.
        override fun createFromParcel(parcel: Parcel): AccelerometerSample {
            return AccelerometerSample(parcel)
        }

        // Creates an array of AccelerometerSample instances.
        override fun newArray(size: Int): Array<AccelerometerSample?> {
            return arrayOfNulls(size)
        }
    }
}

/**
 * Data model representing a collection of accelerometer samples.
 * Provides functionality to add, process, and clear samples.
 */
class AccelerometerMeasurements : Parcelable {
    // List of accelerometer samples
    private val samples = mutableListOf<AccelerometerSample>()
    private var samplesCount = 0 // Count of samples added
    private var maxSize = 500 // Maximum size for the sample collection
    private val accelerometerDataProcessor = AccelerometerDataProcessor() // Processor for processing data

    constructor()

    // Parcelable constructor to read data from Parcel.
    private constructor(parcel: Parcel) {
        samplesCount = parcel.readInt()
        maxSize = parcel.readInt()
        parcel.readList(samples, AccelerometerSample::class.java.classLoader)
    }

    // Write the state of this object to a Parcel.
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(samplesCount)
        parcel.writeInt(maxSize)
        parcel.writeList(samples)
    }

    // Describe the contents (no special objects in this class).
    override fun describeContents(): Int = 0

    /**
     * Adds a sample to the list and processes the data if the list reaches the maximum size.
     * @param context The context to interact with the database and other system features.
     * @param sample The accelerometer sample to add.
     */
    fun addSample(context: Context, sample: AccelerometerSample) {
        // Add the sample to the list and increment the sample count.
        samples.add(sample)
        samplesCount += 1

        // If the list is full, process the data and save it.
        if (isFull()) {
            println("Processing data...")

            try {
                // Uncomment the following lines to process and save data to the database.
                // Process the data (currently commented out for the actual processing).
                // val processedData = accelerometerDataProcessor.processMeasurementsToEntity(this)
                // println("Processed data: $processedData")

                // Save processed data to the database.
                // saveProcessedDataToDatabase(context, processedData)

                // Set the flag indicating that the accelerometer data is filled.
                SensorDataManager.accelerometerIsFilled = true
                // Save accelerometer measurements to the manager.
                SensorDataManager.setAccelerometerMeasurements(this, context)

                // Retrieve the processed data from the database (if necessary).
                // retrieveProcessedDataFromDatabase(context, "accelerometer")

            } catch (e: Exception) {
                // Handle any exceptions that might occur during processing.
                println("Error during processing: ${e.message}")
            }

            // Clear the sample list and reset the sample count after processing.
            samples.clear()
            samplesCount = 0
        }
    }

    /**
     * Returns the list of accelerometer sample values as FloatArrays.
     */
    fun getSamples(): List<FloatArray> {
        return samples.map { it.getValues() }
    }

    /**
     * Checks if the sample list has reached its maximum size.
     */
    fun isFull(): Boolean = samples.size >= maxSize

    /**
     * Clears the list of samples and resets the sample count.
     */
    fun clear() {
        samples.clear()
        samplesCount = 0
    }
      // parcelable allow it to be passed between Android components
    companion object CREATOR : Parcelable.Creator<AccelerometerMeasurements> {
        // Creates an instance of AccelerometerMeasurements from a Parcel.
        override fun createFromParcel(parcel: Parcel): AccelerometerMeasurements {
            return AccelerometerMeasurements(parcel)
        }

        // Creates an array of AccelerometerMeasurements instances.
        override fun newArray(size: Int): Array<AccelerometerMeasurements?> {
            return arrayOfNulls(size)
        }
    }
}
