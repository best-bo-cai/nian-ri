package com.nianri.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "smtp_config")
data class SmtpConfigEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String = "",
    val email: String = "",
    val smtpServer: String = "",
    val port: Int = 465,
    val password: String = "",
    val isActive: Boolean = false
)
