package com.example.burnify

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.burnify.model.AccelerometerProcessedSample

@Database(entities = [AccelerometerProcessedSample::class], version = 2)
abstract class AppDatabase : RoomDatabase() {
    abstract fun accelerometerDao(): AccelerometerDao
    //abstract fun gyroscopeProcessedSampleDao(): GyroscopeProcessedSampleDao TODO: ADD GYROSCOPE DAO AND ENTITY
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
