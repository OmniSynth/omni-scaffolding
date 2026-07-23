import type { LoginLogView, PageQuery, PageResult } from '@/types/api'
import { deleteData, getData } from '@/utils/request'

export function searchLoginLogs(params?: PageQuery & {
  username?: string
  status?: string
  ip?: string
}): Promise<PageResult<LoginLogView>> {
  return getData<PageResult<LoginLogView>>('/system/login-logs', params)
}

export function removeLoginLog(id: number): Promise<void> {
  return deleteData(`/system/login-logs/${id}`)
}
