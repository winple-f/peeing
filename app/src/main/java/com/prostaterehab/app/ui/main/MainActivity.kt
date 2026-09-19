package com.prostaterehab.app.ui.main

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.prostaterehab.app.ProstateRehabApp
import com.prostaterehab.app.databinding.ActivityMainBinding
import com.prostaterehab.app.ui.chart.ChartActivity
import com.prostaterehab.app.ui.export.ExportActivity
import com.prostaterehab.app.ui.history.HistoryActivity
import com.prostaterehab.app.ui.login.LoginActivity
import com.prostaterehab.app.ui.record.AddRecordActivity
import com.prostaterehab.app.ui.survey.SurveyActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var app: ProstateRehabApp
    private var userId: Long = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        app = application as ProstateRehabApp
        userId = app.getCurrentUserId()

        if (userId == -1L) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        setupUserInfo()
        setupClickListeners()
        observeTodayRecords()
    }

    private fun setupUserInfo() {
        CoroutineScope(Dispatchers.IO).launch {
            val user = app.userRepository.getUserById(userId)
            user?.let {
                runOnUiThread {
                    binding.tvWelcome.text = String.format(getString(com.prostaterehab.app.R.string.welcome), it.nickname)
                    binding.tvPatientNumber.text = "编号：${it.patientNumber}"
                }
            }
        }
    }

    private fun setupClickListeners() {
        // 快速记录
        binding.cardQuickRecord.setOnClickListener {
            startActivity(Intent(this, AddRecordActivity::class.java))
        }

        // 填写量表
        binding.cardSurvey.setOnClickListener {
            startActivity(Intent(this, SurveyActivity::class.java))
        }

        // 查看记录
        binding.cardHistory.setOnClickListener {
            startActivity(Intent(this, HistoryActivity::class.java))
        }

        // 数据统计
        binding.cardChart.setOnClickListener {
            startActivity(Intent(this, ChartActivity::class.java))
        }

        // 导出数据
        binding.cardExport.setOnClickListener {
            startActivity(Intent(this, ExportActivity::class.java))
        }

        // 退出登录
        binding.btnLogout.setOnClickListener {
            app.clearCurrentUser()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun observeTodayRecords() {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val todayStart = calendar.timeInMillis
        val todayEnd = todayStart + 24 * 60 * 60 * 1000

        app.urinationRecordRepository.getRecordsByUserId(userId).observe(this, Observer { records ->
            val todayCount = records.count { it.recordTime >= todayStart && it.recordTime < todayEnd }
            binding.tvTodayCount.text = String.format(getString(com.prostaterehab.app.R.string.today_count), todayCount)
        })
    }
}
