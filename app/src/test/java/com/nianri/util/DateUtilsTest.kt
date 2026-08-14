package com.nianri.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class DateUtilsTest {

    @Test
    fun `formatDate 返回 yyyy-MM-dd 格式`() {
        val timestamp = DateUtils.getTimestamp(2026, 7, 13) // 2026-08-13
        assertEquals("2026-08-13", DateUtils.formatDate(timestamp))
    }

    @Test
    fun `formatDisplayDate 返回中文日期格式`() {
        val timestamp = DateUtils.getTimestamp(2026, 7, 13) // 2026-08-13
        assertEquals("2026年8月13日", DateUtils.formatDisplayDate(timestamp))
    }

    @Test
    fun `parseDate 解析合法日期字符串`() {
        val expected = DateUtils.getTimestamp(2026, 0, 1)
        val actual = DateUtils.parseDate("2026-01-01")
        assertEquals(expected, actual)
    }

    @Test
    fun `parseDate 非法字符串返回当前时间`() {
        val before = System.currentTimeMillis()
        val result = DateUtils.parseDate("not-a-date")
        val after = System.currentTimeMillis()
        assertTrue(result in before..after)
    }

    @Test
    fun `getNextOccurrence 未指定规则时返回原时间戳`() {
        val timestamp = DateUtils.getTimestamp(2026, 0, 1)
        assertEquals(timestamp, DateUtils.getNextOccurrence(timestamp, "none"))
    }

    @Test
    fun `getNextOccurrence yearly 返回今年或明年的对应日期`() {
        val timestamp = DateUtils.getTimestamp(2020, 0, 1) // 2020-01-01
        val result = DateUtils.getNextOccurrence(timestamp, "yearly")
        val cal = Calendar.getInstance()
        cal.timeInMillis = result
        assertEquals(Calendar.JANUARY, cal.get(Calendar.MONTH))
        assertEquals(1, cal.get(Calendar.DAY_OF_MONTH))
    }

    @Test
    fun `getNextOccurrence monthly 返回本月的对应日`() {
        val timestamp = DateUtils.getTimestamp(2020, 0, 15) // 2020-01-15
        val result = DateUtils.getNextOccurrence(timestamp, "monthly")
        val cal = Calendar.getInstance()
        cal.timeInMillis = result
        assertEquals(15, cal.get(Calendar.DAY_OF_MONTH))
    }

    @Test
    fun `getNextOccurrence weekly 返回未来一周内的同星期几`() {
        val now = Calendar.getInstance()
        val target = now.clone() as Calendar
        target.add(Calendar.DAY_OF_YEAR, 1)
        val result = DateUtils.getNextOccurrence(target.timeInMillis, "weekly")
        val resultCal = Calendar.getInstance()
        resultCal.timeInMillis = result
        assertEquals(target.get(Calendar.DAY_OF_WEEK), resultCal.get(Calendar.DAY_OF_WEEK))
        assertTrue(result >= DateUtils.getStartOfDay(now.timeInMillis))
    }

    @Test
    fun `getNextOccurrence monthly 31号在30天月份钳制为30号`() {
        // 原始日期为 2020-01-31，本月假设为 4 月（30 天）
        // 由于依赖当前时间，这里验证钳制逻辑：直接用固定月份场景
        // 用 2020-01-31 作为原日期，当前月份若非 1 月，则 31 号应钳制到当月最大天数
        val timestamp = DateUtils.getTimestamp(2020, 0, 31) // 2020-01-31
        val result = DateUtils.getNextOccurrence(timestamp, "monthly")
        val resultCal = Calendar.getInstance()
        resultCal.timeInMillis = result
        val expectedDay = resultCal.getActualMaximum(Calendar.DAY_OF_MONTH)
        // 结果日不应超过当月最大天数
        assertTrue(resultCal.get(Calendar.DAY_OF_MONTH) <= expectedDay)
        // 且结果日必须是原日期与当月最大天数中的较小者
        val originalDay = 31
        assertEquals(minOf(originalDay, expectedDay), resultCal.get(Calendar.DAY_OF_MONTH))
    }

    @Test
    fun `getNextOccurrence yearly 闰年2月29日在平年钳制为2月28日`() {
        // 2024-02-29 是闰年，若当年为平年（如 2025），应钳制到 2 月 28 日而非溢出到 3 月
        val timestamp = DateUtils.getTimestamp(2024, 1, 29) // 2024-02-29
        val result = DateUtils.getNextOccurrence(timestamp, "yearly")
        val resultCal = Calendar.getInstance()
        resultCal.timeInMillis = result
        // 结果必须是 2 月
        assertEquals(Calendar.FEBRUARY, resultCal.get(Calendar.MONTH))
        // 结果日必须是 2 月的最后一天（闰年 29，平年 28）
        val expectedDay = resultCal.getActualMaximum(Calendar.DAY_OF_MONTH)
        assertEquals(expectedDay, resultCal.get(Calendar.DAY_OF_MONTH))
    }

    @Test
    fun `getNextOccurrence yearly 原日期已过则顺延到明年`() {
        // 用很久以前（2000 年）的日期，确保候选日期一定早于 now，触发顺延分支
        val timestamp = DateUtils.getTimestamp(2000, 5, 15) // 2000-06-15
        val result = DateUtils.getNextOccurrence(timestamp, "yearly")
        val now = Calendar.getInstance()
        val resultCal = Calendar.getInstance()
        resultCal.timeInMillis = result
        // 结果必须是未来的某年 6 月 15 日
        assertTrue(result >= DateUtils.getStartOfDay(now.timeInMillis))
        assertEquals(Calendar.JUNE, resultCal.get(Calendar.MONTH))
        assertEquals(15, resultCal.get(Calendar.DAY_OF_MONTH))
        // 结果年份必须大于等于今年
        assertTrue(resultCal.get(Calendar.YEAR) >= now.get(Calendar.YEAR))
    }

    @Test
    fun `getNextOccurrence yearly 候选日期在未来则返回今年`() {
        // 用「明天」的月/日作为原日期的月/日，使候选日期（今年同月同日）一定晚于 now，
        // 稳定覆盖非顺延分支（candidate.timeInMillis）
        val now = Calendar.getInstance()
        val tomorrow = now.clone() as Calendar
        tomorrow.add(Calendar.DAY_OF_YEAR, 1)
        val originalMonth = tomorrow.get(Calendar.MONTH)
        val originalDay = tomorrow.get(Calendar.DAY_OF_MONTH)
        val timestamp = DateUtils.getTimestamp(2000, originalMonth, originalDay)
        val result = DateUtils.getNextOccurrence(timestamp, "yearly")
        val resultCal = Calendar.getInstance()
        resultCal.timeInMillis = result
        assertEquals(now.get(Calendar.YEAR), resultCal.get(Calendar.YEAR))
        assertEquals(originalMonth, resultCal.get(Calendar.MONTH))
        assertEquals(originalDay, resultCal.get(Calendar.DAY_OF_MONTH))
    }

    @Test
    fun `getNextOccurrence monthly 原日期已过则顺延到下个月`() {
        // 用「昨天」的日作为原日期的日，使候选日期（今年本月的昨天）一定早于 now，
        // 稳定覆盖顺延到下一月分支（month += 1）
        val now = Calendar.getInstance()
        val yesterday = now.clone() as Calendar
        yesterday.add(Calendar.DAY_OF_YEAR, -1)
        val originalDay = yesterday.get(Calendar.DAY_OF_MONTH)
        val timestamp = DateUtils.getTimestamp(2000, 0, originalDay)
        val result = DateUtils.getNextOccurrence(timestamp, "monthly")
        val resultCal = Calendar.getInstance()
        resultCal.timeInMillis = result
        // 结果必须是未来时间
        assertTrue(result >= DateUtils.getStartOfDay(now.timeInMillis))
        // 结果日必须是原日（除非被下月最大天数钳制）
        val maxDay = resultCal.getActualMaximum(Calendar.DAY_OF_MONTH)
        assertEquals(minOf(originalDay, maxDay), resultCal.get(Calendar.DAY_OF_MONTH))
    }

    @Test
    fun `getNextOccurrence monthly 年底顺延跨年`() {
        // 12 月 31 日类型，若当前是 12 月且已过，顺延到下一年 1 月，需覆盖 month 归零、year+1 分支
        val timestamp = DateUtils.getTimestamp(2000, 11, 31) // 2000-12-31
        val result = DateUtils.getNextOccurrence(timestamp, "monthly")
        val now = Calendar.getInstance()
        val resultCal = Calendar.getInstance()
        resultCal.timeInMillis = result
        assertTrue(result >= DateUtils.getStartOfDay(now.timeInMillis))
        // 结果日必须是 31 号或被钳制为当月最大天数
        val originalDay = 31
        val maxDay = resultCal.getActualMaximum(Calendar.DAY_OF_MONTH)
        assertEquals(minOf(originalDay, maxDay), resultCal.get(Calendar.DAY_OF_MONTH))
    }

    @Test
    fun `getNextOccurrence weekly 目标星期几已过则跳到下周`() {
        // 构造原日期为「今天之前的同星期几」，确保 daysUntilTarget <= 0 分支被覆盖
        val now = Calendar.getInstance()
        // 取今天往前 3 天作为原日期（星期几不同，但会触发 daysUntilTarget 计算）
        val past = now.clone() as Calendar
        past.add(Calendar.DAY_OF_YEAR, -3)
        val result = DateUtils.getNextOccurrence(past.timeInMillis, "weekly")
        val resultCal = Calendar.getInstance()
        resultCal.timeInMillis = result
        // 结果必须是未来
        assertTrue(result >= DateUtils.getStartOfDay(now.timeInMillis))
        // 结果的星期几必须等于原日期的星期几
        assertEquals(past.get(Calendar.DAY_OF_WEEK), resultCal.get(Calendar.DAY_OF_WEEK))
    }

    @Test
    fun `daysBetween 计算两个日期的天数差`() {
        val start = DateUtils.getTimestamp(2026, 0, 1)
        val end = DateUtils.getTimestamp(2026, 0, 11)
        assertEquals(10, DateUtils.daysBetween(start, end))
    }

    @Test
    fun `daysBetween 跨月计算正确`() {
        val start = DateUtils.getTimestamp(2026, 0, 31)
        val end = DateUtils.getTimestamp(2026, 1, 1)
        assertEquals(1, DateUtils.daysBetween(start, end))
    }

    @Test
    fun `daysBetween 结束早于开始返回负数`() {
        val start = DateUtils.getTimestamp(2026, 1, 1)
        val end = DateUtils.getTimestamp(2026, 0, 1)
        assertTrue(DateUtils.daysBetween(start, end) < 0)
    }

    @Test
    fun `getStartOfDay 将时分秒毫秒归零`() {
        val cal = Calendar.getInstance()
        cal.set(2026, 0, 1, 23, 59, 59)
        cal.set(Calendar.MILLISECOND, 999)
        val result = DateUtils.getStartOfDay(cal.timeInMillis)
        val resultCal = Calendar.getInstance()
        resultCal.timeInMillis = result
        assertEquals(0, resultCal.get(Calendar.HOUR_OF_DAY))
        assertEquals(0, resultCal.get(Calendar.MINUTE))
        assertEquals(0, resultCal.get(Calendar.SECOND))
        assertEquals(0, resultCal.get(Calendar.MILLISECOND))
    }

    @Test
    fun `getTodayStart 返回今天的零点`() {
        val todayStart = DateUtils.getTodayStart()
        assertEquals(todayStart, DateUtils.getStartOfDay(System.currentTimeMillis()))
    }

    @Test
    fun `getYearMonthDay 返回年月日三元组`() {
        val timestamp = DateUtils.getTimestamp(2026, 7, 13) // 2026-08-13
        val (year, month, day) = DateUtils.getYearMonthDay(timestamp)
        assertEquals(2026, year)
        assertEquals(7, month) // Calendar.MONTH 从 0 开始
        assertEquals(13, day)
    }

    @Test
    fun `getDaysInMonth 返回当月天数`() {
        assertEquals(31, DateUtils.getDaysInMonth(2026, 0)) // 1月
        assertEquals(28, DateUtils.getDaysInMonth(2026, 1)) // 2月（非闰年）
        assertEquals(29, DateUtils.getDaysInMonth(2024, 1)) // 2月（闰年）
        assertEquals(30, DateUtils.getDaysInMonth(2026, 3)) // 4月
    }

    @Test
    fun `getFirstDayOfWeek 返回当月第一天是星期几`() {
        // 2026-08-01 是星期六，DAY_OF_WEEK=7，减 1 后为 6
        assertEquals(6, DateUtils.getFirstDayOfWeek(2026, 7))
    }

    @Test
    fun `getTimestamp 构造指定日期零点时间戳`() {
        val timestamp = DateUtils.getTimestamp(2026, 7, 13)
        val cal = Calendar.getInstance()
        cal.timeInMillis = timestamp
        assertEquals(2026, cal.get(Calendar.YEAR))
        assertEquals(7, cal.get(Calendar.MONTH))
        assertEquals(13, cal.get(Calendar.DAY_OF_MONTH))
        assertEquals(0, cal.get(Calendar.HOUR_OF_DAY))
    }

    @Test
    fun `isSameDay 同一天返回 true`() {
        val t1 = DateUtils.getTimestamp(2026, 0, 1)
        val t2 = DateUtils.getTimestamp(2026, 0, 1) + 3600_000L // 同一日 +1 小时
        assertTrue(DateUtils.isSameDay(t1, t2))
    }

    @Test
    fun `isSameDay 不同天返回 false`() {
        val t1 = DateUtils.getTimestamp(2026, 0, 1)
        val t2 = DateUtils.getTimestamp(2026, 0, 2)
        assertFalse(DateUtils.isSameDay(t1, t2))
    }
}
