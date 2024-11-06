package com.example.burnify.presentation

class AccelerometerSample(val x: Float, val y: Float, val z: Float)

class AccelerometerMeasurements {
    private val samples = mutableListOf<AccelerometerSample>()
    private val accelerometerDataProcessor = AccelerometerDataProcessor()
    private var samplesCount = 0

    fun addSample(sample: AccelerometerSample) {
        samplesCount += 1
        if (isFull()) {
            samples.removeAt(0) // Rimuove il campione più vecchio se la lista è piena

        }
        samples.add(sample) // Aggiunge il nuovo campione
        //Stampa dopo 500 volte che è stato fatto il sampling
        if (samplesCount == 500) {
            println(accelerometerDataProcessor.getResultsAsString(this))
            println(this.getSamples())
            samplesCount = 0
        }
    }

    fun getSamples(): List<AccelerometerSample> {
        return samples.toList() // Restituisce una copia della lista
    }

    fun isFull(): Boolean {
        return samples.size >= 500 // Controlla se sono stati raccolti 500 campioni
    }
}

