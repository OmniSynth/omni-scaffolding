import type { OperLogView, PageQuery, PageResult } from '@/types/api'
import { deleteData, getData } from '@/utils/request'

export function searchOperLogs(params?: PageQuery & {
  username?: string
  module?: string
  status?: string
}): Promise<PageResult<OperLogView>> {
  return getData<PageResult<OperLogView>>('/system/oper-logs', params)
}

export function removeOperLog(id: number): Promise<void> {
  return deleteData(`/system/oper-logs/${id}`)
}
