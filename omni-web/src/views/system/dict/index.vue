<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import {
  changeDictDataStatus,
  changeDictTypeStatus,
  createDictData,
  createDictType,
  exportDictData,
  exportDictTypes,
  listDictData,
  listDictTypes,
  removeDictData,
  removeDictType,
  updateDictData,
  updateDictType,
} from '@/api/system/dict'
import { useUserStore } from '@/stores/user'
import type { DictDataView, DictTypeView } from '@/types/api'
import { DICT_STYLE_OPTIONS, normalizeDictCssClass } from '@/constants/dictStyles'
import DictStyleTag from '@/components/DictStyleTag.vue'

const userStore = useUserStore()
const canEdit = computed(() => userStore.hasPermission('system:dict:edit'))

const typeLoading = ref(false)
const typeRows = ref<DictTypeView[]>([])
const typeKeyword = ref('')
const typePage = ref(1)
const typeSize = ref(10)
const typeTotal = ref(0)
const selectedType = ref<DictTypeView | null>(null)
const typeStatusLoadingId = ref<number | null>(null)
const typeExporting = ref(false)

const dataLoading = ref(false)
const dataRows = ref<DictDataView[]>([])
const dataKeyword = ref('')
const dataPage = ref(1)
const dataSize = ref(10)
const dataTotal = ref(0)
const dataStatusLoadingId = ref<number | null>(null)
const dataExporting = ref(false)

const typeDialogVisible = ref(false)
const typeEditingId = ref<number | null>(null)
const typeFormRef = ref<FormInstance>()
const typeForm = reactive({
  code: '',
  name: '',
  remark: '',
  sort: 0,
  status: true,
})
const typeRules: FormRules = {
  code: [{ required: true, message: '请输入字典编码', trigger: 'blur' }],
  name: [{ required: true, message: '请输入字典名称', trigger: 'blur' }],
}

const dataDialogVisible = ref(false)
const dataEditingId = ref<number | null>(null)
const dataFormRef = ref<FormInstance>()
const dataForm = reactive({
  typeCode: '',
  label: '',
  value: '',
  sort: 0,
  cssClass: '',
  defaultFlag: false,
  status: true,
  remark: '',
})
const dataRules: FormRules = {
  label: [{ required: true, message: '请输入标签', trigger: 'blur' }],
  value: [{ required: true, message: '请输入键值', trigger: 'blur' }],
}

async function loadTypes() {
  typeLoading.value = true
  try {
    const data = await listDictTypes({
      keyword: typeKeyword.value || undefined,
      page: typePage.value,
      size: typeSize.value,
    })
    typeRows.value = data.records
    typeTotal.value = data.total
    typePage.value = data.page
    typeSize.value = data.size
    if (selectedType.value) {
      const still = data.records.find((r) => r.id === selectedType.value?.id)
      if (still) {
        selectedType.value = still
      } else if (data.records.length) {
        selectedType.value = data.records[0]
      } else {
        selectedType.value = null
      }
    } else if (data.records.length) {
      selectedType.value = data.records[0]
    }
  } finally {
    typeLoading.value = false
  }
}

async function loadData() {
  if (!selectedType.value) {
    dataRows.value = []
    dataTotal.value = 0
    return
  }
  dataLoading.value = true
  try {
    const data = await listDictData({
      typeCode: selectedType.value.code,
      keyword: dataKeyword.value || undefined,
      page: dataPage.value,
      size: dataSize.value,
    })
    dataRows.value = data.records
    dataTotal.value = data.total
    dataPage.value = data.page
    dataSize.value = data.size
  } finally {
    dataLoading.value = false
  }
}

function onTypeSearch() {
  typePage.value = 1
  loadTypes()
}

function onDataSearch() {
  dataPage.value = 1
  loadData()
}

function selectType(row: DictTypeView) {
  if (selectedType.value?.id === row.id) return
  selectedType.value = row
  dataPage.value = 1
  dataKeyword.value = ''
}

watch(
  () => selectedType.value?.code,
  () => {
    loadData()
  },
)

function openCreateType() {
  typeEditingId.value = null
  Object.assign(typeForm, { code: '', name: '', remark: '', sort: 0, status: true })
  typeDialogVisible.value = true
}

