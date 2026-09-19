const STORAGE_KEYS = {
  URINATION_RECORDS: 'urination_records',
  SURVEY_RECORDS: 'survey_records',
  USER_INFO: 'userInfo',
  PATIENT_ID: 'patientId',
  REGISTER_DATE: 'registerDate'
}

function getRecords(key) {
  return wx.getStorageSync(key) || []
}

function saveRecords(key, records) {
  wx.setStorageSync(key, records)
}

function addUrinationRecord(record) {
  const records = getRecords(STORAGE_KEYS.URINATION_RECORDS)
  record.id = Date.now()
  record.createdAt = Date.now()
  records.push(record)
  saveRecords(STORAGE_KEYS.URINATION_RECORDS, records)
  return record
}

function updateUrinationRecord(record) {
  const records = getRecords(STORAGE_KEYS.URINATION_RECORDS)
  const idx = records.findIndex(r => r.id === record.id)
  if (idx !== -1) {
    records[idx] = record
    saveRecords(STORAGE_KEYS.URINATION_RECORDS, records)
  }
}

function deleteUrinationRecord(id) {
  const records = getRecords(STORAGE_KEYS.URINATION_RECORDS)
  const filtered = records.filter(r => r.id !== id)
  saveRecords(STORAGE_KEYS.URINATION_RECORDS, filtered)
}

function getUrinationRecordById(id) {
  const records = getRecords(STORAGE_KEYS.URINATION_RECORDS)
  return records.find(r => r.id === id)
}

function getUrinationRecordsByDateRange(start, end) {
  const records = getRecords(STORAGE_KEYS.URINATION_RECORDS)
  return records.filter(r => r.recordTime >= start && r.recordTime < end)
}

function getDailyCount(date) {
  const start = new Date(date)
  start.setHours(0, 0, 0, 0)
  const end = new Date(start)
  end.setDate(end.getDate() + 1)
  return getUrinationRecordsByDateRange(start.getTime(), end.getTime()).length
}

function addSurveyRecord(record) {
  const records = getRecords(STORAGE_KEYS.SURVEY_RECORDS)
  record.id = Date.now()
  record.createdAt = Date.now()
  records.push(record)
  saveRecords(STORAGE_KEYS.SURVEY_RECORDS, records)
  return record
}

function getSurveyRecords() {
  return getRecords(STORAGE_KEYS.SURVEY_RECORDS)
}

function getAllUrinationRecords() {
  return getRecords(STORAGE_KEYS.URINATION_RECORDS)
}

module.exports = {
  STORAGE_KEYS,
  addUrinationRecord,
  updateUrinationRecord,
  deleteUrinationRecord,
  getUrinationRecordById,
  getUrinationRecordsByDateRange,
  getDailyCount,
  addSurveyRecord,
  getSurveyRecords,
  getAllUrinationRecords
}
