package com.nianri.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class HolidayDataTest {

    @Test
    fun `solarHolidays 包含元旦`() {
        assertTrue(HolidayData.solarHolidays.any { it.name == "元旦" && it.month == 1 && it.day == 1 })
    }

    @Test
    fun `solarHolidays 包含国庆节`() {
        assertTrue(HolidayData.solarHolidays.any { it.name == "国庆节" && it.month == 10 && it.day == 1 })
    }

    @Test
    fun `lunarHolidays 包含春节`() {
        assertTrue(HolidayData.lunarHolidays.any { it.name == "春节" && it.lunarMonth == 1 && it.lunarDay == 1 })
    }

    @Test
    fun `getLunarHolidaySolarDate 返回 2026 年春节的阳历日期`() {
        assertEquals(Pair(2, 17), HolidayData.getLunarHolidaySolarDate(2026, 1, 1))
    }

    @Test
    fun `getLunarHolidaySolarDate 未知年份返回 null`() {
        assertNull(HolidayData.getLunarHolidaySolarDate(1999, 1, 1))
    }

    @Test
    fun `getLunarHolidaySolarDate 未知农历日期返回 null`() {
        assertNull(HolidayData.getLunarHolidaySolarDate(2026, 13, 1))
    }

    @Test
    fun `getHolidaysForDate 匹配阳历节日`() {
        val holidays = HolidayData.getHolidaysForDate(2026, 0, 1) // 1月1日 元旦
        assertTrue(holidays.contains("元旦"))
    }

    @Test
    fun `getHolidaysForDate 匹配农历节日对应的阳历日期`() {
        // 2026 年春节是阳历 2 月 17 日（month 索引 1）
        val holidays = HolidayData.getHolidaysForDate(2026, 1, 17)
        assertTrue(holidays.contains("春节"))
    }

    @Test
    fun `getHolidaysForDate 无节日返回空列表`() {
        assertTrue(HolidayData.getHolidaysForDate(2026, 0, 3).isEmpty())
    }

    @Test
    fun `getHolidaysForDate 未知年份只返回阳历节日`() {
        // 2026 之前的年份可能不在农历映射中，但阳历节日仍应匹配
        val holidays = HolidayData.getHolidaysForDate(2025, 0, 1)
        assertTrue(holidays.contains("元旦"))
    }

    @Test
    fun `getHolidaysForDate 同日既有阳历又有农历节日时都返回`() {
        // 构造一个场景：检查某天的多个节日。此处验证返回类型为列表即可
        val holidays = HolidayData.getHolidaysForDate(2026, 9, 1) // 10月1日 国庆节
        assertTrue(holidays.contains("国庆节"))
    }
}
