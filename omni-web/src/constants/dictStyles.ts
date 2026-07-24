/**
 * 字典回显样式色板（写入 sys_dict_data.css_class，供 DictTag 使用）。
 * 含 Element Plus 语义色 + 扩展色，便于业务区分状态。
 */
export interface DictStyleOption {
  /** 存库值；空串表示默认描边灰 */
  value: string
  label: string
  /** 文字色 */
  color: string
  /** 浅底色 */
  bg: string
  /** 边框色 */
  border: string
}

export const DICT_STYLE_OPTIONS: DictStyleOption[] = [
  { value: '', label: '默认', color: '#64748b', bg: '#f8fafc', border: '#e2e8f0' },
  { value: 'primary', label: '主题蓝', color: '#2563eb', bg: '#eff6ff', border: '#93c5fd' },
  { value: 'success', label: '成功绿', color: '#16a34a', bg: '#f0fdf4', border: '#86efac' },
  { value: 'warning', label: '警告橙', color: '#d97706', bg: '#fffbeb', border: '#fcd34d' },
  { value: 'danger', label: '危险红', color: '#dc2626', bg: '#fef2f2', border: '#fca5a5' },
  { value: 'info', label: '信息灰', color: '#475569', bg: '#f1f5f9', border: '#cbd5e1' },
  { value: 'cyan', label: '青色', color: '#0891b2', bg: '#ecfeff', border: '#67e8f9' },
  { value: 'teal', label: '青绿', color: '#0d9488', bg: '#f0fdfa', border: '#5eead4' },
  { value: 'lime', label: '青柠', color: '#65a30d', bg: '#f7fee7', border: '#bef264' },
  { value: 'yellow', label: '明黄', color: '#ca8a04', bg: '#fefce8', border: '#fde047' },
  { value: 'amber', label: '琥珀', color: '#b45309', bg: '#fff7ed', border: '#fdba74' },
  { value: 'orange', label: '橘红', color: '#ea580c', bg: '#fff7ed', border: '#fdba74' },
  { value: 'rose', label: '玫红', color: '#e11d48', bg: '#fff1f2', border: '#fda4af' },
  { value: 'pink', label: '粉色', color: '#db2777', bg: '#fdf2f8', border: '#f9a8d4' },
  { value: 'fuchsia', label: '紫红', color: '#c026d3', bg: '#fdf4ff', border: '#f0abfc' },
  { value: 'purple', label: '紫色', color: '#7c3aed', bg: '#f5f3ff', border: '#c4b5fd' },
  { value: 'violet', label: '紫罗兰', color: '#6d28d9', bg: '#f5f3ff', border: '#a78bfa' },
  { value: 'indigo', label: '靛蓝', color: '#4f46e5', bg: '#eef2ff', border: '#a5b4fc' },
  { value: 'sky', label: '天蓝', color: '#0284c7', bg: '#f0f9ff', border: '#7dd3fc' },
  { value: 'slate', label: '石板', color: '#334155', bg: '#f8fafc', border: '#94a3b8' },
  { value: 'brown', label: '棕色', color: '#92400e', bg: '#fef3c7', border: '#d6a56a' },
]

const STYLE_MAP = new Map(DICT_STYLE_OPTIONS.map((o) => [o.value, o]))

export function resolveDictStyle(cssClass?: string | null): DictStyleOption {
  const key = (cssClass || '').trim().toLowerCase()
  return STYLE_MAP.get(key) || DICT_STYLE_OPTIONS[0]
}

export function normalizeDictCssClass(cssClass?: string | null): string {
  const key = (cssClass || '').trim().toLowerCase()
  return STYLE_MAP.has(key) ? key : ''
}

export function dictStyleInline(cssClass?: string | null): Record<string, string> {
  const s = resolveDictStyle(cssClass)
  return {
    color: s.color,
    backgroundColor: s.bg,
    borderColor: s.border,
  }
}
