package com.example.burnify

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.burnify.model.AccelerometerProcessedSample
import com.example.burnify.model.GyroscopeProcessedSample
import com.example.burnify.model.MagnetometerProcessedSample

@Database(entities = [AccelerometerProcessedSample::class, GyroscopeProcessedSample::class, MagnetometerProcessedSample::class], version = 4)
abstract class AppDatabase : RoomDatabase() {
    abstract fun accelerometerDao(): AccelerometerDao
    abstract fun gyroscopeDao(): GyroscopeDao
    abstract fun magnetometerDao(): MagnetometerDao
}

object AppDatabaseProvider {

    @Volatile
    private var INSTANCE: AppDatabase? = null

    fun getInstance(context: Context): AppDatabase {
        return INSTANCE ?: synchronized(this) {
            val instance = Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "app_database"
            ).fallbackToDestructiveMigration().build()
            INSTANCE = instance
            instance
        }
    }
}
