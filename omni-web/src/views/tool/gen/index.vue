<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import {
  downloadGenCode,
  fetchGenDictTypes,
  fetchGenTableConfig,
  fetchGenTables,
  previewGenCode,
  type GenFileView,
  type GenQueryType,
  type GenTableConfig,
} from '@/api/tool/gen'
import type { DictTypeView, MysqlTableView } from '@/types/api'

const step = ref(0)
const tablesLoading = ref(false)
const tables = ref<MysqlTableView[]>([])
const tableKeyword = ref('')
const tablePage = ref(1)
const tableSize = ref(10)
const tableTotal = ref(0)
const selectedTable = ref('')
const dictTypesLoading = ref(false)
const dictTypes = ref<DictTypeView[]>([])

const configLoading = ref(false)
const config = reactive<GenTableConfig>({
  tableName: '',
  tableComment: '',
  moduleName: 'demo',
  businessName: '',
  className: '',
  functionName: '',
  packageName: 'com.omni.scaffolding.modules.demo',
  permissionPrefix: 'demo:',
  author: 'omni',
  menuParentId: 200,
  menuIdStart: 5000,
  menuSort: 99,
  columns: [],
})

const previewLoading = ref(false)
const downloadLoading = ref(false)
const previewFiles = ref<GenFileView[]>([])
const activeFile = ref('')

const queryTypes: { label: string; value: GenQueryType }[] = [
  { label: '无', value: 'NONE' },
  { label: '等于', value: 'EQ' },
  { label: '模糊', value: 'LIKE' },
  { label: '范围', value: 'BETWEEN' },
]

const activeContent = computed(() => {
  const hit = previewFiles.value.find((f) => f.path === activeFile.value)
  return hit?.content || ''
})

async function loadTables() {
  tablesLoading.value = true
  try {
    const data = await fetchGenTables({
      keyword: tableKeyword.value || undefined,
      page: tablePage.value,
      size: tableSize.value,
    })
    tables.value = data.records
    tablePage.value = data.page
    tableSize.value = data.size
    tableTotal.value = data.total
  } finally {
    tablesLoading.value = false
  }
}

async function loadDictTypes() {
  dictTypesLoading.value = true
  try {
    dictTypes.value = await fetchGenDictTypes()
  } finally {
    dictTypesLoading.value = false
  }
}

function searchTables() {
  tablePage.value = 1
  loadTables()
}

async function loadConfig(table: string) {
  selectedTable.value = table
  configLoading.value = true
  try {
    const data = await fetchGenTableConfig(table)
    Object.assign(config, data)
    if (!config.columns) config.columns = []
    step.value = 1
  } finally {
    configLoading.value = false
  }
}

function syncPackage() {
  if (config.moduleName) {
    config.packageName = `com.omni.scaffolding.modules.${config.moduleName}`
  }
}

function syncPerm() {
  if (config.moduleName && config.businessName) {
    config.permissionPrefix = `${config.moduleName}:${config.businessName}`
  }
}

async function onPreview() {
  if (!config.tableName || !config.columns.length) {
    ElMessage.warning('请先选择数据表')
    return
  }
  previewLoading.value = true
  try {
    previewFiles.value = await previewGenCode({ ...config, columns: [...config.columns] })
    activeFile.value = previewFiles.value[0]?.path || ''
    step.value = 2
    ElMessage.success('预览已生成')
  } finally {
    previewLoading.value = false
  }
}

async function onDownload() {
  if (!config.tableName || !config.columns.length) {
    ElMessage.warning('请先选择数据表')
    return
  }
  downloadLoading.value = true
  try {
    await downloadGenCode({ ...config, columns: [...config.columns] })
    ElMessage.success('ZIP 已开始下载')
  } catch {
    // ignore
  } finally {
    downloadLoading.value = false
  }
}

onMounted(() => {
  loadTables()
  loadDictTypes()
})
</script>

