<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { removeLoginLog, searchLoginLogs } from '@/api/system/loginLog'
import type { LoginLogView } from '@/types/api'

const loading = ref(false)
const rows = ref<LoginLogView[]>([])
const page = ref(1)
const size = ref(10)
const total = ref(0)
const query = reactive({
  username: '',
  status: '',
  ip: '',
})

async function load() {
  loading.value = true
  try {
    const data = await searchLoginLogs({
      username: query.username || undefined,
      status: query.status || undefined,
      ip: query.ip || undefined,
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
  query.status = ''
  query.ip = ''
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

async function onRemove(row: LoginLogView) {
  await ElMessageBox.confirm(`确认删除登录日志 #${row.id}？`, '提示', { type: 'warning' })
  await removeLoginLog(row.id)
  ElMessage.success('已删除')
  await load()
}

onMounted(load)
</script>

<template>
  <el-card shadow="never">
    <div class="toolbar">
      <el-input v-model="query.username" clearable placeholder="用户名" style="width: 160px" @keyup.enter="onSearch" />
      <el-select v-model="query.status" clearable placeholder="状态" style="width: 140px">
        <el-option label="成功" value="SUCCESS" />
        <el-option label="失败" value="FAIL" />
      </el-select>
      <el-input v-model="query.ip" clearable placeholder="IP" style="width: 160px" @keyup.enter="onSearch" />
      <el-button v-permission="'system:loginLog:query'" type="primary" @click="onSearch">查询</el-button>
      <el-button @click="reset">重置</el-button>
    </div>
    <el-table v-loading="loading" :data="rows" stripe>
      <el-table-column prop="id" label="ID" width="100" />
      <el-table-column prop="username" label="用户名" width="140" />
      <el-table-column prop="ip" label="IP" width="140" />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 'SUCCESS' ? 'success' : 'danger'">
            {{ row.status === 'SUCCESS' ? '成功' : '失败' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="message" label="说明" min-width="160" show-overflow-tooltip />
      <el-table-column prop="userAgent" label="UA" min-width="200" show-overflow-tooltip />
      <el-table-column prop="traceId" label="TraceId" width="160" show-overflow-tooltip />
      <el-table-column prop="loginTime" label="登录时间" width="180" />
      <el-table-column label="操作" width="100" fixed="right">
        <template #default="{ row }">
          <el-button v-permission="'system:loginLog:remove'" link type="danger" @click="onRemove(row)">
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
</style>
