<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import {
  createOpenEndpoint,
  listOpenEndpoints,
  removeOpenEndpoint,
  updateOpenEndpoint,
} from '@/api/open/endpoint'
import type { OpenEndpointView } from '@/types/api'

const loading = ref(false)
const rows = ref<OpenEndpointView[]>([])
const keyword = ref('')
const page = ref(1)
const size = ref(10)
const total = ref(0)
const dialogVisible = ref(false)
const editingId = ref<number | null>(null)
const formRef = ref<FormInstance>()

const form = reactive({
  code: '',
  name: '',
  httpMethod: 'GET',
  pathPattern: '',
  remark: '',
  status: true,
})

const rules: FormRules = {
  code: [{ required: true, message: '请输入接口编码', trigger: 'blur' }],
  name: [{ required: true, message: '请输入接口名称', trigger: 'blur' }],
  httpMethod: [{ required: true, message: '请选择 HTTP 方法', trigger: 'change' }],
  pathPattern: [{ required: true, message: '请输入路径模式', trigger: 'blur' }],
}

async function load() {
  loading.value = true
  try {
    const data = await listOpenEndpoints({
      keyword: keyword.value || undefined,
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

function onPageChange(p: number) {
  page.value = p
  load()
}

function onSizeChange(s: number) {
  size.value = s
  page.value = 1
  load()
}

function openCreate() {
  editingId.value = null
  Object.assign(form, {
    code: '',
    name: '',
    httpMethod: 'GET',
    pathPattern: '/api/open/',
    remark: '',
    status: true,
  })
  dialogVisible.value = true
}

function openEdit(row: OpenEndpointView) {
  editingId.value = row.id
  Object.assign(form, {
    code: row.code,
    name: row.name,
    httpMethod: row.httpMethod,
    pathPattern: row.pathPattern,
    remark: row.remark || '',
    status: row.status,
  })
  dialogVisible.value = true
}

async function submit() {
  if (!formRef.value) return
  await formRef.value.validate()
  if (editingId.value == null) {
    await createOpenEndpoint({ ...form })
    ElMessage.success('创建成功')
  } else {
    await updateOpenEndpoint(editingId.value, { ...form })
    ElMessage.success('更新成功')
  }
  dialogVisible.value = false
  await load()
}

async function onRemove(row: OpenEndpointView) {
  await ElMessageBox.confirm(`确认删除接口 ${row.name}？`, '提示', { type: 'warning' })
  await removeOpenEndpoint(row.id)
  ElMessage.success('已删除')
  await load()
}

onMounted(load)
</script>

<template>
  <el-card shadow="never">
    <div class="toolbar">
      <el-input v-model="keyword" clearable placeholder="编码/名称/路径" style="width: 240px" @keyup.enter="onSearch" />
      <el-button v-permission="'open:endpoint:query'" type="primary" @click="onSearch">查询</el-button>
      <el-button v-permission="'open:endpoint:add'" type="success" @click="openCreate">新增</el-button>
    </div>
    <el-table v-loading="loading" :data="rows" stripe>
      <el-table-column prop="code" label="编码" width="160" />
      <el-table-column prop="name" label="名称" min-width="140" />
      <el-table-column prop="httpMethod" label="方法" width="90" />
      <el-table-column prop="pathPattern" label="路径模式" min-width="220" show-overflow-tooltip />
      <el-table-column prop="remark" label="备注" min-width="120" show-overflow-tooltip />
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="row.status ? 'success' : 'info'">{{ row.status ? '启用' : '停用' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{ row }">
          <el-button v-permission="'open:endpoint:edit'" link type="primary" @click="openEdit(row)">编辑</el-button>
          <el-button v-permission="'open:endpoint:remove'" link type="danger" @click="onRemove(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <div class="pager">
      <el-pagination
        v-model:current-page="page"
        v-model:page-size="size"
        background
        layout="total, sizes, prev, pager, next"
        :total="total"
        :page-sizes="[10, 20, 50]"
        @current-change="onPageChange"
        @size-change="onSizeChange"
      />
    </div>
  </el-card>

  <el-dialog v-model="dialogVisible" :title="editingId == null ? '新增开放接口' : '编辑开放接口'" width="560px">
    <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
      <el-form-item label="编码" prop="code">
        <el-input v-model="form.code" :disabled="editingId != null" placeholder="如 open.demo.ping" />
      </el-form-item>
      <el-form-item label="名称" prop="name">
        <el-input v-model="form.name" />
      </el-form-item>
      <el-form-item label="HTTP 方法" prop="httpMethod">
        <el-select v-model="form.httpMethod" style="width: 100%">
          <el-option label="*" value="*" />
          <el-option label="GET" value="GET" />
          <el-option label="POST" value="POST" />
          <el-option label="PUT" value="PUT" />
          <el-option label="DELETE" value="DELETE" />
        </el-select>
      </el-form-item>
      <el-form-item label="路径模式" prop="pathPattern">
        <el-input v-model="form.pathPattern" placeholder="/api/open/demo/**" />
      </el-form-item>
      <el-form-item label="备注">
        <el-input v-model="form.remark" />
      </el-form-item>
      <el-form-item label="状态">
        <el-switch v-model="form.status" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="dialogVisible = false">取消</el-button>
      <el-button type="primary" @click="submit">保存</el-button>
    </template>
  </el-dialog>
</template>

<style scoped>
.toolbar {
  display: flex;
  gap: 8px;
  margin-bottom: 16px;
}
.pager {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
