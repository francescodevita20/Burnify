package com.example.burnify

import android.content.Context
import com.example.burnify.database.AccelerometerProcessedSample
import com.example.burnify.database.AppDatabaseProvider
import com.example.burnify.database.GyroscopeProcessedSample
import com.example.burnify.database.MagnetometerProcessedSample
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.work.Constraints
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import java.util.concurrent.TimeUnit


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

fun deleteOldSamples(context: Context) {

    Thread{
        val db = AppDatabaseProvider.getInstance(context)
        val accelerometerDao = db.accelerometerDao()
        val gyroscopeDao = db.gyroscopeDao()
        val magnetometerDao = db.magnetometerDao()
        accelerometerDao.deleteOldSamples()
        gyroscopeDao.deleteOldSamples()
        magnetometerDao.deleteOldSamples()

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



class NotificationHelper(private val context: Context) {

    companion object {
        const val GROUP_KEY = "com.example.burnify.services"
        const val CHANNEL_ID = "ServicesChannel"
    }

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        // Crea il canale di notifica se non è già creato
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Servizi Burnify",
                NotificationManager.IMPORTANCE_LOW
            )
            notificationManager.createNotificationChannel(channel)
        }
    }

    // Notifica principale (il gruppo)
    fun createGroupNotification(): Notification {
        return Notification.Builder(context, CHANNEL_ID)
            .setContentTitle("Servizi attivi")
            .setContentText("Stai usando più servizi attivi.")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setGroup(GROUP_KEY)
            .setGroupSummary(true) // Mostra questa come principale
            .build()
    }

    // Notifica individuale per ogni servizio
    fun createServiceNotification(serviceName: String): Notification {
        return Notification.Builder(context, CHANNEL_ID)
            .setContentTitle(serviceName + " is running in background")
            .setContentText("")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setGroup(GROUP_KEY) // Associa questa notifica al gruppo
            .setStyle(null)
            .build()
    }

    fun notify(notificationId: Int, notification: Notification) {
        notificationManager.notify(notificationId, notification)
    }

    fun cancel(notificationId: Int) {
        notificationManager.cancel(notificationId)
    }
}

class DatabaseCleanupWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {

    override fun doWork(): Result {
        val db = AppDatabaseProvider.getInstance(applicationContext)
        val accelerometerDao = db.accelerometerDao()
        val gyroscopeDao = db.gyroscopeDao()
        val magnetometerDao = db.magnetometerDao()

        accelerometerDao.deleteOldSamples()
        gyroscopeDao.deleteOldSamples()
        magnetometerDao.deleteOldSamples()

        // Indica che il lavoro è stato completato con successo
        return Result.success()
    }
}


fun scheduleDatabaseCleanup(context: Context) {
    val workRequest = PeriodicWorkRequestBuilder<DatabaseCleanupWorker>(6, TimeUnit.HOURS)
        .setConstraints(
            Constraints.Builder()
                .setRequiresBatteryNotLow(true) // Opzionale: solo quando la batteria è sufficiente
                .build()
        )
        .build()

    WorkManager.getInstance(context).enqueue(workRequest)
}





