package com.example.burnify.dao

import com.example.burnify.database.MagnetometerProcessedSample
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface MagnetometerDao {
    @Insert
    fun insertProcessedSample(sample: MagnetometerProcessedSample)

    @Query("SELECT * FROM magnetometer_processed_sample")
    fun getAllProcessedSamples(): List<MagnetometerProcessedSample>

    //This function delete all samples from db, use it from IDE
    @Query("DELETE FROM magnetometer_processed_sample")
    suspend fun deleteAllMagnetometerSamples()
}


