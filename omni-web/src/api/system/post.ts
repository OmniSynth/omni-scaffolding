import type { PageQuery, PageResult, PostView } from '@/types/api'
import { deleteData, downloadBlob, getData, postData, putData } from '@/utils/request'

export function listPosts(params?: PageQuery & { keyword?: string }): Promise<PageResult<PostView>> {
  return getData<PageResult<PostView>>('/system/posts', params)
}

export function exportPosts(params?: { keyword?: string }): Promise<void> {
  return downloadBlob('/system/posts/export', params, '岗位数据.xlsx')
}

export function createPost(body: {
  code: string
  name: string
  sort: number
  status: boolean
}): Promise<PostView> {
  return postData<PostView>('/system/posts', body)
}

export function updatePost(
  id: number,
  body: { code: string; name: string; sort: number; status: boolean },
): Promise<PostView> {
  return putData<PostView>(`/system/posts/${id}`, body)
}

export function removePost(id: number): Promise<void> {
  return deleteData(`/system/posts/${id}`)
}
