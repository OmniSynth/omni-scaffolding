<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import {
  analyzeMysqlTable,
  createMysqlIndex,
  dropMysqlIndex,
  fetchMysqlOverview,
  fetchMysqlProcesses,
  fetchMysqlTableDetail,
  fetchMysqlTables,
  killMysqlProcess,
} from '@/api/ops/mysql'
import { useUserStore } from '@/stores/user'
import type {
  MysqlOverviewView,
  MysqlProcessView,
  MysqlTableDetailView,
  MysqlTableView,
} from '@/types/api'

const userStore = useUserStore()
const canQuery = computed(() => userStore.hasPermission('ops:mysql:query'))
const canEdit = computed(() => userStore.hasPermission('ops:mysql:edit'))
const canRemove = computed(() => userStore.hasPermission('ops:mysql:remove'))

const activeTab = ref('tables')

const overviewLoading = ref(false)
const overview = ref<MysqlOverviewView | null>(null)

const tablesLoading = ref(false)
const tables = ref<MysqlTableView[]>([])
const keyword = ref('')

const detailVisible = ref(false)
const detailLoading = ref(false)
const detail = ref<MysqlTableDetailView | null>(null)

const indexVisible = ref(false)
const indexRef = ref<FormInstance>()
const indexForm = reactive({
  table: '',
  name: '',
  columnsText: '',
  unique: false,
})
const indexRules: FormRules = {
  table: [{ required: true, message: '请选择表', trigger: 'change' }],
  name: [{ required: true, message: '请输入索引名', trigger: 'blur' }],
  columnsText: [{ required: true, message: '请输入列名，逗号分隔', trigger: 'blur' }],
}

const processesLoading = ref(false)
const processes = ref<MysqlProcessView[]>([])

function formatBytes(n?: number): string {
  if (n == null || Number.isNaN(n)) return '-'
  if (n < 1024) return `${n} B`
  const units = ['KB', 'MB', 'GB', 'TB']
  let v = n
  let i = -1
  do {
    v /= 1024
    i++
  } while (v >= 1024 && i < units.length - 1)
  return `${v.toFixed(v >= 10 || i === 0 ? 1 : 2)} ${units[i]}`
}

async function loadOverview() {
  if (!canQuery.value) return
  overviewLoading.value = true
  try {
    overview.value = await fetchMysqlOverview()
  } finally {
    overviewLoading.value = false
  }
}

async function loadTables() {
  if (!canQuery.value) return
  tablesLoading.value = true
  try {
    tables.value = await fetchMysqlTables(keyword.value || undefined)
  } finally {
    tablesLoading.value = false
  }
}

async function openDetail(row: MysqlTableView) {
  detailVisible.value = true
  detailLoading.value = true
  detail.value = null
  try {
    detail.value = await fetchMysqlTableDetail(row.name)
  } finally {
    detailLoading.value = false
  }
}

function openCreateIndex(row?: MysqlTableView) {
  indexForm.table = row?.name || detail.value?.table.name || ''
  indexForm.name = ''
  indexForm.columnsText = ''
  indexForm.unique = false
  indexVisible.value = true
}

async function submitCreateIndex() {
  if (!indexRef.value) return
  await indexRef.value.validate()
  const columns = indexForm.columnsText
    .split(/[,，\s]+/)
    .map((c) => c.trim())
    .filter(Boolean)
  if (!columns.length) {
    ElMessage.warning('请至少指定一列')
    return
  }
  const result = await createMysqlIndex({
    table: indexForm.table,
    name: indexForm.name.trim(),
    columns,
    unique: indexForm.unique,
  })
  ElMessage.success('索引已创建')
  indexVisible.value = false
  if (detailVisible.value && detail.value?.table.name === result.table.name) {
    detail.value = result
  }
  await loadTables()
  await loadOverview()
}

async function onDropIndex(indexName: string) {
  if (!detail.value) return
  if (indexName.toUpperCase() === 'PRIMARY') {
    ElMessage.warning('禁止删除主键')
    return
  }
  await ElMessageBox.confirm(`确认删除索引 ${indexName}？`, '提示', { type: 'warning' })
  const result = await dropMysqlIndex({
    table: detail.value.table.name,
    name: indexName,
  })
  ElMessage.success('索引已删除')
  detail.value = result
  await loadTables()
}

async function onAnalyze(row: MysqlTableView) {
  await ElMessageBox.confirm(`确认对表 ${row.name} 执行 ANALYZE？`, '提示', { type: 'info' })
  await analyzeMysqlTable(row.name)
  ElMessage.success('ANALYZE 完成')
  await loadTables()
  if (detailVisible.value && detail.value?.table.name === row.name) {
    detail.value = await fetchMysqlTableDetail(row.name)
  }
}

