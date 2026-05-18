import request from './http'

export function login(data) {
  return request.post('/auth/login', data, { noAuth: true })
}

export function getCurrentUser() {
  return request.get('/auth/me')
}
