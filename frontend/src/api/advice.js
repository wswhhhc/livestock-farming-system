import request from './http'

export function getAdviceList(params) {
  return request.get('/advice', { params })
}

export function generateAdvice() {
  return request.post('/advice/generate')
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

export function getAdviceTemplates(params) {
  return request.get('/advice/templates', { params })
}

export function createAdviceTemplate(data) {
  return request.post('/advice/templates', data)
}

export function updateAdviceTemplate(id, data) {
  return request.put(`/advice/templates/${id}`, data)
}

export function deleteAdviceTemplate(id) {
  return request.delete(`/advice/templates/${id}`)
}
