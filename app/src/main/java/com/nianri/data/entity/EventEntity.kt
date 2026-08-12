package com.nianri.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "events")
data class EventEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val type: String,
    val name: String,
    val calendarType: String,
    val date: Long,
    val endDate: Long? = null,
    val displayMode: String,
    val repeatRule: String,
    val reminderEnabled: Boolean = false,
    val reminderMethods: String = "",
    val reminderTimes: String = "",
    val icon: String = "",
    val email: String = "",
    val notes: String = "",
    val completed: Boolean = false,
    val isDefault: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
