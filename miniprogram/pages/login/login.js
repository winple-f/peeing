const app = getApp()

Page({
  data: {
    username: '',
    password: '',
    canLogin: true
  },

  onShow() {
    if (app.getUserId() !== null) {
      wx.reLaunch({ url: '/pages/index/index' })
    }
  },

  onUsernameInput(e) {
    this.setData({ username: e.detail.value })
  },

  onPasswordInput(e) {
    this.setData({ password: e.detail.value })
  },

  handleLogin() {
    if (!this.data.canLogin) return
    const { username, password } = this.data

    if (!username) {
      wx.showToast({ title: '请输入用户名', icon: 'none' })
      return
    }
    if (!password) {
      wx.showToast({ title: '请输入密码', icon: 'none' })
      return
    }

    this.setData({ canLogin: false })
    wx.showLoading({ title: '登录中...' })

    const users = wx.getStorageSync('users') || []
    const user = users.find(u => u.username === username && u.password === password)

    wx.hideLoading()

    if (user) {
      app.setLogin(user.id, { username: user.username, nickname: user.nickname })
      wx.showToast({ title: '登录成功', icon: 'success' })
      setTimeout(() => {
        wx.reLaunch({ url: '/pages/index/index' })
      }, 1000)
    } else {
      wx.showToast({ title: '用户名或密码错误', icon: 'none' })
      this.setData({ canLogin: true })
    }
  },

  goRegister() {
    wx.navigateTo({ url: '/pages/register/register' })
  }
})
