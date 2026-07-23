import type { MysqlTableView, PageResult } from '@/types/api'
import { downloadBlobPost, getData, postData } from '@/utils/request'

export type GenQueryType = 'NONE' | 'EQ' | 'LIKE' | 'BETWEEN'

export interface GenColumnConfig {
  columnName: string
  columnType: string
  columnComment: string
  javaField: string
  javaType: string
  pk: boolean
  increment: boolean
  nullable: boolean
  list: boolean
  form: boolean
  required: boolean
  queryType: GenQueryType
  audit: boolean
  logicDelete: boolean
}

export interface GenTableConfig {
  tableName: string
  tableComment?: string
  moduleName: string
  businessName: string
  className: string
  functionName: string
  packageName: string
  permissionPrefix: string
  author?: string
  menuParentId: number
  menuIdStart: number
  menuSort?: number
  hasDeleted?: boolean
  extendsAudit?: boolean
  pkField?: string
  pkJavaType?: string
  pkColumn?: string
  columns: GenColumnConfig[]
}

export interface GenFileView {
  path: string
  content: string
}

export function fetchGenTables(params?: {
  keyword?: string
  page?: number
  size?: number
}): Promise<PageResult<MysqlTableView>> {
  return getData<PageResult<MysqlTableView>>('/tool/gen/tables', params)
}

export function fetchGenTableConfig(table: string): Promise<GenTableConfig> {
  return getData<GenTableConfig>(`/tool/gen/tables/${encodeURIComponent(table)}/columns`)
}

export function previewGenCode(config: GenTableConfig): Promise<GenFileView[]> {
  return postData<GenFileView[]>('/tool/gen/preview', config)
}

export function downloadGenCode(config: GenTableConfig): Promise<void> {
  const name = (config.className || 'gen') + '-gen.zip'
  return downloadBlobPost('/tool/gen/download', config, name)
}
