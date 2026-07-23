<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { createDept, exportDepts, fetchDeptTree, removeDept, updateDept } from '@/api/system/dept'
import type { DeptView } from '@/types/api'

const loading = ref(false)
const exporting = ref(false)
const tree = ref<DeptView[]>([])
const dialogVisible = ref(false)
const editingId = ref<number | null>(null)
const formRef = ref<FormInstance>()

const form = reactive({
  parentId: 0,
  name: '',
  sort: 0,
  status: true,
})

const rules: FormRules = {
  name: [{ required: true, message: '请输入部门名称', trigger: 'blur' }],
  parentId: [{ required: true, message: '请选择上级', trigger: 'change' }],
}

async function load() {
  loading.value = true
  try {
    tree.value = await fetchDeptTree()
  } finally {
    loading.value = false
  }
}

async function onExport() {
  exporting.value = true
  try {
    await exportDepts()
    ElMessage.success('导出成功')
  } catch {
    // request 工具已提示错误
  } finally {
    exporting.value = false
  }
}

function openCreate(parentId = 0) {
  editingId.value = null
  Object.assign(form, { parentId, name: '', sort: 0, status: true })
  dialogVisible.value = true
}

function openEdit(row: DeptView) {
  editingId.value = row.id
  Object.assign(form, {
    parentId: row.parentId,
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
    await createDept({ ...form })
    ElMessage.success('创建成功')
  } else {
    await updateDept(editingId.value, { ...form })
    ElMessage.success('更新成功')
  }
  dialogVisible.value = false
  await load()
}

async function onRemove(row: DeptView) {
  await ElMessageBox.confirm(`确认删除部门 ${row.name}？`, '提示', { type: 'warning' })
  await removeDept(row.id)
  ElMessage.success('已删除')
  await load()
}

onMounted(load)
</script>

<template>
  <el-card shadow="never">
    <div class="toolbar">
      <el-button v-permission="'system:dept:add'" type="success" @click="openCreate(0)">新增根部门</el-button>
      <el-button v-permission="'system:dept:export'" :loading="exporting" @click="onExport">导出</el-button>
    </div>
    <el-table v-loading="loading" :data="tree" row-key="id" default-expand-all :tree-props="{ children: 'children' }">
      <el-table-column prop="name" label="部门名称" min-width="200" />
      <el-table-column prop="sort" label="排序" width="80" />
      <el-table-column label="用户数" width="90">
        <template #default="{ row }">{{ row.userCount ?? 0 }}</template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="row.status ? 'success' : 'info'">{{ row.status ? '正常' : '停用' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="240">
        <template #default="{ row }">
          <el-button v-permission="'system:dept:add'" link type="primary" @click="openCreate(row.id)">新增</el-button>
          <el-button v-permission="'system:dept:edit'" link type="primary" @click="openEdit(row)">编辑</el-button>
          <el-button v-permission="'system:dept:remove'" link type="danger" @click="onRemove(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
  </el-card>

  <el-dialog v-model="dialogVisible" :title="editingId == null ? '新增部门' : '编辑部门'" width="480px">
    <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
      <el-form-item label="上级部门" prop="parentId">
        <el-tree-select
          v-model="form.parentId"
          :data="[{ id: 0, name: '根节点', children: tree }]"
          check-strictly
          :props="{ label: 'name', value: 'id', children: 'children' }"
          style="width: 100%"
        />
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
  margin-bottom: 16px;
}
</style>
