package com.example.burnify.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.burnify.database.ActivityPrediction
import com.example.burnify.database.InputModel

@Dao
interface ActivityPredictionDao {

    // Inserisce un nuovo ActivityPrediction con la data calcolata
    @Insert
    suspend fun insertActivityPrediction(activityPrediction: ActivityPrediction)

    // Elimina i campioni più vecchi di un giorno
    @Query("DELETE FROM activity_prediction WHERE datetime(processedAt) < datetime('now', '-1 day')")
    suspend fun deleteSamplesOlderThanOneDay()

    // Ritorna i campioni dell'ultimo giorno
    @Query("SELECT * FROM activity_prediction WHERE datetime(processedAt) >= datetime('now', '-1 day') ORDER BY datetime(processedAt) DESC")
    suspend fun getSamplesFromLastDay(): List<ActivityPrediction>

    // Ritorna tutti i campioni nella tabella
    @Query("SELECT * FROM activity_prediction ORDER BY datetime(processedAt) DESC")
    suspend fun getAllSamples(): List<ActivityPrediction>

    // Ritorna l'ultimo campione inserito
    @Query("SELECT * FROM activity_prediction ORDER BY id DESC LIMIT 1")
    suspend fun getLastActivityPrediction(): ActivityPrediction?

    // Aggiorna un campione specifico
    @Update
    suspend fun updateActivityPrediction(activityPrediction: ActivityPrediction)
}

