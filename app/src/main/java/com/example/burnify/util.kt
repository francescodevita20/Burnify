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
import com.example.burnify.dao.InputModelDao
import com.example.burnify.database.InputModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit


fun setSharedPreferences(context: Context, newMap: Map<String, Any>, sharedPreferencesName: String) {
    try {
        // Get SharedPreferences
        val sharedPreferences = context.getSharedPreferences(sharedPreferencesName, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        // Use Gson to serialize the map
        val gson = GsonBuilder().serializeSpecialFloatingPointValues().create()

        // Serialize the map and save it as a JSON string
        val updatedJson = gson.toJson(newMap)

        // Save the serialized JSON in SharedPreferences
        editor.putString(sharedPreferencesName, updatedJson)
        editor.apply()

        println("Map saved successfully in SharedPreferences.")
    } catch (e: Exception) {
        println("Error saving map: ${e.message}")
    }
}





fun getSharedPreferences(context: Context, sharedPreferencesName: String): Map<String, Any>? {
    return try {
        val sharedPreferences = context.getSharedPreferences(sharedPreferencesName, Context.MODE_PRIVATE)
        val json = sharedPreferences.getString(sharedPreferencesName, null)

        if (json.isNullOrEmpty()) {
            println("No data found in SharedPreferences.")
            return null
        } else {
            val gson = Gson()
            val type = object : TypeToken<Map<String, Any>>() {}.type
            val map: Map<String, Any> = gson.fromJson(json, type)
            println("SharedPreferences retrieved successfully.")
            map
        }
    } catch (e: Exception) {
        println("Error retrieving data: ${e.message}")
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
        val dao = db.gyroscopeDao()
        println("fermo qui")
        // Inserisci i dati processati nel database
        dao.insertProcessedSample(processedData)
        println("Dati processati salvati nel database!!!!!")
    }.start()
}

fun saveProcessedDataToDatabase(context: Context, processedData: MagnetometerProcessedSample) {

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
/*




    // Funzione per aggiungere un ModelInput con predizione
    suspend fun addInputModel(input: Any, context: Context) = withContext(Dispatchers.IO) {
        val db = AppDatabaseProvider.getInstance(context)
        val dao = db.inputModelDao()
        val lastInput = dao.getLastInputModel()

        // Se il database è vuoto, crea una nuova riga iniziale
        if (lastInput == null) {
            addNewInputModelFromInput(input, dao)
            return@withContext
        }

        // Gestione dell'input in base al tipo
        when (input) {
            is AccelerometerProcessedSample -> handleAccelerometerInput(lastInput, input, dao,context)
            is GyroscopeProcessedSample -> handleGyroscopeInput(lastInput, input, dao,context)
            is MagnetometerProcessedSample -> handleMagnetometerInput(lastInput, input, dao,context)
        }
    }

// Gestione dell'input accelerometro
private suspend fun handleAccelerometerInput(lastInput: InputModel?, input: AccelerometerProcessedSample, dao: InputModelDao,context: Context) {
    if (lastInput != null && isFull(lastInput)) {
        // Predizione del modello
        println("Pippo" + lastInput.toString())
        //val prediction = predictModel(context,lastInput)

        //lastInput.label = prediction
        dao.updateInputModel(lastInput)

        // Aggiungi una nuova riga vuota con solo i dati dell'accelerometro
        addNewInputModel(
            accX = input.meanX,
            accY = input.meanY,
            accZ = input.meanZ,
            gyroX = Float.NaN,
            gyroY = Float.NaN,
            gyroZ = Float.NaN,
            magnX = Float.NaN,
            magnY = Float.NaN,
            magnZ = Float.NaN,
            dao = dao
        )
    } else {
        // Aggiorna l'ultima riga con i dati dell'accelerometro
        lastInput?.apply {
            accX = input.meanX
            accY = input.meanY
            accZ = input.meanZ
        }
        lastInput?.let { dao.updateInputModel(it) }
    }
}

// Gestione dell'input giroscopio
private suspend fun handleGyroscopeInput(lastInput: InputModel?, input: GyroscopeProcessedSample, dao: InputModelDao,context: Context) {
    if (lastInput != null && isFull(lastInput)) {
        // Predizione del modello
        println(lastInput.toString())
        //val prediction = predictModel(context,lastInput)
        //lastInput.label = prediction
        dao.updateInputModel(lastInput)

        // Aggiungi una nuova riga vuota con solo i dati del giroscopio
        addNewInputModel(
            accX = Float.NaN,
            accY = Float.NaN,
            accZ = Float.NaN,
            gyroX = input.meanX,
            gyroY = input.meanY,
            gyroZ = input.meanZ,
            magnX = Float.NaN,
            magnY = Float.NaN,
            magnZ = Float.NaN,
            dao = dao
        )
    } else {
        // Aggiorna l'ultima riga con i dati del giroscopio
        lastInput?.apply {
            gyroX = input.meanX
            gyroY = input.meanY
            gyroZ = input.meanZ
        }
        lastInput?.let { dao.updateInputModel(it) }
    }
}

// Gestione dell'input magnetometro
private suspend fun handleMagnetometerInput(lastInput: InputModel?, input: MagnetometerProcessedSample, dao: InputModelDao,context: Context) {
    if (lastInput != null && isFull(lastInput)) {
        // Predizione del modello
        println(lastInput.toString())
        //val prediction = predictModel(context,lastInput)
        //lastInput.label = prediction
        dao.updateInputModel(lastInput)

        // Aggiungi una nuova riga vuota con solo i dati del magnetometro
        addNewInputModel(
            accX = Float.NaN,
            accY = Float.NaN,
            accZ = Float.NaN,
            gyroX = Float.NaN,
            gyroY = Float.NaN,
            gyroZ = Float.NaN,
            magnX = input.meanX,
            magnY = input.meanY,
            magnZ = input.meanZ,
            dao = dao
        )
    } else {
        // Aggiorna l'ultima riga con i dati del magnetometro
        lastInput?.apply {
            magnX = input.meanX
            magnY = input.meanY
            magnZ = input.meanZ
        }
        lastInput?.let { dao.updateInputModel(it) }
    }
}

// Funzione per aggiungere una nuova riga basata sul tipo di input
private suspend fun addNewInputModelFromInput(input: Any, dao: InputModelDao) {
    when (input) {
        is AccelerometerProcessedSample -> addNewInputModel(
            accX = input.meanX,
            accY = input.meanY,
            accZ = input.meanZ,
            gyroX = Float.NaN,
            gyroY = Float.NaN,
            gyroZ = Float.NaN,
            magnX = Float.NaN,
            magnY = Float.NaN,
            magnZ = Float.NaN,
            dao = dao
        )
        is GyroscopeProcessedSample -> addNewInputModel(
            accX = Float.NaN,
            accY = Float.NaN,
            accZ = Float.NaN,
            gyroX = input.meanX,
            gyroY = input.meanY,
            gyroZ = input.meanZ,
            magnX = Float.NaN,
            magnY = Float.NaN,
            magnZ = Float.NaN,
            dao = dao
        )
        is MagnetometerProcessedSample -> addNewInputModel(
            accX = Float.NaN,
            accY = Float.NaN,
            accZ = Float.NaN,
            gyroX = Float.NaN,
            gyroY = Float.NaN,
            gyroZ = Float.NaN,
            magnX = input.meanX,
            magnY = input.meanY,
            magnZ = input.meanZ,
            dao = dao
        )
    }
}

// Aggiungi un nuovo InputModel al database
private suspend fun addNewInputModel(
    accX: Float = Float.NaN,
    accY: Float = Float.NaN,
    accZ: Float = Float.NaN,
    gyroX: Float = Float.NaN,
    gyroY: Float = Float.NaN,
    gyroZ: Float = Float.NaN,
    magnX: Float = Float.NaN,
    magnY: Float = Float.NaN,
    magnZ: Float = Float.NaN,
    dao: InputModelDao
) {
    if (dao.getRowCount() >= 5) {
        dao.deleteOldestInputModel()
    }
    dao.insertInputModel(
        InputModel(
            processedAt = System.currentTimeMillis().toString(),
            accX = accX.replaceNaNWithNull(),
            accY = accY.replaceNaNWithNull(),
            accZ = accZ.replaceNaNWithNull(),
            gyroX = gyroX.replaceNaNWithNull(),
            gyroY = gyroY.replaceNaNWithNull(),
            gyroZ = gyroZ.replaceNaNWithNull(),
            magnX = magnX.replaceNaNWithNull(),
            magnY = magnY.replaceNaNWithNull(),
            magnZ = magnZ.replaceNaNWithNull()
        )
    )
}


private fun predictModel(context: Context, input: InputModel): String {
    return try {
        // Inizializza il modello di inferenza

       println("STAMPO I VALORI DI INPUT")
            println(input.accX.toString() + " " + input.accY + " " + input.accZ + " " + input.gyroX + " " + input.gyroY + " " + input.gyroZ + " " + input.magnX + " " + input.magnY + " " + input.magnZ)
        println("STO FACENDO LA PREDIZIONE")



        val modelInference = LSTMModel(context,"activity_recognition_model.tflite")

        // Prepara un array Float di 9 valori dall'oggetto InputModel
        val inputArray = floatArrayOf(
            input.accX ?: 0f,
            input.accY ?: 0f,
            input.accZ ?: 0f,
            input.gyroX ?: 0f,
            input.gyroY ?: 0f,
            input.gyroZ ?: 0f,
            input.magnX ?: 0f,
            input.magnY ?: 0f,
            input.magnZ ?: 0f
        )

        // Esegui l'inferenza sul modello
        val output = modelInference.predict(inputArray)

        // Trova l'indice della classe con la probabilità più alta
        val predictedClassIndex = output.withIndex().maxByOrNull { it.value }?.index ?: -1

        // Definisci le etichette delle classi
        val labels = listOf(
            "Class1", "Class2", "Class3", "Class4", "Class5",
            "Class6", "Class7", "Class8", "Class9", "Class10",
            "Class11", "Class12"
        )

        // Restituisci l'etichetta corrispondente o "Classe Sconosciuta" se l'indice non è valido
        labels.getOrNull(predictedClassIndex) ?: "Unknown Class"

    } catch (e: Exception) {
        // Gestione degli errori: logga l'eccezione e restituisci una classe sconosciuta
        e.printStackTrace()
        "Prediction Failed"
    }
}



// Verifica se una riga è piena
private fun isFull(input: InputModel): Boolean {
    return input.accX?.isNaN()?.not() == true &&
            input.accY?.isNaN()?.not() == true &&
            input.accZ?.isNaN()?.not() == true &&
            input.gyroX?.isNaN()?.not() == true &&
            input.gyroY?.isNaN()?.not() == true &&
            input.gyroZ?.isNaN()?.not() == true &&
            input.magnX?.isNaN()?.not() == true &&
            input.magnY?.isNaN()?.not() == true &&
            input.magnZ?.isNaN()?.not() == true
}

// Helper function to replace NaN values with null
private fun Float?.replaceNaNWithNull(): Float? {
    return if (this?.isNaN() == true) null else this
}
*/
