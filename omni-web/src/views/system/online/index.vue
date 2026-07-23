<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { kickOnlineUser, listOnlineUsers } from '@/api/system/online'
import { useUserStore } from '@/stores/user'
import type { OnlineUserView } from '@/types/api'

const userStore = useUserStore()
const canKick = computed(() => userStore.hasPermission('system:online:kick'))

const loading = ref(false)
const rows = ref<OnlineUserView[]>([])
const query = reactive({
  username: '',
  ip: '',
})

function formatTs(ts?: number): string {
  if (!ts) return '-'
  const d = new Date(ts)
  const pad = (n: number) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`
}

async function load() {
  loading.value = true
  try {
    rows.value = await listOnlineUsers({
      username: query.username || undefined,
      ip: query.ip || undefined,
    })
  } finally {
    loading.value = false
  }
}

function onSearch() {
  load()
}

function reset() {
  query.username = ''
  query.ip = ''
  load()
}

function isCurrentSession(row: OnlineUserView): boolean {
  return !!row.jti && row.jti === userStore.profile?.jti
}

async function onKick(row: OnlineUserView) {
  await ElMessageBox.confirm(`确认强制下线用户「${row.username}」？`, '提示', { type: 'warning' })
  await kickOnlineUser(row.jti)
  ElMessage.success('已强制下线')
  await load()
}

onMounted(load)
</script>

<template>
  <el-card shadow="never">
    <div class="toolbar">
      <el-input v-model="query.username" clearable placeholder="用户名" style="width: 160px" @keyup.enter="onSearch" />
      <el-input v-model="query.ip" clearable placeholder="IP" style="width: 160px" @keyup.enter="onSearch" />
      <el-button v-permission="'system:online:query'" type="primary" @click="onSearch">查询</el-button>
      <el-button @click="reset">重置</el-button>
      <el-button @click="load">刷新</el-button>
    </div>
    <el-table v-loading="loading" :data="rows" stripe>
      <el-table-column prop="username" label="用户名" width="140" />
      <el-table-column prop="deptName" label="部门" width="160" show-overflow-tooltip />
      <el-table-column prop="ip" label="IP" width="140" />
      <el-table-column prop="userAgent" label="浏览器" min-width="220" show-overflow-tooltip />
      <el-table-column label="登录时间" width="180">
        <template #default="{ row }">{{ formatTs(row.loginTime) }}</template>
      </el-table-column>
      <el-table-column label="过期时间" width="180">
        <template #default="{ row }">{{ formatTs(row.expireAt) }}</template>
      </el-table-column>
      <el-table-column prop="jti" label="会话 ID" min-width="200" show-overflow-tooltip />
      <el-table-column label="操作" width="120" fixed="right">
        <template #default="{ row }">
          <el-button
            v-if="canKick"
            link
            type="danger"
            :disabled="isCurrentSession(row)"
            @click="onKick(row)"
          >
            强退
          </el-button>
        </template>
      </el-table-column>
    </el-table>
  </el-card>
</template>

<style scoped>
.toolbar {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 16px;
}
</style>
