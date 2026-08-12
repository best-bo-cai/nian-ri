package com.nianri.ui.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nianri.NianRiApp
import com.nianri.data.entity.EventEntity
import com.nianri.ui.theme.AnniversaryColor
import com.nianri.ui.theme.BirthdayColor
import com.nianri.ui.theme.EventColor
import com.nianri.ui.theme.HolidayColor
import com.nianri.util.DateUtils
import com.nianri.util.HolidayData
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (Long) -> Unit
) {
    val context = LocalContext.current
    val repository = (context.applicationContext as NianRiApp).repository
    val allEvents by repository.getAllEvents().collectAsState(initial = emptyList())

    val cal = Calendar.getInstance()
    var currentYear by remember { mutableStateOf(cal.get(Calendar.YEAR)) }
    var currentMonth by remember { mutableStateOf(cal.get(Calendar.MONTH)) }
    var selectedDay by remember { mutableStateOf<Int?>(null) }

    val daysInMonth = DateUtils.getDaysInMonth(currentYear, currentMonth)
    val firstDayOfWeek = DateUtils.getFirstDayOfWeek(currentYear, currentMonth)

    val today = Calendar.getInstance()
    val todayYear = today.get(Calendar.YEAR)
    val todayMonth = today.get(Calendar.MONTH)
    val todayDay = today.get(Calendar.DAY_OF_MONTH)

    val eventsOnSelectedDay = selectedDay?.let { day ->
        val selectedTimestamp = DateUtils.getTimestamp(currentYear, currentMonth, day)
        allEvents.filter { event ->
            val nextDate = DateUtils.getNextOccurrence(event.date, event.repeatRule)
            DateUtils.isSameDay(nextDate, selectedTimestamp)
        }
    } ?: emptyList()

    val holidaysOnSelectedDay = selectedDay?.let { day ->
        HolidayData.getHolidaysForDate(currentYear, currentMonth, day)
    } ?: emptyList()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("日历", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = {
                        if (currentMonth == 0) {
                            currentMonth = 11
                            currentYear--
                        } else {
                            currentMonth--
                        }
                        selectedDay = null
                    }) {
                        Icon(
                            Icons.Filled.ChevronLeft,
                            contentDescription = "上月",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }

                    Text(
                        text = "${currentYear}年 ${currentMonth + 1}月",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    IconButton(onClick = {
                        if (currentMonth == 11) {
                            currentMonth = 0
                            currentYear++
                        } else {
                            currentMonth++
                        }
                        selectedDay = null
                    }) {
                        Icon(
                            Icons.Filled.ChevronRight,
                            contentDescription = "下月",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
            }

            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            listOf("日", "一", "二", "三", "四", "五", "六").forEachIndexed { index, dayName ->
                                Text(
                                    text = dayName,
                                    modifier = Modifier.weight(1f),
                                    textAlign = TextAlign.Center,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (index == 0 || index == 6)
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                                    else
                                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        val totalCells = firstDayOfWeek + daysInMonth
                        val rows = (totalCells + 6) / 7

                        for (row in 0 until rows) {
                            Row(modifier = Modifier.fillMaxWidth()) {
                                for (col in 0..6) {
                                    val cellIndex = row * 7 + col
                                    val day = cellIndex - firstDayOfWeek + 1

                                    if (day in 1..daysInMonth) {
                                        val isToday = currentYear == todayYear &&
                                                currentMonth == todayMonth &&
                                                day == todayDay
                                        val isSelected = selectedDay == day

                                        val dayTimestamp = DateUtils.getTimestamp(currentYear, currentMonth, day)
                                        val dayEvents = allEvents.filter { event ->
                                            val nextDate = DateUtils.getNextOccurrence(event.date, event.repeatRule)
                                            DateUtils.isSameDay(nextDate, dayTimestamp)
                                        }

                                        val holidays = HolidayData.getHolidaysForDate(currentYear, currentMonth, day)

                                        val hasBirthday = dayEvents.any { it.type == "birthday" }
                                        val hasAnniversary = dayEvents.any { it.type == "anniversary" }
                                        val hasHoliday = dayEvents.any { it.type == "holiday" } || holidays.isNotEmpty()
                                        val hasEvent = dayEvents.any { it.type == "event" }

                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .padding(1.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(
                                                    when {
                                                        isSelected -> MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                                        isToday -> MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                                                        else -> MaterialTheme.colorScheme.surface.copy(alpha = 0.3f)
                                                    }
                                                )
                                                .then(
                                                    if (isToday) Modifier.border(
                                                        2.dp,
                                                        MaterialTheme.colorScheme.primary,
                                                        RoundedCornerShape(8.dp)
                                                    ) else Modifier
                                                )
                                                .clickable { selectedDay = day },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Column(
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                modifier = Modifier.padding(vertical = 4.dp)
                                            ) {
                                                Text(
                                                    text = "$day",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                                                    color = when {
                                                        isToday -> MaterialTheme.colorScheme.primary
                                                        isSelected -> MaterialTheme.colorScheme.primary
                                                        col == 0 || col == 6 -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                                        else -> MaterialTheme.colorScheme.onSurface
                                                    },
                                                    fontSize = 13.sp
                                                )

                                                if (holidays.isNotEmpty()) {
                                                    Text(
                                                        text = holidays.first(),
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = HolidayColor,
                                                        fontSize = 9.sp,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis,
                                                        textAlign = TextAlign.Center
                                                    )
                                                } else if (dayEvents.isNotEmpty()) {
                                                    Text(
                                                        text = dayEvents.first().name,
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = when {
                                                            hasBirthday -> BirthdayColor
                                                            hasAnniversary -> AnniversaryColor
                                                            hasHoliday -> HolidayColor
                                                            else -> EventColor
                                                        },
                                                        fontSize = 9.sp,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis,
                                                        textAlign = TextAlign.Center
                                                    )
                                                }

                                                if (dayEvents.size > 1 || (holidays.isNotEmpty() && dayEvents.isNotEmpty())) {
                                                    Row(
                                                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                                                        modifier = Modifier.padding(top = 1.dp)
                                                    ) {
                                                        if (hasBirthday) {
                                                            Icon(
                                                                Icons.Filled.Circle,
                                                                contentDescription = null,
                                                                tint = BirthdayColor,
                                                                modifier = Modifier.size(4.dp)
                                                            )
                                                        }
                                                        if (hasAnniversary) {
                                                            Icon(
                                                                Icons.Filled.Circle,
                                                                contentDescription = null,
                                                                tint = AnniversaryColor,
                                                                modifier = Modifier.size(4.dp)
                                                            )
                                                        }
                                                        if (hasHoliday) {
                                                            Icon(
                                                                Icons.Filled.Circle,
                                                                contentDescription = null,
                                                                tint = HolidayColor,
                                                                modifier = Modifier.size(4.dp)
                                                            )
                                                        }
                                                        if (hasEvent) {
                                                            Icon(
                                                                Icons.Filled.Circle,
                                                                contentDescription = null,
                                                                tint = EventColor,
                                                                modifier = Modifier.size(4.dp)
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    } else {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }

            if (selectedDay != null) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${currentMonth + 1}月${selectedDay}日",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Text(
                            text = "${eventsOnSelectedDay.size + holidaysOnSelectedDay.size} 项",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                holidaysOnSelectedDay.forEach { holidayName ->
                    item {
                        HolidayInfoCard(name = holidayName)
                    }
                }

                items(eventsOnSelectedDay) { event ->
                    CalendarEventCard(
                        event = event,
                        onClick = { onNavigateToEdit(event.id) }
                    )
                }

                if (eventsOnSelectedDay.isEmpty() && holidaysOnSelectedDay.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "这天还没有日子或事件",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                            )
                        }
                    }
                }
            } else {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "点击日期查看详情",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                LegendDot(color = BirthdayColor, label = "生日")
                                LegendDot(color = AnniversaryColor, label = "纪念日")
                                LegendDot(color = HolidayColor, label = "节日")
                                LegendDot(color = EventColor, label = "事件")
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(32.dp)) }
        }
    }
}

@Composable
fun LegendDot(color: androidx.compose.ui.graphics.Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )
    }
}

@Composable
fun HolidayInfoCard(name: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = HolidayColor.copy(alpha = 0.1f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(HolidayColor)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = HolidayColor
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "传统节日",
                style = MaterialTheme.typography.labelSmall,
                color = HolidayColor.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
fun CalendarEventCard(
    event: EventEntity,
    onClick: () -> Unit
) {
    val typeColor = when (event.type) {
        "birthday" -> BirthdayColor
        "anniversary" -> AnniversaryColor
        "holiday" -> HolidayColor
        else -> EventColor
    }

    val typeLabel = when (event.type) {
        "birthday" -> "生日"
        "anniversary" -> "纪念日"
        "holiday" -> "节假日"
        else -> "事件"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(typeColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = typeLabel.take(1),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = typeColor
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = event.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = typeLabel,
                        style = MaterialTheme.typography.bodySmall,
                        color = typeColor
                    )
                    if (event.notes.isNotEmpty()) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = event.notes,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}
