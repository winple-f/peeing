const app = getApp()
const storage = require('../../utils/storage.js')
const util = require('../../utils/util.js')

Page({
  data: {
    patientId: '',
    recordCount: 0,
    surveyCount: 0,
    dateRange: '',
    exportTypes: { urination: true, survey: true },
    csvContent: '',
    csvPreview: ''
  },

  onLoad() {
    const patientId = app.getPatientId() || ''
    const records = storage.getAllUrinationRecords()
    const surveys = storage.getSurveyRecords()
    let dateRange = '暂无记录'
    if (records.length > 0) {
      const times = records.map(r => r.recordTime).sort()
      dateRange = util.formatDate(times[0]) + ' 至 ' + util.formatDate(times[times.length - 1])
    }
    this.setData({
      patientId,
      recordCount: records.length,
      surveyCount: surveys.length,
      dateRange
    })
  },

  toggleExportType(e) {
    const type = e.currentTarget.dataset.type
    const exportTypes = this.data.exportTypes
    exportTypes[type] = !exportTypes[type]
    this.setData({ exportTypes })
  },

  generateCSV() {
    if (!this.data.exportTypes.urination && !this.data.exportTypes.survey) {
      wx.showToast({ title: '请至少选择一项', icon: 'none' })
      return
    }

    let csv = ''
    const patientId = this.data.patientId

    if (this.data.exportTypes.urination) {
      csv += '患者编号,记录时间,尿量,伴随症状,漏尿评分,尿急评分,尿痛评分,尿线变细评分,间断排尿评分,夜间排尿评分,备注\n'
      const records = storage.getAllUrinationRecords().sort((a, b) => a.recordTime - b.recordTime)
      records.forEach(r => {
        const time = util.formatTime(r.recordTime)
        const volume = util.escapeCsv(r.urineVolume)
        const symptoms = util.escapeCsv((r.symptoms || []).join('、'))
        const scores = r.symptomScores || {}
        const leakScore = scores['漏尿'] || ''
        const urgeScore = scores['尿急'] || ''
        const painScore = scores['尿痛'] || ''
        const thinScore = scores['尿线变细'] || ''
        const interruptScore = scores['间断排尿'] || ''
        const nightScore = scores['夜间排尿'] || ''
        const note = util.escapeCsv(r.note || '')
        csv += `${patientId},${time},${volume},${symptoms},${leakScore},${urgeScore},${painScore},${thinScore},${interruptScore},${nightScore},${note}\n`
      })
      csv += '\n'
    }

    if (this.data.exportTypes.survey) {
      csv += '患者编号,评估时间,量表类型,评分,严重程度,备注\n'
      const typeMap = { 'iciq': 'ICIQ-SF', 'ipss': 'IPSS', 'qol': 'QoL' }
      const surveys = storage.getSurveyRecords().sort((a, b) => a.createdAt - b.createdAt)
      surveys.forEach(r => {
        const time = util.formatTime(r.createdAt)
        const type = typeMap[r.type] || r.type
        const score = r.score
        const label = util.escapeCsv(r.label || '')
        csv += `${patientId},${time},${type},${score},${label},\n`
      })
    }

    const preview = csv.length > 1000 ? csv.substring(0, 1000) + '\n...' : csv
    this.setData({
      csvContent: csv,
      csvPreview: preview
    })
    wx.showToast({ title: '生成成功', icon: 'success' })
  },

  copyToClipboard() {
    if (!this.data.csvContent) return
    wx.setClipboardData({
      data: this.data.csvContent,
      success: () => {
        wx.showToast({ title: '已复制到剪贴板', icon: 'success' })
      }
    })
  }
})
