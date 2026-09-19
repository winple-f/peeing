const app = getApp()

Page({
  data: {
    username: '',
    nickname: '',
    password: '',
    confirmPassword: ''
  },

  onUsernameInput(e) {
    this.setData({ username: e.detail.value })
  },

  onNicknameInput(e) {
    this.setData({ nickname: e.detail.value })
  },

  onPasswordInput(e) {
    this.setData({ password: e.detail.value })
  },

  onConfirmPasswordInput(e) {
    this.setData({ confirmPassword: e.detail.value })
  },

  handleRegister() {
    const { username, nickname, password, confirmPassword } = this.data

    if (!username) {
      wx.showToast({ title: '请输入用户名', icon: 'none' })
      return
    }
    if (!nickname) {
      wx.showToast({ title: '请输入昵称', icon: 'none' })
      return
    }
    if (!password) {
      wx.showToast({ title: '请输入密码', icon: 'none' })
      return
    }
    if (password !== confirmPassword) {
      wx.showToast({ title: '两次输入的密码不一致', icon: 'none' })
      return
    }

    const users = wx.getStorageSync('users') || []
    if (users.find(u => u.username === username)) {
      wx.showToast({ title: '该用户名已被注册', icon: 'none' })
      return
    }

    const patientId = app.generatePatientId()
    const userId = Date.now()
    const registerDate = Date.now()

    const newUser = {
      id: userId,
      username,
      nickname,
      password,
      patientNumber: patientId,
      registerDate
    }
    users.push(newUser)
    wx.setStorageSync('users', users)
    wx.setStorageSync('registerDate', registerDate)

    app.setLogin(userId, { username, nickname })

    wx.showToast({ title: '注册成功！编号：' + patientId, icon: 'none', duration: 3000 })
    setTimeout(() => {
      wx.reLaunch({ url: '/pages/index/index' })
    }, 2000)
  },

  goLogin() {
    wx.navigateBack()
  }
})
