App({
  globalData: {
    userInfo: null,
    patientId: null,
    surveyStep: 0,
    surveyType: '',
    surveyAnswers: []
  },

  onLaunch() {
    const userInfo = wx.getStorageSync('userInfo')
    const patientId = wx.getStorageSync('patientId')
    if (userInfo) {
      this.globalData.userInfo = userInfo
    }
    if (patientId) {
      this.globalData.patientId = patientId
    }
  },

  getPatientId() {
    if (this.globalData.patientId) {
      return this.globalData.patientId
    }
    const stored = wx.getStorageSync('patientId')
    if (stored) {
      this.globalData.patientId = stored
      return stored
    }
    return null
  },

  generatePatientId() {
    const year = new Date().getFullYear()
    const random = String(Math.floor(Math.random() * 9000) + 1000)
    const patientId = 'PR' + year + random
    wx.setStorageSync('patientId', patientId)
    this.globalData.patientId = patientId
    return patientId
  },

  checkLogin() {
    const patientId = this.getPatientId()
    if (!patientId) {
      wx.redirectTo({ url: '/pages/login/login' })
      return false
    }
    return true
  }
})
