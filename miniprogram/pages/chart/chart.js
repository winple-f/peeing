const app = getApp()
const storage = require('../../utils/storage.js')
const util = require('../../utils/util.js')

Page({
  data: {
    heatmapYear: 0,
    heatmapMonth: 0,
    calendarDays: [],
    chartTab: 0,
    chartTitle: '最近7天排尿次数',
    chartData: [],
    yLabels: [],
    chartSummary: ''
  },

  onLoad() {
    if (!app.checkLogin()) return
    const now = new Date()
    this.setData({
      heatmapYear: now.getFullYear(),
      heatmapMonth: now.getMonth() + 1
    })
    this.buildCalendar()
    this.buildChart(0)
  },

  buildCalendar() {
    const { heatmapYear, heatmapMonth } = this.data
    const firstDay = new Date(heatmapYear, heatmapMonth - 1, 1)
    const daysInMonth = new Date(heatmapYear, heatmapMonth, 0).getDate()
    const startWeekday = firstDay.getDay()
    const records = storage.getAllUrinationRecords()
    const days = []

    for (let i = 0; i < startWeekday; i++) {
      days.push({ empty: true })
    }

    for (let d = 1; d <= daysInMonth; d++) {
      const date = new Date(heatmapYear, heatmapMonth - 1, d)
      const dayStart = new Date(date)
      dayStart.setHours(0, 0, 0, 0)
      const dayEnd = new Date(dayStart)
      dayEnd.setDate(dayEnd.getDate() + 1)

      const dayRecords = records.filter(r => r.recordTime >= dayStart.getTime() && r.recordTime < dayEnd.getTime())
      let scoreSum = 0
      dayRecords.forEach(r => {
        if (r.hasLeakage && r.leakageSeverity) scoreSum += r.leakageSeverity
        if (r.hasUrgency && r.urgencySeverity) scoreSum += r.urgencySeverity
        if (r.hasPain && r.painSeverity) scoreSum += r.painSeverity
        if (r.hasWeakStream && r.weakStreamSeverity) scoreSum += r.weakStreamSeverity
        if (r.hasIntermittent && r.intermittentSeverity) scoreSum += r.intermittentSeverity
        if (r.hasNocturia && r.nocturiaSeverity) scoreSum += r.nocturiaSeverity
      })

      let color = '#F5F5F5'
      if (scoreSum > 0) {
        const intensity = Math.min(scoreSum / 18, 1)
        const r = 255
        const g = Math.round(255 - intensity * 200)
        const b = Math.round(255 - intensity * 200)
        color = `rgb(${r},${g},${b})`
      }

      days.push({ day: d, score: scoreSum, color, empty: false })
    }

    this.setData({ calendarDays: days })
  },

  prevMonth() {
    let m = this.data.heatmapMonth - 1
    let y = this.data.heatmapYear
    if (m < 1) { m = 12; y-- }
    this.setData({ heatmapMonth: m, heatmapYear: y })
    this.buildCalendar()
  },

  nextMonth() {
    let m = this.data.heatmapMonth + 1
    let y = this.data.heatmapYear
    if (m > 12) { m = 1; y++ }
    this.setData({ heatmapMonth: m, heatmapYear: y })
    this.buildCalendar()
  },

  switchTab(e) {
    const tab = parseInt(e.currentTarget.dataset.tab)
    this.setData({ chartTab: tab })
    this.buildChart(tab)
  },

  buildChart(tab) {
    const days = tab === 0 ? 7 : 30
    const records = storage.getAllUrinationRecords()
    const now = new Date()
    now.setHours(0, 0, 0, 0)
    const startDate = new Date(now)
    startDate.setDate(startDate.getDate() - days + 1)

    const chartData = []
    let total = 0
    for (let i = 0; i < days; i++) {
      const date = new Date(startDate)
      date.setDate(date.getDate() + i)
      const dayStart = new Date(date)
      dayStart.setHours(0, 0, 0, 0)
      const dayEnd = new Date(dayStart)
      dayEnd.setDate(dayEnd.getDate() + 1)

      const count = records.filter(r => r.recordTime >= dayStart.getTime() && r.recordTime < dayEnd.getTime()).length
      total += count

      const label = (date.getMonth() + 1) + '/' + date.getDate()
      const height = count > 0 ? Math.max(count * 40, 20) : 4
      chartData.push({ label, count, height })
    }

    const maxCount = Math.max(...chartData.map(d => d.count), 1)
    const yMax = Math.ceil(maxCount / 5) * 5 || 5
    const yLabels = []
    for (let i = 0; i <= yMax; i += Math.max(1, Math.floor(yMax / 4))) {
      yLabels.push(i)
    }

    const avg = (total / days).toFixed(1)
    const title = tab === 0 ? '最近7天排尿次数' : '最近30天排尿次数'
    const summary = `总计 ${total} 次，平均每天 ${avg} 次`

    this.setData({
      chartData,
      yLabels: yLabels.reverse(),
      chartTitle: title,
      chartSummary: summary
    })
  }
})
