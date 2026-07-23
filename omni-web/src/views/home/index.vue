<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import {
  Collection,
  Document,
  FolderOpened,
  Link,
  Monitor,
  Setting,
  Timer,
  User,
  UserFilled,
} from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { fetchUnreadNotices, markNoticeRead } from '@/api/system/notice'
import { fetchServerRuntime } from '@/api/ops/server'
import type { NoticeView, ServerRuntimeView } from '@/types/api'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

interface Shortcut {
  title: string
  desc: string
  path: string
  permission: string
  icon: typeof User
}

const shortcuts: Shortcut[] = [
  { title: '用户管理', desc: '账号、角色与部门', path: '/system/user', permission: 'system:user:list', icon: User },
  { title: '角色管理', desc: '权限与数据范围', path: '/system/role', permission: 'system:role:list', icon: UserFilled },
  { title: '文件管理', desc: '统一上传与预览', path: '/system/file', permission: 'system:file:list', icon: FolderOpened },
  { title: '定时任务', desc: 'Cron 与执行日志', path: '/system/job', permission: 'system:job:list', icon: Timer },
  { title: '数据字典', desc: '枚举与下拉选项', path: '/system/dict', permission: 'system:dict:list', icon: Collection },
  { title: '系统参数', desc: '运行时配置项', path: '/system/config', permission: 'system:config:list', icon: Setting },
  { title: '通知公告', desc: '发布与管理公告', path: '/system/notice', permission: 'system:notice:list', icon: Document },
  { title: '系统详情', desc: 'JVM / 连接池 / Redis', path: '/ops/server', permission: 'ops:server:list', icon: Monitor },
]

const visibleShortcuts = computed(() =>
  shortcuts.filter((item) => userStore.hasPermission(item.permission)),
)

const dataScopeLabel = computed(() => {
  switch (userStore.dataScope) {
    case 'ALL':
      return '全部数据'
    case 'DEPT_AND_CHILD':
      return '本部门及以下'
    case 'DEPT':
      return '本部门'
    case 'SELF':
      return '仅本人'
    default:
      return userStore.dataScope || '-'
  }
})

const greeting = computed(() => {
  const hour = new Date().getHours()
  if (hour < 6) return '夜深了'
  if (hour < 12) return '上午好'
  if (hour < 18) return '下午好'
  return '晚上好'
})

const noticesLoading = ref(false)
const notices = ref<NoticeView[]>([])
const noticeDialogVisible = ref(false)
const activeNotice = ref<NoticeView | null>(null)

const canViewRuntime = computed(() => userStore.hasPermission('ops:server:query'))
const runtimeLoading = ref(false)
const runtime = ref<ServerRuntimeView | null>(null)

function formatBytes(bytes?: number | null): string {
  if (bytes == null || bytes < 0) return '-'
  if (bytes === 0) return '0 B'
  const units = ['B', 'KB', 'MB', 'GB', 'TB']
  const i = Math.min(Math.floor(Math.log(bytes) / Math.log(1024)), units.length - 1)
  const value = bytes / 1024 ** i
  return `${value.toFixed(i === 0 ? 0 : 2)} ${units[i]}`
}

function formatPercent(used?: number, max?: number): string {
  if (used == null || max == null || max <= 0) return '-'
  return `${((used / max) * 100).toFixed(1)}%`
}

function formatUptime(ms?: number): string {
  if (ms == null) return '-'
  const sec = Math.floor(ms / 1000)
  const d = Math.floor(sec / 86400)
  const h = Math.floor((sec % 86400) / 3600)
  const m = Math.floor((sec % 3600) / 60)
  if (d > 0) return `${d}天 ${h}时`
  if (h > 0) return `${h}时 ${m}分`
  return `${m}分`
}

function typeLabel(type?: string): string {
  if (type === 'ANNOUNCE') return '公告'
  return '通知'
}

async function loadNotices() {
  noticesLoading.value = true
  try {
    notices.value = await fetchUnreadNotices()
  } catch {
    notices.value = []
  } finally {
    noticesLoading.value = false
  }
}

async function loadRuntime() {
  if (!canViewRuntime.value) {
    runtime.value = null
    return
  }
  runtimeLoading.value = true
  try {
    runtime.value = await fetchServerRuntime()
  } catch {
    runtime.value = null
  } finally {
    runtimeLoading.value = false
  }
}

