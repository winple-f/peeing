package com.prostaterehab.app.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.prostaterehab.app.data.entity.SurveyRecord

@Dao
interface SurveyRecordDao {

    @Insert
    suspend fun insert(record: SurveyRecord): Long

    @Update
    suspend fun update(record: SurveyRecord)

    @Delete
    suspend fun delete(record: SurveyRecord)

    @Query("SELECT * FROM survey_records WHERE userId = :userId ORDER BY surveyDate DESC")
    fun getRecordsByUserId(userId: Long): LiveData<List<SurveyRecord>>

    @Query("SELECT * FROM survey_records WHERE userId = :userId ORDER BY surveyDate DESC")
    suspend fun getRecordsListByUserId(userId: Long): List<SurveyRecord>

    @Query("SELECT * FROM survey_records WHERE id = :recordId")
    suspend fun getRecordById(recordId: Long): SurveyRecord?

    @Query("SELECT * FROM survey_records WHERE userId = :userId AND timePoint = :timePoint LIMIT 1")
    suspend fun getRecordByTimePoint(userId: Long, timePoint: String): SurveyRecord?
}
