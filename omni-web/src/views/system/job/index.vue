<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import {
  changeJobStatus,
  clearJobLogs,
  createJob,
  listJobLogs,
  listJobs,
  removeJob,
  runJobOnce,
  updateJob,
} from '@/api/system/job'
import CronEditor from '@/components/CronEditor.vue'
import { useUserStore } from '@/stores/user'
import type { JobLogView, JobView } from '@/types/api'

const userStore = useUserStore()
const canEdit = computed(() => userStore.hasPermission('system:job:edit'))
const canRun = computed(() => userStore.hasPermission('system:job:run'))
const canRemove = computed(() => userStore.hasPermission('system:job:remove'))

const loading = ref(false)
const rows = ref<JobView[]>([])
const keyword = ref('')
const statusFilter = ref<boolean | undefined>(undefined)
const page = ref(1)
const size = ref(10)
const total = ref(0)
const statusLoadingId = ref<number | null>(null)

const dialogVisible = ref(false)
const editingId = ref<number | null>(null)
const formRef = ref<FormInstance>()
const form = reactive({
  jobName: '',
  jobGroup: 'omni-job',
  invokeTarget: 'sampleScheduledTasks.ping',
  jobParams: '',
  cronExpression: '0 0/5 * * * ?',
  misfirePolicy: 0,
  concurrent: false,
  status: true,
  remark: '',
})

const rules: FormRules = {
  jobName: [{ required: true, message: '请输入任务名称', trigger: 'blur' }],
  invokeTarget: [{ required: true, message: '请输入调用目标', trigger: 'blur' }],
  cronExpression: [{ required: true, message: '请输入 Cron', trigger: 'blur' }],
}

const logVisible = ref(false)
const logJob = ref<JobView | null>(null)
const logLoading = ref(false)
const logRows = ref<JobLogView[]>([])
const logPage = ref(1)
const logSize = ref(10)
const logTotal = ref(0)

