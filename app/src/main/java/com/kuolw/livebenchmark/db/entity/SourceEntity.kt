package com.kuolw.livebenchmark.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sources")
data class SourceEntity(
    @PrimaryKey(autoGenerate = true) var id: Int,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "src") val src: String,
    @ColumnInfo(name = "fail") val fail: Int = 0, // 播放失败次数
    @ColumnInfo(name = "score") var score: Int = 0, // 得分
)