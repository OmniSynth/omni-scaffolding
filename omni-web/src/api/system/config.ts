import type { ConfigView, PageQuery, PageResult } from '@/types/api'
import { deleteData, downloadBlob, getData, postData, putData } from '@/utils/request'

export interface ConfigWriteBody {
  configKey: string
  configName: string
  configValue?: string
  remark?: string
  sort: number
  status: boolean
}

export function listConfigs(params?: PageQuery & { keyword?: string }): Promise<PageResult<ConfigView>> {
  return getData<PageResult<ConfigView>>('/system/configs', params)
}

export function getConfig(id: number): Promise<ConfigView> {
  return getData<ConfigView>(`/system/configs/${id}`)
}

export function getConfigValue(configKey: string): Promise<string | null> {
  return getData<string | null>(`/system/configs/value/${encodeURIComponent(configKey)}`)
}

export function createConfig(body: ConfigWriteBody): Promise<ConfigView> {
  return postData<ConfigView>('/system/configs', body)
}

export function updateConfig(id: number, body: ConfigWriteBody): Promise<ConfigView> {
  return putData<ConfigView>(`/system/configs/${id}`, body)
}

export function changeConfigStatus(id: number, status: boolean): Promise<ConfigView> {
  return putData<ConfigView>(`/system/configs/${id}/status`, { status })
}

export function removeConfig(id: number): Promise<void> {
  return deleteData(`/system/configs/${id}`)
}

export function exportConfigs(params?: { keyword?: string }): Promise<void> {
  return downloadBlob('/system/configs/export', params, '系统参数.xlsx')
}

export function refreshConfigCache(): Promise<void> {
  return postData('/system/configs/cache/refresh')
}
