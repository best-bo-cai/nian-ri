package com.nianri.ui.edit

import android.app.DatePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.nianri.NianRiApp
import com.nianri.data.entity.EventEntity
import com.nianri.util.DateUtils
import com.nianri.util.NotificationHelper
import kotlinx.coroutines.launch
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditEventScreen(
    eventId: Long?,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val repository = (context.applicationContext as NianRiApp).repository
    val scope = rememberCoroutineScope()

    var name by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("birthday") }
    var calendarType by remember { mutableStateOf("solar") }
    var date by remember { mutableStateOf(System.currentTimeMillis()) }
    var endDate by remember { mutableStateOf<Long?>(null) }
    var displayMode by remember { mutableStateOf("countdown") }
    var repeatRule by remember { mutableStateOf("none") }
    var reminderEnabled by remember { mutableStateOf(false) }
    var reminderMethods by remember { mutableStateOf("local") }
    // 提醒时间点：三级下拉框（提前/当天 + 数字 + 天/时）
    var reminderMode by remember { mutableStateOf("before") }
    var reminderAmount by remember { mutableStateOf("1") }
    var reminderUnit by remember { mutableStateOf("day") }
    var notes by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var isNewEvent by remember { mutableStateOf(true) }

    LaunchedEffect(eventId) {
        eventId?.let { id ->
            val event = repository.getEventById(id)
            if (event != null) {
                name = event.name
                type = event.type
                calendarType = event.calendarType
                date = event.date
                endDate = event.endDate
                displayMode = event.displayMode
                repeatRule = event.repeatRule
                reminderEnabled = event.reminderEnabled
                reminderMethods = event.reminderMethods
                // 从 "mode:amount:unit" 格式解析回三个状态
                parseReminderTime(event.reminderTimes).let { (mode, amount, unit) ->
                    reminderMode = mode
                    reminderAmount = amount
                    reminderUnit = unit
                }
                notes = event.notes
                email = event.email
                isNewEvent = false
            }
        }
    }

    val isDayType = type != "event"

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (isNewEvent) "创建" else "编辑",
                        fontWeight = FontWeight.Bold
                    )
                },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("事件名称") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                ),
                singleLine = true
            )

            DropdownSelector(
                label = "类型",
                value = type,
                options = listOf("birthday" to "生日", "anniversary" to "纪念日", "holiday" to "节假日", "event" to "普通事件"),
                onValueChange = { type = it }
            )

            DropdownSelector(
                label = "日历类型",
                value = calendarType,
                options = listOf("solar" to "阳历", "lunar" to "农历"),
                onValueChange = { calendarType = it }
            )

            DatePickerField(
                label = if (isDayType) "日期" else "开始日期",
                timestamp = date,
                onDateChange = { date = it }
            )

            if (!isDayType) {
                DatePickerField(
                    label = "结束日期",
                    timestamp = endDate,
                    onDateChange = { endDate = it }
                )
            }

            DropdownSelector(
                label = "展示方式",
                value = displayMode,
                options = listOf("countdown" to "倒计时", "countup" to "正计时"),
                onValueChange = { displayMode = it }
            )

            DropdownSelector(
                label = "循环规则",
                value = repeatRule,
                options = listOf("none" to "不循环", "yearly" to "每年", "monthly" to "每月", "weekly" to "每周"),
                onValueChange = { repeatRule = it }
            )

            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "开启提醒",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Checkbox(
                            checked = reminderEnabled,
                            onCheckedChange = { reminderEnabled = it },
                            colors = CheckboxDefaults.colors(
                                checkedColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    }

                    if (reminderEnabled) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "提醒方式",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = reminderMethods.contains("local"),
                                onCheckedChange = { checked ->
                                    reminderMethods = if (checked) "local" else ""
                                }
                            )
                            Text("本地通知", style = MaterialTheme.typography.bodyMedium)
                            Spacer(modifier = Modifier.width(16.dp))
                            Checkbox(
                                checked = reminderMethods.contains("email"),
                                onCheckedChange = { checked ->
                                    reminderMethods = if (checked) "email" else "local"
                                }
                            )
                            Text("邮件通知", style = MaterialTheme.typography.bodyMedium)
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "提醒时间点",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )

                        // 三级下拉框：提前/当天 + 数字 + 天/时
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // 第一级：提前 / 当天
                            DropdownSelector(
                                label = "方式",
                                value = reminderMode,
                                options = listOf("before" to "提前", "at" to "当天"),
                                onValueChange = { reminderMode = it },
                                modifier = Modifier.weight(1f)
                            )

                            // 第二级：数字（填写）
                            OutlinedTextField(
                                value = reminderAmount,
                                onValueChange = { input ->
                                    // 仅允许数字
                                    reminderAmount = input.filter { it.isDigit() }
                                },
                                label = { Text("数值") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                                )
                            )

                            // 第三级：天 / 时
                            DropdownSelector(
                                label = "单位",
                                value = reminderUnit,
                                options = listOf("day" to "天", "hour" to "时"),
                                onValueChange = { reminderUnit = it },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        if (reminderMethods.contains("email")) {
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = email,
                                onValueChange = { email = it },
                                label = { Text("邮箱") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                singleLine = true
                            )
                        }
                    }
                }
            }

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("备注信息") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                ),
                maxLines = 3
            )

            Button(
                onClick = {
                    scope.launch {
                        val event = EventEntity(
                            id = eventId ?: 0,
                            type = type,
                            name = name,
                            calendarType = calendarType,
                            date = date,
                            endDate = endDate,
                            displayMode = displayMode,
                            repeatRule = repeatRule,
                            reminderEnabled = reminderEnabled,
                            reminderMethods = reminderMethods,
                            reminderTimes = "$reminderMode:$reminderAmount:$reminderUnit",
                            email = email,
                            notes = notes
                        )
                        val savedId = if (isNewEvent) {
                            repository.insertEvent(event)
                        } else {
                            repository.updateEvent(event)
                            eventId
                        }

                        // 保存后根据提醒设置调度或取消通知
                        val savedEvent = event.copy(id = savedId ?: event.id)
                        if (savedEvent.reminderEnabled) {
                            NotificationHelper.scheduleNotification(context, savedEvent)
                        } else {
                            NotificationHelper.cancelNotification(context, savedEvent.id)
                        }
                        onNavigateBack()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                enabled = name.isNotBlank()
            ) {
                Text(
                    if (isNewEvent) "创建" else "保存",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropdownSelector(
    label: String,
    value: String,
    options: List<Pair<String, String>>,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = options.find { it.first == value }?.second ?: value,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
            )
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { (key, displayName) ->
                DropdownMenuItem(
                    text = { Text(displayName) },
                    onClick = {
                        onValueChange(key)
                        expanded = false
                    }
                )
            }
        }
    }
}

/**
 * 解析提醒时间格式 "mode:amount:unit"，返回三元组 (mode, amount, unit)。
 * 无法解析时回退到默认值 ("before", "1", "day")。
 */
private fun parseReminderTime(reminderTime: String): Triple<String, String, String> {
    val parts = reminderTime.split(":")
    if (parts.size == 3) {
        val mode = if (parts[0] == "at") "at" else "before"
        val amount = parts[1].ifBlank { "1" }
        val unit = if (parts[2] == "hour") "hour" else "day"
        return Triple(mode, amount, unit)
    }
    return Triple("before", "1", "day")
}

@Composable
fun DatePickerField(
    label: String,
    timestamp: Long?,
    onDateChange: (Long) -> Unit
) {
    val context = LocalContext.current
    val cal = Calendar.getInstance()

    if (timestamp != null) {
        cal.timeInMillis = timestamp
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                DatePickerDialog(
                    context,
                    { _, year, month, dayOfMonth ->
                        cal.set(year, month, dayOfMonth)
                        onDateChange(cal.timeInMillis)
                    },
                    cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH),
                    cal.get(Calendar.DAY_OF_MONTH)
                ).show()
            },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Filled.DateRange,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    label,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Text(
                    if (timestamp != null) DateUtils.formatDisplayDate(timestamp) else "未设置",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}
