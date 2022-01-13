package com.kuolw.livebenchmark.db.dao

import androidx.room.*
import com.kuolw.livebenchmark.db.entity.SourceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SourceDao {
    @Query("SELECT * FROM sources")
    fun getAll(): Flow<List<SourceEntity>>

    @Insert
    fun insert(vararg source: SourceEntity)

    @Update
    fun update(vararg source: SourceEntity)

    @Delete
    fun delete(vararg source: SourceEntity)

    @Query("DELETE FROM sources")
    fun deleteAll()
}