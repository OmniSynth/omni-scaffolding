<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import {
  changeIpWhitelistStatus,
  createIpWhitelist,
  listIpWhitelist,
  refreshIpWhitelistCache,
  removeIpWhitelist,
  todayIpVisits,
  updateIpWhitelist,
} from '@/api/system/ip-whitelist'
import { useUserStore } from '@/stores/user'
import type { IpVisitTodayView, IpWhitelistView } from '@/types/api'

const userStore = useUserStore()
const canEdit = computed(() => userStore.hasPermission('system:ipWhitelist:edit'))
const canRemove = computed(() => userStore.hasPermission('system:ipWhitelist:remove'))

const loading = ref(false)
const rows = ref<IpWhitelistView[]>([])
const keyword = ref('')
const statusFilter = ref<boolean | undefined>(undefined)
const page = ref(1)
const size = ref(10)
const total = ref(0)
const statusLoadingId = ref<number | null>(null)

const visitLoading = ref(false)
const visitStats = ref<IpVisitTodayView | null>(null)
const refreshing = ref(false)

const dialogVisible = ref(false)
const editingId = ref<number | null>(null)
const formRef = ref<FormInstance>()
const form = reactive({
  ipAddr: '',
  remark: '',
  status: true,
})

const rules: FormRules = {
  ipAddr: [{ required: true, message: '请输入 IP', trigger: 'blur' }],
}

async function load() {
  loading.value = true
  try {
    const data = await listIpWhitelist({
      keyword: keyword.value || undefined,
      status: statusFilter.value,
      page: page.value,
      size: size.value,
    })
    rows.value = data.records
    total.value = data.total
    page.value = data.page
    size.value = data.size
  } finally {
    loading.value = false
  }
}

async function loadVisits() {
  visitLoading.value = true
  try {
    visitStats.value = await todayIpVisits()
  } finally {
    visitLoading.value = false
  }
}

function onSearch() {
  page.value = 1
  load()
}

function openCreate() {
  editingId.value = null
  Object.assign(form, { ipAddr: '', remark: '', status: true })
  dialogVisible.value = true
}

function openEdit(row: IpWhitelistView) {
  editingId.value = row.id
  Object.assign(form, {
    ipAddr: row.ipAddr,
    remark: row.remark || '',
    status: !!row.status,
  })
  dialogVisible.value = true
}

async function submit() {
  if (!formRef.value) return
  await formRef.value.validate()
  const body = {
    ipAddr: form.ipAddr.trim(),
    remark: form.remark?.trim() || undefined,
    status: form.status,
  }
  if (editingId.value == null) {
    await createIpWhitelist(body)
    ElMessage.success('已创建')
  } else {
    await updateIpWhitelist(editingId.value, body)
    ElMessage.success('已保存')
  }
  dialogVisible.value = false
  await load()
}

async function onStatusChange(row: IpWhitelistView, value: boolean) {
  statusLoadingId.value = row.id
  try {
    await changeIpWhitelistStatus(row.id, value)
    row.status = value
    ElMessage.success(value ? '已启用' : '已停用')
  } catch {
    row.status = !value
  } finally {
    statusLoadingId.value = null
  }
}

async function onRemove(row: IpWhitelistView) {
  await ElMessageBox.confirm(`确认删除白名单「${row.ipAddr}」？`, '提示', { type: 'warning' })
  await removeIpWhitelist(row.id)
  ElMessage.success('已删除')
  await load()
}

async function onRefreshCache() {
  refreshing.value = true
  try {
    await refreshIpWhitelistCache()
    ElMessage.success('缓存已刷新')
  } finally {
    refreshing.value = false
  }
}

onMounted(() => {
  load()
  loadVisits()
})
</script>

<template>
  <div class="page">
    <el-card shadow="never">
      <div class="visit-head">
        <div>
          <div class="visit-title">今日访问</div>
          <div class="visit-sub">
            日期 {{ visitStats?.date || '-' }} · 合计 {{ visitStats?.total ?? 0 }} 次
          </div>
        </div>
        <el-button v-permission="'system:ipWhitelist:query'" :loading="visitLoading" @click="loadVisits">
          刷新统计
        </el-button>
      </div>
      <el-table v-loading="visitLoading" :data="visitStats?.items || []" size="small" max-height="220">
        <el-table-column prop="ip" label="IP" min-width="160" />
        <el-table-column prop="count" label="次数" width="100" />
      </el-table>
    </el-card>

    <el-card shadow="never">
      <div class="toolbar">
        <el-input
          v-model="keyword"
          clearable
          placeholder="IP / 备注"
          style="width: 220px"
          @keyup.enter="onSearch"
        />
        <el-select v-model="statusFilter" clearable placeholder="状态" style="width: 120px">
          <el-option label="启用" :value="true" />
          <el-option label="停用" :value="false" />
        </el-select>
        <el-button v-permission="'system:ipWhitelist:query'" type="primary" @click="onSearch">查询</el-button>
        <el-button v-permission="'system:ipWhitelist:add'" type="success" @click="openCreate">新增</el-button>
        <el-button v-permission="'system:ipWhitelist:refresh'" :loading="refreshing" @click="onRefreshCache">
          刷新缓存
        </el-button>
      </div>

      <el-table v-loading="loading" :data="rows" stripe>
        <el-table-column prop="ipAddr" label="IP" min-width="180" show-overflow-tooltip />
        <el-table-column prop="remark" label="备注" min-width="160" show-overflow-tooltip />
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-switch
              :model-value="row.status"
              :disabled="!canEdit"
              :loading="statusLoadingId === row.id"
              @change="(v: boolean) => onStatusChange(row, v)"
            />
          </template>
        </el-table-column>
        <el-table-column prop="updatedAt" label="更新时间" min-width="170" />
        <el-table-column label="操作" width="140" fixed="right">
          <template #default="{ row }">
            <el-button v-if="canEdit" link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button v-if="canRemove" link type="danger" @click="onRemove(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pager">
        <el-pagination
          v-model:current-page="page"
          v-model:page-size="size"
          :total="total"
          layout="total, sizes, prev, pager, next"
          :page-sizes="[10, 20, 50]"
          @current-change="load"
          @size-change="onSearch"
        />
      </div>
    </el-card>

    <el-dialog
      v-model="dialogVisible"
      :title="editingId == null ? '新增 IP 白名单' : '编辑 IP 白名单'"
      width="480px"
      destroy-on-close
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="IP" prop="ipAddr">
          <el-input v-model="form.ipAddr" maxlength="64" placeholder="如 127.0.0.1 或 10.0.0.1" />
        </el-form-item>
        <el-form-item label="启用">
          <el-switch v-model="form.status" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" maxlength="255" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.visit-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}
.visit-title {
  font-weight: 600;
}
.visit-sub {
  margin-top: 4px;
  color: var(--el-text-color-secondary);
  font-size: 13px;
}
.toolbar {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 16px;
  align-items: center;
}
.pager {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
