package com.prostaterehab.app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.prostaterehab.app.data.dao.SurveyRecordDao
import com.prostaterehab.app.data.dao.UrinationRecordDao
import com.prostaterehab.app.data.dao.UserDao
import com.prostaterehab.app.data.entity.SurveyRecord
import com.prostaterehab.app.data.entity.UrinationRecord
import com.prostaterehab.app.data.entity.User

@Database(
    entities = [
        User::class,
        UrinationRecord::class,
        SurveyRecord::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun urinationRecordDao(): UrinationRecordDao
    abstract fun surveyRecordDao(): SurveyRecordDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "prostate_rehab_db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
