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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
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
            // 设备重启后重新调度所有开启提醒的事件
            val app = context.applicationContext as? com.nianri.NianRiApp ?: return
            CoroutineScope(Dispatchers.IO).launch {
                runCatching {
                    app.repository.getAllEvents().first()
                        .filter { it.reminderEnabled && !it.completed }
                        .forEach { event ->
                            NotificationHelper.scheduleNotification(context, event)
                        }
                }
            }
        }
    }
}

object NotificationHelper {

    fun scheduleNotification(context: Context, event: EventEntity) {
        if (!event.reminderEnabled) return

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val triggerTime = calculateTriggerTime(event.date, event.reminderTimes)
        if (triggerTime <= System.currentTimeMillis()) return

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("event_name", event.name)
            putExtra("event_id", event.id)
        }

        val requestCode = (event.id * 10).toInt()
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

    fun cancelNotification(context: Context, eventId: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            (eventId * 10).toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    /**
     * 根据提醒时间格式 "mode:amount:unit" 计算触发时间戳。
     * - mode: "before"(提前) / "at"(当天)
     * - amount: 数字
     * - unit: "day"(天) / "hour"(时)
     *
     * 语义：
     * - before:N:day  -> 事件日期当天 9 点往前推 N 天
     * - before:N:hour -> 事件日期当天 9 点往前推 N 小时
     * - at:N:hour     -> 事件日期当天 N 点
     * - at:N:day      -> 事件日期当天 9 点（当天配"天"无意义，回退默认 9 点）
     */
    private fun calculateTriggerTime(eventDate: Long, timeStr: String): Long {
        val cal = Calendar.getInstance()
        cal.timeInMillis = eventDate
        cal.set(Calendar.HOUR_OF_DAY, 9)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)

        val parts = timeStr.split(":")
        if (parts.size != 3) return cal.timeInMillis

        val mode = parts[0]
        val amount = parts[1].toIntOrNull() ?: 1
        val unit = parts[2]

        return when {
            mode == "before" && unit == "day" -> {
                cal.add(Calendar.DAY_OF_YEAR, -amount)
                cal.timeInMillis
            }
            mode == "before" && unit == "hour" -> {
                cal.add(Calendar.HOUR_OF_DAY, -amount)
                cal.timeInMillis
            }
            mode == "at" && unit == "hour" -> {
                cal.set(Calendar.HOUR_OF_DAY, amount)
                cal.timeInMillis
            }
            // at + day（无意义）或未知格式，回退到当天 9 点
            else -> cal.timeInMillis
        }
    }
}
