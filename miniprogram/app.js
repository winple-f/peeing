App({
  globalData: {
    userInfo: null,
    patientId: null,
    userId: null,
    registerDate: null
  },

  onLaunch() {
    const userInfo = wx.getStorageSync('userInfo')
    const patientId = wx.getStorageSync('patientId')
    const userId = wx.getStorageSync('userId')
    if (userInfo) {
      this.globalData.userInfo = userInfo
    }
    if (patientId) {
      this.globalData.patientId = patientId
    }
    if (userId) {
      this.globalData.userId = userId
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

  getUserId() {
    if (this.globalData.userId !== null) {
      return this.globalData.userId
    }
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

  setLogin(userId, userInfo) {
    this.globalData.userId = userId
    this.globalData.userInfo = userInfo
    wx.setStorageSync('userId', userId)
    wx.setStorageSync('userInfo', userInfo)
  },

  checkLogin() {
    const userId = this.getUserId()
    if (userId === null) {
      wx.redirectTo({ url: '/pages/login/login' })
      return false
    }
    return true
  },

  clearLogin() {
    this.globalData.userId = null
    this.globalData.userInfo = null
    this.globalData.patientId = null
    wx.removeStorageSync('userId')
    wx.removeStorageSync('userInfo')
    wx.removeStorageSync('patientId')
    wx.removeStorageSync('registerDate')
  }
})
