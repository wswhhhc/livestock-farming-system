import request from './http'

export function getBatchList(params) {
  return request.get('/batches', { params })
}

export function getBatchById(id) {
  return request.get(`/batches/${id}`)
}

export function getNextBatchNo(categoryId) {
  return request.get('/batches/next-no', { params: { categoryId } })
}

export function createBatch(data) {
  return request.post('/batches', data)
}

export function updateBatch(id, data) {
  return request.put(`/batches/${id}`, data)
}

export function deleteBatch(id) {
  return request.delete(`/batches/${id}`)
}

export function getBatchChanges(batchId) {
  return request.get(`/batches/${batchId}/changes`)
}

export function addBatchChange(batchId, data) {
  return request.post(`/batches/${batchId}/changes`, data)
}

export function deleteBatchChange(batchId, changeId) {
  return request.delete(`/batches/${batchId}/changes/${changeId}`)
}
