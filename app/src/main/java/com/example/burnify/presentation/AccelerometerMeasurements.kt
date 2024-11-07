package com.example.burnify.presentation

class AccelerometerSample {
    private var x: Float = 0f
    private var y: Float = 0f
    private var z: Float = 0f

    // Getter methods
    fun getX(): Float {
        return x
    }

    fun getY(): Float {
        return y
    }

    fun getZ(): Float {
        return z
    }

    // Setter method
    fun setSample(x: Float, y: Float, z: Float) {
        this.x = x
        this.y = y
        this.z = z
    }
}

class AccelerometerMeasurements {
    private val samples = mutableListOf<AccelerometerSample>()
    private val accelerometerDataProcessor = AccelerometerDataProcessor()
    private var samplesCount = 0
    private var maxSize = 500

    fun setMaxSize(size: Int) {
        maxSize = size
    }

    fun addSample(sample: AccelerometerSample) {
        samplesCount += 1

        // Se la lista è piena, processa i dati e svuota la lista
        if (isFull()) {
            val results = accelerometerDataProcessor.processMeasurements(this)
            // Per adesso Stampa ma dovrebbe salvarli all'interno del database per "Comprimerli"
            println(results)
            samples.clear() // Svuota la lista dei campioni
            samplesCount = 0 // Reset del conteggio dei campioni
        }

        samples.add(sample) // Aggiunge il nuovo campione alla lista
    }

    fun getSamples(): List<AccelerometerSample> {
        return samples.toList() // Restituisce una copia della lista
    }

    fun getLastSample(): AccelerometerSample? {
        return samples.lastOrNull()
    }

    fun isFull(): Boolean {
        return samples.size >= maxSize // Controlla se sono stati raccolti 500 campioni
    }
}


