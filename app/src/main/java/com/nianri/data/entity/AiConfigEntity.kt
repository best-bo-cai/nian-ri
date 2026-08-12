package com.nianri.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ai_config")
data class AiConfigEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String = "",
    val provider: String = "openai",
    val baseUrl: String = "",
    val apiKey: String = "",
    val model: String = "",
    val isActive: Boolean = false
)
