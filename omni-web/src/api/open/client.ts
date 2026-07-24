import type { OpenClientCredentialsView, OpenClientView, PageQuery, PageResult } from '@/types/api'
import { deleteData, getData, postData, putData } from '@/utils/request'

export function listOpenClients(
  params?: PageQuery & { keyword?: string; status?: boolean },
): Promise<PageResult<OpenClientView>> {
  return getData<PageResult<OpenClientView>>('/open/admin/clients', params)
}

export function getOpenClient(id: number): Promise<OpenClientView> {
  return getData<OpenClientView>(`/open/admin/clients/${id}`)
}

export function createOpenClient(body: {
  name: string
  dailyLimit?: number | null
  qpsLimit?: number | null
  expireAt?: string | null
  remark?: string
  status: boolean
  ipList?: string[]
  endpointIds?: number[]
}): Promise<OpenClientCredentialsView> {
  return postData<OpenClientCredentialsView>('/open/admin/clients', body)
}

export function updateOpenClient(
  id: number,
  body: {
    name: string
    dailyLimit?: number | null
    qpsLimit?: number | null
    expireAt?: string | null
    remark?: string
    status: boolean
    ipList?: string[]
    endpointIds?: number[]
  },
): Promise<OpenClientView> {
  return putData<OpenClientView>(`/open/admin/clients/${id}`, body)
}

export function resetOpenClientKeys(id: number): Promise<OpenClientCredentialsView> {
  return postData<OpenClientCredentialsView>(`/open/admin/clients/${id}/reset-keys`)
}

export function removeOpenClient(id: number): Promise<void> {
  return deleteData(`/open/admin/clients/${id}`)
}
