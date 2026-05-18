import request from './http'

export function getRevenueEstimate(params) {
  return request.get('/revenue/estimate', { params })
}
