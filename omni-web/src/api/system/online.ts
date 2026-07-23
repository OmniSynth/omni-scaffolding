import type { OnlineUserView } from '@/types/api'
import { deleteData, getData } from '@/utils/request'

export function listOnlineUsers(params?: {
  username?: string
  ip?: string
}): Promise<OnlineUserView[]> {
  return getData<OnlineUserView[]>('/system/online-users', params)
}

export function kickOnlineUser(jti: string): Promise<void> {
  return deleteData(`/system/online-users/${encodeURIComponent(jti)}`)
}
