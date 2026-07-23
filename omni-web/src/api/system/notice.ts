import type { NoticeView, PageQuery, PageResult } from '@/types/api'
import { deleteData, getData, postData, putData } from '@/utils/request'

export interface NoticeWriteBody {
  title: string
  content: string
  type: string
  status: boolean
}

export function listNotices(params?: PageQuery & {
  keyword?: string
  status?: boolean
  type?: string
}): Promise<PageResult<NoticeView>> {
  return getData<PageResult<NoticeView>>('/system/notices', params)
}

export function getNotice(id: number): Promise<NoticeView> {
  return getData<NoticeView>(`/system/notices/${id}`)
}

export function createNotice(body: NoticeWriteBody): Promise<NoticeView> {
  return postData<NoticeView>('/system/notices', body)
}

export function updateNotice(id: number, body: NoticeWriteBody): Promise<NoticeView> {
  return putData<NoticeView>(`/system/notices/${id}`, body)
}

export function changeNoticeStatus(id: number, status: boolean): Promise<NoticeView> {
  return putData<NoticeView>(`/system/notices/${id}/status`, { status })
}

export function removeNotice(id: number): Promise<void> {
  return deleteData(`/system/notices/${id}`)
}

export function fetchUnreadNotices(): Promise<NoticeView[]> {
  return getData<NoticeView[]>('/system/notices/unread')
}

export function fetchUnreadNoticeCount(): Promise<{ count: number }> {
  return getData<{ count: number }>('/system/notices/unread-count')
}

export function fetchNoticeInbox(): Promise<NoticeView[]> {
  return getData<NoticeView[]>('/system/notices/inbox')
}

export function markNoticeRead(id: number): Promise<void> {
  return putData(`/system/notices/${id}/read`)
}

export function markAllNoticesRead(): Promise<void> {
  return putData('/system/notices/read-all')
}
