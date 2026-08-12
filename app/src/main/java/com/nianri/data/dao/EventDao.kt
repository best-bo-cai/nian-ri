package com.nianri.data.dao

import androidx.room.*
import com.nianri.data.entity.EventEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EventDao {

    @Query("SELECT * FROM events ORDER BY date ASC")
    fun getAllEvents(): Flow<List<EventEntity>>

    @Query("SELECT * FROM events WHERE type != 'holiday' ORDER BY date ASC")
    fun getNonHolidayEvents(): Flow<List<EventEntity>>

    @Query("SELECT * FROM events WHERE type = 'birthday' OR type = 'anniversary' OR type = 'holiday' ORDER BY date ASC")
    fun getDays(): Flow<List<EventEntity>>

    @Query("SELECT * FROM events WHERE type = 'event' ORDER BY date ASC")
    fun getEvents(): Flow<List<EventEntity>>

    @Query("SELECT * FROM events WHERE id = :id")
    suspend fun getEventById(id: Long): EventEntity?

    @Query("SELECT * FROM events WHERE name LIKE '%' || :query || '%'")
    fun searchEvents(query: String): Flow<List<EventEntity>>

    @Query("SELECT COUNT(*) FROM events")
    fun getTotalCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM events WHERE type = 'event'")
    fun getEventCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM events WHERE type = 'anniversary'")
    fun getAnniversaryCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM events WHERE type = 'birthday'")
    fun getBirthdayCount(): Flow<Int>

    @Query("SELECT * FROM events WHERE isDefault = 1")
    fun getDefaultEvents(): Flow<List<EventEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: EventEntity): Long

    @Update
    suspend fun updateEvent(event: EventEntity)

    @Delete
    suspend fun deleteEvent(event: EventEntity)

    @Query("DELETE FROM events WHERE id = :id")
    suspend fun deleteEventById(id: Long)

    @Query("DELETE FROM events WHERE isDefault = 1")
    suspend fun deleteDefaultEvents()
}
