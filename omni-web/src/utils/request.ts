import axios, { type AxiosError, type AxiosInstance, type AxiosResponse } from 'axios'
import { ElMessage } from 'element-plus'
import type { ApiResponse } from '@/types/api'
import { useUserStore } from '@/stores/user'
import router from '@/router'

const TOKEN_KEY = 'omni_access_token'

export function getToken(): string {
  return localStorage.getItem(TOKEN_KEY) || ''
}

export function setToken(token: string): void {
  localStorage.setItem(TOKEN_KEY, token)
}

export function clearToken(): void {
  localStorage.removeItem(TOKEN_KEY)
}

const request: AxiosInstance = axios.create({
  baseURL: import.meta.env.VITE_API_BASE || '/api',
  timeout: 30_000,
})

request.interceptors.request.use((config) => {
  const token = getToken()
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

request.interceptors.response.use(
  (response: AxiosResponse<ApiResponse>) => {
    const payload = response.data
    if (payload && typeof payload.code === 'number' && payload.code !== 0) {
      ElMessage.error(payload.message || '请求失败')
      if (payload.code === 401) {
        handleUnauthorized()
      }
      return Promise.reject(payload)
    }
    return response
  },
  (error: AxiosError<ApiResponse>) => {
    const status = error.response?.status
    const message = error.response?.data?.message || error.message || '网络异常'
    if (status === 401) {
      handleUnauthorized()
    } else {
      ElMessage.error(message)
    }
    return Promise.reject(error)
  },
)

function handleUnauthorized(): void {
  const userStore = useUserStore()
  userStore.clearSession()
  if (router.currentRoute.value.path !== '/login') {
    router.replace({ path: '/login', query: { redirect: router.currentRoute.value.fullPath } })
  }
}

export async function getData<T>(url: string, params?: object): Promise<T> {
  const res = await request.get<ApiResponse<T>>(url, { params })
  return res.data.data
}

export async function postData<T>(url: string, body?: unknown): Promise<T> {
  const res = await request.post<ApiResponse<T>>(url, body)
  return res.data.data
}

export async function putData<T>(url: string, body?: unknown): Promise<T> {
  const res = await request.put<ApiResponse<T>>(url, body)
  return res.data.data
}

export async function deleteData<T = void>(url: string): Promise<T> {
  const res = await request.delete<ApiResponse<T>>(url)
  return res.data.data
}

/** multipart 上传（勿手动设 Content-Type，交给浏览器带 boundary） */
export async function uploadData<T>(url: string, formData: FormData): Promise<T> {
  const res = await request.post<ApiResponse<T>>(url, formData)
  return res.data.data
}

async function saveBlobResponse(
  res: AxiosResponse,
  filename: string,
  failMessage: string,
): Promise<void> {
  const blob = res.data as Blob
  const contentType = String(res.headers['content-type'] || '')
  if (res.status >= 400 || contentType.includes('application/json')) {
    const text = await blob.text()
    let message = failMessage
    try {
      const payload = JSON.parse(text) as ApiResponse
      message = payload.message || message
      if (payload.code === 401 || res.status === 401) {
        handleUnauthorized()
      }
    } catch {
      // ignore parse error
    }
    ElMessage.error(message)
    return Promise.reject(new Error(message))
  }

  let finalName = filename
  const disposition = String(res.headers['content-disposition'] || '')
  const utf8Match = disposition.match(/filename\*=UTF-8''([^;]+)/i)
  if (utf8Match?.[1]) {
    finalName = decodeURIComponent(utf8Match[1])
  } else {
    const plainMatch = disposition.match(/filename="?([^";]+)"?/i)
    if (plainMatch?.[1]) {
      finalName = plainMatch[1]
    }
  }

  const objectUrl = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = objectUrl
  link.download = finalName
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  URL.revokeObjectURL(objectUrl)
}

/**
 * 下载二进制文件（Excel 等）。失败时若响应为 JSON 业务错误则解析提示。
 */
export async function downloadBlob(url: string, params?: object, filename?: string): Promise<void> {
  const res = await request.get(url, {
    params,
    responseType: 'blob',
    // 绕过 JSON ApiResponse 拦截器对 blob 的误判
    transformResponse: [(data) => data],
    validateStatus: () => true,
  })
  return saveBlobResponse(res, filename || 'download.xlsx', '导出失败')
}

/**
 * POST 下载二进制文件（如代码生成 ZIP）。
 */
export async function downloadBlobPost(url: string, body?: unknown, filename?: string): Promise<void> {
  const res = await request.post(url, body, {
    responseType: 'blob',
    transformResponse: [(data) => data],
    validateStatus: () => true,
  })
  return saveBlobResponse(res, filename || 'download.zip', '下载失败')
}

export { request }
export default request
