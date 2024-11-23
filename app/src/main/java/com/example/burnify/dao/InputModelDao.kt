package com.example.burnify.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.burnify.database.InputModel

@Dao
interface InputModelDao {

    @Insert
    suspend fun insertInputModel(input: InputModel)

    @Query("DELETE FROM input_model WHERE id = (SELECT id FROM input_model ORDER BY id ASC LIMIT 1)")
    suspend fun deleteOldestInputModel()

    @Query("SELECT * FROM input_model ORDER BY id DESC LIMIT 1")
    suspend fun getLastInputModel(): InputModel?

    @Query("SELECT COUNT(*) FROM input_model")
    suspend fun getRowCount(): Int

    @Update
    suspend fun updateInputModel(input: InputModel)
}
