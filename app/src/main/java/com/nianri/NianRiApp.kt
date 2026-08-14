package com.nianri

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.nianri.data.database.AppDatabase
import com.nianri.data.repository.EventRepository
import com.nianri.util.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class NianRiApp : Application() {

    val database by lazy { AppDatabase.getDatabase(this) }
    val repository by lazy {
        EventRepository(
            database.eventDao(),
            database.aiConfigDao(),
            database.smtpConfigDao()
        )
    }

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        rescheduleAllReminders()
    }

    /**
     * 应用启动时恢复所有开启提醒的事件通知。
     * 设备重启后 AlarmManager 的闹钟会丢失，需要在启动时重新调度。
     */
    private fun rescheduleAllReminders() {
        appScope.launch {
            runCatching {
                repository.getAllEvents().first()
                    .filter { it.reminderEnabled && !it.completed }
                    .forEach { event ->
                        NotificationHelper.scheduleNotification(this@NianRiApp, event)
                    }
            }
        }
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
