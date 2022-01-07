package com.kuolw.livebenchmark.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.kuolw.livebenchmark.AppRepository

class SourceViewModel(repository: AppRepository) : ViewModel() {
    val sources = repository.allSources
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