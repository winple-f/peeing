package com.prostaterehab.app.ui.record

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.SeekBar
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
    private var recordId: Long = -1
    private val calendar = Calendar.getInstance()
    private var volumeLevel = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddRecordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        app = application as ProstateRehabApp
        userId = app.getCurrentUserId()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(com.prostaterehab.app.R.string.add_record)

        recordId = intent.getLongExtra("record_id", -1)
        if (recordId != -1L) {
            loadRecordData()
        }

        setupUI()
        setupClickListeners()
        setupSymptomSliders()
        updateTimeDisplay()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun setupUI() {
        updateVolumeSelection()
    }

    private fun setupClickListeners() {
        binding.tvTime.setOnClickListener {
            showDateTimePicker()
        }

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

        binding.btnSave.setOnClickListener {
            saveRecord()
        }
    }

    private fun setupSymptomSliders() {
        setupSymptomSlider(binding.cbLeakage, binding.sliderLeakage, binding.seekbarLeakage, binding.tvLeakageScore)
        setupSymptomSlider(binding.cbUrgency, binding.sliderUrgency, binding.seekbarUrgency, binding.tvUrgencyScore)
        setupSymptomSlider(binding.cbPain, binding.sliderPain, binding.seekbarPain, binding.tvPainScore)
        setupSymptomSlider(binding.cbWeakStream, binding.sliderWeakStream, binding.seekbarWeakStream, binding.tvWeakStreamScore)
        setupSymptomSlider(binding.cbIntermittent, binding.sliderIntermittent, binding.seekbarIntermittent, binding.tvIntermittentScore)
        setupSymptomSlider(binding.cbNocturia, binding.sliderNocturia, binding.seekbarNocturia, binding.tvNocturiaScore)
    }

    private fun setupSymptomSlider(
        checkBox: com.google.android.material.checkbox.MaterialCheckBox,
        sliderLayout: View,
        seekBar: SeekBar,
        scoreText: android.widget.TextView
    ) {
        checkBox.setOnCheckedChangeListener { _, isChecked ->
            sliderLayout.visibility = if (isChecked) View.VISIBLE else View.GONE
        }

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                scoreText.text = "${progress + 1}分"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
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

                    it.leakageSeverity?.let { s -> binding.seekbarLeakage.progress = s - 1 }
                    it.urgencySeverity?.let { s -> binding.seekbarUrgency.progress = s - 1 }
                    it.painSeverity?.let { s -> binding.seekbarPain.progress = s - 1 }
                    it.weakStreamSeverity?.let { s -> binding.seekbarWeakStream.progress = s - 1 }
                    it.intermittentSeverity?.let { s -> binding.seekbarIntermittent.progress = s - 1 }
                    it.nocturiaSeverity?.let { s -> binding.seekbarNocturia.progress = s - 1 }

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
            note = binding.etNote.text.toString().trim().ifEmpty { null },
            leakageSeverity = if (binding.cbLeakage.isChecked) binding.seekbarLeakage.progress + 1 else null,
            urgencySeverity = if (binding.cbUrgency.isChecked) binding.seekbarUrgency.progress + 1 else null,
            painSeverity = if (binding.cbPain.isChecked) binding.seekbarPain.progress + 1 else null,
            weakStreamSeverity = if (binding.cbWeakStream.isChecked) binding.seekbarWeakStream.progress + 1 else null,
            intermittentSeverity = if (binding.cbIntermittent.isChecked) binding.seekbarIntermittent.progress + 1 else null,
            nocturiaSeverity = if (binding.cbNocturia.isChecked) binding.seekbarNocturia.progress + 1 else null
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
