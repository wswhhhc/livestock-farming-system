import request from './http'

export function getAdviceList(params) {
  return request.get('/advice', { params })
}

export function generateAdvice() {
  return request.post('/advice/generate', null, { timeout: 120000 })
}

export function markAdviceRead(id) {
  return request.put(`/advice/${id}/read`)
}

export function deleteAdvice(id) {
  return request.delete(`/advice/${id}`)
}

export function getUnreadCount() {
  return request.get('/advice/unread-count')
}
