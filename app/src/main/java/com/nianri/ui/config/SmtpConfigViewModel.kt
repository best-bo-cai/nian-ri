package com.nianri.ui.config

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.nianri.data.entity.SmtpConfigEntity
import com.nianri.data.repository.EventRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SmtpConfigViewModel(private val repository: EventRepository) : ViewModel() {

    private val _configs = MutableStateFlow<List<SmtpConfigEntity>>(emptyList())
    val configs: StateFlow<List<SmtpConfigEntity>> = _configs.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getSmtpConfigs().collect { list ->
                _configs.value = list
            }
        }
    }

    fun save(config: SmtpConfigEntity) {
        viewModelScope.launch {
            repository.saveSmtpConfig(config)
        }
    }

    fun update(config: SmtpConfigEntity) {
        viewModelScope.launch {
            repository.updateSmtpConfig(config)
        }
    }

    fun delete(id: Int) {
        viewModelScope.launch {
            repository.deleteSmtpConfig(id)
        }
    }

    fun setActive(id: Int) {
        viewModelScope.launch {
            repository.setActiveSmtpConfig(id)
        }
    }

    class Factory(private val repository: EventRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SmtpConfigViewModel(repository) as T
        }
    }
}
