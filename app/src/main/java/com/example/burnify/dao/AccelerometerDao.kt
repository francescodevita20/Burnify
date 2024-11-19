package com.example.burnify.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.burnify.database.AccelerometerProcessedSample

@Dao
interface AccelerometerDao {
    @Insert
    fun insertProcessedSample(sample: AccelerometerProcessedSample)

    @Query("SELECT * FROM accelerometer_processed_sample")
    fun getAllProcessedSamples(): List<AccelerometerProcessedSample>

    // Elimina tutte le righe con un'età superiore a 1 giorno
    @Query("""
        DELETE FROM accelerometer_processed_sample
        WHERE processedAt <= datetime('now', '-1 day')
    """) fun deleteOldSamples()

    // Questa funzione elimina tutti i campioni dal database, usala da IDE
    @Query("DELETE FROM accelerometer_processed_sample")
    fun deleteAllAccelerometerSamples()
}
