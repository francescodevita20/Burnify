package com.example.burnify

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.burnify.model.AccelerometerProcessedSample
import com.example.burnify.model.GyroscopeProcessedSample

@Dao
interface GyroscopeDao {
    @Insert
    fun insertProcessedSample(sample: GyroscopeProcessedSample)

    @Query("SELECT * FROM gyroscope_processed_sample")
    fun getAllProcessedSamples(): List<GyroscopeProcessedSample>
}


