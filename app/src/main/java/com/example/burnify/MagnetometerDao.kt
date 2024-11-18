package com.example.burnify

import com.example.burnify.model.MagnetometerProcessedSample
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface MagnetometerDao {
    @Insert
    fun insertProcessedSample(sample: MagnetometerProcessedSample)

    @Query("SELECT * FROM magnetometer_processed_sample")
    fun getAllProcessedSamples(): List<MagnetometerProcessedSample>

    @Query("DELETE FROM magnetometer_processed_sample")
    suspend fun deleteAllMagnetometerSamples()
}


