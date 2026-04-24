import { http } from './http'
import type { ApiResponse } from '../types'
import type { LoginRequest, RegisterRequest, LoginResponse, UserInfo } from '../types/auth'

export const authApi = {
  login: (data: LoginRequest) =>
    http.post<ApiResponse<LoginResponse>>('/auth/login', data),

  register: (data: RegisterRequest) =>
    http.post<ApiResponse<void>>('/auth/register', data),

  me: () =>
    http.get<ApiResponse<UserInfo>>('/auth/me'),
}
