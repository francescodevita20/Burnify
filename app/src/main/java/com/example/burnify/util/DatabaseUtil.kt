package com.example.burnify.util

import android.content.Context
import androidx.work.Constraints
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.burnify.database.AccelerometerProcessedSample
import com.example.burnify.database.AppDatabaseProvider
import com.example.burnify.database.GyroscopeProcessedSample
import com.example.burnify.database.MagnetometerProcessedSample
import java.util.concurrent.TimeUnit

fun saveProcessedDataToDatabase(context: Context, processedData: AccelerometerProcessedSample) {
    val correlationXY = if (processedData.correlationXY?.isNaN() == true) null else processedData.correlationXY
    val correlationXZ = if (processedData.correlationXZ?.isNaN() == true) null else processedData.correlationXZ
    val correlationYZ = if (processedData.correlationYZ?.isNaN() == true) null else processedData.correlationYZ

    val updatedProcessedData = processedData.copy(
        correlationXY = correlationXY,
        correlationXZ = correlationXZ,
        correlationYZ = correlationYZ
    )
    Thread {
        val db = AppDatabaseProvider.getInstance(context)
        val dao = db.accelerometerDao()
        dao.insertProcessedSample(processedData)
        println("Processed data saved to database!")
    }.start()
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



fun deleteOldSamples(context: Context) {
    Thread {
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


class DatabaseCleanupWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {

    override fun doWork(): Result {
        val db = AppDatabaseProvider.getInstance(applicationContext)
        val accelerometerDao = db.accelerometerDao()
        val gyroscopeDao = db.gyroscopeDao()
        val magnetometerDao = db.magnetometerDao()

        accelerometerDao.deleteOldSamples()
        gyroscopeDao.deleteOldSamples()
        magnetometerDao.deleteOldSamples()

        return Result.success()
    }
}

fun scheduleDatabaseCleanup(context: Context) {
    val workRequest = PeriodicWorkRequestBuilder<DatabaseCleanupWorker>(6, TimeUnit.HOURS)
        .setConstraints(
            Constraints.Builder()
                .setRequiresBatteryNotLow(true)
                .build()
        )
        .build()

    WorkManager.getInstance(context).enqueue(workRequest)
}
