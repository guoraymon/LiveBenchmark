package com.kuolw.livebenchmark.viewmodel

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.kuolw.livebenchmark.db.entity.SourceEntity

class AppViewModel : ViewModel() {
    var currSource: MutableState<SourceEntity?> = mutableStateOf(null)
}

class AppViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AppViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AppViewModel() as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}