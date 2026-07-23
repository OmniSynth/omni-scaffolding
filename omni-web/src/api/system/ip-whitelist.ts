import type { IpVisitTodayView, IpWhitelistView, PageQuery, PageResult } from '@/types/api'
import { deleteData, getData, postData, putData } from '@/utils/request'

export interface IpWhitelistWriteBody {
  ipAddr: string
  remark?: string
  status: boolean
}

export function listIpWhitelist(
  params?: PageQuery & { keyword?: string; status?: boolean },
): Promise<PageResult<IpWhitelistView>> {
  return getData<PageResult<IpWhitelistView>>('/system/ip-whitelist', params)
}

export function getIpWhitelist(id: number): Promise<IpWhitelistView> {
  return getData<IpWhitelistView>(`/system/ip-whitelist/${id}`)
}

export function createIpWhitelist(body: IpWhitelistWriteBody): Promise<IpWhitelistView> {
  return postData<IpWhitelistView>('/system/ip-whitelist', body)
}

export function updateIpWhitelist(id: number, body: IpWhitelistWriteBody): Promise<IpWhitelistView> {
  return putData<IpWhitelistView>(`/system/ip-whitelist/${id}`, body)
}

export function changeIpWhitelistStatus(id: number, status: boolean): Promise<IpWhitelistView> {
  return putData<IpWhitelistView>(`/system/ip-whitelist/${id}/status`, { status })
}

export function removeIpWhitelist(id: number): Promise<void> {
  return deleteData(`/system/ip-whitelist/${id}`)
}

export function todayIpVisits(): Promise<IpVisitTodayView> {
  return getData<IpVisitTodayView>('/system/ip-whitelist/visits/today')
}

export function refreshIpWhitelistCache(): Promise<void> {
  return postData('/system/ip-whitelist/cache/refresh')
}
