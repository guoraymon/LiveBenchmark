package com.kuolw.livebenchmark.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kuolw.livebenchmark.AppRepository
import com.kuolw.livebenchmark.db.entity.SourceEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SourceViewModel(private val repository: AppRepository) : ViewModel() {
    val sources = repository.allSources

    fun insert(source: SourceEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertSource(source)
        }
    }

    fun update(source: SourceEntity) {
        if (source.id == 0) {
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateSource(source)
        }
    }

    fun delete(source: SourceEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteSource(source)
        }
    }

    fun deleteAll() {
        viewModelScope.launch(Dispatchers.IO) {
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