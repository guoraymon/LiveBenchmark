package com.kuolw.livebenchmark

import com.kuolw.livebenchmark.db.AppDatabase
import com.kuolw.livebenchmark.db.entity.SourceEntity

class AppRepository(private val database: AppDatabase) {
    val allSources = database.sourceDao().getAll()

    fun insertSource(source: SourceEntity) {
        database.sourceDao().insert(source)
    }

    fun updateSource(source: SourceEntity) {
        database.sourceDao().update(source)
    }

    fun deleteSource(source: SourceEntity) {
        database.sourceDao().delete(source)
    }

    fun deleteAllSource() {
        database.sourceDao().deleteAll()
    }
}