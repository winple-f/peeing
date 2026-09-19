package com.prostaterehab.app.data.repository

import com.prostaterehab.app.data.dao.UserDao
import com.prostaterehab.app.data.entity.User
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class UserRepository(private val userDao: UserDao) {

    // 简单的密码加密（MD5，仅用于本地存储，非安全用途）
    private fun hashPassword(password: String): String {
        val md = MessageDigest.getInstance("MD5")
        val digest = md.digest(password.toByteArray())
        return digest.joinToString("") { "%02x".format(it) }
    }

    // 生成患者编号：PR + 年份 + 4位序号
    private suspend fun generatePatientNumber(): String {
        val count = userDao.getUserCount()
        val year = SimpleDateFormat("yyyy", Locale.CHINA).format(Date())
        val seq = String.format("%04d", count + 1)
        return "PR$year$seq"
    }

    suspend fun register(username: String, password: String, nickname: String): User? {
        // 检查用户名是否已存在
        if (userDao.findByUsername(username) != null) {
            return null
        }
        val patientNumber = generatePatientNumber()
        val user = User(
            patientNumber = patientNumber,
            username = username,
            password = hashPassword(password),
            nickname = nickname,
            registerDate = System.currentTimeMillis()
        )
        val id = userDao.insert(user)
        return user.copy(id = id)
    }

    suspend fun login(username: String, password: String): User? {
        return userDao.login(username, hashPassword(password))
    }

    suspend fun getUserById(userId: Long): User? {
        return userDao.getUserById(userId)
    }

    suspend fun updateUser(user: User) {
        userDao.update(user)
    }
}
