import type {
  ApiResponse,
  MysqlOverviewView,
  MysqlProcessView,
  MysqlTableDetailView,
  MysqlTableView,
} from '@/types/api'
import { deleteData, getData, postData, request } from '@/utils/request'

export function fetchMysqlOverview(): Promise<MysqlOverviewView> {
  return getData<MysqlOverviewView>('/ops/mysql/overview')
}

export function fetchMysqlTables(keyword?: string): Promise<MysqlTableView[]> {
  return getData<MysqlTableView[]>('/ops/mysql/tables', keyword ? { keyword } : undefined)
}

export function fetchMysqlTableDetail(table: string): Promise<MysqlTableDetailView> {
  return getData<MysqlTableDetailView>(`/ops/mysql/tables/${encodeURIComponent(table)}`)
}

export function createMysqlIndex(body: {
  table: string
  name: string
  columns: string[]
  unique?: boolean
}): Promise<MysqlTableDetailView> {
  return postData<MysqlTableDetailView>('/ops/mysql/indexes', body)
}

export async function dropMysqlIndex(body: {
  table: string
  name: string
}): Promise<MysqlTableDetailView> {
  const res = await request.delete<ApiResponse<MysqlTableDetailView>>('/ops/mysql/indexes', {
    data: body,
  })
  return res.data.data
}

export function analyzeMysqlTable(table: string): Promise<{ table: string; messages: unknown[] }> {
  return postData(`/ops/mysql/tables/${encodeURIComponent(table)}/analyze`)
}

export function fetchMysqlProcesses(): Promise<MysqlProcessView[]> {
  return getData<MysqlProcessView[]>('/ops/mysql/processes')
}

export function killMysqlProcess(id: number): Promise<{ killed: number }> {
  return deleteData<{ killed: number }>(`/ops/mysql/processes/${id}`)
}
