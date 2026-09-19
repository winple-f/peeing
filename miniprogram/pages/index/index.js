const app = getApp()
const util = require('../../utils/util.js')

Page({
  data: {
    patientId: '',
    todayDate: '',
    nextFollowup: null
  },

  onShow() {
    if (!app.checkLogin()) return
    const patientId = app.getPatientId()
    const today = util.formatDate(Date.now())

    const nextFollowup = this.getNextFollowup()

    this.setData({
      patientId,
      todayDate: today,
      nextFollowup
    })
  },

  getNextFollowup() {
    const registerDate = wx.getStorageSync('registerDate') || Date.now()
    const labels = ['基线评估', '1月随访', '3月随访', '6月随访']
    const offsets = [0, 30, 90, 180]
    const now = Date.now()

    for (let i = 0; i < offsets.length; i++) {
      const target = new Date(registerDate)
      target.setDate(target.getDate() + offsets[i])
      if (target.getTime() > now || i === 0) {
        return {
          name: labels[i],
          date: util.formatDate(target.getTime())
        }
      }
    }
    return null
  },

  goRecord() {
    wx.navigateTo({ url: '/pages/record/record' })
  },

  goSurvey() {
    wx.navigateTo({ url: '/pages/survey/survey' })
  },

  goChart() {
    wx.switchTab({ url: '/pages/chart/chart' })
  },

  goHistory() {
    wx.switchTab({ url: '/pages/history/history' })
  },

  goExport() {
    wx.navigateTo({ url: '/pages/export/export' })
  }
})
