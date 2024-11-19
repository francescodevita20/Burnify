package com.example.burnify.model

import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import com.example.burnify.processor.MagnetometerDataProcessor
import com.example.burnify.retrieveProcessedDataFromDatabase
import com.example.burnify.saveProcessedDataToDatabase

class MagnetometerSample() : Parcelable {
    var x: Float = 0f
    var y: Float = 0f
    var z: Float = 0f

    // Costruttore che prende i valori X, Y e Z
    constructor(x: Float, y: Float, z: Float) : this() {
        this.x = x
        this.y = y
        this.z = z
    }

    // Metodo per impostare i valori
    fun setSample(x: Float, y: Float, z: Float) {
        this.x = x
        this.y = y
        this.z = z
    }

    fun getSampleValues(): Triple<Float, Float, Float> {
        return Triple(x, y, z)
    }

    // Metodo per ottenere una rappresentazione dei valori
    fun getSample(): String {
        return "X: $x, Y: $y, Z: $z"
    }

    // Metodo per restituire i valori come un array
    fun getValues(): FloatArray {
        return floatArrayOf(x, y, z)
    }

    // Costruttore che riceve un Parcel per creare l'oggetto da un Parcel
    private constructor(parcel: Parcel) : this() {
        x = parcel.readFloat()
        y = parcel.readFloat()
        z = parcel.readFloat()
    }

    // Metodo per scrivere i dati nell'oggetto Parcel
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeFloat(x)
        parcel.writeFloat(y)
        parcel.writeFloat(z)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<MagnetometerSample> {
        override fun createFromParcel(parcel: Parcel): MagnetometerSample {
            return MagnetometerSample(parcel)
        }

        override fun newArray(size: Int): Array<MagnetometerSample?> {
            return arrayOfNulls(size)
        }
    }
}

class MagnetometerMeasurements : Parcelable {
    private val samples = mutableListOf<MagnetometerSample>()
    private var samplesCount = 0
    private var maxSize = 500
    private val magnetometerDataProcessor = MagnetometerDataProcessor()

    constructor()

    private constructor(parcel: Parcel) {
        samplesCount = parcel.readInt()
        maxSize = parcel.readInt()
        parcel.readList(samples, MagnetometerSample::class.java.classLoader)
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(samplesCount)
        parcel.writeInt(maxSize)
        parcel.writeList(samples)
    }

    override fun describeContents(): Int {
        return 0
    }

    fun addSample(context: Context, sample: MagnetometerSample) {
        samplesCount += 1

        // Aggiungi il nuovo campione
        samples.add(sample)

        // Se la lista è piena, processa i dati e salva nel database
        if (isFull()) {
            println("Elaborazione in corso...")
            try {
                // Processa i dati
                val processedData = magnetometerDataProcessor.processMeasurementsToEntity(this)
                println("Dati processati: $processedData")


                saveProcessedDataToDatabase(context,processedData)
                retrieveProcessedDataFromDatabase(context,"magnetometer")
            } catch (e: Exception) {
                println("Errore durante l'elaborazione: ${e.message}")
            }

            // Svuota la lista dei campioni
            samples.clear()
            samplesCount = 0
        }
    }
    fun getSamples(): List<MagnetometerSample> {
        return samples.toList()
    }

    fun getLastSample(): MagnetometerSample? {
        return samples.lastOrNull()
    }

    fun isFull(): Boolean {
        return samples.size >= maxSize
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
