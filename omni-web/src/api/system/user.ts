import type { PageQuery, PageResult, UserDetailView } from '@/types/api'
import { deleteData, downloadBlob, getData, postData, putData, uploadData } from '@/utils/request'

export interface UserWriteBody {
  username?: string
  password?: string
  nickname: string
  realName?: string
  mobile?: string
  email?: string
  gender?: string
  avatar?: string
  deptId: number
  postIds?: number[]
  roleIds: number[]
  enabled?: boolean
}

export function listUsers(params?: PageQuery & { keyword?: string }): Promise<PageResult<UserDetailView>> {
  return getData<PageResult<UserDetailView>>('/system/users', params)
}

export function exportUsers(params?: { keyword?: string }): Promise<void> {
  return downloadBlob('/system/users/export', params, '用户数据.xlsx')
}

export function getUser(id: number): Promise<UserDetailView> {
  return getData<UserDetailView>(`/system/users/${id}`)
}

export function createUser(body: UserWriteBody): Promise<UserDetailView> {
  return postData<UserDetailView>('/system/users', body)
}

export function updateUser(id: number, body: UserWriteBody): Promise<UserDetailView> {
  return putData<UserDetailView>(`/system/users/${id}`, body)
}

export function changeUserEnabled(id: number, enabled: boolean): Promise<UserDetailView> {
  return putData<UserDetailView>(`/system/users/${id}/status`, { enabled })
}

export function removeUser(id: number): Promise<void> {
  return deleteData(`/system/users/${id}`)
}

export function resetUserPassword(id: number, password: string): Promise<void> {
  return putData(`/system/users/${id}/password`, { password })
}

export function uploadAvatar(file: File): Promise<{ url: string }> {
  const form = new FormData()
  form.append('file', file)
  return uploadData<{ url: string }>('/system/users/avatar', form)
}
