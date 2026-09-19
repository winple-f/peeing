package com.prostaterehab.app.ui.survey

import android.os.Bundle
import android.view.View
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.prostaterehab.app.ProstateRehabApp
import com.prostaterehab.app.data.entity.SurveyRecord
import com.prostaterehab.app.databinding.ActivitySurveyBinding
import kotlinx.coroutines.launch

class SurveyActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySurveyBinding
    private lateinit var app: ProstateRehabApp
    private var userId: Long = -1
    private var currentStep = 0 // 0=选择时间点, 1=ICIQ-SF, 2=IPSS, 3=QoL, 4=完成
    private var selectedTimePoint = "T0基线"

    // ICIQ-SF 答案
    private var iciqQ1: Int = -1
    private var iciqQ2: Int = -1
    private var iciqQ3: Int = -1

    // IPSS 答案
    private var ipssQ1: Int = -1
    private var ipssQ2: Int = -1
    private var ipssQ3: Int = -1
    private var ipssQ4: Int = -1
    private var ipssQ5: Int = -1
    private var ipssQ6: Int = -1
    private var ipssQ7: Int = -1

    // QoL
    private var qolScore: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySurveyBinding.inflate(layoutInflater)
        setContentView(binding.root)

        app = application as ProstateRehabApp
        userId = app.getCurrentUserId()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(com.prostaterehab.app.R.string.survey_title)

        setupClickListeners()
        showStep(0)
    }

    override fun onSupportNavigateUp(): Boolean {
        if (currentStep > 0) {
            showStep(currentStep - 1)
            return true
        }
        finish()
        return true
    }

    private fun setupClickListeners() {
        // 时间点选择
        binding.timepointT0.setOnClickListener {
            selectedTimePoint = "T0基线"
            updateTimePointSelection()
        }
        binding.timepointT1.setOnClickListener {
            selectedTimePoint = "T1（疗程后1月）"
            updateTimePointSelection()
        }
        binding.timepointT2.setOnClickListener {
            selectedTimePoint = "T2（疗程后3月）"
            updateTimePointSelection()
        }
        binding.timepointT3.setOnClickListener {
            selectedTimePoint = "T3（疗程后6月）"
            updateTimePointSelection()
        }

        // 下一步/上一步/提交
        binding.btnNext.setOnClickListener {
            if (currentStep < 4) {
                if (validateCurrentStep()) {
                    showStep(currentStep + 1)
                }
            } else {
                submitSurvey()
            }
        }

        binding.btnPrev.setOnClickListener {
            if (currentStep > 0) {
                showStep(currentStep - 1)
            }
        }
    }

    private fun updateTimePointSelection() {
        binding.timepointT0.strokeWidth = if (selectedTimePoint == "T0基线") 4 else 0
        binding.timepointT1.strokeWidth = if (selectedTimePoint == "T1（疗程后1月）") 4 else 0
        binding.timepointT2.strokeWidth = if (selectedTimePoint == "T2（疗程后3月）") 4 else 0
        binding.timepointT3.strokeWidth = if (selectedTimePoint == "T3（疗程后6月）") 4 else 0

        val selectedColor = resources.getColor(com.prostaterehab.app.R.color.primary_light)
        val normalColor = resources.getColor(com.prostaterehab.app.R.color.white)

        binding.timepointT0.setCardBackgroundColor(if (selectedTimePoint == "T0基线") selectedColor else normalColor)
        binding.timepointT1.setCardBackgroundColor(if (selectedTimePoint == "T1（疗程后1月）") selectedColor else normalColor)
        binding.timepointT2.setCardBackgroundColor(if (selectedTimePoint == "T2（疗程后3月）") selectedColor else normalColor)
        binding.timepointT3.setCardBackgroundColor(if (selectedTimePoint == "T3（疗程后6月）") selectedColor else normalColor)
    }

    private fun showStep(step: Int) {
        currentStep = step

        binding.stepTimepoint.visibility = View.GONE
        binding.stepIciq.visibility = View.GONE
        binding.stepIpss.visibility = View.GONE
        binding.stepQol.visibility = View.GONE
        binding.stepResult.visibility = View.GONE

        binding.btnPrev.visibility = if (step > 0 && step < 4) View.VISIBLE else View.GONE

        when (step) {
            0 -> {
                binding.stepTimepoint.visibility = View.VISIBLE
                binding.btnNext.text = getString(com.prostaterehab.app.R.string.next)
            }
            1 -> {
                binding.stepIciq.visibility = View.VISIBLE
                binding.btnNext.text = getString(com.prostaterehab.app.R.string.next)
            }
            2 -> {
                binding.stepIpss.visibility = View.VISIBLE
                binding.btnNext.text = getString(com.prostaterehab.app.R.string.next)
            }
            3 -> {
                binding.stepQol.visibility = View.VISIBLE
                binding.btnNext.text = getString(com.prostaterehab.app.R.string.submit)
            }
            4 -> {
                binding.stepResult.visibility = View.VISIBLE
                binding.btnPrev.visibility = View.GONE
                binding.btnNext.text = "返回首页"
                showResults()
            }
        }
    }

    private fun validateCurrentStep(): Boolean {
        when (currentStep) {
            0 -> {
                // 时间点必选
                return true
            }
            1 -> {
                // ICIQ-SF 检查
                if (binding.rgIciqQ1.checkedRadioButtonId == -1 ||
                    binding.rgIciqQ2.checkedRadioButtonId == -1 ||
                    binding.rgIciqQ3.checkedRadioButtonId == -1) {
                    Toast.makeText(this, "请完成所有问题", Toast.LENGTH_SHORT).show()
                    return false
                }
                // 保存答案
                iciqQ1 = getRadioGroupScore(binding.rgIciqQ1, intArrayOf(0,1,2,3,4,5))
                iciqQ2 = getRadioGroupScore(binding.rgIciqQ2, intArrayOf(0,1,2,3))
                iciqQ3 = getRadioGroupScore(binding.rgIciqQ3, intArrayOf(0,2,4,6,8,10))
                return true
            }
            2 -> {
                // IPSS 检查
                if (binding.rgIpssQ1.checkedRadioButtonId == -1 ||
                    binding.rgIpssQ2.checkedRadioButtonId == -1 ||
                    binding.rgIpssQ3.checkedRadioButtonId == -1 ||
                    binding.rgIpssQ4.checkedRadioButtonId == -1 ||
                    binding.rgIpssQ5.checkedRadioButtonId == -1 ||
                    binding.rgIpssQ6.checkedRadioButtonId == -1 ||
                    binding.rgIpssQ7.checkedRadioButtonId == -1) {
                    Toast.makeText(this, "请完成所有问题", Toast.LENGTH_SHORT).show()
                    return false
                }
                // 保存答案
                ipssQ1 = getRadioGroupScore(binding.rgIpssQ1, intArrayOf(0,1,2,3,4,5))
                ipssQ2 = getRadioGroupScore(binding.rgIpssQ2, intArrayOf(0,1,2,3,4,5))
                ipssQ3 = getRadioGroupScore(binding.rgIpssQ3, intArrayOf(0,1,2,3,4,5))
                ipssQ4 = getRadioGroupScore(binding.rgIpssQ4, intArrayOf(0,1,2,3,4,5))
                ipssQ5 = getRadioGroupScore(binding.rgIpssQ5, intArrayOf(0,1,2,3,4,5))
                ipssQ6 = getRadioGroupScore(binding.rgIpssQ6, intArrayOf(0,1,2,3,4,5))
                ipssQ7 = getRadioGroupScore(binding.rgIpssQ7, intArrayOf(0,1,2,3,4,5))
                return true
            }
            3 -> {
                // QoL 检查
                if (binding.rgQol.checkedRadioButtonId == -1) {
                    Toast.makeText(this, "请选择一个答案", Toast.LENGTH_SHORT).show()
                    return false
                }
                qolScore = getRadioGroupScore(binding.rgQol, intArrayOf(0,1,2,3,4,5,6))
                return true
            }
            else -> return true
        }
    }

    private fun getRadioGroupScore(radioGroup: RadioGroup, scores: IntArray): Int {
        val checkedId = radioGroup.checkedRadioButtonId
        val radioButton = radioGroup.findViewById<RadioButton>(checkedId)
        val index = radioGroup.indexOfChild(radioButton)
        return if (index >= 0 && index < scores.size) scores[index] else 0
    }

    private fun showResults() {
        val iciqTotal = iciqQ1 + iciqQ2 + iciqQ3
        val ipssTotal = ipssQ1 + ipssQ2 + ipssQ3 + ipssQ4 + ipssQ5 + ipssQ6 + ipssQ7

        binding.tvIciqScore.text = "$iciqTotal 分"
        binding.tvIpssScore.text = "$ipssTotal 分"
        binding.tvQolScore.text = "$qolScore 分"

        // IPSS 严重程度
        val severity = when {
            ipssTotal <= 7 -> "轻度"
            ipssTotal <= 19 -> "中度"
            else -> "重度"
        }
        binding.tvIpssSeverity.text = "症状程度：$severity"
    }

    private fun submitSurvey() {
        if (currentStep == 4) {
            finish()
            return
        }

        val iciqTotal = iciqQ1 + iciqQ2 + iciqQ3
        val ipssTotal = ipssQ1 + ipssQ2 + ipssQ3 + ipssQ4 + ipssQ5 + ipssQ6 + ipssQ7

        val record = SurveyRecord(
            userId = userId,
            timePoint = selectedTimePoint,
            surveyDate = System.currentTimeMillis(),
            iciqQ1 = iciqQ1,
            iciqQ2 = iciqQ2,
            iciqQ3 = iciqQ3,
            iciqTotalScore = iciqTotal,
            ipssQ1 = ipssQ1,
            ipssQ2 = ipssQ2,
            ipssQ3 = ipssQ3,
            ipssQ4 = ipssQ4,
            ipssQ5 = ipssQ5,
            ipssQ6 = ipssQ6,
            ipssQ7 = ipssQ7,
            ipssTotalScore = ipssTotal,
            qolScore = qolScore
        )

        lifecycleScope.launch {
            app.surveyRecordRepository.insert(record)
            runOnUiThread {
                Toast.makeText(
                    this@SurveyActivity,
                    getString(com.prostaterehab.app.R.string.survey_submitted),
                    Toast.LENGTH_SHORT
                ).show()
                showStep(4)
            }
        }
    }
}
