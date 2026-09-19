const app = getApp()
const storage = require('../../utils/storage.js')
const util = require('../../utils/util.js')

Page({
  data: {
    activeTab: 0,
    urinationRecords: [],
    surveyRecords: []
  },

  onShow() {
    if (!app.checkLogin()) return
    this.loadData()
  },

  switchTab(e) {
    this.setData({ activeTab: parseInt(e.currentTarget.dataset.tab) })
  },

  loadData() {
    const urinationRecords = storage.getAllUrinationRecords().sort((a, b) => b.recordTime - a.recordTime)
    const volumeTexts = ['少量', '中量', '大量']
    const formattedU = urinationRecords.map(r => {
      const symptoms = []
      if (r.hasLeakage) symptoms.push('漏尿' + (r.leakageSeverity ? r.leakageSeverity + '分' : ''))
      if (r.hasUrgency) symptoms.push('尿急' + (r.urgencySeverity ? r.urgencySeverity + '分' : ''))
      if (r.hasPain) symptoms.push('尿痛' + (r.painSeverity ? r.painSeverity + '分' : ''))
      if (r.hasWeakStream) symptoms.push('尿线变细' + (r.weakStreamSeverity ? r.weakStreamSeverity + '分' : ''))
      if (r.hasIntermittent) symptoms.push('间断排尿' + (r.intermittentSeverity ? r.intermittentSeverity + '分' : ''))
      if (r.hasNocturia) symptoms.push('夜间排尿' + (r.nocturiaSeverity ? r.nocturiaSeverity + '分' : ''))
      return {
        id: r.id,
        timeText: util.formatDateTime(r.recordTime),
        volumeText: volumeTexts[r.volumeLevel] || '少量',
        symptomsText: symptoms.length > 0 ? symptoms.join('，') : '无症状'
      }
    })

    const surveyRecords = storage.getSurveyRecords().sort((a, b) => b.surveyDate - a.surveyDate)
    const formattedS = surveyRecords.map(r => {
      const ipssTotal = r.ipssTotalScore || 0
      const severity = ipssTotal <= 7 ? '轻度' : (ipssTotal <= 19 ? '中度' : '重度')
      return {
        id: r.id,
        timePoint: r.timePoint,
        dateText: util.formatDate(r.surveyDate),
        iciqTotalScore: r.iciqTotalScore || 0,
        ipssTotalScore: ipssTotal,
        qolScore: r.qolScore || 0,
        severity
      }
    })

    this.setData({ urinationRecords: formattedU, surveyRecords: formattedS })
  }
})
