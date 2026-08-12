package com.nianri.ui.days

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.nianri.data.entity.EventEntity
import com.nianri.data.repository.EventRepository
import com.nianri.util.DateUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class DayItem(
    val event: EventEntity,
    val daysRemaining: Int,
    val nextDate: Long
)

class DaysViewModel(private val repository: EventRepository) : ViewModel() {

    private val _days = MutableStateFlow<List<DayItem>>(emptyList())
    val days: StateFlow<List<DayItem>> = _days.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadDays()
        ensureDefaultEvents()
    }

    private fun ensureDefaultEvents() {
        viewModelScope.launch {
            val defaults = repository.getDays().first().filter { it.isDefault }
            if (defaults.isEmpty()) {
                repository.insertDefaultEvents()
                loadDays()
            }
        }
    }

    fun loadDays() {
        viewModelScope.launch {
            _isLoading.value = true
            repository.getDays().collect { events ->
                val now = DateUtils.getTodayStart()
                val dayItems = events
                    .filter { it.type != "event" }
                    .map { event ->
                        val nextDate = DateUtils.getNextOccurrence(event.date, event.repeatRule)
                        val daysRemaining = DateUtils.daysBetween(now, nextDate)
                        DayItem(
                            event = event.copy(date = nextDate),
                            daysRemaining = daysRemaining,
                            nextDate = nextDate
                        )
                    }
                    .sortedBy { it.daysRemaining }
                _days.value = dayItems
                _isLoading.value = false
            }
        }
    }

    fun deleteEvent(eventId: Long) {
        viewModelScope.launch {
            repository.deleteEventById(eventId)
        }
    }

    class Factory(private val repository: EventRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return DaysViewModel(repository) as T
        }
    }
}
