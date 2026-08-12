package com.nianri.data.repository

import com.nianri.data.dao.AiConfigDao
import com.nianri.data.dao.EventDao
import com.nianri.data.dao.SmtpConfigDao
import com.nianri.data.entity.AiConfigEntity
import com.nianri.data.entity.EventEntity
import com.nianri.data.entity.SmtpConfigEntity
import com.nianri.util.DateUtils
import com.nianri.util.EncryptionUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class EventRepository(
    private val eventDao: EventDao,
    private val aiConfigDao: AiConfigDao,
    private val smtpConfigDao: SmtpConfigDao
) {
    fun getAllEvents(): Flow<List<EventEntity>> = eventDao.getAllEvents()

    fun getDays(): Flow<List<EventEntity>> = eventDao.getDays()
        .map { events ->
            events.map { event ->
                val nextDate = DateUtils.getNextOccurrence(event.date, event.repeatRule)
                event.copy(date = nextDate)
            }.sortedBy { it.date }
        }

    fun getEvents(): Flow<List<EventEntity>> = eventDao.getEvents()

    fun searchEvents(query: String): Flow<List<EventEntity>> = eventDao.searchEvents(query)

    suspend fun getEventById(id: Long): EventEntity? = eventDao.getEventById(id)

    fun getTotalCount(): Flow<Int> = eventDao.getTotalCount()
    fun getEventCount(): Flow<Int> = eventDao.getEventCount()
    fun getAnniversaryCount(): Flow<Int> = eventDao.getAnniversaryCount()
    fun getBirthdayCount(): Flow<Int> = eventDao.getBirthdayCount()

    suspend fun insertEvent(event: EventEntity): Long = eventDao.insertEvent(event)

    suspend fun updateEvent(event: EventEntity) = eventDao.updateEvent(event)

    suspend fun deleteEvent(event: EventEntity) = eventDao.deleteEvent(event)

    suspend fun deleteEventById(id: Long) = eventDao.deleteEventById(id)

    fun getAiConfigs(): Flow<List<AiConfigEntity>> = aiConfigDao.getAll()

    suspend fun getActiveAiConfig(): AiConfigEntity? {
        val config = aiConfigDao.getActive() ?: return null
        return config.copy(apiKey = EncryptionUtils.decrypt(config.apiKey))
    }

    suspend fun saveAiConfig(config: AiConfigEntity) {
        val encryptedConfig = config.copy(apiKey = EncryptionUtils.encrypt(config.apiKey))
        aiConfigDao.insert(encryptedConfig)
    }

    suspend fun updateAiConfig(config: AiConfigEntity) {
        val encryptedConfig = config.copy(apiKey = EncryptionUtils.encrypt(config.apiKey))
        aiConfigDao.update(encryptedConfig)
    }

    suspend fun deleteAiConfig(id: Int) = aiConfigDao.deleteById(id)

    suspend fun setActiveAiConfig(id: Int) {
        aiConfigDao.deactivateAll()
        aiConfigDao.setActive(id)
    }

    fun getSmtpConfigs(): Flow<List<SmtpConfigEntity>> = smtpConfigDao.getAll()

    suspend fun getActiveSmtpConfig(): SmtpConfigEntity? {
        val config = smtpConfigDao.getActive() ?: return null
        return config.copy(password = EncryptionUtils.decrypt(config.password))
    }

    suspend fun saveSmtpConfig(config: SmtpConfigEntity) {
        val encryptedConfig = config.copy(password = EncryptionUtils.encrypt(config.password))
        smtpConfigDao.insert(encryptedConfig)
    }

    suspend fun updateSmtpConfig(config: SmtpConfigEntity) {
        val encryptedConfig = config.copy(password = EncryptionUtils.encrypt(config.password))
        smtpConfigDao.update(encryptedConfig)
    }

    suspend fun deleteSmtpConfig(id: Int) = smtpConfigDao.deleteById(id)

    suspend fun setActiveSmtpConfig(id: Int) {
        smtpConfigDao.deactivateAll()
        smtpConfigDao.setActive(id)
    }

    suspend fun insertDefaultEvents() {
        val now = System.currentTimeMillis()
        val cal = java.util.Calendar.getInstance()

        val springFestival2027 = DateUtils.parseDate("2027-02-06")
        eventDao.insertEvent(
            EventEntity(
                type = "holiday",
                name = "春节",
                calendarType = "solar",
                date = springFestival2027,
                displayMode = "countdown",
                repeatRule = "yearly",
                reminderEnabled = true,
                reminderMethods = "local",
                reminderTimes = "day_before_1,day_before_3",
                isDefault = true
            )
        )

        cal.timeInMillis = now
        cal.set(java.util.Calendar.MONTH, java.util.Calendar.JUNE)
        cal.set(java.util.Calendar.DAY_OF_WEEK_IN_MONTH, 3)
        cal.set(java.util.Calendar.DAY_OF_WEEK, java.util.Calendar.SUNDAY)
        val fathersDay = cal.timeInMillis
        eventDao.insertEvent(
            EventEntity(
                type = "holiday",
                name = "父亲节",
                calendarType = "solar",
                date = fathersDay,
                displayMode = "countdown",
                repeatRule = "yearly",
                reminderEnabled = true,
                reminderMethods = "local",
                reminderTimes = "day_before_1",
                isDefault = true
            )
        )

        cal.timeInMillis = now
        cal.set(java.util.Calendar.MONTH, java.util.Calendar.MAY)
        cal.set(java.util.Calendar.DAY_OF_WEEK_IN_MONTH, 2)
        cal.set(java.util.Calendar.DAY_OF_WEEK, java.util.Calendar.SUNDAY)
        val mothersDay = cal.timeInMillis
        eventDao.insertEvent(
            EventEntity(
                type = "holiday",
                name = "母亲节",
                calendarType = "solar",
                date = mothersDay,
                displayMode = "countdown",
                repeatRule = "yearly",
                reminderEnabled = true,
                reminderMethods = "local",
                reminderTimes = "day_before_1",
                isDefault = true
            )
        )

        val nextHoliday = DateUtils.parseDate("2026-10-01")
        eventDao.insertEvent(
            EventEntity(
                type = "holiday",
                name = "下一个法定节假日",
                calendarType = "solar",
                date = nextHoliday,
                displayMode = "countdown",
                repeatRule = "yearly",
                reminderEnabled = false,
                isDefault = true
            )
        )
    }
}
