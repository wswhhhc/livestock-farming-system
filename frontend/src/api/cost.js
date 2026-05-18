import request from './http'

export function getCostList(params) {
  return request.get('/costs', { params })
}

export function createCost(data) {
  return request.post('/costs', data)
}

export function deleteCost(id) {
  return request.delete(`/costs/${id}`)
}

export function autoFeedCost() {
  return request.post('/costs/auto-feed')
}

export function autoMgmtCost() {
  return request.post('/costs/auto-mgmt')
}

export function autoCalcCost(costType, unitCost) {
  return request.post('/costs/auto-calc', null, { params: { costType, unitCost } })
}

export function batchDeleteCost(ids) {
  return request.post('/costs/batch-delete', ids)
}
