const app = getApp()
const storage = require('../../utils/storage.js')
const util = require('../../utils/util.js')

Page({
  data: {
    exportRange: 0
  },

  onLoad() {
    if (!app.checkLogin()) return
  },

  onRangeChange(e) {
    this.setData({ exportRange: parseInt(e.detail.value) })
  },

  handleExport() {
    const range = this.data.exportRange
    const urinationRecords = storage.getUrinationRecordsByRange(range)
    const surveyRecords = storage.getSurveyRecords()

    const userInfo = app.globalData.userInfo || {}
    const patientId = app.getPatientId() || ''

    const volumeTexts = ['少量', '中量', '大量']
    let csv = '\uFEFF'

    csv += '患者信息\n'
    csv += '患者编号,' + patientId + '\n'
    csv += '昵称,' + (userInfo.nickname || '') + '\n'
    csv += '\n'

    csv += '排尿记录\n'
    csv += '序号,记录时间,尿量,漏尿,漏尿评分,尿急,尿急评分,尿痛,尿痛评分,尿线变细,尿线变细评分,间断排尿,间断排尿评分,夜间排尿,夜间排尿评分,备注\n'
    urinationRecords.sort((a, b) => a.recordTime - b.recordTime).forEach((r, idx) => {
      csv += (idx + 1) + ','
      csv += util.formatDateTime(r.recordTime) + ','
      csv += (volumeTexts[r.volumeLevel] || '少量') + ','
      csv += (r.hasLeakage ? '是' : '否') + ','
      csv += (r.leakageSeverity || '') + ','
      csv += (r.hasUrgency ? '是' : '否') + ','
      csv += (r.urgencySeverity || '') + ','
      csv += (r.hasPain ? '是' : '否') + ','
      csv += (r.painSeverity || '') + ','
      csv += (r.hasWeakStream ? '是' : '否') + ','
      csv += (r.weakStreamSeverity || '') + ','
      csv += (r.hasIntermittent ? '是' : '否') + ','
      csv += (r.intermittentSeverity || '') + ','
      csv += (r.hasNocturia ? '是' : '否') + ','
      csv += (r.nocturiaSeverity || '') + ','
      csv += util.escapeCsv(r.note || '') + '\n'
    })
    csv += '\n'

    csv += '量表评估\n'
    csv += '时间点,评估日期,ICIQ-Q1,ICIQ-Q2,ICIQ-Q3,ICIQ总分,IPSS-Q1,IPSS-Q2,IPSS-Q3,IPSS-Q4,IPSS-Q5,IPSS-Q6,IPSS-Q7,IPSS总分,QoL评分\n'
    surveyRecords.sort((a, b) => a.surveyDate - b.surveyDate).forEach(r => {
      csv += (r.timePoint || '') + ','
      csv += util.formatDate(r.surveyDate) + ','
      csv += (r.iciqQ1 !== undefined ? r.iciqQ1 : '') + ','
      csv += (r.iciqQ2 !== undefined ? r.iciqQ2 : '') + ','
      csv += (r.iciqQ3 !== undefined ? r.iciqQ3 : '') + ','
      csv += (r.iciqTotalScore || '') + ','
      csv += (r.ipssQ1 !== undefined ? r.ipssQ1 : '') + ','
      csv += (r.ipssQ2 !== undefined ? r.ipssQ2 : '') + ','
      csv += (r.ipssQ3 !== undefined ? r.ipssQ3 : '') + ','
      csv += (r.ipssQ4 !== undefined ? r.ipssQ4 : '') + ','
      csv += (r.ipssQ5 !== undefined ? r.ipssQ5 : '') + ','
      csv += (r.ipssQ6 !== undefined ? r.ipssQ6 : '') + ','
      csv += (r.ipssQ7 !== undefined ? r.ipssQ7 : '') + ','
      csv += (r.ipssTotalScore || '') + ','
      csv += (r.qolScore !== undefined ? r.qolScore : '') + '\n'
    })

    const fileName = '康复数据_' + patientId + '_' + this.getTimeStr() + '.csv'
    const fs = wx.getFileSystemManager()
    const filePath = wx.env.USER_DATA_PATH + '/' + fileName
    fs.writeFileSync(filePath, csv, 'utf8')

    wx.showModal({
      title: '导出成功',
      content: '文件已保存：' + fileName + '\n是否打开分享？',
      confirmText: '分享',
      success: (res) => {
        if (res.confirm) {
          wx.shareFileMessage({
            filePath: filePath,
            success: () => {},
            fail: () => {
              wx.showToast({ title: '分享取消', icon: 'none' })
            }
          })
        }
      }
    })
  },

  getTimeStr() {
    const d = new Date()
    const y = d.getFullYear()
    const m = String(d.getMonth() + 1).padStart(2, '0')
    const day = String(d.getDate()).padStart(2, '0')
    const h = String(d.getHours()).padStart(2, '0')
    const min = String(d.getMinutes()).padStart(2, '0')
    const s = String(d.getSeconds()).padStart(2, '0')
    return y + m + day + '_' + h + min + s
  }
})
