package com.kuolw.livebenchmark

import android.app.Application
import com.kuolw.livebenchmark.db.AppDatabase

class MainApplication : Application() {
    val database by lazy { AppDatabase.getDatabase(this) }
    val repository by lazy { AppRepository(database) }
}