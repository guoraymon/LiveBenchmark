package com.kuolw.livebenchmark

import android.app.Application
import com.kuolw.livebenchmark.db.AppDatabase

class MainApplication : Application() {
    fun getDatabase(): AppDatabase {
        return AppDatabase.getDatabase(this)
    }
}