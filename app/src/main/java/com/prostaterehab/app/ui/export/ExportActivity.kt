package com.prostaterehab.app.ui.export

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import com.prostaterehab.app.ProstateRehabApp
import com.prostaterehab.app.databinding.ActivityExportBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.OutputStreamWriter
import java.io.Writer
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ExportActivity : AppCompatActivity() {

    private lateinit var binding: ActivityExportBinding
    private lateinit var app: ProstateRehabApp
    private var userId: Long = -1
    private var exportRange = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityExportBinding.inflate(layoutInflater)
        setContentView(binding.root)

        app = application as ProstateRehabApp
        userId = app.getCurrentUserId()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(com.prostaterehab.app.R.string.export_title)

        setupClickListeners()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun setupClickListeners() {
        binding.rangeGroup.setOnCheckedChangeListener { _, checkedId ->
            exportRange = when (checkedId) {
                com.prostaterehab.app.R.id.range_all -> 0
                com.prostaterehab.app.R.id.range_30 -> 1
                com.prostaterehab.app.R.id.range_90 -> 2
                else -> 0
            }
        }

        binding.btnExport.setOnClickListener {
            exportData()
        }
    }

    private fun exportData() {
        lifecycleScope.launch {
            binding.btnExport.isEnabled = false
            binding.btnExport.text = "正在导出..."

            val result = withContext(Dispatchers.IO) {
                try {
                    val file = createCsvFile()
                    Result.success(file.absolutePath)
                } catch (e: Exception) {
                    e.printStackTrace()
                    Result.failure(e)
                }
            }

            binding.btnExport.isEnabled = true
            binding.btnExport.text = getString(com.prostaterehab.app.R.string.export_excel)

            if (result.isSuccess) {
                val path = result.getOrNull()
                val file = path?.let { File(it) }
                if (file != null && file.exists()) {
                    shareFile(file)
                }
            } else {
                android.widget.Toast.makeText(
                    this@ExportActivity,
                    getString(com.prostaterehab.app.R.string.export_failed),
                    android.widget.Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun shareFile(file: File) {
        val uri = FileProvider.getUriForFile(
            this,
            "${packageName}.fileprovider",
            file
        )
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        startActivity(Intent.createChooser(intent, "分享导出文件"))
    }

    private suspend fun createCsvFile(): File {
        val user = app.userRepository.getUserById(userId)
            ?: throw Exception("用户不存在")

        val endTime = System.currentTimeMillis()
        val startTime = when (exportRange) {
            1 -> endTime - 30L * 24 * 60 * 60 * 1000
            2 -> endTime - 90L * 24 * 60 * 60 * 1000
            else -> 0L
        }

        val urinationRecords = if (startTime > 0) {
            app.urinationRecordRepository.getRecordsByDateRange(userId, startTime, endTime)
        } else {
            app.urinationRecordRepository.getRecordsListByUserId(userId)
        }

        val surveyRecords = app.surveyRecordRepository.getRecordsListByUserId(userId)

        val sdf = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.CHINA)
        val fileName = "康复数据_${user.patientNumber}_${sdf.format(Date())}.csv"

        val dir = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS) ?: filesDir
        val file = File(dir, fileName)

        OutputStreamWriter(file.outputStream(), "UTF-8").use { writer ->
            writer.write("\uFEFF")

            writer.write("患者信息\n")
            writer.write("患者编号,${user.patientNumber}\n")
            writer.write("昵称,${user.nickname}\n")
            writer.write("用户名,${user.username}\n")
            writer.write("注册日期,${formatDate(user.registerDate)}\n")
            writer.write("\n")

            writer.write("排尿记录\n")
            writer.write("序号,记录时间,尿量,漏尿,漏尿评分,尿急,尿急评分,尿痛,尿痛评分,尿线变细,尿线变细评分,间断排尿,间断排尿评分,夜间排尿,夜间排尿评分,备注\n")
            val volumeTexts = arrayOf("少量", "中量", "大量")
            for ((idx, record) in urinationRecords.sortedBy { it.recordTime }.withIndex()) {
                writer.write("${idx + 1},")
                writer.write("${formatDateTime(record.recordTime)},")
                writer.write("${volumeTexts[record.volumeLevel]},")
                writer.write("${if (record.hasLeakage) "是" else "否"},")
                writer.write("${record.leakageSeverity ?: ""},")
                writer.write("${if (record.hasUrgency) "是" else "否"},")
                writer.write("${record.urgencySeverity ?: ""},")
                writer.write("${if (record.hasPain) "是" else "否"},")
                writer.write("${record.painSeverity ?: ""},")
                writer.write("${if (record.hasWeakStream) "是" else "否"},")
                writer.write("${record.weakStreamSeverity ?: ""},")
                writer.write("${if (record.hasIntermittent) "是" else "否"},")
                writer.write("${record.intermittentSeverity ?: ""},")
                writer.write("${if (record.hasNocturia) "是" else "否"},")
                writer.write("${record.nocturiaSeverity ?: ""},")
                writer.write("${escapeCsv(record.note ?: "")}\n")
            }
            writer.write("\n")

            writer.write("量表评估\n")
            writer.write("时间点,评估日期,ICIQ-Q1,ICIQ-Q2,ICIQ-Q3,ICIQ总分,IPSS-Q1,IPSS-Q2,IPSS-Q3,IPSS-Q4,IPSS-Q5,IPSS-Q6,IPSS-Q7,IPSS总分,QoL评分,24h尿垫数,并发症,失访,备注\n")
            for (record in surveyRecords.sortedBy { it.surveyDate }) {
                writer.write("${record.timePoint},")
                writer.write("${formatDate(record.surveyDate)},")
                writer.write("${record.iciqQ1 ?: ""},")
                writer.write("${record.iciqQ2 ?: ""},")
                writer.write("${record.iciqQ3 ?: ""},")
                writer.write("${record.iciqTotalScore ?: ""},")
                writer.write("${record.ipssQ1 ?: ""},")
                writer.write("${record.ipssQ2 ?: ""},")
                writer.write("${record.ipssQ3 ?: ""},")
                writer.write("${record.ipssQ4 ?: ""},")
                writer.write("${record.ipssQ5 ?: ""},")
                writer.write("${record.ipssQ6 ?: ""},")
                writer.write("${record.ipssQ7 ?: ""},")
                writer.write("${record.ipssTotalScore ?: ""},")
                writer.write("${record.qolScore ?: ""},")
                writer.write("${record.padCount24h ?: ""},")
                writer.write("${if (record.hasComplication == true) "是" else "否"},")
                writer.write("${if (record.isLostFollowUp) "是" else "否"},")
                writer.write("${escapeCsv(record.note ?: "")}\n")
            }
        }

        return file
    }

    private fun escapeCsv(text: String): String {
        return if (text.contains(",") || text.contains("\"") || text.contains("\n")) {
            "\"${text.replace("\"", "\"\"")}\""
        } else {
            text
        }
    }

    private fun formatDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("yyyy年MM月dd日", Locale.CHINA)
        return sdf.format(Date(timestamp))
    }

    private fun formatDateTime(timestamp: Long): String {
        val sdf = SimpleDateFormat("yyyy年MM月dd日 HH:mm", Locale.CHINA)
        return sdf.format(Date(timestamp))
    }
}
