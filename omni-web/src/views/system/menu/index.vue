<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { createMenu, fetchMenuTree, removeMenu, updateMenu } from '@/api/system/menu'
import MenuIconSelect from '@/components/MenuIconSelect.vue'
import { resolveMenuIcon } from '@/utils/icons'
import type { MenuTreeNode } from '@/types/api'

const loading = ref(false)
const tree = ref<MenuTreeNode[]>([])
const dialogVisible = ref(false)
const editingId = ref<number | null>(null)
const formRef = ref<FormInstance>()

const typeOptions = [
  { label: '目录', value: 'DIR' },
  { label: '菜单', value: 'MENU' },
  { label: '按钮', value: 'BUTTON' },
]

const typeLabel: Record<string, string> = {
  DIR: '目录',
  MENU: '菜单',
  BUTTON: '按钮',
}

const form = reactive({
  parentId: 0,
  type: 'MENU',
  name: '',
  path: '',
  component: '',
  icon: '',
  perms: '',
  sort: 0,
  visible: true,
  status: true,
})

const rules: FormRules = {
  type: [{ required: true, message: '请选择类型', trigger: 'change' }],
  name: [{ required: true, message: '请输入名称', trigger: 'blur' }],
}

async function load() {
  loading.value = true
  try {
    tree.value = await fetchMenuTree()
  } finally {
    loading.value = false
  }
}

function openCreate(parentId = 0, type = 'MENU') {
  editingId.value = null
  Object.assign(form, {
    parentId,
    type,
    name: '',
    path: '',
    component: '',
    icon: '',
    perms: '',
    sort: 0,
    visible: true,
    status: true,
  })
  dialogVisible.value = true
}

function openEdit(row: MenuTreeNode) {
  editingId.value = row.id
  Object.assign(form, {
    parentId: row.parentId,
    type: row.type,
    name: row.name,
    path: row.path || '',
    component: row.component || '',
    icon: row.icon || '',
    perms: row.perms || '',
    sort: row.sort || 0,
    visible: row.visible !== false,
    status: row.status !== false,
  })
  dialogVisible.value = true
}

async function submit() {
  if (!formRef.value) return
  await formRef.value.validate()
  const body = { ...form }
  if (editingId.value == null) {
    await createMenu(body)
    ElMessage.success('创建成功')
  } else {
    await updateMenu(editingId.value, body)
    ElMessage.success('更新成功')
  }
  dialogVisible.value = false
  await load()
}

async function onRemove(row: MenuTreeNode) {
  await ElMessageBox.confirm(`确认删除菜单 ${row.name}？`, '提示', { type: 'warning' })
  await removeMenu(row.id)
  ElMessage.success('已删除')
  await load()
}

function childType(row: MenuTreeNode): string {
  return row.type === 'DIR' ? 'MENU' : 'BUTTON'
}

onMounted(load)
</script>

<template>
  <el-card shadow="never">
    <div class="toolbar">
      <el-button v-permission="'system:menu:add'" type="success" @click="openCreate(0, 'DIR')">新增目录</el-button>
    </div>
    <el-table v-loading="loading" :data="tree" row-key="id" :tree-props="{ children: 'children' }">
      <el-table-column label="名称" min-width="200">
        <template #default="{ row }">
          <span class="name-cell">
            <el-icon v-if="resolveMenuIcon(row.icon)" class="row-icon">
              <component :is="resolveMenuIcon(row.icon)" />
            </el-icon>
            <span>{{ row.name }}</span>
          </span>
        </template>
      </el-table-column>
      <el-table-column label="类型" width="90">
        <template #default="{ row }">
          <el-tag
            size="small"
            :type="row.type === 'DIR' ? 'warning' : row.type === 'MENU' ? 'primary' : 'info'"
          >
            {{ typeLabel[row.type] || row.type }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="path" label="路径" min-width="140" />
      <el-table-column prop="perms" label="权限码" min-width="180" />
      <el-table-column prop="sort" label="排序" width="70" />
      <el-table-column label="操作" width="260">
        <template #default="{ row }">
          <el-button
            v-permission="'system:menu:add'"
            link
            type="primary"
            @click="openCreate(row.id, childType(row))"
          >
            新增
          </el-button>
          <el-button v-permission="'system:menu:edit'" link type="primary" @click="openEdit(row)">编辑</el-button>
          <el-button v-permission="'system:menu:remove'" link type="danger" @click="onRemove(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
  </el-card>

  <el-dialog v-model="dialogVisible" :title="editingId == null ? '新增菜单' : '编辑菜单'" width="560px">
    <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
      <el-form-item label="上级" prop="parentId">
        <el-tree-select
          v-model="form.parentId"
          :data="[{ id: 0, name: '根节点', children: tree }]"
          check-strictly
          :props="{ label: 'name', value: 'id', children: 'children' }"
          style="width: 100%"
        />
      </el-form-item>
      <el-form-item label="类型" prop="type">
        <el-select v-model="form.type" style="width: 100%">
          <el-option v-for="opt in typeOptions" :key="opt.value" :label="opt.label" :value="opt.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="名称" prop="name">
        <el-input v-model="form.name" />
      </el-form-item>
      <el-form-item v-if="form.type !== 'BUTTON'" label="路径">
        <el-input v-model="form.path" placeholder="目录如 /system，菜单如 user" />
      </el-form-item>
      <el-form-item v-if="form.type === 'MENU'" label="组件">
        <el-input v-model="form.component" placeholder="如 system/user/index" />
      </el-form-item>
      <el-form-item v-if="form.type !== 'BUTTON'" label="图标">
        <MenuIconSelect v-model="form.icon" />
      </el-form-item>
      <el-form-item label="权限码">
        <el-input v-model="form.perms" placeholder="如 system:user:add" />
      </el-form-item>
      <el-form-item label="排序">
        <el-input-number v-model="form.sort" :min="0" />
      </el-form-item>
      <el-form-item label="可见">
        <el-switch v-model="form.visible" />
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
.name-cell {
  display: inline-flex;
  align-items: center;
  gap: 6px;
}
.row-icon {
  color: #64748b;
}
</style>
