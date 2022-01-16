package com.kuolw.livebenchmark.viewmodel

import android.content.ContentValues.TAG
import android.util.Log
import androidx.compose.runtime.*
import androidx.lifecycle.*
import com.kuolw.livebenchmark.AppRepository
import com.kuolw.livebenchmark.db.entity.SourceEntity
import kotlinx.coroutines.launch

class SourceViewModel(private val repository: AppRepository) : ViewModel() {
    var sources by mutableStateOf(listOf<SourceEntity>())
        private set

    init {
        viewModelScope.launch {
            loadSources()
        }
    }

    private fun loadSources() {
        viewModelScope.launch {
            sources = repository.getAllSources()
        }
    }

    fun insert(source: SourceEntity) {
        viewModelScope.launch {
            repository.insertSource(source)
            loadSources()
        }
    }

    fun update(source: SourceEntity) {
        if (source.id == 0) {
            loadSources()
            return
        }
        viewModelScope.launch {
            repository.updateSource(source)
            val oldSources = sources
            sources = listOf()
            sources = oldSources.map { item ->
                if (item.id == source.id)
                    item.copy(score = source.score)
                else
                    item
            }
            Log.d(TAG, "test: $sources")
        }
    }

    fun delete(source: SourceEntity) {
        viewModelScope.launch {
            repository.deleteSource(source)
        }
    }

    fun deleteAll() {
        viewModelScope.launch {
            repository.deleteAllSource()
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