const app = getApp()
const storage = require('../../utils/storage.js')

Page({
  data: {
    welcomeText: '您好',
    patientId: '',
    todayCount: 0
  },

  onShow() {
    app.checkLogin()
    const userInfo = app.globalData.userInfo || {}
    const patientId = app.getPatientId() || ''
    const todayCount = storage.getTodayCount()
    this.setData({
      welcomeText: '您好，' + (userInfo.nickname || '微信用户'),
      patientId,
      todayCount
    })
  },

  goRecord() {
    wx.navigateTo({ url: '/pages/record/record' })
  },

  goSurvey() {
    wx.navigateTo({ url: '/pages/survey/survey' })
  },

  goChart() {
    wx.navigateTo({ url: '/pages/chart/chart' })
  },

  goHistory() {
    wx.navigateTo({ url: '/pages/history/history' })
  },

  goExport() {
    wx.navigateTo({ url: '/pages/export/export' })
  },

  handleLogout() {
    wx.showModal({
      title: '退出登录',
      content: '退出后将清除当前编号，重新进入会生成新编号。',
      confirmText: '退出',
      confirmColor: '#F44336',
      success: (res) => {
        if (res.confirm) {
          app.clearLogin()
          wx.reLaunch({ url: '/pages/index/index' })
        }
      }
    })
  }
})
