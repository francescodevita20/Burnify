package com.example.burnify

import android.content.Context
import com.example.burnify.model.AccelerometerProcessedSample
import com.example.burnify.model.AccelerometerSample
import com.example.burnify.model.GyroscopeProcessedSample
import com.example.burnify.model.MagnetometerProcessedSample
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import java.io.File

fun setSharedPreferences(context: Context, newMap: Map<String, Any>,sharedPreferencesName: String) {
    try {
        val sharedPreferences = context.getSharedPreferences(sharedPreferencesName, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        // Recupera la lista esistente
        val gson = GsonBuilder().serializeSpecialFloatingPointValues().create()
        val json = sharedPreferences.getString(sharedPreferencesName, null)
        val type = object : TypeToken<List<Map<String, Any>>>() {}.type
        val existingList: MutableList<Map<String, Any>> = if (json.isNullOrEmpty()) {
            mutableListOf() // Se non esiste, crea una nuova lista vuota
        } else {
            gson.fromJson(json, type) // Altrimenti, deserializza l'elenco esistente
        }

        // Aggiungi la nuova mappa
        existingList.add(newMap)

        // Salva nuovamente la lista aggiornata
        val updatedJson = gson.toJson(existingList)
        editor.putString(sharedPreferencesName, updatedJson)
        editor.apply()

        println("Mappa aggiunta e salvata correttamente nello SharedPreferences.")
    } catch (e: Exception) {
        println("Errore durante il salvataggio della mappa: ${e.message}")
    }
}




fun getSharedPreferences(context: Context,sharedPreferencesName: String): List<Map<String, Any>>? {
    return try {
        val sharedPreferences = context.getSharedPreferences(sharedPreferencesName, Context.MODE_PRIVATE)
        val json = sharedPreferences.getString(sharedPreferencesName, null)

        if (json.isNullOrEmpty()) {
            println("Nessun dato trovato nelle SharedPreferences.")
            return null
        } else {
            val gson = Gson()
            val type = object : TypeToken<List<Map<String, Any>>>() {}.type
            val listOfMaps: List<Map<String, Any>> = gson.fromJson(json, type)
            println("SharedPreferences: $sharedPreferencesName recuperata con successo.")
            return listOfMaps
        }
    } catch (e: Exception) {
        println("Errore durante il recupero dei dati: ${e.message}")
        null
    }


}
fun clearSharedPreferences(context: Context,sharedPreferencesName: String) {
    try {

        val sharedPreferences = context.getSharedPreferences(sharedPreferencesName, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        editor.clear() // Cancella tutti i dati
        editor.apply()

        println("SharedPreferences: $sharedPreferencesName cancellate con successo.")
    } catch (e: Exception) {
        println("Errore durante la cancellazione delle SharedPreferences: ${e.message}")
    }
}

fun saveProcessedDataToDatabase(context: Context, processedData: AccelerometerProcessedSample) {

    val correlationXY = if (processedData.correlationXY?.isNaN() == true) null else processedData.correlationXY
    val correlationXZ = if (processedData.correlationXZ?.isNaN() == true) null else processedData.correlationXZ
    val correlationYZ = if (processedData.correlationYZ?.isNaN() == true) null else processedData.correlationYZ
    // Esegui l'inserimento nel database in un thread separato

    val updatedProcessedData = processedData.copy(
        correlationXY = correlationXY,
        correlationXZ = correlationXZ,
        correlationYZ = correlationYZ
    )
    Thread {
    println("Salvataggio in corso...")
        val db = AppDatabaseProvider.getInstance(context) // Usa il singleton
        val dao = db.accelerometerDao()
        println("fermo qui")
        // Inserisci i dati processati nel database
        dao.insertProcessedSample(processedData)
        println("Dati processati salvati nel database!!!!!")
    }.start()
}

fun retrieveProcessedDataFromDatabase(context: Context,daoName: String) {
    // Esegui la lettura dei dati dal database in un thread separato
    if (daoName == "accelerometer") {
    Thread {
        // Ottieni l'istanza del database
        val db = AppDatabaseProvider.getInstance(context) // Usa il singleton del database
        val dao = db.accelerometerDao()

        // Recupera tutti i campioni processati dal database
        val processedSamples = dao.getAllProcessedSamples()

        // Stampa i dati recuperati per la verifica
        println("ACCELEROMETER Dati recuperati dal database:")
        for (sample in processedSamples) {
            println("ID: ${sample.id}, X: $sample.") // Supponendo che l'entità abbia questi campi
        }
    }.start()
}
else if (daoName == "gyroscope") {
        Thread {
            // Ottieni l'istanza del database
            val db = AppDatabaseProvider.getInstance(context) // Usa il singleton del database
            val dao = db.gyroscopeDao()

            // Recupera tutti i campioni processati dal database
            val processedSamples = dao.getAllProcessedSamples()

            // Stampa i dati recuperati per la verifica
            println("GYROSCOPE Dati recuperati dal database:")
            for (sample in processedSamples) {
                println("ID: ${sample.id}, X: $sample.") // Supponendo che l'entità abbia questi campi
            }
        }.start()
}
    else if (daoName == "magnetometer") {
        Thread {
        // Ottieni l'istanza del database
        val db = AppDatabaseProvider.getInstance(context) // Usa il singleton del database
        val dao = db.magnetometerDao()

        // Recupera tutti i campioni processati dal database
        val processedSamples = dao.getAllProcessedSamples()

        // Stampa i dati recuperati per la verifica
        println("MAGNETOMETER Dati recuperati dal database:")
        for (sample in processedSamples) {
            println("ID: ${sample.id}, X: $sample.") // Supponendo che l'entità abbia questi campi
        }
    }.start()}
}

fun saveProcessedDataToDatabase(context: Context, processedData: GyroscopeProcessedSample) {

    // Esegui l'inserimento nel database in un thread separato

    Thread {
        println("Salvataggio in corso...")
        val db = AppDatabaseProvider.getInstance(context) // Usa il singleton
        val dao = db.gyroscopeDao()
        println("fermo qui")
        // Inserisci i dati processati nel database
        dao.insertProcessedSample(processedData)
        println("Dati processati salvati nel database!!!!!")
    }.start()
}

fun saveProcessedDataToDatabase(context: Context, processedData: MagnetometerProcessedSample) {
    Thread {
    println("Salvataggio in corso...")
    val db = AppDatabaseProvider.getInstance(context) // Usa il singleton
    val dao = db.magnetometerDao()
    println("fermo qui")
    // Inserisci i dati processati nel database
    dao.insertProcessedSample(processedData)
    println("Dati processati salvati nel database!!!!!")
}.start()
}


