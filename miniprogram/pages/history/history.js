const storage = require('../../utils/storage.js')
const util = require('../../utils/util.js')

Page({
  data: {
    activeTab: 'urination',
    groupedRecords: [],
    surveyRecords: []
  },

  onShow() {
    this.loadUrinationRecords()
    this.loadSurveyRecords()
  },

  switchTab(e) {
    this.setData({ activeTab: e.currentTarget.dataset.tab })
  },

  loadUrinationRecords() {
    const records = storage.getAllUrinationRecords()
    const sorted = records.sort((a, b) => b.recordTime - a.recordTime)
    const grouped = {}
    sorted.forEach(r => {
      const dateText = util.formatDate(r.recordTime)
      if (!grouped[dateText]) {
        grouped[dateText] = []
      }
      const timeDate = new Date(r.recordTime)
      const hour = String(timeDate.getHours()).padStart(2, '0')
      const minute = String(timeDate.getMinutes()).padStart(2, '0')
      const scoreList = []
      if (r.symptomScores) {
        Object.entries(r.symptomScores).forEach(([name, score]) => {
          scoreList.push({ name, score })
        })
      }
      grouped[dateText].push({
        id: r.id,
        timeText: `${hour}:${minute}`,
        urineVolume: r.urineVolume,
        symptoms: r.symptoms || [],
        scoreList,
        note: r.note || ''
      })
    })
    const groupedArr = Object.keys(grouped).map(dateText => ({
      dateText,
      records: grouped[dateText]
    }))
    this.setData({ groupedRecords: groupedArr })
  },

  loadSurveyRecords() {
    const records = storage.getSurveyRecords()
    const sorted = records.sort((a, b) => b.createdAt - a.createdAt)
    const typeMap = { 'iciq': 'ICIQ-SF', 'ipss': 'IPSS', 'qol': 'QoL' }
    const formatted = sorted.map(r => ({
      id: r.id,
      typeText: typeMap[r.type] || r.type,
      dateText: util.formatTime(r.createdAt),
      score: r.score,
      label: r.label || ''
    }))
    this.setData({ surveyRecords: formatted })
  },

  deleteRecord(e) {
    const id = e.currentTarget.dataset.id
    wx.showModal({
      title: '确认删除',
      content: '删除后无法恢复，确定删除这条记录吗？',
      confirmText: '删除',
      confirmColor: '#F44336',
      success: (res) => {
        if (res.confirm) {
          storage.deleteUrinationRecord(id)
          this.loadUrinationRecords()
          wx.showToast({ title: '已删除', icon: 'success' })
        }
      }
    })
  }
})