function openEditType(row: DictTypeView) {
  typeEditingId.value = row.id
  Object.assign(typeForm, {
    code: row.code,
    name: row.name,
    remark: row.remark || '',
    sort: row.sort,
    status: row.status,
  })
  typeDialogVisible.value = true
}

async function submitType() {
  if (!typeFormRef.value) return
  await typeFormRef.value.validate()
  if (typeEditingId.value == null) {
    await createDictType({ ...typeForm })
    ElMessage.success('创建成功')
  } else {
    await updateDictType(typeEditingId.value, { ...typeForm })
    ElMessage.success('更新成功')
  }
  typeDialogVisible.value = false
  await loadTypes()
}

async function onRemoveType(row: DictTypeView) {
  await ElMessageBox.confirm(`确认删除字典类型「${row.name}」？`, '提示', { type: 'warning' })
  await removeDictType(row.id)
  ElMessage.success('已删除')
  if (selectedType.value?.id === row.id) {
    selectedType.value = null
  }
  await loadTypes()
}

async function onTypeStatusChange(row: DictTypeView, status: boolean | string | number) {
  const next = Boolean(status)
  typeStatusLoadingId.value = row.id
  try {
    await changeDictTypeStatus(row.id, next)
    row.status = next
    if (selectedType.value?.id === row.id) {
      selectedType.value = { ...selectedType.value, status: next }
    }
    ElMessage.success(next ? '已启用' : '已停用')
  } catch {
    row.status = !next
  } finally {
    typeStatusLoadingId.value = null
  }
}

async function onExportTypes() {
  typeExporting.value = true
  try {
    await exportDictTypes({ keyword: typeKeyword.value || undefined })
    ElMessage.success('导出成功')
  } catch {
    // ignore
  } finally {
    typeExporting.value = false
  }
}

function openCreateData() {
  if (!selectedType.value) {
    ElMessage.warning('请先选择字典类型')
    return
  }
  dataEditingId.value = null
  Object.assign(dataForm, {
    typeCode: selectedType.value.code,
    label: '',
    value: '',
    sort: 0,
    cssClass: '',
    defaultFlag: false,
    status: true,
    remark: '',
  })
  dataDialogVisible.value = true
}

function openEditData(row: DictDataView) {
  dataEditingId.value = row.id
  Object.assign(dataForm, {
    typeCode: row.typeCode,
    label: row.label,
    value: row.value,
    sort: row.sort,
    cssClass: normalizeDictCssClass(row.cssClass),
    defaultFlag: row.defaultFlag,
    status: row.status,
    remark: row.remark || '',
  })
  dataDialogVisible.value = true
}

async function submitData() {
  if (!dataFormRef.value) return
  await dataFormRef.value.validate()
  if (dataEditingId.value == null) {
    await createDictData({ ...dataForm })
    ElMessage.success('创建成功')
  } else {
    await updateDictData(dataEditingId.value, { ...dataForm })
    ElMessage.success('更新成功')
  }
  dataDialogVisible.value = false
  await Promise.all([loadData(), loadTypes()])
}

async function onRemoveData(row: DictDataView) {
  await ElMessageBox.confirm(`确认删除字典项「${row.label}」？`, '提示', { type: 'warning' })
  await removeDictData(row.id)
  ElMessage.success('已删除')
  await Promise.all([loadData(), loadTypes()])
}

async function onDataStatusChange(row: DictDataView, status: boolean | string | number) {
  const next = Boolean(status)
  dataStatusLoadingId.value = row.id
  try {
    await changeDictDataStatus(row.id, next)
    row.status = next
    ElMessage.success(next ? '已启用' : '已停用')
  } catch {
    row.status = !next
  } finally {
    dataStatusLoadingId.value = null
  }
}

async function onExportData() {
  if (!selectedType.value) {
    ElMessage.warning('请先选择字典类型')
    return
  }
  dataExporting.value = true
  try {
    await exportDictData({
      typeCode: selectedType.value.code,
      keyword: dataKeyword.value || undefined,
    })
    ElMessage.success('导出成功')
  } catch {
    // ignore
  } finally {
    dataExporting.value = false
  }
}

onMounted(loadTypes)
</script>

