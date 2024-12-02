package com.example.burnify.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.burnify.database.ActivityPrediction
import com.example.burnify.database.InputModel

/**
 * Data Access Object (DAO) for performing operations on the activity_prediction table.
 */
@Dao
interface ActivityPredictionDao {

    /**
     * Inserts a new ActivityPrediction record with the calculated date.
     * @param activityPrediction The activity prediction to be inserted.
     */
    @Insert
    suspend fun insertActivityPrediction(activityPrediction: ActivityPrediction)

    /**
     * Deletes activity prediction samples that are older than one day.
     * This function removes entries with a processed date older than 1 day from the current time.
     */
    @Query("DELETE FROM activity_prediction WHERE datetime(processedAt) < datetime('now', '-1 day')")
    suspend fun deleteSamplesOlderThanOneDay()

    /**
     * Retrieves activity prediction samples from the last day.
     * @return A list of samples processed within the last day, ordered by the most recent.
     */
    @Query("SELECT * FROM activity_prediction WHERE datetime(processedAt) >= datetime('now', '-1 day') ORDER BY datetime(processedAt) DESC")
    suspend fun getSamplesFromLastDay(): List<ActivityPrediction>

    /**
     * Retrieves all activity prediction samples from the database.
     * @return A list of all activity prediction samples, ordered by the most recent.
     */
    @Query("SELECT * FROM activity_prediction ORDER BY datetime(processedAt) DESC")
    suspend fun getAllSamples(): List<ActivityPrediction>

    /**
     * Retrieves the most recent activity prediction sample.
     * @return The latest activity prediction sample, or null if there are no records.
     */
    @Query("SELECT * FROM activity_prediction ORDER BY id DESC LIMIT 1")
    suspend fun getLastActivityPrediction(): ActivityPrediction?

    /**
     * Updates an existing activity prediction sample in the database.
     * @param activityPrediction The activity prediction sample to be updated.
     */
    @Update
    suspend fun updateActivityPrediction(activityPrediction: ActivityPrediction)
}
