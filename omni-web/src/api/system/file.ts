import type { PageQuery, PageResult } from '@/types/api'
import { deleteData, getData, uploadData } from '@/utils/request'

export interface FileView {
  id: number
  originalName: string
  contentType?: string
  sizeBytes: number
  storageType: string
  ossProvider?: string
  objectKey: string
  bizType: string
  md5?: string
  createdBy?: number
  createdAt?: string
  previewUrl?: string
}

export interface FilePreviewUrlView {
  fileId: number
  url: string
  expire: number
}

export function listFiles(
  params?: PageQuery & {
    keyword?: string
    bizType?: string
    storageType?: string
    contentTypePrefix?: string
  },
): Promise<PageResult<FileView>> {
  return getData<PageResult<FileView>>('/system/files', params)
}

export function getFile(id: number): Promise<FileView> {
  return getData<FileView>(`/system/files/${id}`)
}

export function uploadFile(file: File, bizType = 'common'): Promise<FileView> {
  const form = new FormData()
  form.append('file', file)
  return uploadData<FileView>(`/system/files?bizType=${encodeURIComponent(bizType)}`, form)
}

export function getFilePreviewUrl(id: number): Promise<FilePreviewUrlView> {
  return getData<FilePreviewUrlView>(`/system/files/${id}/preview-url`)
}

export function removeFile(id: number): Promise<void> {
  return deleteData(`/system/files/${id}`)
}
