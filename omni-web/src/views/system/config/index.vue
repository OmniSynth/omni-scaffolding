<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import {
  changeConfigStatus,
  createConfig,
  exportConfigs,
  listConfigs,
  refreshConfigCache,
  removeConfig,
  updateConfig,
} from '@/api/system/config'
import { useUserStore } from '@/stores/user'
import type { ConfigView } from '@/types/api'

const userStore = useUserStore()
const canEdit = computed(() => userStore.hasPermission('system:config:edit'))

const loading = ref(false)
const rows = ref<ConfigView[]>([])
const keyword = ref('')
const page = ref(1)
const size = ref(10)
const total = ref(0)
const exporting = ref(false)
const refreshing = ref(false)
const statusLoadingId = ref<number | null>(null)

const dialogVisible = ref(false)
const editingId = ref<number | null>(null)
const editingBuiltin = ref(false)
const formRef = ref<FormInstance>()

const form = reactive({
  configKey: '',
  configName: '',
  configValue: '',
  remark: '',
  sort: 0,
  status: true,
})

const rules: FormRules = {
  configKey: [{ required: true, message: '请输入参数键', trigger: 'blur' }],
  configName: [{ required: true, message: '请输入参数名称', trigger: 'blur' }],
}

async function load() {
  loading.value = true
  try {
    const data = await listConfigs({
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
    await exportConfigs({ keyword: keyword.value || undefined })
    ElMessage.success('导出成功')
  } catch {
    // ignore
  } finally {
    exporting.value = false
  }
}

async function onRefreshCache() {
  refreshing.value = true
  try {
    await refreshConfigCache()
    ElMessage.success('缓存已刷新')
  } finally {
    refreshing.value = false
  }
}

function openCreate() {
  editingId.value = null
  editingBuiltin.value = false
  Object.assign(form, {
    configKey: '',
    configName: '',
    configValue: '',
    remark: '',
    sort: 0,
    status: true,
  })
  dialogVisible.value = true
}

function openEdit(row: ConfigView) {
  editingId.value = row.id
  editingBuiltin.value = !!row.builtin
  Object.assign(form, {
    configKey: row.configKey,
    configName: row.configName,
    configValue: row.configValue || '',
    remark: row.remark || '',
    sort: row.sort,
    status: row.status,
  })
  dialogVisible.value = true
}

async function submit() {
  if (!formRef.value) return
  await formRef.value.validate()
  if (editingId.value == null) {
    await createConfig({ ...form })
    ElMessage.success('创建成功')
  } else {
    await updateConfig(editingId.value, { ...form })
    ElMessage.success('更新成功')
  }
  dialogVisible.value = false
  await load()
}

async function onRemove(row: ConfigView) {
  if (row.builtin) {
    ElMessage.warning('内置参数不可删除')
    return
  }
  await ElMessageBox.confirm(`确认删除参数「${row.configName}」？`, '提示', { type: 'warning' })
  await removeConfig(row.id)
  ElMessage.success('已删除')
  await load()
}

async function onStatusChange(row: ConfigView, status: boolean | string | number) {
  const next = Boolean(status)
  statusLoadingId.value = row.id
  try {
    await changeConfigStatus(row.id, next)
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
        placeholder="参数键/名称/值"
        style="width: 240px"
        @keyup.enter="onSearch"
      />
      <el-button v-permission="'system:config:query'" type="primary" @click="onSearch">查询</el-button>
      <el-button v-permission="'system:config:add'" type="success" @click="openCreate">新增</el-button>
      <el-button v-permission="'system:config:export'" :loading="exporting" @click="onExport">导出</el-button>
      <el-button v-permission="'system:config:refresh'" :loading="refreshing" @click="onRefreshCache">刷新缓存</el-button>
    </div>

    <el-table v-loading="loading" :data="rows" stripe>
      <el-table-column prop="configKey" label="参数键" min-width="200" show-overflow-tooltip />
      <el-table-column prop="configName" label="参数名称" min-width="140" show-overflow-tooltip />
      <el-table-column prop="configValue" label="参数值" min-width="180" show-overflow-tooltip />
      <el-table-column prop="sort" label="排序" width="70" />
      <el-table-column label="内置" width="70">
        <template #default="{ row }">
          <el-tag :type="row.builtin ? 'warning' : 'info'" size="small">{{ row.builtin ? '是' : '否' }}</el-tag>
        </template>
      </el-table-column>
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
      <el-table-column prop="remark" label="备注" min-width="140" show-overflow-tooltip />
      <el-table-column label="操作" width="140" fixed="right">
        <template #default="{ row }">
          <el-button v-permission="'system:config:edit'" link type="primary" @click="openEdit(row)">编辑</el-button>
          <el-button
            v-permission="'system:config:remove'"
            link
            type="danger"
            :disabled="row.builtin"
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

  <el-dialog v-model="dialogVisible" :title="editingId == null ? '新增参数' : '编辑参数'" width="560px">
    <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
      <el-form-item label="参数键" prop="configKey">
        <el-input
          v-model="form.configKey"
          :disabled="editingBuiltin"
          placeholder="如 sys.ui.title / sys.ui.watermark"
        />
      </el-form-item>
      <el-form-item label="参数名称" prop="configName">
        <el-input v-model="form.configName" />
      </el-form-item>
      <el-form-item label="参数值">
        <el-input v-model="form.configValue" type="textarea" :rows="3" />
      </el-form-item>
      <el-form-item label="排序">
        <el-input-number v-model="form.sort" :min="0" />
      </el-form-item>
      <el-form-item label="状态">
        <el-switch v-model="form.status" />
      </el-form-item>
      <el-form-item label="备注">
        <el-input v-model="form.remark" type="textarea" :rows="2" />
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
