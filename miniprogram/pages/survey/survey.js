const app = getApp()
const storage = require('../../utils/storage.js')
const surveyUtil = require('../../utils/survey.js')

Page({
  data: {
    step: 0,
    surveyType: '',
    questions: [],
    totalSteps: 0,
    currentQuestion: null,
    currentAnswer: -1,
    inputValue: '',
    answers: [],
    progressPercent: 0,
    totalScore: 0,
    resultLabel: '',
    resultDesc: ''
  },

  selectSurvey(e) {
    const type = e.currentTarget.dataset.type
    let questions = []
    if (type === 'iciq') {
      questions = surveyUtil.ICIQ_SF_QUESTIONS
    } else if (type === 'ipss') {
      questions = surveyUtil.IPSS_QUESTIONS
    } else if (type === 'qol') {
      questions = [surveyUtil.QOL_QUESTION]
    }
    this.setData({
      step: 1,
      surveyType: type,
      questions,
      totalSteps: questions.length,
      currentQuestion: questions[0],
      currentAnswer: -1,
      inputValue: '',
      answers: new Array(questions.length).fill(-1),
      progressPercent: 100 / questions.length
    })
  },

  selectOption(e) {
    const idx = e.currentTarget.dataset.idx
    this.setData({ currentAnswer: idx })
  },

  onInput(e) {
    this.setData({ inputValue: e.detail.value })
  },

  prevQuestion() {
    if (this.data.step <= 1) return
    this.saveCurrentAnswer()
    const newStep = this.data.step - 1
    this.setData({
      step: newStep,
      currentQuestion: this.data.questions[newStep - 1],
      currentAnswer: this.data.answers[newStep - 1],
      inputValue: this.data.answers[newStep - 1] !== -1 ? String(this.data.answers[newStep - 1]) : '',
      progressPercent: (newStep / this.data.totalSteps) * 100
    })
  },

  nextQuestion() {
    if (this.data.currentAnswer === -1 && this.data.currentQuestion.type !== 'input') {
      return
    }
    this.saveCurrentAnswer()
    const newStep = this.data.step + 1
    this.setData({
      step: newStep,
      currentQuestion: this.data.questions[newStep - 1],
      currentAnswer: this.data.answers[newStep - 1] !== undefined ? this.data.answers[newStep - 1] : -1,
      inputValue: this.data.answers[newStep - 1] !== -1 && this.data.questions[newStep - 1].type === 'input' ? String(this.data.answers[newStep - 1]) : '',
      progressPercent: (newStep / this.data.totalSteps) * 100
    })
  },

  saveCurrentAnswer() {
    const answers = this.data.answers
    if (this.data.currentQuestion.type === 'input') {
      answers[this.data.step - 1] = parseInt(this.data.inputValue) || 0
    } else {
      answers[this.data.step - 1] = this.data.currentAnswer
    }
    this.setData({ answers })
  },

  submitSurvey() {
    this.saveCurrentAnswer()
    const { surveyType, answers } = this.data
    let score = 0
    let label = ''
    let desc = ''

    if (surveyType === 'iciq') {
      score = surveyUtil.calcIciqScore(answers)
      if (score === 0) {
        desc = '未发现明显漏尿症状'
      } else if (score <= 7) {
        label = '轻度'
        desc = '漏尿对生活影响较小，建议继续观察'
      } else if (score <= 14) {
        label = '中度'
        desc = '漏尿对生活有一定影响，建议咨询医生'
      } else {
        label = '重度'
        desc = '漏尿对生活影响较大，建议尽快就医'
      }
    } else if (surveyType === 'ipss') {
      score = surveyUtil.calcIpssScore(answers)
      label = surveyUtil.getIpssSeverity(score)
      if (score <= 7) {
        desc = '前列腺症状较轻，建议定期复查'
      } else if (score <= 19) {
        desc = '前列腺症状中等，建议咨询医生是否需要治疗'
      } else {
        desc = '前列腺症状较重，建议尽快就医评估'
      }
    } else if (surveyType === 'qol') {
      score = surveyUtil.calcQolScore(answers[0])
      if (score <= 1) {
        desc = '对目前排尿情况满意'
      } else if (score <= 3) {
        desc = '对排尿情况有一定不满，建议关注'
      } else {
        desc = '对排尿情况很不满意，建议就医'
      }
    }

    const record = {
      type: surveyType,
      answers,
      score,
      label,
      createdAt: Date.now()
    }
    storage.addSurveyRecord(record)

    this.setData({
      step: -1,
      totalScore: score,
      resultLabel: label,
      resultDesc: desc
    })
  },

  backToHome() {
    wx.navigateBack()
  },

  redoSurvey() {
    this.setData({
      step: 0,
      surveyType: '',
      questions: [],
      currentQuestion: null,
      currentAnswer: -1,
      inputValue: '',
      answers: [],
      totalScore: 0,
      resultLabel: '',
      resultDesc: ''
    })
  }
})
