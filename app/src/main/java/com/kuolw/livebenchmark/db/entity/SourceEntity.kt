package com.kuolw.livebenchmark.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sources")
data class SourceEntity(
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "src")
    val src: String,

    @ColumnInfo(name = "width")
    var width: Int = 0,

    @ColumnInfo(name = "height")
    var height: Int = 0,

    @ColumnInfo(name = "format")
    var format: String = "",

    @ColumnInfo(name = "video_decoder")
    var videoDecoder: String = "",

    @ColumnInfo(name = "audio_decoder")
    var audioDecoder: String = "",

    @ColumnInfo(name = "load_time")
    var loadTime: Long = 0, // 加载时长

    @ColumnInfo(name = "buffer_time")
    var bufferTime: Long = 0, // 缓冲时长

    @ColumnInfo(name = "play_time")
    var playTime: Long = 0, // 播放时长

    @ColumnInfo(name = "score")
    var score: Float = 0.0F, // 得分

    @ColumnInfo(name = "check")
    var check: Boolean = false, // 是否已检测
)