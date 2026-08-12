package com.nianri.data.dao

import androidx.room.*
import com.nianri.data.entity.SmtpConfigEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SmtpConfigDao {

    @Query("SELECT * FROM smtp_config ORDER BY id ASC")
    fun getAll(): Flow<List<SmtpConfigEntity>>

    @Query("SELECT * FROM smtp_config WHERE isActive = 1 LIMIT 1")
    suspend fun getActive(): SmtpConfigEntity?

    @Query("SELECT * FROM smtp_config WHERE id = :id")
    suspend fun getById(id: Int): SmtpConfigEntity?

    @Insert
    suspend fun insert(config: SmtpConfigEntity)

    @Update
    suspend fun update(config: SmtpConfigEntity)

    @Query("DELETE FROM smtp_config WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Query("UPDATE smtp_config SET isActive = 0")
    suspend fun deactivateAll()

    @Query("UPDATE smtp_config SET isActive = 1 WHERE id = :id")
    suspend fun setActive(id: Int)
}
