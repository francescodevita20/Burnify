package com.example.burnify.model
import android.os.Parcel
import android.os.Parcelable
import com.example.burnify.processor.GyroscopeDataProcessor

class GyroscopeSample() : Parcelable {
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

    // Restituisce la dimensione della memoria utilizzata
    override fun describeContents(): Int {
        return 0
    }

    // Companion object per la creazione dell'oggetto Parcelable
    companion object CREATOR : Parcelable.Creator<GyroscopeSample> {
        override fun createFromParcel(parcel: Parcel): GyroscopeSample {
            return GyroscopeSample(parcel)
        }

        override fun newArray(size: Int): Array<GyroscopeSample?> {
            return arrayOfNulls(size)
        }
    }
}





class GyroscopeMeasurements : Parcelable {
    private val samples = mutableListOf<GyroscopeSample>()
    private var samplesCount = 0
    private var maxSize = 500
    private val gyroscopeDataProcessor = GyroscopeDataProcessor()

    // Costruttore vuoto per Parcelable
    constructor()

    // Costruttore che riceve un Parcel per creare l'oggetto da un Parcel
    private constructor(parcel: Parcel) {
        samplesCount = parcel.readInt()
        maxSize = parcel.readInt()
        // Recupera i campioni dal Parcel
        parcel.readList(samples, GyroscopeSample::class.java.classLoader)
    }

    // Metodo che scrive i dati nell'oggetto Parcel
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(samplesCount)
        parcel.writeInt(maxSize)
        parcel.writeList(samples)
    }

    // Restituisce la dimensione della memoria utilizzata
    override fun describeContents(): Int {
        return 0
    }

    // Metodo per aggiungere un campione
    fun addSample(sample: GyroscopeSample) {
        samplesCount += 1

        // Se la lista è piena, processa i dati e svuota la lista
        if (isFull()) {
            println("Elaborazione in corso...")
            try {
                println(gyroscopeDataProcessor.processMeasurements(this))
            } catch (e: Exception) {
                println("Errore durante l'elaborazione: ${e.message}")
            }

            samples.clear() // Svuota la lista dei campioni
            samplesCount = 0 // Reset del conteggio dei campioni
        }

        samples.add(sample) // Aggiunge il nuovo campione alla lista
    }

    // Metodo per ottenere tutti i campioni
    fun getSamples(): List<GyroscopeSample> {
        return samples.toList() // Restituisce una copia della lista
    }

    // Metodo per ottenere l'ultimo campione
    fun getLastSample(): GyroscopeSample? {
        return samples.lastOrNull()
    }

    // Metodo per verificare se la lista dei campioni è piena
    fun isFull(): Boolean {
        return samples.size >= maxSize // Controlla se sono stati raccolti 500 campioni
    }

    // Companion object per la creazione dell'oggetto Parcelable
    companion object CREATOR : Parcelable.Creator<GyroscopeMeasurements> {
        override fun createFromParcel(parcel: Parcel): GyroscopeMeasurements {
            return GyroscopeMeasurements(parcel)
        }

        override fun newArray(size: Int): Array<GyroscopeMeasurements?> {
            return arrayOfNulls(size)
        }
    }
}
