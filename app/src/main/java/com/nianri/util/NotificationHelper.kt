package com.nianri.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.nianri.R
import com.nianri.data.entity.EventEntity
import java.util.Calendar

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val eventName = intent.getStringExtra("event_name") ?: "念日提醒"
        val eventId = intent.getLongExtra("event_id", 0)

        val notification = NotificationCompat.Builder(context, "nianri_reminder")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("念日提醒")
            .setContentText(eventName)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(eventId.toInt(), notification)
        } catch (e: SecurityException) {
            // Permission not granted
        }
    }
}

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            // Re-schedule all alarms after boot
            // This would need to query the database and re-schedule
        }
    }
}

object NotificationHelper {

    fun scheduleNotification(context: Context, event: EventEntity) {
        if (!event.reminderEnabled) return

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val times = event.reminderTimes.split(",").filter { it.isNotEmpty() }

        for (timeStr in times) {
            val triggerTime = calculateTriggerTime(event.date, timeStr)
            if (triggerTime <= System.currentTimeMillis()) continue

            val intent = Intent(context, AlarmReceiver::class.java).apply {
                putExtra("event_name", event.name)
                putExtra("event_id", event.id)
            }

            val requestCode = (event.id * 10 + times.indexOf(timeStr)).toInt()
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        pendingIntent
                    )
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            }
        }
    }

    fun cancelNotification(context: Context, eventId: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        for (i in 0..3) {
            val intent = Intent(context, AlarmReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                (eventId * 10 + i).toInt(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.cancel(pendingIntent)
        }
    }

    private fun calculateTriggerTime(eventDate: Long, timeStr: String): Long {
        val cal = Calendar.getInstance()
        cal.timeInMillis = eventDate
        cal.set(Calendar.HOUR_OF_DAY, 9)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)

        return when (timeStr) {
            "day_before_1" -> {
                cal.add(Calendar.DAY_OF_YEAR, -1)
                cal.timeInMillis
            }
            "day_before_3" -> {
                cal.add(Calendar.DAY_OF_YEAR, -3)
                cal.timeInMillis
            }
            "day_at_9" -> {
                cal.set(Calendar.HOUR_OF_DAY, 9)
                cal.timeInMillis
            }
            "day_at_10" -> {
                cal.set(Calendar.HOUR_OF_DAY, 10)
                cal.timeInMillis
            }
            else -> cal.timeInMillis
        }
    }
}
