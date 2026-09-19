const app = getApp()

Page({
  data: {
    canLogin: true
  },

  handleLogin() {
    if (!this.data.canLogin) return
    this.setData({ canLogin: false })

    wx.showLoading({ title: '正在登录...' })

    wx.getUserProfile({
      desc: '用于完善患者资料',
      success: (res) => {
        const userInfo = res.userInfo
        const patientId = app.generatePatientId()
        wx.setStorageSync('userInfo', userInfo)
        wx.setStorageSync('registerDate', Date.now())

        app.globalData.userInfo = userInfo

        wx.hideLoading()
        wx.showToast({ title: '登录成功', icon: 'success' })

        setTimeout(() => {
          wx.reLaunch({ url: '/pages/index/index' })
        }, 1000)
      },
      fail: () => {
        const patientId = app.generatePatientId()
        wx.setStorageSync('registerDate', Date.now())

        wx.hideLoading()
        wx.showToast({ title: '登录成功', icon: 'success' })

        setTimeout(() => {
          wx.reLaunch({ url: '/pages/index/index' })
        }, 1000)
      },
      complete: () => {
        this.setData({ canLogin: true })
      }
    })
  }
})
