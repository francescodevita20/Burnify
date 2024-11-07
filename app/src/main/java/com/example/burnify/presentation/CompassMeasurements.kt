package com.example.burnify.presentation

class CompassSample{
    private var angle: Float = 0f
    fun setAngle(angle: Float){this.angle = angle}
    fun getAngle(): Float{return angle}

    }

class CompassMeasurements {
    private val samples = mutableListOf<CompassSample>()
    private var samplesCount = 0
    private var maxSize = 500  // Definisce la dimensione massima della lista
    private val compassDataProcessor = CompassDataProcessor()

    // Imposta la dimensione massima
    fun setMaxSize(size: Int) {
        maxSize = size
    }

    // Aggiungi un nuovo campione alla lista
    fun addSample(sample: CompassSample) {
        samplesCount += 1

        // Se la lista è piena, processa i dati e svuota la lista
        if (isFull()) {
            processAndClearData()
        }

        samples.add(sample) // Aggiungi il nuovo campione alla lista
    }

    // Ottieni tutti i campioni
    fun getSamples(): List<CompassSample> {
        return samples.toList() // Restituisce una copia della lista
    }

    // Ottieni l'ultimo campione
    fun getLastSample(): CompassSample? {
        return samples.lastOrNull()
    }

    // Verifica se la lista è piena
    fun isFull(): Boolean {
        return samples.size >= maxSize
    }

    // Processa i dati quando la lista è piena
    private fun processAndClearData() {
        // Simile a quanto fatto con gli accelerometri, qui puoi aggiungere un'elaborazione dei dati
        // Per ora stampiamo i risultati come esempio

        val result = compassDataProcessor.processMeasurements(this)
        print(result)
        // Svuota la lista dei campioni e resetta il contatore
        samples.clear()
        samplesCount = 0
    }
}
