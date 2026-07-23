<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { createPost, exportPosts, listPosts, removePost, updatePost } from '@/api/system/post'
import type { PostView } from '@/types/api'

const loading = ref(false)
const rows = ref<PostView[]>([])
const keyword = ref('')
const page = ref(1)
const size = ref(10)
const total = ref(0)
const dialogVisible = ref(false)
const editingId = ref<number | null>(null)
const formRef = ref<FormInstance>()
const exporting = ref(false)

const form = reactive({
  code: '',
  name: '',
  sort: 0,
  status: true,
})

const rules: FormRules = {
  code: [{ required: true, message: '请输入岗位编码', trigger: 'blur' }],
  name: [{ required: true, message: '请输入岗位名称', trigger: 'blur' }],
}

async function load() {
  loading.value = true
  try {
    const data = await listPosts({
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

async function onExport() {
  exporting.value = true
  try {
    await exportPosts({ keyword: keyword.value || undefined })
    ElMessage.success('导出成功')
  } catch {
    // request 工具已提示错误
  } finally {
    exporting.value = false
  }
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
  Object.assign(form, { code: '', name: '', sort: 0, status: true })
  dialogVisible.value = true
}

function openEdit(row: PostView) {
  editingId.value = row.id
  Object.assign(form, {
    code: row.code,
    name: row.name,
    sort: row.sort,
    status: row.status,
  })
  dialogVisible.value = true
}

async function submit() {
  if (!formRef.value) return
  await formRef.value.validate()
  if (editingId.value == null) {
    await createPost({ ...form })
    ElMessage.success('创建成功')
  } else {
    await updatePost(editingId.value, { ...form })
    ElMessage.success('更新成功')
  }
  dialogVisible.value = false
  await load()
}

async function onRemove(row: PostView) {
  await ElMessageBox.confirm(`确认删除岗位 ${row.name}？`, '提示', { type: 'warning' })
  await removePost(row.id)
  ElMessage.success('已删除')
  await load()
}

onMounted(load)
</script>

<template>
  <el-card shadow="never">
    <div class="toolbar">
      <el-input v-model="keyword" clearable placeholder="编码/名称" style="width: 220px" @keyup.enter="onSearch" />
      <el-button v-permission="'system:post:query'" type="primary" @click="onSearch">查询</el-button>
      <el-button v-permission="'system:post:add'" type="success" @click="openCreate">新增</el-button>
      <el-button v-permission="'system:post:export'" :loading="exporting" @click="onExport">导出</el-button>
    </div>
    <el-table v-loading="loading" :data="rows" stripe>
      <el-table-column prop="id" label="ID" width="100" />
      <el-table-column prop="code" label="编码" width="160" />
      <el-table-column prop="name" label="名称" min-width="160" />
      <el-table-column prop="sort" label="排序" width="80" />
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="row.status ? 'success' : 'info'">{{ row.status ? '启用' : '停用' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="180">
        <template #default="{ row }">
          <el-button v-permission="'system:post:edit'" link type="primary" @click="openEdit(row)">编辑</el-button>
          <el-button v-permission="'system:post:remove'" link type="danger" @click="onRemove(row)">删除</el-button>
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

  <el-dialog v-model="dialogVisible" :title="editingId == null ? '新增岗位' : '编辑岗位'" width="480px">
    <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
      <el-form-item label="编码" prop="code">
        <el-input v-model="form.code" />
      </el-form-item>
      <el-form-item label="名称" prop="name">
        <el-input v-model="form.name" />
      </el-form-item>
      <el-form-item label="排序">
        <el-input-number v-model="form.sort" :min="0" />
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
