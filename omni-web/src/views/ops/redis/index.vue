<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import {
  deleteRedisKeys,
  expireRedisKey,
  fetchRedisInfo,
  fetchRedisKeyDetail,
  scanRedisKeys,
  setRedisString,
} from '@/api/ops/redis'
import { useUserStore } from '@/stores/user'
import type { RedisInfoView, RedisKeyDetailView, RedisKeyView } from '@/types/api'

const userStore = useUserStore()
const canQuery = computed(() => userStore.hasPermission('ops:redis:query'))
const canEdit = computed(() => userStore.hasPermission('ops:redis:edit'))
const canRemove = computed(() => userStore.hasPermission('ops:redis:remove'))

const infoLoading = ref(false)
const info = ref<RedisInfoView | null>(null)

const keysLoading = ref(false)
const keys = ref<RedisKeyView[]>([])
const selectedKeys = ref<RedisKeyView[]>([])
const pattern = ref('omni:*')
const limit = ref(50)

const detailVisible = ref(false)
const detailLoading = ref(false)
const detail = ref<RedisKeyDetailView | null>(null)

const writeVisible = ref(false)
const writeRef = ref<FormInstance>()
const writeForm = reactive({
  key: '',
  value: '',
  ttlSeconds: undefined as number | undefined,
})
const writeRules: FormRules = {
  key: [{ required: true, message: '请输入 Key', trigger: 'blur' }],
  value: [{ required: true, message: '请输入值', trigger: 'blur' }],
}

const ttlVisible = ref(false)
const ttlKey = ref('')
const ttlSeconds = ref(3600)

function formatUptime(sec?: number): string {
  if (sec == null) return '-'
  const d = Math.floor(sec / 86400)
  const h = Math.floor((sec % 86400) / 3600)
  const m = Math.floor((sec % 3600) / 60)
  return `${d}天 ${h}时 ${m}分`
}

function formatTtl(ttl?: number): string {
  if (ttl == null) return '-'
  if (ttl < 0) return ttl === -1 ? '永不过期' : '不存在'
  if (ttl < 60) return `${ttl}s`
  if (ttl < 3600) return `${Math.floor(ttl / 60)}m ${ttl % 60}s`
  return `${Math.floor(ttl / 3600)}h ${Math.floor((ttl % 3600) / 60)}m`
}

async function loadInfo() {
  if (!canQuery.value) return
  infoLoading.value = true
  try {
    info.value = await fetchRedisInfo()
  } finally {
    infoLoading.value = false
  }
}

async function loadKeys() {
  if (!canQuery.value) return
  keysLoading.value = true
  try {
    keys.value = await scanRedisKeys({
      pattern: pattern.value || '*',
      limit: limit.value,
    })
    selectedKeys.value = []
  } finally {
    keysLoading.value = false
  }
}

async function openDetail(row: RedisKeyView) {
  detailVisible.value = true
  detailLoading.value = true
  detail.value = null
  try {
    detail.value = await fetchRedisKeyDetail(row.key)
  } finally {
    detailLoading.value = false
  }
}

function openWrite(row?: RedisKeyView) {
  writeForm.key = row?.key || ''
  writeForm.value = ''
  writeForm.ttlSeconds = undefined
  writeVisible.value = true
}

async function submitWrite() {
  if (!writeRef.value) return
  await writeRef.value.validate()
  await setRedisString({
    key: writeForm.key,
    value: writeForm.value,
    ttlSeconds: writeForm.ttlSeconds && writeForm.ttlSeconds > 0 ? writeForm.ttlSeconds : undefined,
  })
  ElMessage.success('已写入')
  writeVisible.value = false
  await loadKeys()
  await loadInfo()
}

function openTtl(row: RedisKeyView) {
  ttlKey.value = row.key
  ttlSeconds.value = row.ttlSeconds && row.ttlSeconds > 0 ? row.ttlSeconds : 3600
  ttlVisible.value = true
}

async function submitTtl(persist = false) {
  await expireRedisKey({
    key: ttlKey.value,
    ttlSeconds: persist ? 0 : ttlSeconds.value,
  })
  ElMessage.success(persist ? '已设为永不过期' : '已更新 TTL')
  ttlVisible.value = false
  await loadKeys()
}

async function onDelete(rows: RedisKeyView[]) {
  if (!rows.length) return
  await ElMessageBox.confirm(`确认删除选中的 ${rows.length} 个 Key？`, '提示', { type: 'warning' })
  const result = await deleteRedisKeys(rows.map((r) => r.key))
  ElMessage.success(`已删除 ${result.deleted} 个`)
  await loadKeys()
  await loadInfo()
}

function onSelectionChange(rows: RedisKeyView[]) {
  selectedKeys.value = rows
}

onMounted(async () => {
  await loadInfo()
  await loadKeys()
})
</script>

