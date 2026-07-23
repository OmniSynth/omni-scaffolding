<script setup lang="ts">
import { computed, nextTick, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox, ElTree, type FormInstance, type FormRules } from 'element-plus'
import { changeRoleStatus, createRole, exportRoles, listRoles, removeRole, updateRole } from '@/api/system/role'
import { fetchMenuTree } from '@/api/system/menu'
import { useUserStore } from '@/stores/user'
import type { MenuTreeNode, RoleView } from '@/types/api'

const userStore = useUserStore()
const canEditRole = computed(() => userStore.hasPermission('system:role:edit'))

const loading = ref(false)
const rows = ref<RoleView[]>([])
const page = ref(1)
const size = ref(10)
const total = ref(0)
const menus = ref<MenuTreeNode[]>([])
const dialogVisible = ref(false)
const editingId = ref<number | null>(null)
const formRef = ref<FormInstance>()
const menuTreeRef = ref<InstanceType<typeof ElTree>>()
const statusLoadingId = ref<number | null>(null)
const exporting = ref(false)

const form = reactive({
  code: '',
  name: '',
  dataScope: 'SELF',
  status: true,
  menuIds: [] as number[],
})

const rules: FormRules = {
  code: [{ required: true, message: '请输入角色编码', trigger: 'blur' }],
  name: [{ required: true, message: '请输入角色名称', trigger: 'blur' }],
  dataScope: [{ required: true, message: '请选择数据范围', trigger: 'change' }],
}

const dataScopeOptions = [
  { label: '全部数据', value: 'ALL' },
  { label: '本部门及以下', value: 'DEPT_AND_CHILD' },
  { label: '本部门', value: 'DEPT' },
  { label: '仅本人', value: 'SELF' },
]

const dataScopeLabel: Record<string, string> = Object.fromEntries(
  dataScopeOptions.map((item) => [item.value, item.label]),
)

async function load() {
  loading.value = true
  try {
    const data = await listRoles({ page: page.value, size: size.value })
    rows.value = data.records
    total.value = data.total
    page.value = data.page
    size.value = data.size
  } finally {
    loading.value = false
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

async function loadMenus() {
  menus.value = await fetchMenuTree()
}

function openCreate() {
  editingId.value = null
  Object.assign(form, { code: '', name: '', dataScope: 'SELF', status: true, menuIds: [] })
  dialogVisible.value = true
  nextTick(() => menuTreeRef.value?.setCheckedKeys([]))
}

function openEdit(row: RoleView) {
  editingId.value = row.id
  Object.assign(form, {
    code: row.code,
    name: row.name,
    dataScope: row.dataScope,
    status: row.status,
    menuIds: [...(row.menuIds || [])],
  })
  dialogVisible.value = true
  nextTick(() => menuTreeRef.value?.setCheckedKeys(row.menuIds || []))
}

async function submit() {
  if (!formRef.value) return
  await formRef.value.validate()
  const checked = (menuTreeRef.value?.getCheckedKeys(false) || []) as number[]
  const half = (menuTreeRef.value?.getHalfCheckedKeys() || []) as number[]
  const menuIds = [...checked, ...half]
  const body = {
    code: form.code,
    name: form.name,
    dataScope: form.dataScope,
    status: form.status,
    menuIds,
  }
  if (editingId.value == null) {
    await createRole(body)
    ElMessage.success('创建成功')
  } else {
    await updateRole(editingId.value, body)
    ElMessage.success('更新成功')
  }
  dialogVisible.value = false
  await load()
}

async function onRemove(row: RoleView) {
  if (row.id === 1) {
    ElMessage.warning('系统管理员角色不可删除')
    return
  }
  await ElMessageBox.confirm(`确认删除角色「${row.name}」？`, '提示', { type: 'warning' })
  await removeRole(row.id)
  ElMessage.success('已删除')
  await load()
}

async function onExport() {
  exporting.value = true
  try {
    await exportRoles()
    ElMessage.success('导出成功')
  } catch {
    // request 工具已提示错误
  } finally {
    exporting.value = false
  }
}

async function onStatusChange(row: RoleView, status: boolean | string | number) {
  const next = Boolean(status)
  if (row.id === 1 && !next) {
    row.status = true
    ElMessage.warning('系统管理员角色不可停用')
    return
  }
  statusLoadingId.value = row.id
  try {
    await changeRoleStatus(row.id, next)
    row.status = next
    ElMessage.success(next ? '已启用' : '已停用')
  } catch {
    row.status = !next
  } finally {
    statusLoadingId.value = null
  }
}

onMounted(async () => {
  await Promise.all([load(), loadMenus()])
})
</script>

<template>
  <el-card shadow="never">
    <div class="toolbar">
      <el-button v-permission="'system:role:query'" type="primary" @click="load">刷新</el-button>
      <el-button v-permission="'system:role:add'" type="success" @click="openCreate">新增</el-button>
      <el-button v-permission="'system:role:export'" :loading="exporting" @click="onExport">导出</el-button>
    </div>

    <el-table v-loading="loading" :data="rows" stripe>
      <el-table-column prop="id" label="ID" width="90" />
      <el-table-column prop="code" label="编码" width="140" />
      <el-table-column prop="name" label="名称" min-width="140" />
      <el-table-column label="数据范围" width="140">
        <template #default="{ row }">
          {{ dataScopeLabel[row.dataScope] || row.dataScope }}
        </template>
      </el-table-column>
      <el-table-column label="状态" width="110">
        <template #default="{ row }">
          <el-switch
            v-if="canEditRole"
            :model-value="row.status"
            :disabled="row.id === 1"
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
      <el-table-column label="用户数" width="90">
        <template #default="{ row }">{{ row.userCount ?? 0 }}</template>
      </el-table-column>
      <el-table-column label="菜单数" width="90">
        <template #default="{ row }">{{ (row.menuIds || []).length }}</template>
      </el-table-column>
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{ row }">
          <el-button v-permission="'system:role:edit'" link type="primary" @click="openEdit(row)">编辑</el-button>
          <el-button
            v-permission="'system:role:remove'"
            link
            type="danger"
            :disabled="row.id === 1"
            @click="onRemove(row)"
          >
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

  <el-dialog v-model="dialogVisible" :title="editingId == null ? '新增角色' : '编辑角色'" width="680px">
    <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
      <el-form-item label="编码" prop="code">
        <el-input v-model="form.code" :disabled="editingId === 1" placeholder="如 ADMIN、SALES" />
      </el-form-item>
      <el-form-item label="名称" prop="name">
        <el-input v-model="form.name" placeholder="角色显示名称" />
      </el-form-item>
      <el-form-item label="数据范围" prop="dataScope">
        <el-select v-model="form.dataScope" style="width: 100%">
          <el-option v-for="opt in dataScopeOptions" :key="opt.value" :label="opt.label" :value="opt.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态">
        <el-switch
          v-model="form.status"
          :disabled="editingId === 1"
          inline-prompt
          active-text="启用"
          inactive-text="停用"
        />
      </el-form-item>
      <el-form-item label="菜单权限">
        <el-tree
          ref="menuTreeRef"
          :data="menus"
          node-key="id"
          show-checkbox
          default-expand-all
          :props="{ label: 'name', children: 'children' }"
          class="menu-tree"
        />
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
.menu-tree {
  width: 100%;
  max-height: 360px;
  overflow: auto;
  border: 1px solid #e2e8f0;
  border-radius: 6px;
  padding: 8px;
}
</style>
