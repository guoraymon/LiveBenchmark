package com.kuolw.livebenchmark.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.kuolw.livebenchmark.db.dao.SourceDao
import com.kuolw.livebenchmark.db.entity.SourceEntity

@Database(entities = [SourceEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun sourceDao(): SourceDao

    companion object {
        // Singleton prevents multiple instances of database opening at the
        // same time.
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            // if the INSTANCE is not null, then return it,
            // if it is, then create the database
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app"
                ).build()
                INSTANCE = instance
                // return instance
                instance
            }
        }
    }
}