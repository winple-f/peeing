App({
  globalData: {
    userInfo: null,
    patientId: null,
    userId: null
  },

  onLaunch() {
    this.initLogin()
  },

  initLogin() {
    const storedId = wx.getStorageSync('patientId')
    const storedUser = wx.getStorageSync('userInfo')
    if (storedId && storedUser) {
      this.globalData.patientId = storedId
      this.globalData.userInfo = storedUser
      this.globalData.userId = storedUser.userId || null
      return
    }

    const patientId = this.generatePatientId()
    const userId = Date.now()
    this.globalData.userInfo = { userId, nickname: '微信用户' }
    this.globalData.patientId = patientId
    this.globalData.userId = userId
    wx.setStorageSync('userInfo', this.globalData.userInfo)
    wx.setStorageSync('patientId', patientId)
    wx.setStorageSync('userId', userId)
  },

  getPatientId() {
    if (this.globalData.patientId) return this.globalData.patientId
    const stored = wx.getStorageSync('patientId')
    if (stored) {
      this.globalData.patientId = stored
      return stored
    }
    return null
  },

  getUserId() {
    if (this.globalData.userId !== null) return this.globalData.userId
    const stored = wx.getStorageSync('userId')
    if (stored !== '' && stored !== null && stored !== undefined) {
      this.globalData.userId = stored
      return stored
    }
    return null
  },

  generatePatientId() {
    const year = new Date().getFullYear()
    const seq = String(Math.floor(Math.random() * 9000) + 1000)
    const patientId = 'PR' + year + seq
    wx.setStorageSync('patientId', patientId)
    this.globalData.patientId = patientId
    return patientId
  },

  checkLogin() {
    return true
  },

  clearLogin() {
    this.globalData.userId = null
    this.globalData.userInfo = null
    this.globalData.patientId = null
    wx.removeStorageSync('userId')
    wx.removeStorageSync('userInfo')
    wx.removeStorageSync('patientId')
  }
})
