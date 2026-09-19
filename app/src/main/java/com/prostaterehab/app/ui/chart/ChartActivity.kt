package com.prostaterehab.app.ui.chart

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.tabs.TabLayout
import com.prostaterehab.app.ProstateRehabApp
import com.prostaterehab.app.databinding.ActivityChartBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class ChartActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChartBinding
    private lateinit var app: ProstateRehabApp
    private var userId: Long = -1

    private val heatmapCalendar = Calendar.getInstance()
    private var chartMode = 0 // 0=周, 1=月

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChartBinding.inflate(layoutInflater)
        setContentView(binding.root)

        app = application as ProstateRehabApp
        userId = app.getCurrentUserId()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(com.prostaterehab.app.R.string.chart_title)

        setupTabs()
        setupClickListeners()
        loadHeatmap()
        loadLineChart()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun setupTabs() {
        binding.tabChartRange.addTab(binding.tabChartRange.newTab().setText(getString(com.prostaterehab.app.R.string.chart_weekly)))
        binding.tabChartRange.addTab(binding.tabChartRange.newTab().setText(getString(com.prostaterehab.app.R.string.chart_monthly)))

        binding.tabChartRange.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                chartMode = tab?.position ?: 0
                loadLineChart()
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun setupClickListeners() {
        binding.btnPrevMonth.setOnClickListener {
            heatmapCalendar.add(Calendar.MONTH, -1)
            loadHeatmap()
        }
        binding.btnNextMonth.setOnClickListener {
            heatmapCalendar.add(Calendar.MONTH, 1)
            loadHeatmap()
        }
    }

    private fun loadHeatmap() {
        val sdf = SimpleDateFormat("yyyy年MM月", Locale.CHINA)
        binding.tvHeatmapMonth.text = sdf.format(heatmapCalendar.time)

        lifecycleScope.launch {
            val data = withContext(Dispatchers.IO) {
                val cal = heatmapCalendar.clone() as Calendar
                cal.set(Calendar.DAY_OF_MONTH, 1)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                val monthStart = cal.timeInMillis

                val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
                cal.add(Calendar.DAY_OF_MONTH, daysInMonth)
                val monthEnd = cal.timeInMillis

                val records = app.urinationRecordRepository
                    .getRecordsByDateRange(userId, monthStart, monthEnd)

                val dayScores = HashMap<Int, Int>()
                for (record in records) {
                    val dayCal = Calendar.getInstance().apply { timeInMillis = record.recordTime }
                    val day = dayCal.get(Calendar.DAY_OF_MONTH)
                    var score = 0
                    if (record.hasLeakage) score += record.leakageSeverity ?: 1
                    if (record.hasUrgency) score += record.urgencySeverity ?: 1
                    if (record.hasPain) score += record.painSeverity ?: 1
                    if (record.hasWeakStream) score += record.weakStreamSeverity ?: 1
                    if (record.hasIntermittent) score += record.intermittentSeverity ?: 1
                    if (record.hasNocturia) score += record.nocturiaSeverity ?: 1
                    dayScores[day] = (dayScores[day] ?: 0) + score
                }

                cal.set(Calendar.DAY_OF_MONTH, 1)
                val firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK) - 1

                val dataList = mutableListOf<HeatMapView.DayData>()
                for (i in 0 until firstDayOfWeek) {
                    dataList.add(HeatMapView.DayData(0, 0, false))
                }
                for (day in 1..daysInMonth) {
                    dataList.add(HeatMapView.DayData(day, dayScores[day] ?: 0, true))
                }
                while (dataList.size % 7 != 0) {
                    dataList.add(HeatMapView.DayData(0, 0, false))
                }

                val maxScore = dayScores.values.maxOrNull() ?: 0
                Pair(dataList, maxScore)
            }

            runOnUiThread {
                binding.heatmapView.setData(data.first, data.second)
            }
        }
    }

    private fun loadLineChart() {
        lifecycleScope.launch {
            val data = withContext(Dispatchers.IO) {
                val cal = Calendar.getInstance()
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)

                val labels = mutableListOf<String>()
                val values = mutableListOf<Float>()

                if (chartMode == 0) {
                    val daySdf = SimpleDateFormat("MM/dd", Locale.CHINA)
                    cal.add(Calendar.DAY_OF_YEAR, -6)

                    for (i in 0 until 7) {
                        val dayStart = cal.timeInMillis
                        cal.add(Calendar.DAY_OF_YEAR, 1)
                        val dayEnd = cal.timeInMillis

                        val count = app.urinationRecordRepository
                            .getDailyCount(userId, dayStart, dayEnd)
                        labels.add(daySdf.format(Date(dayStart)))
                        values.add(count.toFloat())

                        if (i < 6) cal.add(Calendar.DAY_OF_YEAR, -1)
                    }
                    cal.add(Calendar.DAY_OF_YEAR, 1)

                    val total = values.sum().toInt()
                    val avg = if (values.isNotEmpty()) total / values.size else 0
                    Triple(labels, values, "本周共排尿${total}次，日均${avg}次")
                } else {
                    val daySdf = SimpleDateFormat("d", Locale.CHINA)
                    cal.add(Calendar.DAY_OF_YEAR, -29)

                    for (i in 0 until 30) {
                        val dayStart = cal.timeInMillis
                        cal.add(Calendar.DAY_OF_YEAR, 1)
                        val dayEnd = cal.timeInMillis

                        val count = app.urinationRecordRepository
                            .getDailyCount(userId, dayStart, dayEnd)
                        labels.add(daySdf.format(Date(dayStart)))
                        values.add(count.toFloat())

                        if (i < 29) cal.add(Calendar.DAY_OF_YEAR, -1)
                    }
                    cal.add(Calendar.DAY_OF_YEAR, 1)

                    val total = values.sum().toInt()
                    val avg = if (values.isNotEmpty()) total / values.size else 0
                    Triple(labels, values, "近30天共排尿${total}次，日均${avg}次")
                }
            }

            runOnUiThread {
                if (chartMode == 0) {
                    binding.tvLineTitle.text = "近7天排尿次数"
                } else {
                    binding.tvLineTitle.text = "近30天排尿次数"
                }
                binding.lineChartView.setData(data.first, data.second)
                binding.tvLineSummary.text = data.third
            }
        }
    }
}
