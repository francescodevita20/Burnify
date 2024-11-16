package com.example.burnify

import android.content.Context
import com.example.burnify.model.AccelerometerSample
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import java.io.File

fun setSharedPreferences(context: Context, newMap: Map<String, Any>) {
    try {
        val sharedPreferences = context.getSharedPreferences("ProcessedDataPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        // Recupera la lista esistente
        val gson = GsonBuilder().serializeSpecialFloatingPointValues().create()
        val json = sharedPreferences.getString("processed_data_key", null)
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
        editor.putString("processed_data_key", updatedJson)
        editor.apply()

        println("Mappa aggiunta e salvata correttamente nello SharedPreferences.")
    } catch (e: Exception) {
        println("Errore durante il salvataggio della mappa: ${e.message}")
    }
}




fun getSharedPreferences(context: Context): List<Map<String, Any>>? {
    return try {
        val sharedPreferences = context.getSharedPreferences("ProcessedDataPrefs", Context.MODE_PRIVATE)
        val json = sharedPreferences.getString("processed_data_key", null)

        if (json.isNullOrEmpty()) {
            println("Nessun dato trovato nelle SharedPreferences.")
            return null
        } else {
            val gson = Gson()
            val type = object : TypeToken<List<Map<String, Any>>>() {}.type
            val listOfMaps: List<Map<String, Any>> = gson.fromJson(json, type)
            println("Lista di mappe recuperata con successo.")
            return listOfMaps
        }
    } catch (e: Exception) {
        println("Errore durante il recupero dei dati: ${e.message}")
        null
    }


}
fun clearSharedPreferences(context: Context) {
    try {
        val sharedPreferences = context.getSharedPreferences("ProcessedDataPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        editor.clear() // Cancella tutti i dati
        editor.apply()

        println("SharedPreferences cancellate con successo.")
    } catch (e: Exception) {
        println("Errore durante la cancellazione delle SharedPreferences: ${e.message}")
    }
}