<template>
  <div class="dict-layout">
    <el-card shadow="never" class="panel">
      <div class="toolbar">
        <el-input
          v-model="typeKeyword"
          clearable
          placeholder="编码/名称"
          style="width: 160px"
          @keyup.enter="onTypeSearch"
        />
        <el-button v-permission="'system:dict:query'" type="primary" @click="onTypeSearch">查询</el-button>
        <el-button v-permission="'system:dict:add'" type="success" @click="openCreateType">新增</el-button>
        <el-button v-permission="'system:dict:export'" :loading="typeExporting" @click="onExportTypes">导出</el-button>
      </div>
      <el-table
        v-loading="typeLoading"
        :data="typeRows"
        stripe
        highlight-current-row
        :current-row-key="selectedType?.id"
        row-key="id"
        @row-click="selectType"
      >
        <el-table-column prop="name" label="字典名称" min-width="110" />
        <el-table-column prop="code" label="编码" min-width="120" show-overflow-tooltip />
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-switch
              v-if="canEdit"
              :model-value="row.status"
              :loading="typeStatusLoadingId === row.id"
              inline-prompt
              active-text="启"
              inactive-text="停"
              @click.stop
              @change="(val: string | number | boolean) => onTypeStatusChange(row, val)"
            />
            <el-tag v-else :type="row.status ? 'success' : 'info'" size="small">
              {{ row.status ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button v-permission="'system:dict:edit'" link type="primary" @click.stop="openEditType(row)">
              编辑
            </el-button>
            <el-button v-permission="'system:dict:remove'" link type="danger" @click.stop="onRemoveType(row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pager">
        <el-pagination
          v-model:current-page="typePage"
          v-model:page-size="typeSize"
          small
          background
          layout="total, prev, pager, next"
          :total="typeTotal"
          @current-change="loadTypes"
          @size-change="
            () => {
              typePage = 1
              loadTypes()
            }
          "
        />
      </div>
    </el-card>

    <el-card shadow="never" class="panel panel-right">
      <div class="toolbar">
        <span class="panel-title">{{ selectedType ? `字典数据 - ${selectedType.name}` : '请选择左侧字典类型' }}</span>
        <el-input
          v-model="dataKeyword"
          clearable
          placeholder="标签/键值"
          style="width: 160px"
          :disabled="!selectedType"
          @keyup.enter="onDataSearch"
        />
        <el-button v-permission="'system:dict:query'" type="primary" :disabled="!selectedType" @click="onDataSearch">
          查询
        </el-button>
        <el-button v-permission="'system:dict:add'" type="success" :disabled="!selectedType" @click="openCreateData">
          新增
        </el-button>
        <el-button
          v-permission="'system:dict:export'"
          :loading="dataExporting"
          :disabled="!selectedType"
          @click="onExportData"
        >
          导出
        </el-button>
      </div>
      <el-table v-loading="dataLoading" :data="dataRows" stripe>
        <el-table-column prop="label" label="标签" min-width="100" />
        <el-table-column prop="value" label="键值" min-width="100" />
        <el-table-column prop="sort" label="排序" width="70" />
        <el-table-column label="回显样式" min-width="120">
          <template #default="{ row }">
            <DictStyleTag :css-class="row.cssClass" :label="row.label" />
          </template>
        </el-table-column>
        <el-table-column label="默认" width="70">
          <template #default="{ row }">
            <el-tag :type="row.defaultFlag ? 'warning' : 'info'" size="small">
              {{ row.defaultFlag ? '是' : '否' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-switch
              v-if="canEdit"
              :model-value="row.status"
              :loading="dataStatusLoadingId === row.id"
              inline-prompt
              active-text="启"
              inactive-text="停"
              @change="(val: string | number | boolean) => onDataStatusChange(row, val)"
            />
            <el-tag v-else :type="row.status ? 'success' : 'info'" size="small">
              {{ row.status ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button v-permission="'system:dict:edit'" link type="primary" @click="openEditData(row)">编辑</el-button>
            <el-button v-permission="'system:dict:remove'" link type="danger" @click="onRemoveData(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pager">
        <el-pagination
          v-model:current-page="dataPage"
          v-model:page-size="dataSize"
          background
          layout="total, sizes, prev, pager, next"
          :total="dataTotal"
          :page-sizes="[10, 20, 50, 100]"
          :disabled="!selectedType"
          @current-change="loadData"
          @size-change="
            () => {
              dataPage = 1
              loadData()
            }
          "
        />
      </div>
    </el-card>
  </div>

  <el-dialog v-model="typeDialogVisible" :title="typeEditingId == null ? '新增字典类型' : '编辑字典类型'" width="480px">
    <el-form ref="typeFormRef" :model="typeForm" :rules="typeRules" label-width="90px">
      <el-form-item label="编码" prop="code">
        <el-input v-model="typeForm.code" :disabled="typeEditingId != null" placeholder="如 sys_gender" />
      </el-form-item>
      <el-form-item label="名称" prop="name">
        <el-input v-model="typeForm.name" />
      </el-form-item>
      <el-form-item label="排序">
        <el-input-number v-model="typeForm.sort" :min="0" />
      </el-form-item>
      <el-form-item label="状态">
        <el-switch v-model="typeForm.status" />
      </el-form-item>
      <el-form-item label="备注">
        <el-input v-model="typeForm.remark" type="textarea" :rows="2" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="typeDialogVisible = false">取消</el-button>
      <el-button type="primary" @click="submitType">保存</el-button>
    </template>
  </el-dialog>

  <el-dialog v-model="dataDialogVisible" :title="dataEditingId == null ? '新增字典数据' : '编辑字典数据'" width="580px">
    <el-form ref="dataFormRef" :model="dataForm" :rules="dataRules" label-width="90px">
      <el-form-item label="类型编码">
        <el-input v-model="dataForm.typeCode" disabled />
      </el-form-item>
      <el-form-item label="标签" prop="label">
        <el-input v-model="dataForm.label" />
      </el-form-item>
      <el-form-item label="键值" prop="value">
        <el-input v-model="dataForm.value" />
      </el-form-item>
      <el-form-item label="排序">
        <el-input-number v-model="dataForm.sort" :min="0" />
      </el-form-item>
      <el-form-item label="回显样式">
        <div class="style-picker">
          <button
            v-for="opt in DICT_STYLE_OPTIONS"
            :key="opt.value || 'default'"
            type="button"
            class="style-swatch"
            :class="{ active: dataForm.cssClass === opt.value }"
            :title="opt.label"
            :style="{ backgroundColor: opt.bg, borderColor: opt.border, color: opt.color }"
            @click="dataForm.cssClass = opt.value"
          >
            <span class="style-swatch-dot" :style="{ backgroundColor: opt.color }" />
            <span class="style-swatch-name">{{ opt.label }}</span>
          </button>
        </div>
        <div class="css-preview">
          预览：
          <DictStyleTag :css-class="dataForm.cssClass" :label="dataForm.label || '示例标签'" />
        </div>
      </el-form-item>
      <el-form-item label="默认项">
        <el-switch v-model="dataForm.defaultFlag" />
      </el-form-item>
      <el-form-item label="状态">
        <el-switch v-model="dataForm.status" />
      </el-form-item>
      <el-form-item label="备注">
        <el-input v-model="dataForm.remark" type="textarea" :rows="2" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="dataDialogVisible = false">取消</el-button>
      <el-button type="primary" @click="submitData">保存</el-button>
    </template>
  </el-dialog>
</template>

<style scoped>
.dict-layout {
  display: grid;
  grid-template-columns: minmax(360px, 2fr) minmax(480px, 3fr);
  gap: 12px;
  align-items: start;
}
.panel {
  min-width: 0;
}
.toolbar {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 16px;
  align-items: center;
}
.panel-title {
  margin-right: auto;
  font-weight: 600;
  color: var(--el-text-color-primary);
}
.pager {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
.style-picker {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 8px;
  width: 100%;
}
.style-swatch {
  display: flex;
  align-items: center;
  gap: 6px;
  min-width: 0;
  padding: 7px 8px;
  border: 1px solid;
  border-radius: 8px;
  cursor: pointer;
  background: #fff;
  transition: box-shadow 0.15s ease, transform 0.15s ease;
}
.style-swatch:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(15, 23, 42, 0.08);
}
.style-swatch.active {
  box-shadow: 0 0 0 2px #2563eb inset;
}
.style-swatch-dot {
  flex: 0 0 10px;
  width: 10px;
  height: 10px;
  border-radius: 50%;
}
.style-swatch-name {
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-size: 12px;
  font-weight: 600;
}
.css-preview {
  margin-top: 10px;
  color: var(--el-text-color-secondary);
  font-size: 13px;
  display: flex;
  align-items: center;
  gap: 8px;
}
@media (max-width: 1100px) {
  .dict-layout {
    grid-template-columns: 1fr;
  }
}
</style>
