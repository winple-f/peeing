const app = getApp()
const storage = require('../../utils/storage.js')

Page({
  data: {
    currentStep: 0,
    selectedTimePoint: 'T0基线',
    iciqQ1: -1,
    iciqQ2: -1,
    iciqQ3: -1,
    ipssAnswers: [-1, -1, -1, -1, -1, -1, -1],
    qolScore: -1,
    iciqTotalScore: 0,
    ipssTotalScore: 0,
    ipssSeverity: '轻度',
    iciqQ1Options: ['从不漏尿', '大约每周1次或更少', '每周2~3次', '每天1次', '每天好几次', '一直漏尿'],
    iciqQ2Options: ['不漏尿', '少量（几滴）', '中等量（内裤湿一片）', '大量（全湿了）'],
    iciqQ3Options: ['0分 - 完全没影响', '2分 - 有一点影响', '4分 - 有些影响', '6分 - 影响中等', '8分 - 影响比较大', '10分 - 严重影响生活'],
    ipssQuestions: [
      { text: '排完尿后，是否经常感觉没有排干净？', options: ['无', '很少', '少半', '约半', '多半', '总是'] },
      { text: '两次排尿间隔是否经常不到2小时？', options: ['无', '很少', '少半', '约半', '多半', '总是'] },
      { text: '排尿时是否有断断续续的情况？', options: ['无', '很少', '少半', '约半', '多半', '总是'] },
      { text: '有尿意时是否憋不住，需要马上去厕所？', options: ['无', '很少', '少半', '约半', '多半', '总是'] },
      { text: '尿线是否变细了？', options: ['无', '很少', '少半', '约半', '多半', '总是'] },
      { text: '排尿时是否需要用力才能开始？', options: ['无', '很少', '少半', '约半', '多半', '总是'] },
      { text: '晚上睡觉后，一般要起来尿几次？', options: ['0次', '1次', '2次', '3次', '4次', '≥5次'] }
    ],
    qolOptions: ['非常高兴', '满意', '大致满意', '还可以', '不太满意', '苦恼', '很糟糕']
  },

  onLoad() {
    if (!app.checkLogin()) return
  },

  selectTimePoint(e) {
    this.setData({ selectedTimePoint: e.currentTarget.dataset.point })
  },

  onIciqQ1(e) {
    this.setData({ iciqQ1: parseInt(e.detail.value) })
  },

  onIciqQ2(e) {
    this.setData({ iciqQ2: parseInt(e.detail.value) })
  },

  onIciqQ3(e) {
    this.setData({ iciqQ3: parseInt(e.detail.value) })
  },

  onIpssChange(e) {
    const idx = e.currentTarget.dataset.idx
    const val = parseInt(e.detail.value)
    const answers = this.data.ipssAnswers
    answers[idx] = val
    this.setData({ ipssAnswers: answers })
  },

  onQolChange(e) {
    this.setData({ qolScore: parseInt(e.detail.value) })
  },

  goPrev() {
    if (this.data.currentStep > 0) {
      this.setData({ currentStep: this.data.currentStep - 1 })
    }
  },

  goNext() {
    const step = this.data.currentStep
    if (step < 4) {
      if (!this.validateStep(step)) return
      if (step === 3) {
        this.submitSurvey()
        return
      }
      this.setData({ currentStep: step + 1 })
    } else {
      wx.navigateBack()
    }
  },

  validateStep(step) {
    if (step === 0) return true
    if (step === 1) {
      if (this.data.iciqQ1 === -1 || this.data.iciqQ2 === -1 || this.data.iciqQ3 === -1) {
        wx.showToast({ title: '请完成所有问题', icon: 'none' })
        return false
      }
      return true
    }
    if (step === 2) {
      for (let i = 0; i < 7; i++) {
        if (this.data.ipssAnswers[i] === -1) {
          wx.showToast({ title: '请完成所有问题', icon: 'none' })
          return false
        }
      }
      return true
    }
    if (step === 3) {
      if (this.data.qolScore === -1) {
        wx.showToast({ title: '请选择一个答案', icon: 'none' })
        return false
      }
      return true
    }
    return true
  },

  submitSurvey() {
    const iciqTotal = this.data.iciqQ1 + this.data.iciqQ2 + (this.data.iciqQ3 * 2)
    const iciqScores = [0, 1, 2, 3, 4, 5]
    const iciqQ3Scores = [0, 2, 4, 6, 8, 10]
    const realIciqTotal = iciqScores[this.data.iciqQ1] + iciqScores[this.data.iciqQ2] + iciqQ3Scores[this.data.iciqQ3]

    let ipssTotal = 0
    for (let i = 0; i < 7; i++) {
      ipssTotal += this.data.ipssAnswers[i]
    }

    const severity = ipssTotal <= 7 ? '轻度' : (ipssTotal <= 19 ? '中度' : '重度')

    const record = {
      timePoint: this.data.selectedTimePoint,
      surveyDate: Date.now(),
      iciqQ1: this.data.iciqQ1,
      iciqQ2: this.data.iciqQ2,
      iciqQ3: this.data.iciqQ3,
      iciqTotalScore: realIciqTotal,
      ipssQ1: this.data.ipssAnswers[0],
      ipssQ2: this.data.ipssAnswers[1],
      ipssQ3: this.data.ipssAnswers[2],
      ipssQ4: this.data.ipssAnswers[3],
      ipssQ5: this.data.ipssAnswers[4],
      ipssQ6: this.data.ipssAnswers[5],
      ipssQ7: this.data.ipssAnswers[6],
      ipssTotalScore: ipssTotal,
      qolScore: this.data.qolScore
    }

    storage.addSurveyRecord(record)

    this.setData({
      currentStep: 4,
      iciqTotalScore: realIciqTotal,
      ipssTotalScore: ipssTotal,
      ipssSeverity: severity
    })

    wx.showToast({ title: '问卷已提交', icon: 'success' })
  }
})
