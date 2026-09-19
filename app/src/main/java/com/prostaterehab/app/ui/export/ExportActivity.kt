package com.prostaterehab.app.ui.export

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.prostaterehab.app.ProstateRehabApp
import com.prostaterehab.app.databinding.ActivityExportBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.apache.poi.ss.usermodel.FillPatternType
import org.apache.poi.ss.usermodel.IndexedColors
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ExportActivity : AppCompatActivity() {

    private lateinit var binding: ActivityExportBinding
    private lateinit var app: ProstateRehabApp
    private var userId: Long = -1
    private var exportRange = 0 // 0=全部, 1=最近30天, 2=最近90天

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
            if (checkPermission()) {
                exportData()
            } else {
                requestPermission()
            }
        }
    }

    private fun checkPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            true // Android 10+ 不需要存储权限，用 MediaStore 或 app 私有目录
        } else {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requestPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
            100
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            exportData()
        } else {
            Toast.makeText(this, "需要存储权限才能导出文件", Toast.LENGTH_SHORT).show()
        }
    }

    private fun exportData() {
        lifecycleScope.launch {
            binding.btnExport.isEnabled = false
            binding.btnExport.text = "正在导出..."

            val result = withContext(Dispatchers.IO) {
                try {
                    val file = createExcelFile()
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
                Toast.makeText(
                    this@ExportActivity,
                    String.format(getString(com.prostaterehab.app.R.string.export_success), path),
                    Toast.LENGTH_LONG
                ).show()
            } else {
                Toast.makeText(
                    this@ExportActivity,
                    getString(com.prostaterehab.app.R.string.export_failed),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private suspend fun createExcelFile(): File {
        val user = app.userRepository.getUserById(userId)
            ?: throw Exception("用户不存在")

        // 计算导出时间范围
        val endTime = System.currentTimeMillis()
        val startTime = when (exportRange) {
            1 -> endTime - 30L * 24 * 60 * 60 * 1000 // 最近30天
            2 -> endTime - 90L * 24 * 60 * 60 * 1000 // 最近90天
            else -> 0L // 全部
        }

        // 获取数据
        val urinationRecords = if (startTime > 0) {
            app.urinationRecordRepository.getRecordsByDateRange(userId, startTime, endTime)
        } else {
            app.urinationRecordRepository.getRecordsListByUserId(userId)
        }

        val surveyRecords = app.surveyRecordRepository.getRecordsListByUserId(userId)

        // 创建工作簿
        val workbook = XSSFWorkbook()
        val headerStyle = workbook.createCellStyle().apply {
            fillForegroundColor = IndexedColors.LIGHT_BLUE.index
            fillPattern = FillPatternType.SOLID_FOREGROUND
            font = workbook.createFont().apply {
                bold = true
                fontHeightInPoints = 12
            }
        }

        // Sheet 1: 患者信息
        val userSheet = workbook.createSheet("患者信息")
        userSheet.setColumnWidth(0, 4000)
        userSheet.setColumnWidth(1, 8000)

        val userData = listOf(
            arrayOf("患者编号", user.patientNumber),
            arrayOf("昵称", user.nickname),
            arrayOf("用户名", user.username),
            arrayOf("注册日期", formatDate(user.registerDate))
        )
        for ((rowIdx, rowData) in userData.withIndex()) {
            val row = userSheet.createRow(rowIdx)
            for ((colIdx, cellData) in rowData.withIndex()) {
                val cell = row.createCell(colIdx)
                cell.setCellValue(cellData)
                if (colIdx == 0) cell.cellStyle = headerStyle
            }
        }

        // Sheet 2: 排尿记录
        val urinationSheet = workbook.createSheet("排尿记录")
        val urinationHeaders = arrayOf(
            "序号", "记录时间", "尿量", "漏尿", "尿急", "尿痛", "尿线变细", "间断排尿", "夜间排尿", "备注"
        )
        val headerRow = urinationSheet.createRow(0)
        for ((i, header) in urinationHeaders.withIndex()) {
            val cell = headerRow.createCell(i)
            cell.setCellValue(header)
            cell.cellStyle = headerStyle
            urinationSheet.setColumnWidth(i, 4000)
        }

        val volumeTexts = arrayOf("少量", "中量", "大量")
        for ((idx, record) in urinationRecords.sortedBy { it.recordTime }.withIndex()) {
            val row = urinationSheet.createRow(idx + 1)
            row.createCell(0).setCellValue((idx + 1).toDouble())
            row.createCell(1).setCellValue(formatDateTime(record.recordTime))
            row.createCell(2).setCellValue(volumeTexts[record.volumeLevel])
            row.createCell(3).setCellValue(if (record.hasLeakage) "是" else "否")
            row.createCell(4).setCellValue(if (record.hasUrgency) "是" else "否")
            row.createCell(5).setCellValue(if (record.hasPain) "是" else "否")
            row.createCell(6).setCellValue(if (record.hasWeakStream) "是" else "否")
            row.createCell(7).setCellValue(if (record.hasIntermittent) "是" else "否")
            row.createCell(8).setCellValue(if (record.hasNocturia) "是" else "否")
            row.createCell(9).setCellValue(record.note ?: "")
        }

        // Sheet 3: 量表记录
        val surveySheet = workbook.createSheet("量表评估")
        val surveyHeaders = arrayOf(
            "时间点", "评估日期",
            "ICIQ-Q1", "ICIQ-Q2", "ICIQ-Q3", "ICIQ总分",
            "IPSS-Q1", "IPSS-Q2", "IPSS-Q3", "IPSS-Q4", "IPSS-Q5", "IPSS-Q6", "IPSS-Q7", "IPSS总分",
            "QoL评分", "24h尿垫数", "并发症", "失访", "备注"
        )
        val surveyHeaderRow = surveySheet.createRow(0)
        for ((i, header) in surveyHeaders.withIndex()) {
            val cell = surveyHeaderRow.createCell(i)
            cell.setCellValue(header)
            cell.cellStyle = headerStyle
            surveySheet.setColumnWidth(i, 3500)
        }

        for ((idx, record) in surveyRecords.sortedBy { it.surveyDate }.withIndex()) {
            val row = surveySheet.createRow(idx + 1)
            row.createCell(0).setCellValue(record.timePoint)
            row.createCell(1).setCellValue(formatDate(record.surveyDate))
            row.createCell(2).setCellValue(record.iciqQ1?.toDouble() ?: 0.0)
            row.createCell(3).setCellValue(record.iciqQ2?.toDouble() ?: 0.0)
            row.createCell(4).setCellValue(record.iciqQ3?.toDouble() ?: 0.0)
            row.createCell(5).setCellValue(record.iciqTotalScore?.toDouble() ?: 0.0)
            row.createCell(6).setCellValue(record.ipssQ1?.toDouble() ?: 0.0)
            row.createCell(7).setCellValue(record.ipssQ2?.toDouble() ?: 0.0)
            row.createCell(8).setCellValue(record.ipssQ3?.toDouble() ?: 0.0)
            row.createCell(9).setCellValue(record.ipssQ4?.toDouble() ?: 0.0)
            row.createCell(10).setCellValue(record.ipssQ5?.toDouble() ?: 0.0)
            row.createCell(11).setCellValue(record.ipssQ6?.toDouble() ?: 0.0)
            row.createCell(12).setCellValue(record.ipssQ7?.toDouble() ?: 0.0)
            row.createCell(13).setCellValue(record.ipssTotalScore?.toDouble() ?: 0.0)
            row.createCell(14).setCellValue(record.qolScore?.toDouble() ?: 0.0)
            row.createCell(15).setCellValue(record.padCount24h?.toDouble() ?: 0.0)
            row.createCell(16).setCellValue(if (record.hasComplication == true) "是" else "否")
            row.createCell(17).setCellValue(if (record.isLostFollowUp) "是" else "否")
            row.createCell(18).setCellValue(record.note ?: "")
        }

        // 保存文件
        val sdf = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.CHINA)
        val fileName = "康复数据_${user.patientNumber}_${sdf.format(Date())}.xlsx"

        val dir = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
            ?: filesDir
        val file = File(dir, fileName)

        FileOutputStream(file).use {
            workbook.write(it)
        }
        workbook.close()

        return file
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
