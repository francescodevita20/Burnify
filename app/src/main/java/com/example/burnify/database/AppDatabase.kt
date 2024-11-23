package com.example.burnify.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.burnify.dao.AccelerometerDao
import com.example.burnify.dao.GyroscopeDao
import com.example.burnify.dao.MagnetometerDao
import com.example.burnify.dao.InputModelDao


@Database(
    entities = [AccelerometerProcessedSample::class, GyroscopeProcessedSample::class, MagnetometerProcessedSample::class, InputModel::class],
    version = 9 // Incrementa la versione qui
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun accelerometerDao(): AccelerometerDao
    abstract fun gyroscopeDao(): GyroscopeDao
    abstract fun magnetometerDao(): MagnetometerDao
    abstract fun inputModelDao(): InputModelDao
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

    // Definizione della migrazione
    private val MIGRATION_6_7 = object : Migration(6, 7) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // Verifica se la tabella input_model esiste
            val cursor = database.query(
                "SELECT name FROM sqlite_master WHERE type='table' AND name='input_model'"
            )
            val tableExists = cursor.count > 0
            cursor.close()

            if (tableExists) {
                // Se la tabella esiste, crea la tabella temporanea
                database.execSQL(
                    """
                CREATE TABLE input_model_temp (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    processedAt TEXT NOT NULL,
                    accX REAL,
                    accY REAL,
                    accZ REAL,
                    gyroX REAL,
                    gyroY REAL,
                    gyroZ REAL,
                    magnX REAL,
                    magnY REAL,
                    magnZ REAL,
                    label TEXT
                )
                """.trimIndent()
                )

                // Copia i dati dalla vecchia tabella alla nuova
                database.execSQL(
                    """
                INSERT INTO input_model_temp (
                    id, processedAt, accX, accY, accZ, gyroX, gyroY, gyroZ, magnX, magnY, magnZ, label
                )
                SELECT 
                    id, processedAt, accX, accY, accZ, gyroX, gyroY, gyroZ, magnX, magnY, magnZ, label
                FROM input_model
                """.trimIndent()
                )
                // Elimina la vecchia tabella
                database.execSQL("DROP TABLE input_model")
                // Rinomina la tabella temporanea
                database.execSQL("ALTER TABLE input_model_temp RENAME TO input_model")
            } else {
                // Se la tabella non esiste, creala direttamente
                database.execSQL(
                    """
                CREATE TABLE input_model (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    processedAt TEXT NOT NULL,
                    accX REAL,
                    accY REAL,
                    accZ REAL,
                    gyroX REAL,
                    gyroY REAL,
                    gyroZ REAL,
                    magnX REAL,
                    magnY REAL,
                    magnZ REAL,
                    label TEXT
                )
                """.trimIndent()
                )
            }
        }
    }

}
