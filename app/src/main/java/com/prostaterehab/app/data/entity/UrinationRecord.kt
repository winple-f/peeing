package com.prostaterehab.app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "urination_records")
data class UrinationRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val recordTime: Long,
    val volumeLevel: Int,
    val hasLeakage: Boolean,
    val hasUrgency: Boolean,
    val hasPain: Boolean,
    val hasWeakStream: Boolean,
    val hasIntermittent: Boolean,
    val hasNocturia: Boolean,
    val note: String? = null,
    val leakageSeverity: Int? = null,
    val urgencySeverity: Int? = null,
    val painSeverity: Int? = null,
    val weakStreamSeverity: Int? = null,
    val intermittentSeverity: Int? = null,
    val nocturiaSeverity: Int? = null
)
