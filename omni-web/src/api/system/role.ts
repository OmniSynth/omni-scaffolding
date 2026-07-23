import type { PageQuery, PageResult, RoleView } from '@/types/api'
import { deleteData, downloadBlob, getData, postData, putData } from '@/utils/request'

export interface RoleWriteBody {
  code: string
  name: string
  dataScope: string
  status: boolean
  menuIds: number[]
}

export function listRoles(params?: PageQuery): Promise<PageResult<RoleView>> {
  return getData<PageResult<RoleView>>('/system/roles', params)
}

export function exportRoles(): Promise<void> {
  return downloadBlob('/system/roles/export', undefined, '角色数据.xlsx')
}

export function getRole(id: number): Promise<RoleView> {
  return getData<RoleView>(`/system/roles/${id}`)
}

export function createRole(body: RoleWriteBody): Promise<RoleView> {
  return postData<RoleView>('/system/roles', body)
}

export function updateRole(id: number, body: RoleWriteBody): Promise<RoleView> {
  return putData<RoleView>(`/system/roles/${id}`, body)
}

export function changeRoleStatus(id: number, status: boolean): Promise<RoleView> {
  return putData<RoleView>(`/system/roles/${id}/status`, { status })
}

export function removeRole(id: number): Promise<void> {
  return deleteData(`/system/roles/${id}`)
}
