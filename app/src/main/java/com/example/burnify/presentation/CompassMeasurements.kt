package com.example.burnify.presentation

class CompassSample(val angle: Float)

class CompassMeasurements {
    private val samples = mutableListOf<CompassSample>()
    //private val compassDataProcessor = CompassDataProcessor()
    private var samplesCount = 0

    fun addSample(sample: CompassSample) {
        samplesCount += 1
        if (isFull()) {
            samples.removeAt(0) // Rimuove il campione più vecchio se la lista è piena
        }
        samples.add(sample) // Aggiunge il nuovo campione

        // Stampa dopo 500 campioni raccolti
        if (samplesCount == 500) {
            println(this.getSamples())
            samplesCount = 0
        }
    }

    fun getSamples(): List<CompassSample> {
        return samples.toList() // Restituisce una copia della lista
    }
    fun getLastSample(): CompassSample? {
        return samples.lastOrNull()
    }

    fun isFull(): Boolean {
        return samples.size >= 500
    }
}
