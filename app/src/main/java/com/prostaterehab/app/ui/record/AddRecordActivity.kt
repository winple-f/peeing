package com.prostaterehab.app.ui.record

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.prostaterehab.app.ProstateRehabApp
import com.prostaterehab.app.data.entity.UrinationRecord
import com.prostaterehab.app.databinding.ActivityAddRecordBinding
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AddRecordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddRecordBinding
    private lateinit var app: ProstateRehabApp
    private var userId: Long = -1
    private var recordId: Long = -1 // 编辑模式时的记录ID
    private val calendar = Calendar.getInstance()
    private var volumeLevel = 1 // 默认中量

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddRecordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        app = application as ProstateRehabApp
        userId = app.getCurrentUserId()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(com.prostaterehab.app.R.string.add_record)

        // 检查是否是编辑模式
        recordId = intent.getLongExtra("record_id", -1)
        if (recordId != -1L) {
            loadRecordData()
        }

        setupUI()
        setupClickListeners()
        updateTimeDisplay()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun setupUI() {
        // 默认选中中量
        updateVolumeSelection()
    }

    private fun setupClickListeners() {
        // 时间选择
        binding.tvTime.setOnClickListener {
            showDateTimePicker()
        }

        // 尿量选择
        binding.volumeLow.setOnClickListener {
            volumeLevel = 0
            updateVolumeSelection()
        }
        binding.volumeMedium.setOnClickListener {
            volumeLevel = 1
            updateVolumeSelection()
        }
        binding.volumeHigh.setOnClickListener {
            volumeLevel = 2
            updateVolumeSelection()
        }

        // 保存按钮
        binding.btnSave.setOnClickListener {
            saveRecord()
        }
    }

    private fun updateVolumeSelection() {
        binding.volumeLow.setCardBackgroundColor(
            if (volumeLevel == 0) resources.getColor(com.prostaterehab.app.R.color.primary_light)
            else resources.getColor(com.prostaterehab.app.R.color.white)
        )
        binding.volumeMedium.setCardBackgroundColor(
            if (volumeLevel == 1) resources.getColor(com.prostaterehab.app.R.color.primary_light)
            else resources.getColor(com.prostaterehab.app.R.color.white)
        )
        binding.volumeHigh.setCardBackgroundColor(
            if (volumeLevel == 2) resources.getColor(com.prostaterehab.app.R.color.primary_light)
            else resources.getColor(com.prostaterehab.app.R.color.white)
        )
    }

    private fun showDateTimePicker() {
        val datePicker = DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month)
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                TimePickerDialog(
                    this,
                    { _, hourOfDay, minute ->
                        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                        calendar.set(Calendar.MINUTE, minute)
                        updateTimeDisplay()
                    },
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    true
                ).show()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePicker.show()
    }

    private fun updateTimeDisplay() {
        val sdf = SimpleDateFormat("yyyy年MM月dd日 HH:mm", Locale.CHINA)
        binding.tvTime.text = sdf.format(calendar.time)
    }

    private fun loadRecordData() {
        lifecycleScope.launch {
            val record = app.urinationRecordRepository.getRecordById(recordId)
            record?.let {
                runOnUiThread {
                    calendar.timeInMillis = it.recordTime
                    volumeLevel = it.volumeLevel
                    binding.cbLeakage.isChecked = it.hasLeakage
                    binding.cbUrgency.isChecked = it.hasUrgency
                    binding.cbPain.isChecked = it.hasPain
                    binding.cbWeakStream.isChecked = it.hasWeakStream
                    binding.cbIntermittent.isChecked = it.hasIntermittent
                    binding.cbNocturia.isChecked = it.hasNocturia
                    binding.etNote.setText(it.note ?: "")
                    updateTimeDisplay()
                    updateVolumeSelection()
                }
            }
        }
    }

    private fun saveRecord() {
        val record = UrinationRecord(
            id = if (recordId != -1L) recordId else 0,
            userId = userId,
            recordTime = calendar.timeInMillis,
            volumeLevel = volumeLevel,
            hasLeakage = binding.cbLeakage.isChecked,
            hasUrgency = binding.cbUrgency.isChecked,
            hasPain = binding.cbPain.isChecked,
            hasWeakStream = binding.cbWeakStream.isChecked,
            hasIntermittent = binding.cbIntermittent.isChecked,
            hasNocturia = binding.cbNocturia.isChecked,
            note = binding.etNote.text.toString().trim().ifEmpty { null }
        )

        lifecycleScope.launch {
            if (recordId != -1L) {
                app.urinationRecordRepository.update(record)
            } else {
                app.urinationRecordRepository.insert(record)
            }
            runOnUiThread {
                Toast.makeText(
                    this@AddRecordActivity,
                    getString(com.prostaterehab.app.R.string.record_saved),
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
        }
    }
}
