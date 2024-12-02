package com.example.burnify.util


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
