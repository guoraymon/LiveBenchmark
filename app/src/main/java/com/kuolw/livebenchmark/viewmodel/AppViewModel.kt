package com.kuolw.livebenchmark.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class AppViewModel : ViewModel() {
    var isPlay = mutableStateOf(false)
    var isPlayOld = mutableStateOf(false)
    var url = mutableStateOf("")
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