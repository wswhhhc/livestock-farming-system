import request from './http'

export function getConfigs() {
  return request.get('/system/config')
}

export function updateConfig(data) {
  return request.put('/system/config', data)
}
