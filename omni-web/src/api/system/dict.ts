import type { DictDataView, DictOption, DictTypeView, PageQuery, PageResult } from '@/types/api'
import { deleteData, downloadBlob, getData, postData, putData } from '@/utils/request'

export interface DictTypeWriteBody {
  code: string
  name: string
  remark?: string
  sort: number
  status: boolean
}

export interface DictDataWriteBody {
  typeCode: string
  label: string
  value: string
  sort: number
  cssClass?: string
  defaultFlag: boolean
  status: boolean
  remark?: string
}

export function listDictTypes(params?: PageQuery & { keyword?: string }): Promise<PageResult<DictTypeView>> {
  return getData<PageResult<DictTypeView>>('/system/dicts/types', params)
}

export function getDictType(id: number): Promise<DictTypeView> {
  return getData<DictTypeView>(`/system/dicts/types/${id}`)
}

export function createDictType(body: DictTypeWriteBody): Promise<DictTypeView> {
  return postData<DictTypeView>('/system/dicts/types', body)
}

export function updateDictType(id: number, body: DictTypeWriteBody): Promise<DictTypeView> {
  return putData<DictTypeView>(`/system/dicts/types/${id}`, body)
}

export function changeDictTypeStatus(id: number, status: boolean): Promise<DictTypeView> {
  return putData<DictTypeView>(`/system/dicts/types/${id}/status`, { status })
}

export function removeDictType(id: number): Promise<void> {
  return deleteData(`/system/dicts/types/${id}`)
}

export function exportDictTypes(params?: { keyword?: string }): Promise<void> {
  return downloadBlob('/system/dicts/types/export', params, '字典类型.xlsx')
}

export function listDictData(
  params: PageQuery & { typeCode: string; keyword?: string },
): Promise<PageResult<DictDataView>> {
  return getData<PageResult<DictDataView>>('/system/dicts/data', params)
}

export function getDictData(id: number): Promise<DictDataView> {
  return getData<DictDataView>(`/system/dicts/data/${id}`)
}

export function createDictData(body: DictDataWriteBody): Promise<DictDataView> {
  return postData<DictDataView>('/system/dicts/data', body)
}

export function updateDictData(id: number, body: DictDataWriteBody): Promise<DictDataView> {
  return putData<DictDataView>(`/system/dicts/data/${id}`, body)
}

export function changeDictDataStatus(id: number, status: boolean): Promise<DictDataView> {
  return putData<DictDataView>(`/system/dicts/data/${id}/status`, { status })
}

export function removeDictData(id: number): Promise<void> {
  return deleteData(`/system/dicts/data/${id}`)
}

export function exportDictData(params: { typeCode: string; keyword?: string }): Promise<void> {
  return downloadBlob('/system/dicts/data/export', params, '字典数据.xlsx')
}

export function listDictOptions(typeCode: string): Promise<DictOption[]> {
  return getData<DictOption[]>(`/system/dicts/options/${encodeURIComponent(typeCode)}`)
}
