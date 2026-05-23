package com.rrameshbtech.micromoves.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.rrameshbtech.micromoves.data.Break
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

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
                    .addCallback(SeedCallback(context.applicationContext))
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class SeedCallback(private val context: Context) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            CoroutineScope(Dispatchers.IO).launch {
                INSTANCE?.breakDao()?.let { dao ->
                    DatabaseSeeder.seed(context, dao)
                }
            }
        }
    }
}
