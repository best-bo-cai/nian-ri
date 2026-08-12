package com.nianri.ui.events

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.nianri.data.entity.EventEntity
import com.nianri.data.repository.EventRepository
import com.nianri.util.DateUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class EventStatus {
    NOT_STARTED,
    IN_PROGRESS,
    ENDED
}

data class EventItem(
    val event: EventEntity,
    val status: EventStatus,
    val daysInfo: String
)

class EventsViewModel(private val repository: EventRepository) : ViewModel() {

    private val _events = MutableStateFlow<List<EventItem>>(emptyList())
    val events: StateFlow<List<EventItem>> = _events.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadEvents()
    }

    fun loadEvents() {
        viewModelScope.launch {
            _isLoading.value = true
            repository.getEvents().collect { eventList ->
                val now = DateUtils.getTodayStart()
                val eventItems = eventList.map { event ->
                    val startDate = DateUtils.getStartOfDay(event.date)
                    val endDate = event.endDate?.let { DateUtils.getStartOfDay(it) }

                    val (status, daysInfo) = when {
                        endDate != null && now > endDate -> {
                            val daysSinceEnd = DateUtils.daysBetween(endDate, now)
                            EventStatus.ENDED to "已结束 $daysSinceEnd 天"
                        }
                        now < startDate -> {
                            val daysUntilStart = DateUtils.daysBetween(now, startDate)
                            EventStatus.NOT_STARTED to "距开始还有 $daysUntilStart 天"
                        }
                        else -> {
                            val daysInProgress = DateUtils.daysBetween(startDate, now) + 1
                            EventStatus.IN_PROGRESS to "进行中，第 $daysInProgress 天"
                        }
                    }

                    EventItem(event = event, status = status, daysInfo = daysInfo)
                }.sortedWith(compareBy<EventItem> { it.status.ordinal }.thenBy {
                    when (it.status) {
                        EventStatus.NOT_STARTED -> it.event.date
                        EventStatus.IN_PROGRESS -> -(DateUtils.daysBetween(
                            DateUtils.getStartOfDay(it.event.date),
                            DateUtils.getTodayStart()
                        ))
                        EventStatus.ENDED -> it.event.date
                    }
                })

                _events.value = eventItems
                _isLoading.value = false
            }
        }
    }

    fun toggleCompleted(eventId: Long, completed: Boolean) {
        viewModelScope.launch {
            val event = repository.getEventById(eventId) ?: return@launch
            repository.updateEvent(event.copy(completed = completed))
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
            return EventsViewModel(repository) as T
        }
    }
}
