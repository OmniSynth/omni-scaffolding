import type { OpenEndpointView, PageQuery, PageResult } from '@/types/api'
import { deleteData, getData, postData, putData } from '@/utils/request'

export function listOpenEndpoints(
  params?: PageQuery & { keyword?: string; status?: boolean },
): Promise<PageResult<OpenEndpointView>> {
  return getData<PageResult<OpenEndpointView>>('/open/admin/endpoints', params)
}

export function listEnabledOpenEndpoints(): Promise<OpenEndpointView[]> {
  return getData<OpenEndpointView[]>('/open/admin/endpoints/enabled')
}

export function createOpenEndpoint(body: {
  code: string
  name: string
  httpMethod: string
  pathPattern: string
  remark?: string
  status: boolean
}): Promise<OpenEndpointView> {
  return postData<OpenEndpointView>('/open/admin/endpoints', body)
}

export function updateOpenEndpoint(
  id: number,
  body: {
    code: string
    name: string
    httpMethod: string
    pathPattern: string
    remark?: string
    status: boolean
  },
): Promise<OpenEndpointView> {
  return putData<OpenEndpointView>(`/open/admin/endpoints/${id}`, body)
}

export function removeOpenEndpoint(id: number): Promise<void> {
  return deleteData(`/open/admin/endpoints/${id}`)
}