async function loadProcesses() {
  if (!canQuery.value) return
  processesLoading.value = true
  try {
    processes.value = await fetchMysqlProcesses()
  } finally {
    processesLoading.value = false
  }
}

async function onKill(row: MysqlProcessView) {
  await ElMessageBox.confirm(`确认 Kill 连接 ${row.id}（${row.user}@${row.host}）？`, '警告', {
    type: 'warning',
  })
  await killMysqlProcess(row.id)
  ElMessage.success(`已 Kill ${row.id}`)
  await loadProcesses()
}

async function onTabChange(name: string | number) {
  if (name === 'processes') {
    await loadProcesses()
  } else if (name === 'tables') {
    await loadTables()
  }
}

onMounted(async () => {
  await loadOverview()
  await loadTables()
})
</script>

<template>
  <div class="mysql-page">
    <el-card shadow="never" v-loading="overviewLoading">
      <template #header>
        <div class="card-header">
          <span>MySQL 概览</span>
          <el-button v-permission="'ops:mysql:query'" size="small" @click="loadOverview">刷新</el-button>
        </div>
      </template>
      <el-descriptions v-if="overview" :column="3" border size="small">
        <el-descriptions-item label="版本">{{ overview.version || '-' }}</el-descriptions-item>
        <el-descriptions-item label="当前库">{{ overview.schema || '-' }}</el-descriptions-item>
        <el-descriptions-item label="表数量">{{ overview.tableCount ?? '-' }}</el-descriptions-item>
        <el-descriptions-item label="行数估算">{{ overview.totalRows ?? '-' }}</el-descriptions-item>
        <el-descriptions-item label="数据大小">{{ formatBytes(overview.dataLength) }}</el-descriptions-item>
        <el-descriptions-item label="索引大小">{{ formatBytes(overview.indexLength) }}</el-descriptions-item>
        <el-descriptions-item label="字符集">{{ overview.characterSet || '-' }}</el-descriptions-item>
        <el-descriptions-item label="排序规则">{{ overview.collation || '-' }}</el-descriptions-item>
        <el-descriptions-item label="碎片空间">{{ formatBytes(overview.dataFree) }}</el-descriptions-item>
      </el-descriptions>
      <el-empty v-else description="暂无数据（需 ops:mysql:query）" />
    </el-card>

    <el-card shadow="never" class="mt">
      <el-tabs v-model="activeTab" @tab-change="onTabChange">
        <el-tab-pane label="表" name="tables">
          <div class="toolbar">
            <el-input
              v-model="keyword"
              clearable
              placeholder="表名，支持 % 通配"
              style="width: 240px"
              @keyup.enter="loadTables"
            />
            <el-button v-permission="'ops:mysql:query'" type="primary" :loading="tablesLoading" @click="loadTables">
              查询
            </el-button>
            <el-button v-permission="'ops:mysql:edit'" type="success" @click="openCreateIndex()">新建索引</el-button>
          </div>
          <el-table v-loading="tablesLoading" :data="tables" stripe>
            <el-table-column prop="name" label="表名" min-width="160" show-overflow-tooltip />
            <el-table-column prop="engine" label="引擎" width="100" />
            <el-table-column prop="tableRows" label="行数估算" width="110" />
            <el-table-column label="数据" width="110">
              <template #default="{ row }">{{ formatBytes(row.dataLength) }}</template>
            </el-table-column>
            <el-table-column label="索引" width="110">
              <template #default="{ row }">{{ formatBytes(row.indexLength) }}</template>
            </el-table-column>
            <el-table-column prop="comment" label="注释" min-width="140" show-overflow-tooltip />
            <el-table-column label="操作" width="220" fixed="right">
              <template #default="{ row }">
                <el-button v-if="canQuery" link type="primary" @click="openDetail(row)">结构</el-button>
                <el-button v-if="canEdit" link type="primary" @click="openCreateIndex(row)">索引</el-button>
                <el-button v-if="canEdit" link type="primary" @click="onAnalyze(row)">ANALYZE</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <el-tab-pane label="进程" name="processes">
          <div class="toolbar">
            <el-button v-permission="'ops:mysql:query'" type="primary" :loading="processesLoading" @click="loadProcesses">
              刷新
            </el-button>
          </div>
          <el-table v-loading="processesLoading" :data="processes" stripe>
            <el-table-column prop="id" label="ID" width="80" />
            <el-table-column prop="user" label="用户" width="100" />
            <el-table-column prop="host" label="Host" min-width="140" show-overflow-tooltip />
            <el-table-column prop="db" label="库" width="120" show-overflow-tooltip />
            <el-table-column prop="command" label="命令" width="100" />
            <el-table-column prop="time" label="秒" width="70" />
            <el-table-column prop="state" label="状态" width="120" show-overflow-tooltip />
            <el-table-column prop="info" label="SQL" min-width="200" show-overflow-tooltip />
            <el-table-column label="操作" width="90" fixed="right">
              <template #default="{ row }">
                <el-button v-if="canRemove" link type="danger" @click="onKill(row)">Kill</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <el-drawer v-model="detailVisible" :title="detail ? `表结构 · ${detail.table.name}` : '表结构'" size="720px">
      <el-skeleton v-if="detailLoading" :rows="10" animated />
      <template v-else-if="detail">
        <el-descriptions :column="2" border size="small">
          <el-descriptions-item label="引擎">{{ detail.table.engine || '-' }}</el-descriptions-item>
          <el-descriptions-item label="行数">{{ detail.table.tableRows ?? '-' }}</el-descriptions-item>
          <el-descriptions-item label="数据">{{ formatBytes(detail.table.dataLength) }}</el-descriptions-item>
          <el-descriptions-item label="索引">{{ formatBytes(detail.table.indexLength) }}</el-descriptions-item>
          <el-descriptions-item label="注释" :span="2">{{ detail.table.comment || '-' }}</el-descriptions-item>
        </el-descriptions>

        <div class="section-title">
          <span>列</span>
        </div>
        <el-table :data="detail.columns" size="small" stripe max-height="260">
          <el-table-column prop="name" label="列名" width="140" />
          <el-table-column prop="type" label="类型" width="140" />
          <el-table-column prop="nullable" label="可空" width="70" />
          <el-table-column prop="columnKey" label="键" width="60" />
          <el-table-column prop="defaultValue" label="默认" width="100" show-overflow-tooltip />
          <el-table-column prop="extra" label="额外" width="100" show-overflow-tooltip />
          <el-table-column prop="comment" label="注释" min-width="120" show-overflow-tooltip />
        </el-table>

        <div class="section-title">
          <span>索引</span>
          <el-button v-if="canEdit" size="small" type="primary" @click="openCreateIndex(detail.table)">新建</el-button>
        </div>
        <el-table :data="detail.indexes" size="small" stripe>
          <el-table-column prop="name" label="名称" width="160" />
          <el-table-column label="唯一" width="70">
            <template #default="{ row }">{{ row.unique ? '是' : '否' }}</template>
          </el-table-column>
          <el-table-column prop="indexType" label="类型" width="90" />
          <el-table-column prop="columns" label="列" min-width="160" show-overflow-tooltip />
          <el-table-column prop="cardinality" label="基数" width="90" />
          <el-table-column label="操作" width="80" fixed="right">
            <template #default="{ row }">
              <el-button
                v-if="canRemove && row.name?.toUpperCase() !== 'PRIMARY'"
                link
                type="danger"
                @click="onDropIndex(row.name)"
              >
                删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>

        <div class="section-title">DDL</div>
        <el-input type="textarea" :rows="12" :model-value="detail.ddl || ''" readonly />
      </template>
    </el-drawer>

    <el-dialog v-model="indexVisible" title="新建索引" width="520px">
      <el-form ref="indexRef" :model="indexForm" :rules="indexRules" label-width="90px">
        <el-form-item label="表名" prop="table">
          <el-select v-model="indexForm.table" filterable style="width: 100%" placeholder="选择表">
            <el-option v-for="t in tables" :key="t.name" :label="t.name" :value="t.name" />
          </el-select>
        </el-form-item>
        <el-form-item label="索引名" prop="name">
          <el-input v-model="indexForm.name" maxlength="64" show-word-limit placeholder="如 idx_user_name" />
        </el-form-item>
        <el-form-item label="列" prop="columnsText">
          <el-input v-model="indexForm.columnsText" placeholder="逗号分隔，如 username, status" />
        </el-form-item>
        <el-form-item label="唯一">
          <el-switch v-model="indexForm.unique" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="indexVisible = false">取消</el-button>
        <el-button type="primary" @click="submitCreateIndex">创建</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.mysql-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}
.toolbar {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 16px;
  align-items: center;
}
.mt {
  margin-top: 0;
}
.section-title {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin: 16px 0 8px;
  font-weight: 600;
}
</style>
