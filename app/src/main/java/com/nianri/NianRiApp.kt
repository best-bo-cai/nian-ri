package com.nianri

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.nianri.data.database.AppDatabase
import com.nianri.data.repository.EventRepository

class NianRiApp : Application() {

    val database by lazy { AppDatabase.getDatabase(this) }
    val repository by lazy {
        EventRepository(
            database.eventDao(),
            database.aiConfigDao(),
            database.smtpConfigDao()
        )
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "nianri_reminder",
                "念日提醒",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "用于显示日子和事件的提醒通知"
            }
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }
}
