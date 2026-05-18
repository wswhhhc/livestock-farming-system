import request from './http'

export function getDashboard() {
  return request.get('/dashboard')
}

export function getCategoryDist() {
  return request.get('/dashboard/category-dist')
}

export function getCostTrend(months = 6) {
  return request.get('/dashboard/cost-trend', { params: { months } })
}

export function getSiteUtil() {
  return request.get('/dashboard/site-util')
}

export function getStageDist() {
  return request.get('/dashboard/stage-dist')
}
