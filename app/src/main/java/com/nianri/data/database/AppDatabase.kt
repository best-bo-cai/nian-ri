package com.nianri.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.nianri.data.dao.AiConfigDao
import com.nianri.data.dao.EventDao
import com.nianri.data.dao.SmtpConfigDao
import com.nianri.data.entity.AiConfigEntity
import com.nianri.data.entity.EventEntity
import com.nianri.data.entity.SmtpConfigEntity

@Database(
    entities = [
        EventEntity::class,
        AiConfigEntity::class,
        SmtpConfigEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun eventDao(): EventDao
    abstract fun aiConfigDao(): AiConfigDao
    abstract fun smtpConfigDao(): SmtpConfigDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "nianri_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