async function openNotice(row: NoticeView) {
  activeNotice.value = row
  noticeDialogVisible.value = true
  try {
    await markNoticeRead(row.id)
    notices.value = notices.value.filter((n) => n.id !== row.id)
  } catch {
    ElMessage.warning('标记已读失败，可稍后在铃铛中重试')
  }
}

function go(path: string) {
  router.push(path)
}

onMounted(async () => {
  await Promise.all([loadNotices(), loadRuntime()])
})
</script>

<template>
  <div class="home">
    <el-card shadow="never" class="hero">
      <div class="hero-main">
        <div class="hero-brand">
          <img class="hero-logo" src="/favicon.png" alt="Omni Admin" width="48" height="48" />
          <div>
            <h2>{{ greeting }}，{{ userStore.displayName || userStore.username }}</h2>
            <p class="desc">
              数据范围：{{ dataScopeLabel }}
              <template v-if="userStore.profile?.deptName"> · 部门：{{ userStore.profile.deptName }}</template>
            </p>
            <div class="roles">
              <el-tag v-for="role in userStore.roles" :key="role" size="small" class="tag">{{ role }}</el-tag>
              <el-text v-if="!userStore.roles.length" type="info" size="small">暂无角色</el-text>
            </div>
          </div>
        </div>
        <div class="hero-actions">
          <el-button @click="go('/profile')">个人中心</el-button>
          <el-button v-permission="'system:notice:list'" @click="go('/system/notice')">公告管理</el-button>
        </div>
      </div>
      <p class="tip">
        <el-icon><Link /></el-icon>
        二次开发请遵循仓库根目录 <code>AGENTS.md</code>
        ；接口文档见
        <a href="http://localhost:8080/swagger-ui.html" target="_blank" rel="noopener">Swagger UI</a>
      </p>
    </el-card>

    <el-card shadow="never" class="section" header="快捷入口">
      <el-row v-if="visibleShortcuts.length" :gutter="12">
        <el-col v-for="item in visibleShortcuts" :key="item.path" :xs="12" :sm="8" :md="6">
          <button type="button" class="shortcut" @click="go(item.path)">
            <el-icon class="shortcut-icon"><component :is="item.icon" /></el-icon>
            <div class="shortcut-text">
              <div class="shortcut-title">{{ item.title }}</div>
              <div class="shortcut-desc">{{ item.desc }}</div>
            </div>
          </button>
        </el-col>
      </el-row>
      <el-empty v-else description="暂无可用快捷入口（由菜单权限控制）" :image-size="64" />
    </el-card>

    <el-row :gutter="16" class="section-row">
      <el-col :xs="24" :lg="canViewRuntime ? 14 : 24">
        <el-card v-loading="noticesLoading" shadow="never" class="section">
          <template #header>
            <div class="card-header">
              <span>未读公告</span>
              <el-button link type="primary" @click="loadNotices">刷新</el-button>
            </div>
          </template>
          <el-table v-if="notices.length" :data="notices" size="small" @row-click="openNotice">
            <el-table-column prop="type" label="类型" width="88">
              <template #default="{ row }">
                <el-tag size="small" :type="row.type === 'ANNOUNCE' ? 'warning' : 'info'">
                  {{ typeLabel(row.type) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="title" label="标题" min-width="160" show-overflow-tooltip />
            <el-table-column prop="publishTime" label="发布时间" width="170" />
          </el-table>
          <el-empty v-else description="暂无未读公告" :image-size="64" />
        </el-card>
      </el-col>

      <el-col v-if="canViewRuntime" :xs="24" :lg="10">
        <el-card v-loading="runtimeLoading" shadow="never" class="section">
          <template #header>
            <div class="card-header">
              <span>运行态</span>
              <el-button link type="primary" @click="go('/ops/server')">详情</el-button>
            </div>
          </template>
          <template v-if="runtime">
            <el-row :gutter="10">
              <el-col :span="8">
                <div class="metric">
                  <div class="metric-label">JVM 堆</div>
                  <div class="metric-value">
                    {{ formatPercent(runtime.memory?.heapUsed, runtime.memory?.heapMax) }}
                  </div>
                  <div class="metric-sub">
                    {{ formatBytes(runtime.memory?.heapUsed) }} / {{ formatBytes(runtime.memory?.heapMax) }}
                  </div>
                </div>
              </el-col>
              <el-col :span="8">
                <div class="metric">
                  <div class="metric-label">数据源</div>
                  <div class="metric-value">
                    <template v-if="runtime.dataSource?.available">
                      {{ runtime.dataSource.activeConnections ?? '-' }}/{{ runtime.dataSource.maximumPoolSize ?? '-' }}
                    </template>
                    <template v-else>-</template>
                  </div>
                  <div class="metric-sub">活跃 / 最大连接</div>
                </div>
              </el-col>
              <el-col :span="8">
                <div class="metric">
                  <div class="metric-label">Redis</div>
                  <div class="metric-value">
                    {{ runtime.redis?.available ? runtime.redis.usedMemoryHuman || 'OK' : '-' }}
                  </div>
                  <div class="metric-sub">
                    {{ runtime.redis?.available ? `keys ${runtime.redis.dbSize ?? '-'}` : '不可用' }}
                  </div>
                </div>
              </el-col>
            </el-row>
            <p class="runtime-meta">
              运行 {{ formatUptime(runtime.app?.uptimeMs) }}
              · profile {{ (runtime.app?.activeProfiles || []).join(',') || '-' }}
            </p>
          </template>
          <el-empty v-else description="暂无法采集运行态" :image-size="64" />
        </el-card>
      </el-col>
    </el-row>

    <el-dialog v-model="noticeDialogVisible" :title="activeNotice?.title || '公告'" width="560px" destroy-on-close>
      <el-descriptions v-if="activeNotice" :column="1" size="small" border>
        <el-descriptions-item label="类型">{{ typeLabel(activeNotice.type) }}</el-descriptions-item>
        <el-descriptions-item label="发布时间">{{ activeNotice.publishTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="内容">
          <div class="notice-content">{{ activeNotice.content }}</div>
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<style scoped>
.home {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.hero h2 {
  margin: 0 0 8px;
  color: #0f172a;
  font-size: 22px;
  font-weight: 600;
}

.hero-main {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  align-items: flex-start;
}

.hero-brand {
  display: flex;
  align-items: flex-start;
  gap: 14px;
  min-width: 0;
}

.hero-logo {
  flex-shrink: 0;
  width: 48px;
  height: 48px;
  border-radius: 10px;
  object-fit: contain;
}

.desc {
  margin: 0 0 10px;
  color: #64748b;
}

.roles {
  display: flex;
  flex-wrap: wrap;
  gap: 0;
}

.tag {
  margin: 0 8px 8px 0;
}

.tip {
  display: flex;
  align-items: center;
  gap: 6px;
  margin: 14px 0 0;
  padding-top: 12px;
  border-top: 1px solid #e2e8f0;
  color: #64748b;
  font-size: 13px;
}

.tip a {
  color: #2563eb;
  text-decoration: none;
}

.tip a:hover {
  text-decoration: underline;
}

.tip code {
  padding: 0 4px;
  border-radius: 4px;
  background: #f1f5f9;
  color: #334155;
  font-size: 12px;
}

.section-row {
  margin: 0 !important;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.shortcut {
  width: 100%;
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
  padding: 14px 12px;
  border: 1px solid #e2e8f0;
  border-radius: 10px;
  background: #fff;
  text-align: left;
  cursor: pointer;
  transition: border-color 0.15s ease, box-shadow 0.15s ease;
}

.shortcut:hover {
  border-color: #93c5fd;
  box-shadow: 0 1px 2px rgba(15, 23, 42, 0.06);
}

.shortcut-icon {
  font-size: 22px;
  color: #2563eb;
}

.shortcut-title {
  color: #0f172a;
  font-weight: 600;
}

.shortcut-desc {
  margin-top: 2px;
  color: #94a3b8;
  font-size: 12px;
}

.metric {
  padding: 12px;
  border-radius: 10px;
  background: #f8fafc;
  min-height: 96px;
}

.metric-label {
  color: #64748b;
  font-size: 12px;
}

.metric-value {
  margin-top: 8px;
  color: #0f172a;
  font-size: 20px;
  font-weight: 600;
  word-break: break-all;
}

.metric-sub {
  margin-top: 6px;
  color: #94a3b8;
  font-size: 12px;
}

.runtime-meta {
  margin: 12px 0 0;
  color: #64748b;
  font-size: 12px;
}

.notice-content {
  white-space: pre-wrap;
  line-height: 1.6;
  color: #334155;
}

@media (max-width: 768px) {
  .hero-main {
    flex-direction: column;
  }
}
</style>
