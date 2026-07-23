import type { ApiResponse, RedisInfoView, RedisKeyDetailView, RedisKeyView } from '@/types/api'
import { getData, postData, putData, request } from '@/utils/request'

export function fetchRedisInfo(): Promise<RedisInfoView> {
  return getData<RedisInfoView>('/ops/redis/info')
}

export function scanRedisKeys(params?: {
  pattern?: string
  limit?: number
}): Promise<RedisKeyView[]> {
  return getData<RedisKeyView[]>('/ops/redis/keys', params)
}

export function fetchRedisKeyDetail(key: string): Promise<RedisKeyDetailView> {
  return getData<RedisKeyDetailView>('/ops/redis/key', { key })
}

export function setRedisString(body: {
  key: string
  value: string
  ttlSeconds?: number | null
}): Promise<RedisKeyDetailView> {
  return postData<RedisKeyDetailView>('/ops/redis/string', body)
}

export function expireRedisKey(body: {
  key: string
  ttlSeconds: number
}): Promise<RedisKeyView> {
  return putData<RedisKeyView>('/ops/redis/expire', body)
}

export async function deleteRedisKeys(keys: string[]): Promise<{ deleted: number }> {
  const res = await request.delete<ApiResponse<{ deleted: number }>>('/ops/redis/keys', {
    data: { keys },
  })
  return res.data.data
}
