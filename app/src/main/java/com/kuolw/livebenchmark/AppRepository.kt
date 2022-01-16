package com.kuolw.livebenchmark

import com.kuolw.livebenchmark.db.AppDatabase
import com.kuolw.livebenchmark.db.entity.SourceEntity

class AppRepository(private val database: AppDatabase) {
    suspend fun getAllSources() = database.sourceDao().getAll()
    suspend fun insertSource(source: SourceEntity) = database.sourceDao().insert(source)
    suspend fun updateSource(source: SourceEntity) = database.sourceDao().update(source)
    suspend fun deleteSource(source: SourceEntity) = database.sourceDao().delete(source)
    suspend fun deleteAllSource() = database.sourceDao().deleteAll()
}