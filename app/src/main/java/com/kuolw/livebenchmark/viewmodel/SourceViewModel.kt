package com.kuolw.livebenchmark.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kuolw.livebenchmark.AppRepository
import com.kuolw.livebenchmark.db.entity.SourceEntity
import kotlinx.coroutines.launch

class SourceViewModel(private val repository: AppRepository) : ViewModel() {
    var sources = mutableStateListOf<SourceEntity>()

    init {
        viewModelScope.launch {
            load()
        }
    }

    private fun load() {
        viewModelScope.launch {
            sources.clear()
            sources.addAll(repository.getAllSources())
        }
    }

    fun insert(source: SourceEntity) {
        viewModelScope.launch {
            repository.insertSource(source)
            sources.add(source)
        }
    }

    fun update(source: SourceEntity) {
        viewModelScope.launch {
            repository.updateSource(source)
            sources.indexOf(source).let {
                sources[it] = source
            }
        }
    }

    fun delete(source: SourceEntity) {
        viewModelScope.launch {
            repository.deleteSource(source)
            sources.remove(source)
        }
    }

    fun deleteAll() {
        viewModelScope.launch {
            repository.deleteAllSource()
            sources.removeAll(sources)
        }
    }
}

class SourceViewModelFactory(private val repository: AppRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SourceViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SourceViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}