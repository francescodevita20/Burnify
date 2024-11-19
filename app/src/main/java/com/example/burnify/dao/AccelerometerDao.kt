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

    //This function delete all samples from db, use it from IDE
    @Query("DELETE FROM accelerometer_processed_sample")
    suspend fun deleteAllAccelerometerSamples()
}


