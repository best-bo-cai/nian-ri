package com.nianri.data.dao

import androidx.room.*
import com.nianri.data.entity.AiConfigEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AiConfigDao {

    @Query("SELECT * FROM ai_config ORDER BY id ASC")
    fun getAll(): Flow<List<AiConfigEntity>>

    @Query("SELECT * FROM ai_config WHERE isActive = 1 LIMIT 1")
    suspend fun getActive(): AiConfigEntity?

    @Query("SELECT * FROM ai_config WHERE id = :id")
    suspend fun getById(id: Int): AiConfigEntity?

    @Insert
    suspend fun insert(config: AiConfigEntity)

    @Update
    suspend fun update(config: AiConfigEntity)

    @Query("DELETE FROM ai_config WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Query("UPDATE ai_config SET isActive = 0")
    suspend fun deactivateAll()

    @Query("UPDATE ai_config SET isActive = 1 WHERE id = :id")
    suspend fun setActive(id: Int)
}
