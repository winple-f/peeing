const app = getApp()
const storage = require('../../utils/storage.js')
const util = require('../../utils/util.js')

Page({
  data: {
    recordTime: '',
    recordTimestamp: 0,
    volumeLevel: 1,
    symptoms: [
      { key: 'leakage', label: '有漏尿', checked: false, score: 1 },
      { key: 'urgency', label: '有尿急', checked: false, score: 1 },
      { key: 'pain', label: '有尿痛', checked: false, score: 1 },
      { key: 'weakStream', label: '尿线变细', checked: false, score: 1 },
      { key: 'intermittent', label: '间断排尿', checked: false, score: 1 },
      { key: 'nocturia', label: '夜间排尿', checked: false, score: 1 }
    ],
    note: ''
  },

  onLoad() {
    if (!app.checkLogin()) return
    const now = Date.now()
    this.setData({
      recordTime: util.formatDateTime(now),
      recordTimestamp: now
    })
  },

  pickTime() {
    const now = new Date(this.data.recordTimestamp)
    wx.datePick({
      mode: 'date',
      value: util.formatDate(now.getTime()),
      start: '2020-01-01',
      end: '2099-12-31',
      success: (res) => {
        const timeStr = res.dateStr
        this.setData({
          recordTime: timeStr,
          recordTimestamp: new Date(timeStr).getTime()
        })
      }
    })
  },

  selectVolume(e) {
    this.setData({ volumeLevel: e.currentTarget.dataset.level })
  },

  toggleSymptom(e) {
    const idx = e.currentTarget.dataset.index
    const symptoms = this.data.symptoms
    symptoms[idx].checked = !symptoms[idx].checked
    this.setData({ symptoms })
  },

  onScoreChange(e) {
    const idx = e.currentTarget.dataset.index
    const symptoms = this.data.symptoms
    symptoms[idx].score = e.detail.value
    this.setData({ symptoms })
  },

  onNoteInput(e) {
    this.setData({ note: e.detail.value })
  },

  handleSave() {
    const { recordTimestamp, volumeLevel, symptoms, note } = this.data
    const record = {
      recordTime: recordTimestamp,
      volumeLevel,
      hasLeakage: symptoms[0].checked,
      leakageSeverity: symptoms[0].checked ? symptoms[0].score : null,
      hasUrgency: symptoms[1].checked,
      urgencySeverity: symptoms[1].checked ? symptoms[1].score : null,
      hasPain: symptoms[2].checked,
      painSeverity: symptoms[2].checked ? symptoms[2].score : null,
      hasWeakStream: symptoms[3].checked,
      weakStreamSeverity: symptoms[3].checked ? symptoms[3].score : null,
      hasIntermittent: symptoms[4].checked,
      intermittentSeverity: symptoms[4].checked ? symptoms[4].score : null,
      hasNocturia: symptoms[5].checked,
      nocturiaSeverity: symptoms[5].checked ? symptoms[5].score : null,
      note: note || null
    }
    storage.addUrinationRecord(record)
    wx.showToast({ title: '记录已保存', icon: 'success' })
    setTimeout(() => { wx.navigateBack() }, 1000)
  }
})
