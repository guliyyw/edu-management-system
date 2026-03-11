import { post } from '../utils/request'
import type { UserInfo } from '../stores/auth'

export interface LoginRequest {
  username: string
  password: string
}

export interface LoginResponse {
  token: string
  user: UserInfo
}

export const login = (data: LoginRequest) => {
  return post<LoginResponse>('/auth/login', data)
}

export const getCurrentUser = () => {
  return post<UserInfo>('/auth/me')
}
