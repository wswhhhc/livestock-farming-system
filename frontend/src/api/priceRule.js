import request from './http'

export function getPriceRuleList(params) {
  return request.get('/price-rules', { params })
}

export function createPriceRule(data) {
  return request.post('/price-rules', data)
}

export function updatePriceRule(id, data) {
  return request.put(`/price-rules/${id}`, data)
}

export function deletePriceRule(id) {
  return request.delete(`/price-rules/${id}`)
}
