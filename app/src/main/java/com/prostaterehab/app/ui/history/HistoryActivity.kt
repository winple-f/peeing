package com.prostaterehab.app.ui.history

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.tabs.TabLayout
import com.prostaterehab.app.ProstateRehabApp
import com.prostaterehab.app.data.entity.SurveyRecord
import com.prostaterehab.app.data.entity.UrinationRecord
import com.prostaterehab.app.databinding.ActivityHistoryBinding
import com.prostaterehab.app.ui.record.AddRecordActivity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HistoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHistoryBinding
    private lateinit var app: ProstateRehabApp
    private var userId: Long = -1

    private lateinit var urinationAdapter: UrinationRecordAdapter
    private lateinit var surveyAdapter: SurveyRecordAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        app = application as ProstateRehabApp
        userId = app.getCurrentUserId()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(com.prostaterehab.app.R.string.history_title)

        setupTabs()
        setupRecyclerViews()
        observeData()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun setupTabs() {
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText(getString(com.prostaterehab.app.R.string.tab_urination)))
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText(getString(com.prostaterehab.app.R.string.tab_survey)))

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> {
                        binding.rvUrination.visibility = View.VISIBLE
                        binding.rvSurvey.visibility = View.GONE
                    }
                    1 -> {
                        binding.rvUrination.visibility = View.GONE
                        binding.rvSurvey.visibility = View.VISIBLE
                    }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun setupRecyclerViews() {
        urinationAdapter = UrinationRecordAdapter { record ->
            val intent = Intent(this, AddRecordActivity::class.java)
            intent.putExtra("record_id", record.id)
            startActivity(intent)
        }
        binding.rvUrination.layoutManager = LinearLayoutManager(this)
        binding.rvUrination.adapter = urinationAdapter

        surveyAdapter = SurveyRecordAdapter()
        binding.rvSurvey.layoutManager = LinearLayoutManager(this)
        binding.rvSurvey.adapter = surveyAdapter
    }

    private fun observeData() {
        app.urinationRecordRepository.getRecordsByUserId(userId).observe(this, Observer { records ->
            urinationAdapter.setData(records)
            binding.tvEmptyUrination.visibility = if (records.isEmpty()) View.VISIBLE else View.GONE
        })

        app.surveyRecordRepository.getRecordsByUserId(userId).observe(this, Observer { records ->
            surveyAdapter.setData(records)
            binding.tvEmptySurvey.visibility = if (records.isEmpty()) View.VISIBLE else View.GONE
        })
    }

    // 排尿记录适配器
    inner class UrinationRecordAdapter(
        private val onItemClick: (UrinationRecord) -> Unit
    ) : RecyclerView.Adapter<UrinationRecordAdapter.ViewHolder>() {

        private var data = listOf<UrinationRecord>()

        fun setData(list: List<UrinationRecord>) {
            data = list
            notifyDataSetChanged()
        }

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val tvTime: TextView = itemView.findViewById(com.prostaterehab.app.R.id.tv_time)
            val tvVolume: TextView = itemView.findViewById(com.prostaterehab.app.R.id.tv_volume)
            val tvSymptoms: TextView = itemView.findViewById(com.prostaterehab.app.R.id.tv_symptoms)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(com.prostaterehab.app.R.layout.item_urination_record, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val record = data[position]
            val sdf = SimpleDateFormat("MM月dd日 HH:mm", Locale.CHINA)
            holder.tvTime.text = sdf.format(Date(record.recordTime))

            val volumeText = when (record.volumeLevel) {
                0 -> "少量"
                1 -> "中量"
                else -> "大量"
            }
            holder.tvVolume.text = volumeText

            val symptoms = mutableListOf<String>()
            if (record.hasLeakage) symptoms.add("漏尿")
            if (record.hasUrgency) symptoms.add("尿急")
            if (record.hasPain) symptoms.add("尿痛")
            if (record.hasWeakStream) symptoms.add("尿线细")
            if (record.hasIntermittent) symptoms.add("间断")
            if (record.hasNocturia) symptoms.add("夜间")
            holder.tvSymptoms.text = if (symptoms.isEmpty()) "无症状" else symptoms.joinToString("、")

            holder.itemView.setOnClickListener { onItemClick(record) }
        }

        override fun getItemCount() = data.size
    }

    // 量表记录适配器
    inner class SurveyRecordAdapter : RecyclerView.Adapter<SurveyRecordAdapter.ViewHolder>() {

        private var data = listOf<SurveyRecord>()

        fun setData(list: List<SurveyRecord>) {
            data = list
            notifyDataSetChanged()
        }

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val tvTimePoint: TextView = itemView.findViewById(com.prostaterehab.app.R.id.tv_time_point)
            val tvDate: TextView = itemView.findViewById(com.prostaterehab.app.R.id.tv_date)
            val tvIciq: TextView = itemView.findViewById(com.prostaterehab.app.R.id.tv_iciq)
            val tvIpss: TextView = itemView.findViewById(com.prostaterehab.app.R.id.tv_ipss)
            val tvQol: TextView = itemView.findViewById(com.prostaterehab.app.R.id.tv_qol)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(com.prostaterehab.app.R.layout.item_survey_record, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val record = data[position]
            holder.tvTimePoint.text = record.timePoint

            val sdf = SimpleDateFormat("yyyy年MM月dd日", Locale.CHINA)
            holder.tvDate.text = sdf.format(Date(record.surveyDate))

            holder.tvIciq.text = "ICIQ-SF: ${record.iciqTotalScore ?: "-"}分"
            holder.tvIpss.text = "IPSS: ${record.ipssTotalScore ?: "-"}分"
            holder.tvQol.text = "QoL: ${record.qolScore ?: "-"}分"
        }

        override fun getItemCount() = data.size
    }
}
