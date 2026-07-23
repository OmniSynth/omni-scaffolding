import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
import { getToken } from '@/utils/request'
import { useUserStore } from '@/stores/user'
import { collectMenuPaths } from '@/utils/menu'

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue'),
    meta: { public: true, title: '登录' },
  },
  {
    path: '/',
    component: () => import('@/layouts/AdminLayout.vue'),
    redirect: '/home',
    children: [
      {
        path: 'home',
        name: 'Home',
        component: () => import('@/views/home/index.vue'),
        meta: { title: '首页' },
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('@/views/profile/index.vue'),
        meta: { title: '个人中心' },
      },
      {
        path: 'system/file',
        name: 'SystemFile',
        component: () => import('@/views/system/file/index.vue'),
        meta: { title: '文件管理', permission: 'system:file:list' },
      },
      {
        path: 'system/user',
        name: 'SystemUser',
        component: () => import('@/views/system/user/index.vue'),
        meta: { title: '用户管理', permission: 'system:user:list' },
      },
      {
        path: 'system/role',
        name: 'SystemRole',
        component: () => import('@/views/system/role/index.vue'),
        meta: { title: '角色管理', permission: 'system:role:list' },
      },
      {
        path: 'system/dept',
        name: 'SystemDept',
        component: () => import('@/views/system/dept/index.vue'),
        meta: { title: '部门管理', permission: 'system:dept:list' },
      },
      {
        path: 'system/post',
        name: 'SystemPost',
        component: () => import('@/views/system/post/index.vue'),
        meta: { title: '岗位管理', permission: 'system:post:list' },
      },
      {
        path: 'system/dict',
        name: 'SystemDict',
        component: () => import('@/views/system/dict/index.vue'),
        meta: { title: '数据字典', permission: 'system:dict:list' },
      },
      {
        path: 'system/config',
        name: 'SystemConfig',
        component: () => import('@/views/system/config/index.vue'),
        meta: { title: '系统参数', permission: 'system:config:list' },
      },
      {
        path: 'system/job',
        name: 'SystemJob',
        component: () => import('@/views/system/job/index.vue'),
        meta: { title: '定时任务', permission: 'system:job:list' },
      },
      {
        path: 'system/ip-whitelist',
        name: 'SystemIpWhitelist',
        component: () => import('@/views/system/ip-whitelist/index.vue'),
        meta: { title: 'IP白名单', permission: 'system:ipWhitelist:list' },
      },
      {
        path: 'system/online',
        name: 'SystemOnline',
        component: () => import('@/views/system/online/index.vue'),
        meta: { title: '在线用户', permission: 'system:online:list' },
      },
      {
        path: 'system/notice',
        name: 'SystemNotice',
        component: () => import('@/views/system/notice/index.vue'),
        meta: { title: '通知公告', permission: 'system:notice:list' },
      },
      {
        path: 'system/menu',
        name: 'SystemMenu',
        component: () => import('@/views/system/menu/index.vue'),
        meta: { title: '菜单管理', permission: 'system:menu:list' },
      },
      {
        path: 'log/login-log',
        name: 'LogLogin',
        component: () => import('@/views/system/login-log/index.vue'),
        meta: { title: '登录日志', permission: 'system:loginLog:list' },
      },
      {
        path: 'log/oper-log',
        name: 'LogOper',
        component: () => import('@/views/system/oper-log/index.vue'),
        meta: { title: '操作日志', permission: 'system:operLog:list' },
      },
      {
        path: 'ops/mysql',
        name: 'OpsMysql',
        component: () => import('@/views/ops/mysql/index.vue'),
        meta: { title: 'MySQL 运维', permission: 'ops:mysql:list' },
      },
      {
        path: 'ops/redis',
        name: 'OpsRedis',
        component: () => import('@/views/ops/redis/index.vue'),
        meta: { title: 'Redis 运维', permission: 'ops:redis:list' },
      },
      {
        path: 'ops/server',
        name: 'OpsServer',
        component: () => import('@/views/ops/server/index.vue'),
        meta: { title: '系统详情', permission: 'ops:server:list' },
      },
      {
        path: 'ops/druid',
        name: 'OpsDruid',
        component: () => import('@/views/ops/druid/index.vue'),
        meta: { title: 'Druid监控', permission: 'ops:druid:list' },
      },
      {
        path: 'tool/gen',
        name: 'ToolGen',
        component: () => import('@/views/tool/gen/index.vue'),
        meta: { title: '代码生成', permission: 'tool:gen:list' },
      },
    ],
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

router.beforeEach(async (to) => {
  const token = getToken()
  if (to.meta.public) {
    if (token && to.path === '/login') {
      return { path: '/home' }
    }
    return true
  }
  if (!token) {
    return { path: '/login', query: { redirect: to.fullPath } }
  }

  const userStore = useUserStore()
  try {
    await userStore.ensureSession()
  } catch {
    userStore.clearSession()
    return { path: '/login', query: { redirect: to.fullPath } }
  }

  // 个人中心等无 permission 的页面直接放行
  const permission = typeof to.meta.permission === 'string' ? to.meta.permission : ''
  if (permission && !userStore.hasPermission(permission)) {
    const allowed = collectMenuPaths(userStore.menus)
    if (!allowed.includes(to.path)) {
      return { path: '/home' }
    }
  }
  return true
})

router.afterEach((to) => {
  const appTitle = import.meta.env.VITE_APP_TITLE || 'Omni Admin'
  const pageTitle = typeof to.meta.title === 'string' ? to.meta.title : ''
  document.title = pageTitle ? `${pageTitle} - ${appTitle}` : appTitle
})

export default router
