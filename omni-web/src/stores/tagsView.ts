import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import type { RouteLocationNormalizedLoaded } from 'vue-router'

export interface TagView {
  path: string
  fullPath: string
  title: string
  name?: string | null
  /** 固定标签（如首页）不可关闭 */
  affix?: boolean
}

const HOME: TagView = {
  path: '/home',
  fullPath: '/home',
  title: '首页',
  name: 'Home',
  affix: true,
}

function toTag(route: RouteLocationNormalizedLoaded): TagView | null {
  if (route.meta.public || route.path === '/login') {
    return null
  }
  const title = typeof route.meta.title === 'string' ? route.meta.title : ''
  if (!title) {
    return null
  }
  return {
    path: route.path,
    fullPath: route.fullPath,
    title,
    name: typeof route.name === 'string' ? route.name : null,
    affix: route.path === '/home',
  }
}

export const useTagsViewStore = defineStore('tagsView', () => {
  const visited = ref<TagView[]>([{ ...HOME }])
  /** 各页刷新计数，仅提高当前 path 的 key，避免冲掉其它页缓存 */
  const pathKeys = ref<Record<string, number>>({})
  const excludeNames = ref<string[]>([])

  const paths = computed(() => visited.value.map((t) => t.path))

  function viewKey(path: string) {
    return `${path}__${pathKeys.value[path] || 0}`
  }

  function addView(route: RouteLocationNormalizedLoaded) {
    const tag = toTag(route)
    if (!tag) {
      return
    }
    const idx = visited.value.findIndex((t) => t.path === tag.path)
    if (idx >= 0) {
      visited.value[idx] = { ...visited.value[idx], ...tag, affix: visited.value[idx].affix }
      return
    }
    visited.value.push(tag)
  }

  function delView(path: string): TagView | undefined {
    const target = visited.value.find((t) => t.path === path)
    if (!target || target.affix) {
      return undefined
    }
    visited.value = visited.value.filter((t) => t.path !== path)
    const next = { ...pathKeys.value }
    delete next[path]
    pathKeys.value = next
    return target
  }

  function delOthers(path: string) {
    visited.value = visited.value.filter((t) => t.affix || t.path === path)
    const keep = new Set(visited.value.map((t) => t.path))
    const next: Record<string, number> = {}
    for (const [p, k] of Object.entries(pathKeys.value)) {
      if (keep.has(p)) {
        next[p] = k
      }
    }
    pathKeys.value = next
  }

  function delAll() {
    visited.value = visited.value.filter((t) => t.affix)
    pathKeys.value = {}
  }

  function refreshView(path: string, routeName?: string | null) {
    if (routeName) {
      excludeNames.value = [routeName]
    }
    pathKeys.value = {
      ...pathKeys.value,
      [path]: (pathKeys.value[path] || 0) + 1,
    }
    requestAnimationFrame(() => {
      excludeNames.value = []
    })
  }

  function reset() {
    visited.value = [{ ...HOME }]
    pathKeys.value = {}
    excludeNames.value = []
  }

  return {
    visited,
    pathKeys,
    excludeNames,
    paths,
    viewKey,
    addView,
    delView,
    delOthers,
    delAll,
    refreshView,
    reset,
  }
})