async function load() {
  loading.value = true
  try {
    const data = await listJobs({
      keyword: keyword.value || undefined,
      status: statusFilter.value,
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
    jobName: '',
    jobGroup: 'omni-job',
    invokeTarget: 'sampleScheduledTasks.ping',
    jobParams: '',
    cronExpression: '0 0/5 * * * ?',
    misfirePolicy: 0,
    concurrent: false,
    status: true,
    remark: '',
  })
  dialogVisible.value = true
}

function openEdit(row: JobView) {
  editingId.value = row.id
  Object.assign(form, {
    jobName: row.jobName,
    jobGroup: row.jobGroup || 'omni-job',
    invokeTarget: row.invokeTarget,
    jobParams: row.jobParams || '',
    cronExpression: row.cronExpression,
    misfirePolicy: row.misfirePolicy ?? 0,
    concurrent: !!row.concurrent,
    status: !!row.status,
    remark: row.remark || '',
  })
  dialogVisible.value = true
}

async function submit() {
  if (!formRef.value) return
  await formRef.value.validate()
  const body = {
    jobName: form.jobName.trim(),
    jobGroup: form.jobGroup.trim() || 'omni-job',
    invokeTarget: form.invokeTarget.trim(),
    jobParams: form.jobParams?.trim() || undefined,
    cronExpression: form.cronExpression.trim(),
    misfirePolicy: form.misfirePolicy,
    concurrent: form.concurrent,
    status: form.status,
    remark: form.remark || undefined,
  }
  if (editingId.value == null) {
    await createJob(body)
    ElMessage.success('已创建')
  } else {
    await updateJob(editingId.value, body)
    ElMessage.success('已保存')
  }
  dialogVisible.value = false
  await load()
}

async function onStatusChange(row: JobView, value: boolean) {
  statusLoadingId.value = row.id
  try {
    await changeJobStatus(row.id, value)
    row.status = value
    ElMessage.success(value ? '已启用' : '已停用')
  } catch {
    row.status = !value
  } finally {
    statusLoadingId.value = null
  }
}

async function onRun(row: JobView) {
  await ElMessageBox.confirm(`确认立即执行任务「${row.jobName}」？`, '提示', { type: 'info' })
  await runJobOnce(row.id)
  ElMessage.success('已触发执行')
}

async function onRemove(row: JobView) {
  await ElMessageBox.confirm(`确认删除任务「${row.jobName}」？`, '提示', { type: 'warning' })
  await removeJob(row.id)
  ElMessage.success('已删除')
  await load()
}

async function openLogs(row: JobView) {
  logJob.value = row
  logPage.value = 1
  logVisible.value = true
  await loadLogs()
}

async function loadLogs() {
  if (!logJob.value) return
  logLoading.value = true
  try {
    const data = await listJobLogs(logJob.value.id, { page: logPage.value, size: logSize.value })
    logRows.value = data.records
    logTotal.value = data.total
    logPage.value = data.page
    logSize.value = data.size
  } finally {
    logLoading.value = false
  }
}

async function onClearLogs() {
  if (!logJob.value) return
  await ElMessageBox.confirm('确认清空该任务的全部执行日志？', '提示', { type: 'warning' })
  await clearJobLogs(logJob.value.id)
  ElMessage.success('已清空')
  await loadLogs()
}

onMounted(load)
</script>

<template>
  <div class="page">
    <el-card shadow="never">
      <div class="toolbar">
        <el-input v-model="keyword" clearable placeholder="名称 / 调用目标 / Cron" style="width: 240px" @keyup.enter="onSearch" />
        <el-select v-model="statusFilter" clearable placeholder="状态" style="width: 120px">
          <el-option label="启用" :value="true" />
          <el-option label="停用" :value="false" />
        </el-select>
        <el-button v-permission="'system:job:query'" type="primary" @click="onSearch">查询</el-button>
        <el-button v-permission="'system:job:add'" type="success" @click="openCreate">新增</el-button>
      </div>

      <el-table v-loading="loading" :data="rows" stripe>
        <el-table-column prop="jobName" label="任务名称" min-width="140" show-overflow-tooltip />
        <el-table-column prop="invokeTarget" label="调用目标" min-width="200" show-overflow-tooltip />
        <el-table-column prop="cronExpression" label="Cron" min-width="140" show-overflow-tooltip />
        <el-table-column label="参数" width="80">
          <template #default="{ row }">{{ row.jobParams ? '有参' : '无参' }}</template>
        </el-table-column>
        <el-table-column label="并发" width="70">
          <template #default="{ row }">{{ row.concurrent ? '是' : '否' }}</template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-switch
              :model-value="row.status"
              :disabled="!canEdit"
              :loading="statusLoadingId === row.id"
              @change="(v: boolean) => onStatusChange(row, v)"
            />
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="120" show-overflow-tooltip />
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="{ row }">
            <el-button v-if="canEdit" link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button v-if="canRun" link type="primary" @click="onRun(row)">执行一次</el-button>
            <el-button v-permission="'system:job:query'" link type="primary" @click="openLogs(row)">日志</el-button>
            <el-button v-if="canRemove" link type="danger" @click="onRemove(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pager">
        <el-pagination
          v-model:current-page="page"
          v-model:page-size="size"
          :total="total"
          layout="total, sizes, prev, pager, next"
          :page-sizes="[10, 20, 50]"
          @current-change="load"
          @size-change="onSearch"
        />
      </div>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="editingId == null ? '新增定时任务' : '编辑定时任务'" width="720px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="任务名称" prop="jobName">
          <el-input v-model="form.jobName" maxlength="128" show-word-limit />
        </el-form-item>
        <el-form-item label="调用目标" prop="invokeTarget">
          <el-input v-model="form.invokeTarget" placeholder="beanName.methodName，如 sampleScheduledTasks.ping" />
        </el-form-item>
        <el-form-item label="任务参数">
          <el-input
            v-model="form.jobParams"
            type="textarea"
            :rows="3"
            placeholder="可空=无参；有值则调用 method(String)，如 {&quot;msg&quot;:&quot;hi&quot;}"
          />
        </el-form-item>
        <el-form-item label="Cron" prop="cronExpression">
          <CronEditor v-model="form.cronExpression" />
        </el-form-item>
        <el-form-item label="错过策略">
          <el-select v-model="form.misfirePolicy" style="width: 100%">
            <el-option label="忽略（默认）" :value="0" />
            <el-option label="立即触发一次" :value="1" />
            <el-option label="触发所有错过" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="允许并发">
          <el-switch v-model="form.concurrent" />
        </el-form-item>
        <el-form-item label="启用">
          <el-switch v-model="form.status" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" maxlength="255" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submit">保存</el-button>
      </template>
    </el-dialog>

    <el-drawer v-model="logVisible" :title="logJob ? `执行日志 · ${logJob.jobName}` : '执行日志'" size="720px">
      <div class="toolbar">
        <el-button v-permission="'system:job:query'" type="primary" :loading="logLoading" @click="loadLogs">刷新</el-button>
        <el-button v-if="canRemove" type="danger" plain @click="onClearLogs">清空</el-button>
      </div>
      <el-table v-loading="logLoading" :data="logRows" stripe size="small">
        <el-table-column label="结果" width="70">
          <template #default="{ row }">
            <el-tag :type="row.status ? 'success' : 'danger'" size="small">{{ row.status ? '成功' : '失败' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="startTime" label="开始时间" width="170" />
        <el-table-column prop="costMs" label="耗时ms" width="80" />
        <el-table-column prop="message" label="消息" min-width="180" show-overflow-tooltip />
      </el-table>
      <div class="pager">
        <el-pagination
          v-model:current-page="logPage"
          v-model:page-size="logSize"
          :total="logTotal"
          layout="total, prev, pager, next"
          @current-change="loadLogs"
          @size-change="loadLogs"
        />
      </div>
    </el-drawer>
  </div>
</template>

<style scoped>
.page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.toolbar {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 16px;
  align-items: center;
}
.pager {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
