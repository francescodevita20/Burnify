package com.example.burnify.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.burnify.database.GyroscopeProcessedSample

@Dao
interface GyroscopeDao {
    @Insert
    fun insertProcessedSample(sample: GyroscopeProcessedSample)

    @Query("SELECT * FROM gyroscope_processed_sample")
    fun getAllProcessedSamples(): List<GyroscopeProcessedSample>

    //This function delete all samples from db, use it from IDE
    @Query("DELETE FROM gyroscope_processed_sample")
    suspend fun deleteAllGyroscopeSamples()
}


