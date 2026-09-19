const ICIQ_SF_QUESTIONS = [
  { id: 0, text: '您大约多久漏尿一次？', options: ['从不漏尿', '一周不到一次', '一周一两次', '每天一两次', '每天两次以上', '一直漏尿'], scores: [0, 1, 2, 3, 4, 5] },
  { id: 1, text: '您每次漏尿大概有多少？', options: ['没有漏尿', '少量（几滴）', '中等量', '大量'], scores: [0, 2, 4, 6] },
  { id: 2, text: '您觉得漏尿对日常生活影响有多大？', options: ['没有影响', '影响很小', '有一定影响', '影响较大', '影响很大', '影响非常大'], scores: [0, 1, 2, 3, 4, 5, 6].slice(0, 6) },
  { id: 3, text: '您每天通常漏尿几次？（请填写数字）', type: 'input', score: 0 }
]

const IPSS_QUESTIONS = [
  { id: 0, text: '过去一个月，小便排不尽的感觉有多频繁？', options: ['从不', '少于一半时间', '约一半时间', '多于一半时间', '几乎总是'], scores: [0, 1, 2, 3, 4] },
  { id: 1, text: '过去一个月，不到两小时就想小便有多频繁？', options: ['从不', '少于一半时间', '约一半时间', '多于一半时间', '几乎总是'], scores: [0, 1, 2, 3, 4] },
  { id: 2, text: '过去一个月，小便时断断续续有多频繁？', options: ['从不', '少于一半时间', '约一半时间', '多于一半时间', '几乎总是'], scores: [0, 1, 2, 3, 4] },
  { id: 3, text: '过去一个月，憋尿困难有多频繁？', options: ['从不', '少于一半时间', '约一半时间', '多于一半时间', '几乎总是'], scores: [0, 1, 2, 3, 4] },
  { id: 4, text: '过去一个月，尿线变细有多频繁？', options: ['从不', '少于一半时间', '约一半时间', '多于一半时间', '几乎总是'], scores: [0, 1, 2, 3, 4] },
  { id: 5, text: '过去一个月，需要使劲排尿有多频繁？', options: ['从不', '少于一半时间', '约一半时间', '多于一半时间', '几乎总是'], scores: [0, 1, 2, 3, 4] },
  { id: 6, text: '过去一个月，夜间起夜小便几次？', options: ['0次', '1次', '2次', '3次', '4次或更多'], scores: [0, 1, 2, 3, 4, 5].slice(0, 5) }
]

const QOL_QUESTION = {
  id: 0,
  text: '如果在今后的生活中一直保持现在的小便情况，您觉得怎么样？',
  options: ['非常满意', '比较满意', '感觉一般', '比较不满', '很不满意', '非常不满意'],
  scores: [0, 1, 2, 3, 4, 5]
}

function calcIciqScore(answers) {
  let total = 0
  for (let i = 0; i < 3; i++) {
    if (answers[i] !== undefined && answers[i] !== -1) {
      total += ICIQ_SF_QUESTIONS[i].scores[answers[i]] || 0
    }
  }
  return total
}

function calcIpssScore(answers) {
  let total = 0
  for (let i = 0; i < 7; i++) {
    if (answers[i] !== undefined && answers[i] !== -1) {
      total += IPSS_QUESTIONS[i].scores[answers[i]] || 0
    }
  }
  return total
}

function getIpssSeverity(score) {
  if (score <= 7) return '轻度'
  if (score <= 19) return '中度'
  return '重度'
}

function calcQolScore(answer) {
  if (answer !== undefined && answer !== -1) {
    return QOL_QUESTION.scores[answer] || 0
  }
  return 0
}

module.exports = {
  ICIQ_SF_QUESTIONS,
  IPSS_QUESTIONS,
  QOL_QUESTION,
  calcIciqScore,
  calcIpssScore,
  getIpssSeverity,
  calcQolScore
}
