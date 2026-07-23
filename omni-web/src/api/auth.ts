import type { ApiResponse, CaptchaChallenge, CurrentUserView, LoginRequest, LoginResponse } from '@/types/api'
import { getData, postData, putData, request } from '@/utils/request'
import { buildLoginSignHeaders } from '@/utils/sign'

export function fetchCaptcha(): Promise<CaptchaChallenge> {
  return getData<CaptchaChallenge>('/auth/captcha')
}

export async function login(data: LoginRequest): Promise<LoginResponse> {
  const headers = await buildLoginSignHeaders(data.username, data.password)
  const res = await request.post<ApiResponse<LoginResponse>>('/auth/login', data, { headers })
  return res.data.data
}

export function fetchCurrentUser(): Promise<CurrentUserView> {
  return getData<CurrentUserView>('/auth/me')
}

export function changePassword(body: { oldPassword: string; newPassword: string }): Promise<void> {
  return putData('/auth/password', body)
}

export function logout(): Promise<void> {
  return postData('/auth/logout')
}
