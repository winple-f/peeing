package com.prostaterehab.app.data.repository

import androidx.lifecycle.LiveData
import com.prostaterehab.app.data.dao.SurveyRecordDao
import com.prostaterehab.app.data.entity.SurveyRecord

class SurveyRecordRepository(private val dao: SurveyRecordDao) {

    fun getRecordsByUserId(userId: Long): LiveData<List<SurveyRecord>> {
        return dao.getRecordsByUserId(userId)
    }

    suspend fun getRecordsListByUserId(userId: Long): List<SurveyRecord> {
        return dao.getRecordsListByUserId(userId)
    }

    suspend fun insert(record: SurveyRecord): Long {
        return dao.insert(record)
    }

    suspend fun update(record: SurveyRecord) {
        dao.update(record)
    }

    suspend fun delete(record: SurveyRecord) {
        dao.delete(record)
    }

    suspend fun getRecordById(recordId: Long): SurveyRecord? {
        return dao.getRecordById(recordId)
    }

    suspend fun getRecordByTimePoint(userId: Long, timePoint: String): SurveyRecord? {
        return dao.getRecordByTimePoint(userId, timePoint)
    }

    // 计算ICIQ-SF总分
    fun calculateIciqScore(q1: Int, q2: Int, q3: Int): Int {
        return q1 + q2 + q3
    }

    // 计算IPSS总分
    fun calculateIpssScore(q1: Int, q2: Int, q3: Int, q4: Int, q5: Int, q6: Int, q7: Int): Int {
        return q1 + q2 + q3 + q4 + q5 + q6 + q7
    }

    // 获取IPSS严重程度
    fun getIpssSeverity(score: Int): String {
        return when {
            score <= 7 -> "轻度"
            score <= 19 -> "中度"
            else -> "重度"
        }
    }
}
