import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import { fetchCurrentUser, login as loginApi, logout as logoutApi } from '@/api/auth'
import type { LoginRequest, LoginResponse, MenuTreeNode } from '@/types/api'
import { clearToken, getToken, setToken } from '@/utils/request'

const USER_KEY = 'omni_user_profile'
const MENU_KEY = 'omni_user_menus'
const DYNAMIC_KEY = 'omni_dynamic_permission'

/** 动态权限开启时，后台刷新 /me 的最小间隔（毫秒） */
const ME_REFRESH_INTERVAL_MS = 10_000

interface StoredProfile {
  userId: number
  username: string
  jti?: string
  nickname?: string
  realName?: string
  mobile?: string
  email?: string
  gender?: string
  avatarFileId?: number
  avatarUrl?: string
  deptId?: number
  deptName?: string
  posts?: string[]
  dataScope?: string
  roles: string[]
  permissions: string[]
  mustChangePwd?: boolean
}

function loadProfile(): StoredProfile | null {
  const raw = localStorage.getItem(USER_KEY)
  if (!raw) {
    return null
  }
  try {
    return JSON.parse(raw) as StoredProfile
  } catch {
    return null
  }
}

function loadMenus(): MenuTreeNode[] {
  const raw = localStorage.getItem(MENU_KEY)
  if (!raw) {
    return []
  }
  try {
    return JSON.parse(raw) as MenuTreeNode[]
  } catch {
    return []
  }
}

function toStoredProfile(me: {
  userId: number
  username: string
  jti?: string
  nickname?: string
  realName?: string
  mobile?: string
  email?: string
  gender?: string
  avatarFileId?: number
  avatarUrl?: string
  deptId?: number
  deptName?: string
  posts?: string[]
  dataScope?: string
  roles?: string[]
  permissions?: string[]
  mustChangePwd?: boolean
}): StoredProfile {
  return {
    userId: me.userId,
    username: me.username,
    jti: me.jti,
    nickname: me.nickname,
    realName: me.realName,
    mobile: me.mobile,
    email: me.email,
    gender: me.gender,
    avatarFileId: me.avatarFileId,
    avatarUrl: me.avatarUrl,
    deptId: me.deptId,
    deptName: me.deptName,
    posts: me.posts || [],
    dataScope: me.dataScope,
    roles: me.roles || [],
    permissions: me.permissions || [],
    mustChangePwd: !!me.mustChangePwd,
  }
}

export const useUserStore = defineStore('user', () => {
  const token = ref(getToken())
  const profile = ref<StoredProfile | null>(loadProfile())
  const menus = ref<MenuTreeNode[]>(loadMenus())
  const dynamicPermission = ref(localStorage.getItem(DYNAMIC_KEY) === '1')
  const lastMeLoadedAt = ref(0)
  /** 避免动态权限下并发刷 /me */
  let meLoading: Promise<void> | null = null

  const isLoggedIn = computed(() => !!token.value)
  const username = computed(() => profile.value?.username || '')
  const nickname = computed(() => profile.value?.nickname || profile.value?.username || '')
  const avatar = computed(() => profile.value?.avatarUrl || '')
  const displayName = computed(() => profile.value?.realName || profile.value?.nickname || profile.value?.username || '')
  const roles = computed(() => profile.value?.roles || [])
  const permissions = computed(() => profile.value?.permissions || [])
  const dataScope = computed(() => profile.value?.dataScope || '')
  const deptId = computed(() => profile.value?.deptId)
  const mustChangePwd = computed(() => !!profile.value?.mustChangePwd)

  function hasPermission(code: string): boolean {
    return permissions.value.includes(code)
  }

  function hasAnyPermission(codes: string[]): boolean {
    return codes.some((code) => hasPermission(code))
  }

  function persistProfile(next: StoredProfile) {
    profile.value = next
    localStorage.setItem(USER_KEY, JSON.stringify(next))
  }

  function persistMenus(next: MenuTreeNode[]) {
    // 内容不变时不触发侧栏重渲染，避免 el-menu 偶发失去点击响应
    if (JSON.stringify(menus.value) === JSON.stringify(next)) {
      return
    }
    menus.value = next
    localStorage.setItem(MENU_KEY, JSON.stringify(next))
  }

  function persistDynamicPermission(enabled: boolean) {
    dynamicPermission.value = enabled
    localStorage.setItem(DYNAMIC_KEY, enabled ? '1' : '0')
  }

  async function login(payload: LoginRequest): Promise<LoginResponse> {
    const data = await loginApi(payload)
    token.value = data.accessToken
    setToken(data.accessToken)
    persistProfile({
      userId: data.userId,
      username: data.username,
      deptId: data.deptId,
      dataScope: data.dataScope,
      roles: data.roles || [],
      permissions: data.permissions || [],
      mustChangePwd: !!data.mustChangePwd,
    })
    try {
      await loadMe(true)
    } catch (err) {
      // 已写入 token 但会话拉取失败时回滚，避免停在登录页却带脏 token（刷新才「突然」进系统）
      clearSession()
      throw err
    }
    return data
  }

  async function loadMe(force = false): Promise<void> {
    if (!getToken()) {
      return
    }
    const now = Date.now()
    if (!force && dynamicPermission.value && now - lastMeLoadedAt.value < ME_REFRESH_INTERVAL_MS) {
      return
    }
    if (meLoading) {
      if (!force) {
        return meLoading
      }
      await meLoading.catch(() => undefined)
    }
    meLoading = (async () => {
      const me = await fetchCurrentUser()
      persistProfile(toStoredProfile(me))
      persistMenus(me.menus || [])
      persistDynamicPermission(!!me.dynamicPermission)
      lastMeLoadedAt.value = Date.now()
    })()
    try {
      await meLoading
    } finally {
      meLoading = null
    }
  }

  /**
   * 路由守卫用：无本地会话时阻塞拉取；动态权限仅后台刷新，避免 await /me 拖死/取消菜单导航。
   */
  async function ensureSession(): Promise<void> {
    if (!menus.value.length || !profile.value) {
      await loadMe(true)
      return
    }
    if (dynamicPermission.value) {
      void loadMe(false).catch(() => {
        // 后台刷新失败不踢出；下一次导航或强制刷新再试
      })
    }
  }

  /** 仅清理本地登录态（401 / 路由失败时使用，不再请求后端）。 */
  function clearSession(): void {
    token.value = ''
    profile.value = null
    menus.value = []
    dynamicPermission.value = false
    lastMeLoadedAt.value = 0
    meLoading = null
    clearToken()
    localStorage.removeItem(USER_KEY)
    localStorage.removeItem(MENU_KEY)
    localStorage.removeItem(DYNAMIC_KEY)
  }

  /** 主动退出：通知后端使当前令牌失效，再清理本地。 */
  async function logout(): Promise<void> {
    if (getToken()) {
      try {
        await logoutApi()
      } catch {
        // 令牌已失效时忽略
      }
    }
    clearSession()
  }

  return {
    token,
    profile,
    menus,
    dynamicPermission,
    isLoggedIn,
    username,
    nickname,
    avatar,
    displayName,
    roles,
    permissions,
    dataScope,
    deptId,
    mustChangePwd,
    hasPermission,
    hasAnyPermission,
    login,
    loadMe,
    ensureSession,
    clearSession,
    logout,
  }
})
