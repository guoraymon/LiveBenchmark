package com.kuolw.livebenchmark

import com.kuolw.livebenchmark.db.AppDatabase

class AppRepository(database: AppDatabase) {
    val allSources = database.sourceDao().getAll()
}