package com.prostaterehab.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.multidex.MultiDex
import com.prostaterehab.app.data.AppDatabase
import com.prostaterehab.app.data.repository.SurveyRecordRepository
import com.prostaterehab.app.data.repository.UrinationRecordRepository
import com.prostaterehab.app.data.repository.UserRepository

class ProstateRehabApp : Application() {

    lateinit var userRepository: UserRepository
    lateinit var urinationRecordRepository: UrinationRecordRepository
    lateinit var surveyRecordRepository: SurveyRecordRepository

    companion object {
        const val CHANNEL_ID_REMINDER = "reminder_channel"
        const val PREF_NAME = "prostate_rehab_pref"
        const val KEY_USER_ID = "current_user_id"
    }

    override fun onCreate() {
        super.onCreate()
        MultiDex.install(this)
        val database = AppDatabase.getDatabase(this)
        userRepository = UserRepository(database.userDao())
        urinationRecordRepository = UrinationRecordRepository(database.urinationRecordDao())
        surveyRecordRepository = SurveyRecordRepository(database.surveyRecordDao())

        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID_REMINDER,
                "随访提醒",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "前列腺术后康复随访提醒通知"
                enableVibration(true)
            }
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    // 获取当前登录用户ID
    fun getCurrentUserId(): Long {
        val pref = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return pref.getLong(KEY_USER_ID, -1)
    }

    // 设置当前登录用户
    fun setCurrentUserId(userId: Long) {
        val pref = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        pref.edit().putLong(KEY_USER_ID, userId).apply()
    }

    // 清除登录状态
    fun clearCurrentUser() {
        val pref = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        pref.edit().remove(KEY_USER_ID).apply()
    }
}
