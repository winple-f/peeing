package com.prostaterehab.app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 量表评估记录实体
 * 包含 ICIQ-SF、IPSS、QoL 三个量表的评分和原始回答
 */
@Entity(tableName = "survey_records")
data class SurveyRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long, // 关联用户ID
    val timePoint: String, // 时间点：T0基线 / T1疗程后1月 / T2疗程后3月 / T3疗程后6月
    val surveyDate: Long, // 填写日期（时间戳）

    // ICIQ-SF 量表（尿失禁）
    val iciqQ1: Int? = null, // 第1题：漏尿次数 0-5
    val iciqQ2: Int? = null, // 第2题：漏尿量 0-3
    val iciqQ3: Int? = null, // 第3题：对日常生活影响 0-10
    val iciqQ4: String? = null, // 第4题：什么时候漏尿（多选，逗号分隔）
    val iciqTotalScore: Int? = null, // ICIQ-SF 总分（Q1+Q2+Q3）

    // IPSS 量表（前列腺症状）
    val ipssQ1: Int? = null, // 尿不尽感 0-5
    val ipssQ2: Int? = null, // 排尿间隔<2小时 0-5
    val ipssQ3: Int? = null, // 间断排尿 0-5
    val ipssQ4: Int? = null, // 排尿不能等待 0-5
    val ipssQ5: Int? = null, // 尿线变细 0-5
    val ipssQ6: Int? = null, // 用力排尿 0-5
    val ipssQ7: Int? = null, // 夜尿次数 0-5
    val ipssTotalScore: Int? = null, // IPSS 总分 0-35

    // QoL 生活质量评分
    val qolScore: Int? = null, // QoL 0-6

    // 其他
    val padCount24h: Int? = null, // 24h尿垫数量
    val hasComplication: Boolean? = null, // 是否有并发症
    val complicationNote: String? = null, // 并发症说明
    val isLostFollowUp: Boolean = false, // 是否失访
    val note: String? = null // 备注
)