<template>
  <div class="redis-page">
    <el-card shadow="never" v-loading="infoLoading">
      <template #header>
        <div class="card-header">
          <span>Redis 概览</span>
          <el-button v-permission="'ops:redis:query'" size="small" @click="loadInfo">刷新</el-button>
        </div>
      </template>
      <el-descriptions v-if="info" :column="3" border size="small">
        <el-descriptions-item label="版本">{{ info.redisVersion || '-' }}</el-descriptions-item>
        <el-descriptions-item label="模式">{{ info.mode || '-' }}</el-descriptions-item>
        <el-descriptions-item label="角色">{{ info.role || '-' }}</el-descriptions-item>
        <el-descriptions-item label="运行时长">{{ formatUptime(info.uptimeInSeconds) }}</el-descriptions-item>
        <el-descriptions-item label="客户端">{{ info.connectedClients ?? '-' }}</el-descriptions-item>
        <el-descriptions-item label="Key 总数">{{ info.totalKeys ?? '-' }}</el-descriptions-item>
        <el-descriptions-item label="已用内存">{{ info.usedMemoryHuman || '-' }}</el-descriptions-item>
        <el-descriptions-item label="最大内存">{{ info.maxMemoryHuman || '未限制' }}</el-descriptions-item>
        <el-descriptions-item label="瞬时 OPS">{{ info.instantaneousOpsPerSec ?? '-' }}</el-descriptions-item>
        <el-descriptions-item label="系统" :span="3">{{ info.os || '-' }}</el-descriptions-item>
      </el-descriptions>
      <el-empty v-else description="暂无数据（需 ops:redis:query）" />
    </el-card>

    <el-card shadow="never" class="mt">
      <template #header>
        <div class="card-header">
          <span>Key 浏览器</span>
          <el-text type="info" size="small">使用 SCAN，默认匹配 omni:*；不支持 FLUSHDB</el-text>
        </div>
      </template>
      <div class="toolbar">
        <el-input v-model="pattern" clearable placeholder="匹配模式，如 omni:online:*" style="width: 260px" @keyup.enter="loadKeys" />
        <el-input-number v-model="limit" :min="1" :max="200" />
        <el-button v-permission="'ops:redis:query'" type="primary" :loading="keysLoading" @click="loadKeys">查询</el-button>
        <el-button v-permission="'ops:redis:edit'" type="success" @click="openWrite()">新增 String</el-button>
        <el-button
          v-permission="'ops:redis:remove'"
          type="danger"
          :disabled="!selectedKeys.length"
          @click="onDelete(selectedKeys)"
        >
          批量删除
        </el-button>
      </div>

      <el-table
        v-loading="keysLoading"
        :data="keys"
        stripe
        @selection-change="onSelectionChange"
      >
        <el-table-column v-if="canRemove" type="selection" width="48" />
        <el-table-column prop="key" label="Key" min-width="260" show-overflow-tooltip />
        <el-table-column prop="type" label="类型" width="100" />
        <el-table-column label="TTL" width="140">
          <template #default="{ row }">{{ formatTtl(row.ttlSeconds) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button v-if="canQuery" link type="primary" @click="openDetail(row)">详情</el-button>
            <el-button v-if="canEdit" link type="primary" @click="openTtl(row)">TTL</el-button>
            <el-button v-if="canEdit && row.type === 'string'" link type="primary" @click="openWrite(row)">改值</el-button>
            <el-button v-if="canRemove" link type="danger" @click="onDelete([row])">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-drawer v-model="detailVisible" title="Key 详情" size="480px">
      <el-skeleton v-if="detailLoading" :rows="8" animated />
      <template v-else-if="detail">
        <el-descriptions :column="1" border size="small">
          <el-descriptions-item label="Key">{{ detail.key }}</el-descriptions-item>
          <el-descriptions-item label="类型">{{ detail.type }}</el-descriptions-item>
          <el-descriptions-item label="TTL">{{ formatTtl(detail.ttlSeconds) }}</el-descriptions-item>
          <el-descriptions-item label="大小">{{ detail.size ?? '-' }}</el-descriptions-item>
        </el-descriptions>
        <el-alert
          v-if="detail.truncated"
          class="mt"
          type="warning"
          :closable="false"
          title="内容已截断（集合最多 100 项 / 字符串最多 4000 字符）"
        />
        <el-input class="mt" type="textarea" :rows="16" :model-value="detail.value || ''" readonly />
      </template>
    </el-drawer>

    <el-dialog v-model="writeVisible" title="写入 String" width="560px">
      <el-form ref="writeRef" :model="writeForm" :rules="writeRules" label-width="90px">
        <el-form-item label="Key" prop="key">
          <el-input v-model="writeForm.key" maxlength="512" show-word-limit />
        </el-form-item>
        <el-form-item label="值" prop="value">
          <el-input v-model="writeForm.value" type="textarea" :rows="6" maxlength="4000" show-word-limit />
        </el-form-item>
        <el-form-item label="TTL(秒)">
          <el-input-number v-model="writeForm.ttlSeconds" :min="0" :max="2592000" />
          <el-text class="hint" type="info" size="small">0 或不填表示永不过期</el-text>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="writeVisible = false">取消</el-button>
        <el-button type="primary" @click="submitWrite">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="ttlVisible" title="设置 TTL" width="420px">
      <el-form label-width="80px">
        <el-form-item label="Key">
          <el-input :model-value="ttlKey" disabled />
        </el-form-item>
        <el-form-item label="秒数">
          <el-input-number v-model="ttlSeconds" :min="1" :max="2592000" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="submitTtl(true)">永不过期</el-button>
        <el-button type="primary" @click="submitTtl(false)">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.redis-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}
.toolbar {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 16px;
  align-items: center;
}
.mt {
  margin-top: 12px;
}
.hint {
  margin-left: 8px;
}
</style>
