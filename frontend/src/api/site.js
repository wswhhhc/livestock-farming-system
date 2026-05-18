import request from './http'

export function getSiteList() {
  return request.get('/sites')
}

export function getSiteById(id) {
  return request.get(`/sites/${id}`)
}

export function createSite(data) {
  return request.post('/sites', data)
}

export function updateSite(id, data) {
  return request.put(`/sites/${id}`, data)
}

export function deleteSite(id) {
  return request.delete(`/sites/${id}`)
}
