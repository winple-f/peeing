package com.prostaterehab.app.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.prostaterehab.app.data.entity.UrinationRecord

@Dao
interface UrinationRecordDao {

    @Insert
    suspend fun insert(record: UrinationRecord): Long

    @Update
    suspend fun update(record: UrinationRecord)

    @Delete
    suspend fun delete(record: UrinationRecord)

    @Query("SELECT * FROM urination_records WHERE userId = :userId ORDER BY recordTime DESC")
    fun getRecordsByUserId(userId: Long): LiveData<List<UrinationRecord>>

    @Query("SELECT * FROM urination_records WHERE userId = :userId ORDER BY recordTime DESC")
    suspend fun getRecordsListByUserId(userId: Long): List<UrinationRecord>

    @Query("SELECT * FROM urination_records WHERE id = :recordId")
    suspend fun getRecordById(recordId: Long): UrinationRecord?

    @Query("SELECT COUNT(*) FROM urination_records WHERE userId = :userId AND recordTime >= :startTime AND recordTime < :endTime")
    suspend fun getCountByDateRange(userId: Long, startTime: Long, endTime: Long): Int

    @Query("SELECT * FROM urination_records WHERE userId = :userId AND recordTime >= :startTime AND recordTime < :endTime ORDER BY recordTime DESC")
    suspend fun getRecordsByDateRange(userId: Long, startTime: Long, endTime: Long): List<UrinationRecord>
}
