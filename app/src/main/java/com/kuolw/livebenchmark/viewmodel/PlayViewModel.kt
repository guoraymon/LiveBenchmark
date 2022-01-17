package com.kuolw.livebenchmark.viewmodel

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.kuolw.livebenchmark.db.entity.SourceEntity
import kotlin.math.roundToInt

class PlayViewModel(private val sourceViewModel: SourceViewModel) : ViewModel() {
    var currIndex: MutableState<Int?> = mutableStateOf(null)
    var currSource: MutableState<SourceEntity?> = mutableStateOf(null)

    var width = mutableStateOf(0)
    var height = mutableStateOf(0)
    var format = mutableStateOf("")
    var videoDecoder = mutableStateOf("")
    var audioDecoder = mutableStateOf("")

    var loadTime = mutableStateOf(0L)  //加载时长
    var bufferTime = mutableStateOf(0L)  //缓冲时长
    var currBufferTime = mutableStateOf(0L)  //当前缓冲时长
    var playTime = mutableStateOf(0L)  //播放时长

    fun reset() {
        width.value = 0
        height.value = 0
        format.value = ""
        videoDecoder.value = ""
        audioDecoder.value = ""
        loadTime.value = 0L
        bufferTime.value = 0L
        currBufferTime.value = 0L
        playTime.value = 0L
    }

    fun getCurrSource(): SourceEntity? {
        return currSource.value
    }

    fun getSumBufferTime(): Long {
        return bufferTime.value + currBufferTime.value
    }

    fun getScore(): Float {
        val loadScore = (5000 - loadTime.value) / 5000F
        val playScore = (playTime.value - (bufferTime.value + currBufferTime.value)) / playTime.value.toFloat()
        return ((loadScore * 300F).roundToInt() + (playScore * 700F).roundToInt()) / 10.0F
    }
}

class PlayViewModelFactory(private val sourceViewModel: SourceViewModel) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PlayViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PlayViewModel(sourceViewModel) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}