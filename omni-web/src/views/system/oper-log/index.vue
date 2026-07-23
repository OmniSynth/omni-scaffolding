<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { removeOperLog, searchOperLogs } from '@/api/system/operLog'
import type { OperLogView } from '@/types/api'

const loading = ref(false)
const rows = ref<OperLogView[]>([])
const page = ref(1)
const size = ref(10)
const total = ref(0)
const detailVisible = ref(false)
const current = ref<OperLogView | null>(null)
const query = reactive({
  username: '',
  module: '',
  status: '',
})

async function load() {
  loading.value = true
  try {
    const data = await searchOperLogs({
      username: query.username || undefined,
      module: query.module || undefined,
      status: query.status || undefined,
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

function onSearch() {
  page.value = 1
  load()
}

function reset() {
  query.username = ''
  query.module = ''
  query.status = ''
  page.value = 1
  load()
}

function onPageChange(p: number) {
  page.value = p
  load()
}

function onSizeChange(s: number) {
  size.value = s
  page.value = 1
  load()
}

function openDetail(row: OperLogView) {
  current.value = row
  detailVisible.value = true
}

async function onRemove(row: OperLogView) {
  await ElMessageBox.confirm(`确认删除操作日志 #${row.id}？`, '提示', { type: 'warning' })
  await removeOperLog(row.id)
  ElMessage.success('已删除')
  await load()
}

onMounted(load)
</script>

<template>
  <el-card shadow="never">
    <div class="toolbar">
      <el-input v-model="query.username" clearable placeholder="操作人" style="width: 160px" @keyup.enter="onSearch" />
      <el-input v-model="query.module" clearable placeholder="模块" style="width: 160px" @keyup.enter="onSearch" />
      <el-select v-model="query.status" clearable placeholder="状态" style="width: 140px">
        <el-option label="成功" value="SUCCESS" />
        <el-option label="失败" value="FAIL" />
      </el-select>
      <el-button v-permission="'system:operLog:query'" type="primary" @click="onSearch">查询</el-button>
      <el-button @click="reset">重置</el-button>
    </div>
    <el-table v-loading="loading" :data="rows" stripe>
      <el-table-column prop="id" label="ID" width="100" />
      <el-table-column prop="username" label="操作人" width="120" />
      <el-table-column prop="module" label="模块" width="120" />
      <el-table-column prop="action" label="动作" width="100" />
      <el-table-column prop="requestMethod" label="方法" width="80" />
      <el-table-column prop="requestUri" label="URI" min-width="180" show-overflow-tooltip />
      <el-table-column prop="ip" label="IP" width="130" />
      <el-table-column prop="status" label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="row.status === 'SUCCESS' ? 'success' : 'danger'">
            {{ row.status === 'SUCCESS' ? '成功' : '失败' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="costMs" label="耗时(ms)" width="90" />
      <el-table-column prop="operTime" label="操作时间" width="180" />
      <el-table-column label="操作" width="140" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="openDetail(row)">详情</el-button>
          <el-button v-permission="'system:operLog:remove'" link type="danger" @click="onRemove(row)">
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>
    <div class="pager">
      <el-pagination
        v-model:current-page="page"
        v-model:page-size="size"
        background
        layout="total, sizes, prev, pager, next, jumper"
        :total="total"
        :page-sizes="[10, 20, 50, 100]"
        @current-change="onPageChange"
        @size-change="onSizeChange"
      />
    </div>
  </el-card>

  <el-dialog v-model="detailVisible" title="操作日志详情" width="640px">
    <el-descriptions v-if="current" :column="1" border>
      <el-descriptions-item label="模块">{{ current.module }} / {{ current.action }}</el-descriptions-item>
      <el-descriptions-item label="方法">{{ current.method }}</el-descriptions-item>
      <el-descriptions-item label="请求">{{ current.requestMethod }} {{ current.requestUri }}</el-descriptions-item>
      <el-descriptions-item label="IP">{{ current.ip }}</el-descriptions-item>
      <el-descriptions-item label="TraceId">{{ current.traceId || '-' }}</el-descriptions-item>
      <el-descriptions-item label="错误">{{ current.errorMsg || '-' }}</el-descriptions-item>
      <el-descriptions-item label="参数">
        <pre class="params">{{ current.params || '-' }}</pre>
      </el-descriptions-item>
    </el-descriptions>
  </el-dialog>
</template>

<style scoped>
.toolbar {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 16px;
}
.pager {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
.params {
  margin: 0;
  white-space: pre-wrap;
  word-break: break-all;
  font-size: 12px;
  line-height: 1.5;
}
</style>
