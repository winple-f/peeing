package com.prostaterehab.app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 用户实体
 */
@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val patientNumber: String, // 患者编号，系统自动生成
    val username: String, // 用户名/手机号
    val password: String, // 密码（简单加密存储）
    val nickname: String, // 昵称
    val registerDate: Long, // 注册时间（时间戳）
    val baselineDate: Long? = null // 基线评估日期
)
