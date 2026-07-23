import { computed, ref, type Ref } from 'vue'
import { listDictOptions } from '@/api/system/dict'
import type { DictOption } from '@/types/api'

const cache = new Map<string, DictOption[]>()
const inflight = new Map<string, Promise<DictOption[]>>()

async function loadOptions(typeCode: string, force = false): Promise<DictOption[]> {
  const key = typeCode.trim()
  if (!key) {
    return []
  }
  if (!force && cache.has(key)) {
    return cache.get(key) || []
  }
  const pending = inflight.get(key)
  if (pending) {
    return pending
  }
  const task = listDictOptions(key)
    .then((rows) => {
      const options = rows || []
      cache.set(key, options)
      return options
    })
    .finally(() => {
      inflight.delete(key)
    })
  inflight.set(key, task)
  return task
}

/**
 * 字典选项 composable：按 typeCode 拉取并缓存（如 sys_gender）。
 */
export function useDict(typeCode: string) {
  const options: Ref<DictOption[]> = ref(cache.get(typeCode) || [])
  const loading = ref(false)
  const loaded = ref(cache.has(typeCode))

  async function load(force = false): Promise<DictOption[]> {
    loading.value = true
    try {
      const rows = await loadOptions(typeCode, force)
      options.value = rows
      loaded.value = true
      return rows
    } finally {
      loading.value = false
    }
  }

  function labelOf(value?: string | null): string {
    if (value == null || value === '') {
      return ''
    }
    const hit = options.value.find((item) => item.value === value)
    return hit?.label || value
  }

  function optionOf(value?: string | null): DictOption | undefined {
    if (value == null || value === '') {
      return undefined
    }
    return options.value.find((item) => item.value === value)
  }

  const defaultValue = computed(() => {
    const hit = options.value.find((item) => item.defaultFlag)
    return hit?.value || options.value[0]?.value || ''
  })

  // 有缓存时同步可用；无缓存时后台拉取
  if (!cache.has(typeCode)) {
    void load()
  }

  return {
    options,
    loading,
    loaded,
    defaultValue,
    load,
    labelOf,
    optionOf,
  }
}

/** 测试或字典变更后清空前端缓存。 */
export function clearDictCache(typeCode?: string): void {
  if (typeCode) {
    cache.delete(typeCode.trim())
    return
  }
  cache.clear()
}
