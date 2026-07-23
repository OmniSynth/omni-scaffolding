import type { PageQuery, PageResult } from '@/types/api'
import { deleteData, getData, postData, putData } from '@/utils/request'

export interface ${functionCamel}View {
<#list viewColumns as col>
  ${col.javaField}<#if col.javaType == "String" || col.javaType == "Instant">?: string<#elseif col.javaType == "Boolean">?: boolean<#elseif col.javaType == "Integer" || col.javaType == "Long" || col.javaType == "BigDecimal">?: number<#else>?: unknown</#if>
<#if col.dictType?? && col.dictType?has_content>
  ${col.javaField}Text?: string
</#if>
</#list>
}

export interface ${functionCamel}WriteBody {
<#list formColumns as col>
  ${col.javaField}<#if col.javaType == "String" || col.javaType == "Instant">?: string<#elseif col.javaType == "Boolean">?: boolean<#elseif col.javaType == "Integer" || col.javaType == "Long" || col.javaType == "BigDecimal">?: number<#else>?: unknown</#if>
</#list>
}

const BASE = '/${cfg.moduleName}/${cfg.businessName}s'

export function list${functionCamel}s(params?: PageQuery & {
<#list queryColumns as col>
<#if col.queryType == "BETWEEN">
  ${col.javaField}From?: string
  ${col.javaField}To?: string
<#else>
  ${col.javaField}?: <#if col.javaType == "Boolean">boolean<#elseif col.javaType == "Integer" || col.javaType == "Long" || col.javaType == "BigDecimal">number<#else>string</#if>
</#if>
</#list>
}): Promise<PageResult<${functionCamel}View>> {
  return getData<PageResult<${functionCamel}View>>(BASE, params)
}

export function get${functionCamel}(id: number): Promise<${functionCamel}View> {
  return getData<${functionCamel}View>(BASE + '/' + id)
}

export function create${functionCamel}(body: ${functionCamel}WriteBody): Promise<${functionCamel}View> {
  return postData<${functionCamel}View>(BASE, body)
}

export function update${functionCamel}(id: number, body: ${functionCamel}WriteBody): Promise<${functionCamel}View> {
  return putData<${functionCamel}View>(BASE + '/' + id, body)
}

export function remove${functionCamel}(id: number): Promise<void> {
  return deleteData(BASE + '/' + id)
}
