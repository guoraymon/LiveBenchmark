package com.kuolw.livebenchmark.db.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.kuolw.livebenchmark.db.entity.SourceEntity

@Dao
interface SourceDao {
    @Query("SELECT * FROM sources")
    suspend fun getAll(): List<SourceEntity>

    @Insert
    suspend fun insert(vararg source: SourceEntity)

    @Update
    suspend fun update(vararg source: SourceEntity)

    @Delete
    suspend fun delete(vararg source: SourceEntity)

    @Query("DELETE FROM sources")
    suspend fun deleteAll()
}