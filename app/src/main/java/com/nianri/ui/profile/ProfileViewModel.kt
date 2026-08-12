package com.nianri.ui.profile

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.nianri.data.entity.EventEntity
import com.nianri.data.repository.EventRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProfileViewModel(private val repository: EventRepository) : ViewModel() {

    val totalDays = repository.getTotalCount()
    val totalEvents = repository.getEventCount()
    val totalAnniversaries = repository.getAnniversaryCount()
    val totalBirthdays = repository.getBirthdayCount()

    private val _isDarkMode = MutableStateFlow(false)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    fun toggleDarkMode() {
        _isDarkMode.value = !_isDarkMode.value
    }

    suspend fun exportData(context: Context, uri: Uri) {
        withContext(Dispatchers.IO) {
            try {
                val events = repository.getAllEvents().first()
                val json = Gson().toJson(events)
                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    outputStream.write(json.toByteArray())
                }
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "数据导出成功", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "导出失败：${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    suspend fun importData(context: Context, uri: Uri) {
        withContext(Dispatchers.IO) {
            try {
                val json = context.contentResolver.openInputStream(uri)?.bufferedReader()?.readText()
                    ?: return@withContext
                val type = object : TypeToken<List<EventEntity>>() {}.type
                val events: List<EventEntity> = Gson().fromJson(json, type)
                events.forEach { event ->
                    repository.insertEvent(event.copy(id = 0))
                }
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "数据导入成功，共导入 ${events.size} 条记录", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "导入失败：${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    class Factory(private val repository: EventRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ProfileViewModel(repository) as T
        }
    }
}
