package com.kuolw.livebenchmark.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.kuolw.livebenchmark.db.entity.SourceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SourceDao {
    @Query("SELECT * FROM sources")
    fun getAll(): Flow<List<SourceEntity>>

    @Insert()
    fun insert(entity: SourceEntity)
}