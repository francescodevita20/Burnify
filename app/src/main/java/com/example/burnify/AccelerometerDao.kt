package com.example.burnify

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.burnify.model.AccelerometerProcessedSample

@Dao
interface AccelerometerDao {
    @Insert
    fun insertProcessedSample(sample: AccelerometerProcessedSample)

    @Query("SELECT * FROM accelerometer_processed_sample")
    fun getAllProcessedSamples(): List<AccelerometerProcessedSample>

    @Query("DELETE FROM accelerometer_processed_sample")
    suspend fun deleteAllAccelerometerSamples()
}


