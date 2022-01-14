package com.kuolw.livebenchmark.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sources")
data class SourceEntity(
    @PrimaryKey(autoGenerate = true) var id: Int,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "src") val src: String,
    @ColumnInfo(name = "load_time") var loadTime: Long = 0, // 加载时长
    @ColumnInfo(name = "buffer_time") var bufferTime: Long = 0, // 缓冲时长
    @ColumnInfo(name = "play_time") var playTime: Long = 0, // 播放时长
    @ColumnInfo(name = "score") var score: Float = 0.0F, // 得分
    @ColumnInfo(name = "over") var over: Boolean = false, // 是否测试完成
)