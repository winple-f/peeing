const app = getApp()
const storage = require('../../utils/storage.js')
const util = require('../../utils/util.js')

Page({
  data: {
    recordDate: '',
    recordTime: '',
    recordDateText: '',
    recordTimeText: '',
    urineVolume: '',
    symptoms: [
      { name: '漏尿', checked: false, score: 2 },
      { name: '尿急', checked: false, score: 2 },
      { name: '尿痛', checked: false, score: 2 },
      { name: '尿线变细', checked: false, score: 2 },
      { name: '间断排尿', checked: false, score: 2 },
      { name: '夜间排尿', checked: false, score: 2 }
    ],
    note: ''
  },

  onLoad() {
    const now = new Date()
    const year = now.getFullYear()
    const month = String(now.getMonth() + 1).padStart(2, '0')
    const day = String(now.getDate()).padStart(2, '0')
    const hour = String(now.getHours()).padStart(2, '0')
    const minute = String(now.getMinutes()).padStart(2, '0')
    const dateStr = `${year}-${month}-${day}`
    const timeStr = `${hour}:${minute}`
    this.setData({
      recordDate: dateStr,
      recordTime: timeStr,
      recordDateText: `${year}年${month}月${day}日`,
      recordTimeText: `${hour}:${minute}`
    })
  },

  onDateChange(e) {
    const val = e.detail.value
    const parts = val.split('-')
    this.setData({
      recordDate: val,
      recordDateText: `${parts[0]}年${parts[1]}月${parts[2]}日`
    })
  },

  onTimeChange(e) {
    const val = e.detail.value
    this.setData({
      recordTime: val,
      recordTimeText: val
    })
  },

  selectVolume(e) {
    this.setData({ urineVolume: e.currentTarget.dataset.val })
  },

  toggleSymptom(e) {
    const idx = e.currentTarget.dataset.idx
    const symptoms = this.data.symptoms
    symptoms[idx].checked = !symptoms[idx].checked
    this.setData({ symptoms })
  },

  onScoreChange(e) {
    const idx = e.currentTarget.dataset.idx
    const symptoms = this.data.symptoms
    symptoms[idx].score = e.detail.value
    this.setData({ symptoms })
  },

  onNoteInput(e) {
    this.setData({ note: e.detail.value })
  },

  saveRecord() {
    if (!this.data.urineVolume) {
      wx.showToast({ title: '请选择尿量', icon: 'none' })
      return
    }

    const dateStr = this.data.recordDate + ' ' + this.data.recordTime
    const recordTime = new Date(dateStr.replace(/-/g, '/')).getTime()

    const symptomScores = {}
    this.data.symptoms.forEach(s => {
      if (s.checked) {
        symptomScores[s.name] = s.score
      }
    })

    const record = {
      recordTime,
      urineVolume: this.data.urineVolume,
      symptoms: this.data.symptoms.filter(s => s.checked).map(s => s.name),
      symptomScores,
      note: this.data.note
    }

    storage.addUrinationRecord(record)
    wx.showToast({ title: '记录成功', icon: 'success' })
    setTimeout(() => {
      wx.navigateBack()
    }, 1500)
  }
})
