package com.nianri.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

object DateUtils {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.CHINA)
    private val displayFormat = SimpleDateFormat("yyyy年M月d日", Locale.CHINA)

    fun formatDate(timestamp: Long): String {
        return dateFormat.format(Date(timestamp))
    }

    fun formatDisplayDate(timestamp: Long): String {
        return displayFormat.format(Date(timestamp))
    }

    fun parseDate(dateStr: String): Long {
        return try {
            dateFormat.parse(dateStr)?.time ?: System.currentTimeMillis()
        } catch (e: Exception) {
            System.currentTimeMillis()
        }
    }

    fun getNextOccurrence(timestamp: Long, repeatRule: String): Long {
        val cal = Calendar.getInstance()
        cal.timeInMillis = timestamp
        val now = Calendar.getInstance()

        val targetCal = Calendar.getInstance()
        targetCal.timeInMillis = timestamp
        targetCal.set(Calendar.YEAR, now.get(Calendar.YEAR))

        if (targetCal.timeInMillis < now.timeInMillis) {
            targetCal.add(Calendar.YEAR, 1)
        }

        return when (repeatRule) {
            "yearly" -> {
                val nextCal = Calendar.getInstance()
                nextCal.timeInMillis = timestamp
                nextCal.set(Calendar.YEAR, now.get(Calendar.YEAR))
                if (nextCal.timeInMillis < now.timeInMillis) {
                    nextCal.add(Calendar.YEAR, 1)
                }
                nextCal.timeInMillis
            }
            "monthly" -> {
                val nextCal = Calendar.getInstance()
                nextCal.timeInMillis = timestamp
                nextCal.set(Calendar.YEAR, now.get(Calendar.YEAR))
                nextCal.set(Calendar.MONTH, now.get(Calendar.MONTH))
                nextCal.set(Calendar.DAY_OF_MONTH, cal.get(Calendar.DAY_OF_MONTH))
                if (nextCal.timeInMillis < now.timeInMillis) {
                    nextCal.add(Calendar.MONTH, 1)
                }
                nextCal.timeInMillis
            }
            "weekly" -> {
                val nextCal = Calendar.getInstance()
                nextCal.time = now.time
                val targetDayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
                val currentDayOfWeek = now.get(Calendar.DAY_OF_WEEK)
                var daysUntilTarget = targetDayOfWeek - currentDayOfWeek
                if (daysUntilTarget <= 0) {
                    daysUntilTarget += 7
                }
                nextCal.add(Calendar.DAY_OF_YEAR, daysUntilTarget)
                nextCal.timeInMillis
            }
            else -> timestamp
        }
    }

    fun daysBetween(startTimestamp: Long, endTimestamp: Long): Int {
        val startDay = getStartOfDay(startTimestamp)
        val endDay = getStartOfDay(endTimestamp)
        val diff = endDay - startDay
        return (diff / (1000 * 60 * 60 * 24)).toInt()
    }

    fun getStartOfDay(timestamp: Long): Long {
        val cal = Calendar.getInstance()
        cal.timeInMillis = timestamp
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    fun getTodayStart(): Long {
        return getStartOfDay(System.currentTimeMillis())
    }

    fun getYearMonthDay(timestamp: Long): Triple<Int, Int, Int> {
        val cal = Calendar.getInstance()
        cal.timeInMillis = timestamp
        return Triple(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH))
    }

    fun getDaysInMonth(year: Int, month: Int): Int {
        val cal = Calendar.getInstance()
        cal.set(year, month, 1)
        return cal.getActualMaximum(Calendar.DAY_OF_MONTH)
    }

    fun getFirstDayOfWeek(year: Int, month: Int): Int {
        val cal = Calendar.getInstance()
        cal.set(year, month, 1)
        return cal.get(Calendar.DAY_OF_WEEK) - 1
    }

    fun getTimestamp(year: Int, month: Int, day: Int): Long {
        val cal = Calendar.getInstance()
        cal.set(year, month, day, 0, 0, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    fun isSameDay(timestamp1: Long, timestamp2: Long): Boolean {
        val cal1 = Calendar.getInstance()
        cal1.timeInMillis = timestamp1
        val cal2 = Calendar.getInstance()
        cal2.timeInMillis = timestamp2
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }
}
