package com.prostaterehab.app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 排尿记录实体
 */
@Entity(tableName = "urination_records")
data class UrinationRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long, // 关联用户ID
    val recordTime: Long, // 记录时间（时间戳）
    val volumeLevel: Int, // 尿量：0=少量，1=中量，2=大量
    val hasLeakage: Boolean, // 是否有漏尿
    val hasUrgency: Boolean, // 是否有尿急
    val hasPain: Boolean, // 是否有尿痛
    val hasWeakStream: Boolean, // 是否尿线变细
    val hasIntermittent: Boolean, // 是否有间断排尿
    val hasNocturia: Boolean, // 是否夜间排尿（本次排尿是否在夜间）
    val note: String? = null // 备注
)
