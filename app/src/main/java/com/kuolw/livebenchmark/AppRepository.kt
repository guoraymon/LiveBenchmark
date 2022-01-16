package com.kuolw.livebenchmark

import android.content.ContentValues
import android.util.Log
import com.kuolw.livebenchmark.db.AppDatabase
import com.kuolw.livebenchmark.db.entity.SourceEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AppRepository(private val database: AppDatabase) {
    suspend fun getAllSources() = database.sourceDao().getAll()

    suspend fun insertSource(source: SourceEntity) {
        return withContext(Dispatchers.IO) {
            database.sourceDao().insert(source)
        }
    }

    suspend fun updateSource(source: SourceEntity) {
        return withContext(Dispatchers.IO) {
            database.sourceDao().update(source)
        }
    }

    suspend fun deleteSource(source: SourceEntity) {
        return withContext(Dispatchers.IO) {
            database.sourceDao().delete(source)
        }
    }

    suspend fun deleteAllSource() {
        return withContext(Dispatchers.IO) {
            database.sourceDao().deleteAll()
        }
    }
}