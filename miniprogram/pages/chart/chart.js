const storage = require('../../utils/storage.js')
const util = require('../../utils/util.js')

Page({
  data: {
    activeTab: 'heatmap',
    heatmapYear: 0,
    heatmapMonth: 0,
    calendarDays: [],
    linePeriod: 'week',
    totalCount: 0,
    avgCount: 0,
    chartData: []
  },

  onLoad() {
    const now = new Date()
    this.setData({
      heatmapYear: now.getFullYear(),
      heatmapMonth: now.getMonth() + 1
    })
    this.buildHeatmap()
    this.buildLineChart('week')
  },

  onShow() {
    if (this.data.activeTab === 'heatmap') {
      this.buildHeatmap()
    } else {
      this.buildLineChart(this.data.linePeriod)
    }
  },

  switchTab(e) {
    const tab = e.currentTarget.dataset.tab
    this.setData({ activeTab: tab })
    if (tab === 'heatmap') {
      this.buildHeatmap()
    } else {
      this.buildLineChart(this.data.linePeriod)
    }
  },

  prevMonth() {
    let m = this.data.heatmapMonth - 1
    let y = this.data.heatmapYear
    if (m < 1) { m = 12; y-- }
    this.setData({ heatmapMonth: m, heatmapYear: y })
    this.buildHeatmap()
  },

  nextMonth() {
    let m = this.data.heatmapMonth + 1
    let y = this.data.heatmapYear
    if (m > 12) { m = 1; y++ }
    this.setData({ heatmapMonth: m, heatmapYear: y })
    this.buildHeatmap()
  },

  buildHeatmap() {
    const { heatmapYear, heatmapMonth } = this.data
    const firstDay = new Date(heatmapYear, heatmapMonth - 1, 1)
    const lastDay = new Date(heatmapYear, heatmapMonth, 0)
    const startWeekday = firstDay.getDay()
    const daysInMonth = lastDay.getDate()
    const allRecords = storage.getAllUrinationRecords()
    const days = []

    for (let i = 0; i < startWeekday; i++) {
      days.push({ empty: true })
    }

    for (let d = 1; d <= daysInMonth; d++) {
      const dayStart = new Date(heatmapYear, heatmapMonth - 1, d, 0, 0, 0, 0).getTime()
      const dayEnd = new Date(heatmapYear, heatmapMonth - 1, d, 23, 59, 59, 999).getTime()
      const dayRecords = allRecords.filter(r => r.recordTime >= dayStart && r.recordTime <= dayEnd)
      let totalScore = 0
      dayRecords.forEach(r => {
        if (r.symptomScores) {
          Object.values(r.symptomScores).forEach(s => { totalScore += s })
        }
      })
      days.push({
        day: d,
        empty: false,
        score: totalScore,
        color: this.getHeatColor(totalScore)
      })
    }

    this.setData({ calendarDays: days })
  },

  getHeatColor(score) {
    if (score === 0) return '#F5F5F5'
    if (score <= 5) return '#FFF9C4'
    if (score <= 10) return '#FFE082'
    if (score <= 18) return '#FFB74D'
    return '#FF8A65'
  },

  switchPeriod(e) {
    const period = e.currentTarget.dataset.period
    this.setData({ linePeriod: period })
    this.buildLineChart(period)
  },

  buildLineChart(period) {
    const now = new Date()
    let days = period === 'week' ? 7 : 30
    const dataArr = []

    for (let i = days - 1; i >= 0; i--) {
      const date = new Date(now)
      date.setDate(date.getDate() - i)
      date.setHours(0, 0, 0, 0)
      const dayStart = date.getTime()
      const dayEnd = dayStart + 24 * 60 * 60 * 1000
      const count = storage.getUrinationRecordsByDateRange(dayStart, dayEnd).length
      dataArr.push({
        date: util.formatDateShort(date.getTime()),
        count,
        dayStart
      })
    }

    const maxCount = Math.max(...dataArr.map(d => d.count), 1)
    dataArr.forEach(d => {
      d.barWidth = (d.count / maxCount) * 100
    })

    const total = dataArr.reduce((sum, d) => sum + d.count, 0)
    const avg = (total / days).toFixed(1)

    this.setData({
      chartData: dataArr,
      totalCount: total,
      avgCount: avg
    })

    setTimeout(() => { this.drawCanvas(dataArr, maxCount) }, 100)
  },

  drawCanvas(dataArr, maxCount) {
    const ctx = wx.createCanvasContext('lineChart', this)
    const w = 318
    const h = 200
    const padding = 30
    const chartW = w - padding * 2
    const chartH = h - padding * 2
    const stepX = chartW / Math.max(dataArr.length - 1, 1)

    ctx.setFillStyle('#F5F5F5')
    ctx.fillRect(padding, padding, chartW, chartH)

    ctx.setStrokeStyle('#E0E0E0')
    ctx.setLineWidth(1)
    for (let i = 0; i <= 4; i++) {
      const y = padding + (chartH / 4) * i
      ctx.beginPath()
      ctx.moveTo(padding, y)
      ctx.lineTo(w - padding, y)
      ctx.stroke()
    }

    ctx.setStrokeStyle('#2196F3')
    ctx.setLineWidth(3)
    ctx.beginPath()
    dataArr.forEach((d, i) => {
      const x = padding + stepX * i
      const y = padding + chartH - (d.count / maxCount) * chartH
      if (i === 0) {
        ctx.moveTo(x, y)
      } else {
        ctx.lineTo(x, y)
      }
    })
    ctx.stroke()

    ctx.setFillStyle('rgba(33,150,243,0.1)')
    ctx.beginPath()
    dataArr.forEach((d, i) => {
      const x = padding + stepX * i
      const y = padding + chartH - (d.count / maxCount) * chartH
      if (i === 0) {
        ctx.moveTo(x, padding + chartH)
        ctx.lineTo(x, y)
      } else {
        ctx.lineTo(x, y)
      }
    })
    ctx.lineTo(padding + stepX * (dataArr.length - 1), padding + chartH)
    ctx.closePath()
    ctx.fill()

    ctx.setFillStyle('#2196F3')
    dataArr.forEach((d, i) => {
      const x = padding + stepX * i
      const y = padding + chartH - (d.count / maxCount) * chartH
      ctx.beginPath()
      ctx.arc(x, y, 4, 0, 2 * Math.PI)
      ctx.fill()
    })

    if (dataArr.length <= 7) {
      ctx.setFillStyle('#9E9E9E')
      ctx.setFontSize(9)
      dataArr.forEach((d, i) => {
        const x = padding + stepX * i
        ctx.fillText(d.date, x - 12, h - padding + 15)
      })
    } else {
      ctx.setFillStyle('#9E9E9E')
      ctx.setFontSize(9)
      const showIndices = [0, Math.floor(dataArr.length / 3), Math.floor(dataArr.length * 2 / 3), dataArr.length - 1]
      showIndices.forEach(i => {
        const x = padding + stepX * i
        ctx.fillText(dataArr[i].date, x - 12, h - padding + 15)
      })
    }

    ctx.draw()
  }
})
