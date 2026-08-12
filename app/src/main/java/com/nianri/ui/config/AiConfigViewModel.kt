package com.nianri.ui.config

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.nianri.data.entity.AiConfigEntity
import com.nianri.data.repository.EventRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AiConfigViewModel(private val repository: EventRepository) : ViewModel() {

    private val _configs = MutableStateFlow<List<AiConfigEntity>>(emptyList())
    val configs: StateFlow<List<AiConfigEntity>> = _configs.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getAiConfigs().collect { list ->
                _configs.value = list
            }
        }
    }

    fun save(config: AiConfigEntity) {
        viewModelScope.launch {
            repository.saveAiConfig(config)
        }
    }

    fun update(config: AiConfigEntity) {
        viewModelScope.launch {
            repository.updateAiConfig(config)
        }
    }

    fun delete(id: Int) {
        viewModelScope.launch {
            repository.deleteAiConfig(id)
        }
    }

    fun setActive(id: Int) {
        viewModelScope.launch {
            repository.setActiveAiConfig(id)
        }
    }

    class Factory(private val repository: EventRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AiConfigViewModel(repository) as T
        }
    }
}
