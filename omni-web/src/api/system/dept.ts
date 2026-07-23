import type { DeptView } from '@/types/api'
import { deleteData, downloadBlob, getData, postData, putData } from '@/utils/request'

export function fetchDeptTree(): Promise<DeptView[]> {
  return getData<DeptView[]>('/system/depts/tree')
}

export function exportDepts(): Promise<void> {
  return downloadBlob('/system/depts/export', undefined, '部门数据.xlsx')
}

export function createDept(body: {
  parentId: number
  name: string
  sort: number
  status: boolean
}): Promise<DeptView> {
  return postData<DeptView>('/system/depts', body)
}

export function updateDept(
  id: number,
  body: { parentId: number; name: string; sort: number; status: boolean },
): Promise<DeptView> {
  return putData<DeptView>(`/system/depts/${id}`, body)
}

export function removeDept(id: number): Promise<void> {
  return deleteData(`/system/depts/${id}`)
}
