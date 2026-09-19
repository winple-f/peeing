package com.prostaterehab.app.data.repository

import androidx.lifecycle.LiveData
import com.prostaterehab.app.data.dao.UrinationRecordDao
import com.prostaterehab.app.data.entity.UrinationRecord

class UrinationRecordRepository(private val dao: UrinationRecordDao) {

    fun getRecordsByUserId(userId: Long): LiveData<List<UrinationRecord>> {
        return dao.getRecordsByUserId(userId)
    }

    suspend fun getRecordsListByUserId(userId: Long): List<UrinationRecord> {
        return dao.getRecordsListByUserId(userId)
    }

    suspend fun getRecordsByDateRange(userId: Long, startTime: Long, endTime: Long): List<UrinationRecord> {
        return dao.getRecordsByDateRange(userId, startTime, endTime)
    }

    suspend fun insert(record: UrinationRecord): Long {
        return dao.insert(record)
    }

    suspend fun update(record: UrinationRecord) {
        dao.update(record)
    }

    suspend fun delete(record: UrinationRecord) {
        dao.delete(record)
    }

    suspend fun getRecordById(recordId: Long): UrinationRecord? {
        return dao.getRecordById(recordId)
    }

    suspend fun getDailyCount(userId: Long, dateStart: Long, dateEnd: Long): Int {
        return dao.getCountByDateRange(userId, dateStart, dateEnd)
    }
}
