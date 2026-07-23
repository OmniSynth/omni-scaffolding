<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import {
  changeNoticeStatus,
  createNotice,
  listNotices,
  removeNotice,
  updateNotice,
} from '@/api/system/notice'
import { useUserStore } from '@/stores/user'
import type { NoticeView } from '@/types/api'

const userStore = useUserStore()
const canEdit = computed(() => userStore.hasPermission('system:notice:edit'))

const loading = ref(false)
const rows = ref<NoticeView[]>([])
const keyword = ref('')
const typeFilter = ref('')
const statusFilter = ref<boolean | ''>('')
const page = ref(1)
const size = ref(10)
const total = ref(0)
const statusLoadingId = ref<number | null>(null)

const dialogVisible = ref(false)
const editingId = ref<number | null>(null)
const formRef = ref<FormInstance>()

const form = reactive({
  title: '',
  content: '',
  type: 'NOTICE',
  status: true,
})

const rules: FormRules = {
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  content: [{ required: true, message: '请输入内容', trigger: 'blur' }],
  type: [{ required: true, message: '请选择类型', trigger: 'change' }],
}

function typeLabel(type?: string): string {
  return type === 'ANNOUNCE' ? '公告' : '通知'
}

async function load() {
  loading.value = true
  try {
    const data = await listNotices({
      keyword: keyword.value || undefined,
      type: typeFilter.value || undefined,
      status: statusFilter.value === '' ? undefined : statusFilter.value,
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

function openCreate() {
  editingId.value = null
  Object.assign(form, {
    title: '',
    content: '',
    type: 'NOTICE',
    status: true,
  })
  dialogVisible.value = true
}

function openEdit(row: NoticeView) {
  editingId.value = row.id
  Object.assign(form, {
    title: row.title,
    content: row.content,
    type: row.type || 'NOTICE',
    status: row.status,
  })
  dialogVisible.value = true
}

async function submit() {
  if (!formRef.value) return
  await formRef.value.validate()
  if (editingId.value == null) {
    await createNotice({ ...form })
    ElMessage.success('创建成功')
  } else {
    await updateNotice(editingId.value, { ...form })
    ElMessage.success('更新成功')
  }
  dialogVisible.value = false
  await load()
}

async function onRemove(row: NoticeView) {
  await ElMessageBox.confirm(`确认删除公告「${row.title}」？`, '提示', { type: 'warning' })
  await removeNotice(row.id)
  ElMessage.success('已删除')
  await load()
}

async function onStatusChange(row: NoticeView, status: boolean | string | number) {
  const next = Boolean(status)
  statusLoadingId.value = row.id
  try {
    await changeNoticeStatus(row.id, next)
    row.status = next
    ElMessage.success(next ? '已启用' : '已停用')
  } catch {
    row.status = !next
  } finally {
    statusLoadingId.value = null
  }
}

onMounted(load)
</script>

<template>
  <el-card shadow="never">
    <div class="toolbar">
      <el-input
        v-model="keyword"
        clearable
        placeholder="标题/内容"
        style="width: 200px"
        @keyup.enter="onSearch"
      />
      <el-select v-model="typeFilter" clearable placeholder="类型" style="width: 120px">
        <el-option label="通知" value="NOTICE" />
        <el-option label="公告" value="ANNOUNCE" />
      </el-select>
      <el-select v-model="statusFilter" clearable placeholder="状态" style="width: 120px">
        <el-option label="启用" :value="true" />
        <el-option label="停用" :value="false" />
      </el-select>
      <el-button v-permission="'system:notice:query'" type="primary" @click="onSearch">查询</el-button>
      <el-button v-permission="'system:notice:add'" type="success" @click="openCreate">新增</el-button>
    </div>

    <el-table v-loading="loading" :data="rows" stripe>
      <el-table-column prop="title" label="标题" min-width="180" show-overflow-tooltip />
      <el-table-column label="类型" width="90">
        <template #default="{ row }">
          <el-tag :type="row.type === 'ANNOUNCE' ? 'warning' : 'info'" size="small">
            {{ typeLabel(row.type) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="publisherName" label="发布人" width="120" show-overflow-tooltip />
      <el-table-column prop="publishTime" label="发布时间" width="180" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-switch
            v-if="canEdit"
            :model-value="row.status"
            :loading="statusLoadingId === row.id"
            inline-prompt
            active-text="启"
            inactive-text="停"
            @change="(val: string | number | boolean) => onStatusChange(row, val)"
          />
          <el-tag v-else :type="row.status ? 'success' : 'info'" size="small">
            {{ row.status ? '启用' : '停用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="140" fixed="right">
        <template #default="{ row }">
          <el-button v-permission="'system:notice:edit'" link type="primary" @click="openEdit(row)">编辑</el-button>
          <el-button v-permission="'system:notice:remove'" link type="danger" @click="onRemove(row)">删除</el-button>
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
        @current-change="load"
        @size-change="
          () => {
            page = 1
            load()
          }
        "
      />
    </div>
  </el-card>

  <el-dialog v-model="dialogVisible" :title="editingId == null ? '新增公告' : '编辑公告'" width="640px">
    <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
      <el-form-item label="标题" prop="title">
        <el-input v-model="form.title" maxlength="200" show-word-limit />
      </el-form-item>
      <el-form-item label="类型" prop="type">
        <el-radio-group v-model="form.type">
          <el-radio value="NOTICE">通知</el-radio>
          <el-radio value="ANNOUNCE">公告</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="内容" prop="content">
        <el-input v-model="form.content" type="textarea" :rows="8" maxlength="4000" show-word-limit />
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
  flex-wrap: wrap;
}
.pager {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