<template>
  <el-card shadow="never">
    <el-steps :active="step" finish-status="success" align-center style="margin-bottom: 20px">
      <el-step title="选择表" />
      <el-step title="配置字段" />
      <el-step title="预览下载" />
    </el-steps>

    <div v-show="step === 0">
      <div class="toolbar">
        <el-input
          v-model="tableKeyword"
          clearable
          placeholder="表名关键字"
          style="width: 220px"
          @keyup.enter="searchTables"
        />
        <el-button v-permission="'tool:gen:query'" type="primary" :loading="tablesLoading" @click="searchTables">
          查询
        </el-button>
      </div>
      <el-table v-loading="tablesLoading" :data="tables" stripe highlight-current-row @row-click="(row: MysqlTableView) => loadConfig(row.name)">
        <el-table-column prop="name" label="表名" min-width="180" />
        <el-table-column prop="comment" label="注释" min-width="200" show-overflow-tooltip />
        <el-table-column prop="engine" label="引擎" width="100" />
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button v-permission="'tool:gen:query'" link type="primary" :loading="configLoading && selectedTable === row.name" @click.stop="loadConfig(row.name)">
              选择
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pager">
        <el-pagination
          v-model:current-page="tablePage"
          v-model:page-size="tableSize"
          background
          layout="total, sizes, prev, pager, next, jumper"
          :total="tableTotal"
          :page-sizes="[10, 20, 50, 100]"
          @current-change="loadTables"
          @size-change="
            () => {
              tablePage = 1
              loadTables()
            }
          "
        />
      </div>
    </div>

    <div v-show="step === 1" v-loading="configLoading">
      <el-form label-width="110px" class="cfg-form">
        <el-row :gutter="16">
          <el-col :span="8">
            <el-form-item label="表名">
              <el-input v-model="config.tableName" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="功能名">
              <el-input v-model="config.functionName" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="作者">
              <el-input v-model="config.author" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="模块名">
              <el-input v-model="config.moduleName" @change="() => { syncPackage(); syncPerm() }" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="业务名">
              <el-input v-model="config.businessName" @change="syncPerm" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="实体类名">
              <el-input v-model="config.className" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="包路径">
              <el-input v-model="config.packageName" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="权限前缀">
              <el-input v-model="config.permissionPrefix" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="菜单父级ID">
              <el-input-number v-model="config.menuParentId" :min="0" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="菜单起始ID">
              <el-input-number v-model="config.menuIdStart" :min="1" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="菜单排序">
              <el-input-number v-model="config.menuSort" :min="0" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>

      <el-table :data="config.columns" stripe border max-height="420">
        <el-table-column prop="columnName" label="列名" min-width="120" fixed />
        <el-table-column prop="columnType" label="类型" width="120" />
        <el-table-column label="显示名" min-width="120">
          <template #default="{ row }">
            <el-input v-model="row.columnComment" size="small" />
          </template>
        </el-table-column>
        <el-table-column label="Java字段" min-width="120">
          <template #default="{ row }">
            <el-input v-model="row.javaField" size="small" />
          </template>
        </el-table-column>
        <el-table-column prop="javaType" label="Java类型" width="100" />
        <el-table-column label="列表" width="70" align="center">
          <template #default="{ row }">
            <el-checkbox v-model="row.list" />
          </template>
        </el-table-column>
        <el-table-column label="表单" width="70" align="center">
          <template #default="{ row }">
            <el-checkbox v-model="row.form" />
          </template>
        </el-table-column>
        <el-table-column label="必填" width="70" align="center">
          <template #default="{ row }">
            <el-checkbox v-model="row.required" :disabled="!row.form" />
          </template>
        </el-table-column>
        <el-table-column label="查询方式" width="130">
          <template #default="{ row }">
            <el-select v-model="row.queryType" size="small" style="width: 110px">
              <el-option v-for="opt in queryTypes" :key="opt.value" :label="opt.label" :value="opt.value" />
            </el-select>
          </template>
        </el-table-column>
        <el-table-column label="字典类型" min-width="150">
          <template #default="{ row }">
            <el-select
              v-model="row.dictType"
              clearable
              filterable
              size="small"
              placeholder="选择字典"
              :loading="dictTypesLoading"
              style="width: 100%"
            >
              <el-option
                v-for="dict in dictTypes"
                :key="dict.code"
                :label="`${dict.name}（${dict.code}）`"
                :value="dict.code"
              />
            </el-select>
          </template>
        </el-table-column>
        <el-table-column label="标记" width="120">
          <template #default="{ row }">
            <el-tag v-if="row.pk" size="small" type="danger">PK</el-tag>
            <el-tag v-if="row.audit" size="small" type="info">审计</el-tag>
            <el-tag v-if="row.logicDelete" size="small" type="warning">删除</el-tag>
          </template>
        </el-table-column>
      </el-table>

      <div class="actions">
        <el-button @click="step = 0">上一步</el-button>
        <el-button v-permission="'tool:gen:preview'" type="primary" :loading="previewLoading" @click="onPreview">
          预览
        </el-button>
        <el-button v-permission="'tool:gen:code'" type="success" :loading="downloadLoading" @click="onDownload">
          下载 ZIP
        </el-button>
      </div>
    </div>

    <div v-show="step === 2">
      <div class="actions" style="margin-top: 0; margin-bottom: 12px">
        <el-button @click="step = 1">返回配置</el-button>
        <el-button v-permission="'tool:gen:preview'" :loading="previewLoading" @click="onPreview">刷新预览</el-button>
        <el-button v-permission="'tool:gen:code'" type="success" :loading="downloadLoading" @click="onDownload">
          下载 ZIP
        </el-button>
      </div>
      <el-row :gutter="12">
        <el-col :span="8">
          <el-scrollbar height="560px">
            <el-menu :default-active="activeFile" @select="(path: string) => (activeFile = path)">
              <el-menu-item v-for="f in previewFiles" :key="f.path" :index="f.path">
                <span class="file-path" :title="f.path">{{ f.path }}</span>
              </el-menu-item>
            </el-menu>
          </el-scrollbar>
        </el-col>
        <el-col :span="16">
          <el-input
            type="textarea"
            :model-value="activeContent"
            :rows="28"
            readonly
            class="code-view"
          />
        </el-col>
      </el-row>
    </div>
  </el-card>
</template>

<style scoped>
.toolbar {
  display: flex;
  gap: 8px;
  margin-bottom: 12px;
}
.cfg-form {
  margin-bottom: 12px;
}
.actions {
  display: flex;
  gap: 8px;
  margin-top: 16px;
}
.pager {
  display: flex;
  justify-content: flex-end;
  margin-top: 12px;
}
.file-path {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-size: 12px;
}
.code-view :deep(textarea) {
  font-family: ui-monospace, SFMono-Regular, Menlo, Consolas, monospace;
  font-size: 12px;
  line-height: 1.45;
}
</style>
