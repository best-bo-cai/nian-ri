package com.nianri.util

data class HolidayInfo(
    val name: String,
    val month: Int,
    val day: Int,
    val isLunar: Boolean = false
)

object HolidayData {

    val solarHolidays = listOf(
        HolidayInfo("元旦", 1, 1),
        HolidayInfo("情人节", 2, 14),
        HolidayInfo("妇女节", 3, 8),
        HolidayInfo("植树节", 3, 12),
        HolidayInfo("愚人节", 4, 1),
        HolidayInfo("劳动节", 5, 1),
        HolidayInfo("青年节", 5, 4),
        HolidayInfo("母亲节", 5, 12),
        HolidayInfo("儿童节", 6, 1),
        HolidayInfo("父亲节", 6, 15),
        HolidayInfo("建党节", 7, 1),
        HolidayInfo("建军节", 8, 1),
        HolidayInfo("教师节", 9, 10),
        HolidayInfo("国庆节", 10, 1),
        HolidayInfo("万圣节", 10, 31),
        HolidayInfo("平安夜", 12, 24),
        HolidayInfo("圣诞节", 12, 25)
    )

    data class LunarHolidayDate(
        val name: String,
        val lunarMonth: Int,
        val lunarDay: Int
    )

    val lunarHolidays = listOf(
        LunarHolidayDate("春节", 1, 1),
        LunarHolidayDate("元宵节", 1, 15),
        LunarHolidayDate("龙抬头", 2, 2),
        LunarHolidayDate("端午节", 5, 5),
        LunarHolidayDate("七夕", 7, 7),
        LunarHolidayDate("中元节", 7, 15),
        LunarHolidayDate("中秋节", 8, 15),
        LunarHolidayDate("重阳节", 9, 9),
        LunarHolidayDate("除夕", 12, 30)
    )

    private val lunarDateMap: Map<Int, Map<Pair<Int, Int>, Pair<Int, Int>>> = mapOf(
        2024 to mapOf(
            Pair(1, 1) to Pair(2, 10),
            Pair(1, 15) to Pair(2, 24),
            Pair(2, 2) to Pair(3, 11),
            Pair(5, 5) to Pair(6, 10),
            Pair(7, 7) to Pair(8, 10),
            Pair(7, 15) to Pair(8, 18),
            Pair(8, 15) to Pair(9, 17),
            Pair(9, 9) to Pair(10, 11),
            Pair(12, 30) to Pair(1, 28)
        ),
        2025 to mapOf(
            Pair(1, 1) to Pair(1, 29),
            Pair(1, 15) to Pair(2, 12),
            Pair(2, 2) to Pair(3, 1),
            Pair(5, 5) to Pair(5, 31),
            Pair(7, 7) to Pair(8, 29),
            Pair(7, 15) to Pair(9, 6),
            Pair(8, 15) to Pair(10, 6),
            Pair(9, 9) to Pair(10, 29),
            Pair(12, 30) to Pair(2, 16)
        ),
        2026 to mapOf(
            Pair(1, 1) to Pair(2, 17),
            Pair(1, 15) to Pair(3, 3),
            Pair(2, 2) to Pair(3, 20),
            Pair(5, 5) to Pair(6, 19),
            Pair(7, 7) to Pair(8, 19),
            Pair(7, 15) to Pair(8, 27),
            Pair(8, 15) to Pair(9, 25),
            Pair(9, 9) to Pair(10, 18),
            Pair(12, 30) to Pair(2, 5)
        ),
        2027 to mapOf(
            Pair(1, 1) to Pair(2, 6),
            Pair(1, 15) to Pair(2, 20),
            Pair(2, 2) to Pair(3, 9),
            Pair(5, 5) to Pair(6, 9),
            Pair(7, 7) to Pair(8, 8),
            Pair(7, 15) to Pair(8, 16),
            Pair(8, 15) to Pair(9, 15),
            Pair(9, 9) to Pair(10, 8),
            Pair(12, 30) to Pair(1, 25)
        ),
        2028 to mapOf(
            Pair(1, 1) to Pair(1, 26),
            Pair(1, 15) to Pair(2, 9),
            Pair(2, 2) to Pair(2, 26),
            Pair(5, 5) to Pair(5, 28),
            Pair(7, 7) to Pair(7, 27),
            Pair(7, 15) to Pair(8, 4),
            Pair(8, 15) to Pair(10, 3),
            Pair(9, 9) to Pair(10, 26),
            Pair(12, 30) to Pair(2, 12)
        ),
        2029 to mapOf(
            Pair(1, 1) to Pair(2, 13),
            Pair(1, 15) to Pair(2, 27),
            Pair(2, 2) to Pair(3, 16),
            Pair(5, 5) to Pair(6, 16),
            Pair(7, 7) to Pair(8, 16),
            Pair(7, 15) to Pair(8, 24),
            Pair(8, 15) to Pair(9, 22),
            Pair(9, 9) to Pair(10, 16),
            Pair(12, 30) to Pair(2, 1)
        ),
        2030 to mapOf(
            Pair(1, 1) to Pair(2, 3),
            Pair(1, 15) to Pair(2, 17),
            Pair(2, 2) to Pair(3, 5),
            Pair(5, 5) to Pair(6, 5),
            Pair(7, 7) to Pair(8, 5),
            Pair(7, 15) to Pair(8, 13),
            Pair(8, 15) to Pair(9, 12),
            Pair(9, 9) to Pair(10, 5),
            Pair(12, 30) to Pair(1, 22)
        )
    )

    fun getLunarHolidaySolarDate(year: Int, lunarMonth: Int, lunarDay: Int): Pair<Int, Int>? {
        return lunarDateMap[year]?.get(Pair(lunarMonth, lunarDay))
    }

    fun getHolidaysForDate(year: Int, month: Int, day: Int): List<String> {
        val holidays = mutableListOf<String>()

        for (holiday in solarHolidays) {
            if (holiday.month == month + 1 && holiday.day == day) {
                holidays.add(holiday.name)
            }
        }

        val yearMap = lunarDateMap[year] ?: return holidays
        for ((lunarKey, solarDate) in yearMap) {
            if (solarDate.first == month + 1 && solarDate.second == day) {
                val lunarHoliday = lunarHolidays.find {
                    it.lunarMonth == lunarKey.first && it.lunarDay == lunarKey.second
                }
                lunarHoliday?.let { holidays.add(it.name) }
            }
        }

        return holidays
    }
}
