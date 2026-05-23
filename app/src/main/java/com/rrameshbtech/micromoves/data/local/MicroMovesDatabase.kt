package com.rrameshbtech.micromoves.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.rrameshbtech.micromoves.data.Break

@Database(
    entities = [Break::class],
    version = 1,
    exportSchema = true
)
@TypeConverters(MicroMovesDBConverters::class)
abstract class MicroMovesDatabase : RoomDatabase() {

    abstract fun breakDao(): BreakDao

    companion object {
        @Volatile
        private var INSTANCE: MicroMovesDatabase? = null

        fun getDatabase(context: Context): MicroMovesDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MicroMovesDatabase::class.java,
                    "micromoves_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}


