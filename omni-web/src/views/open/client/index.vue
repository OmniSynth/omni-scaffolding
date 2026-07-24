<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import {
  createOpenClient,
  getOpenClient,
  listOpenClients,
  removeOpenClient,
  resetOpenClientKeys,
  updateOpenClient,
} from '@/api/open/client'
import { listEnabledOpenEndpoints } from '@/api/open/endpoint'
import type { OpenClientCredentialsView, OpenClientView, OpenEndpointView } from '@/types/api'

const loading = ref(false)
const rows = ref<OpenClientView[]>([])
const keyword = ref('')
const page = ref(1)
const size = ref(10)
const total = ref(0)
const dialogVisible = ref(false)
const credVisible = ref(false)
const editingId = ref<number | null>(null)
const formRef = ref<FormInstance>()
const endpointOptions = ref<OpenEndpointView[]>([])
const credentials = ref<OpenClientCredentialsView | null>(null)

const form = reactive({
  name: '',
  dailyLimit: undefined as number | undefined,
  qpsLimit: undefined as number | undefined,
  expireAt: '' as string,
  remark: '',
  status: true,
  ipText: '',
  endpointIds: [] as number[],
})

const rules: FormRules = {
  name: [{ required: true, message: '请输入客户端名称', trigger: 'blur' }],
}

async function loadEndpoints() {
  endpointOptions.value = await listEnabledOpenEndpoints()
}

async function load() {
  loading.value = true
  try {
    const data = await listOpenClients({
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
  Object.assign(form, {
    name: '',
    dailyLimit: undefined,
    qpsLimit: undefined,
    expireAt: '',
    remark: '',
    status: true,
    ipText: '',
    endpointIds: [],
  })
  dialogVisible.value = true
}

async function openEdit(row: OpenClientView) {
  const detail = await getOpenClient(row.id)
  editingId.value = detail.id
  Object.assign(form, {
    name: detail.name,
    dailyLimit: detail.dailyLimit ?? undefined,
    qpsLimit: detail.qpsLimit ?? undefined,
    expireAt: detail.expireAt ? String(detail.expireAt).replace('Z', '').slice(0, 19) : '',
    remark: detail.remark || '',
    status: detail.status,
    ipText: (detail.ipList || []).join('\n'),
    endpointIds: detail.endpointIds || [],
  })
  dialogVisible.value = true
}

function buildPayload() {
  const ipList = form.ipText
    .split(/[\n,]/)
    .map((s) => s.trim())
    .filter(Boolean)
  return {
    name: form.name,
    dailyLimit: form.dailyLimit || null,
    qpsLimit: form.qpsLimit || null,
    expireAt: form.expireAt ? new Date(form.expireAt).toISOString() : null,
    remark: form.remark || undefined,
    status: form.status,
    ipList,
    endpointIds: form.endpointIds,
  }
}

async function submit() {
  if (!formRef.value) return
  await formRef.value.validate()
  const payload = buildPayload()
  if (editingId.value == null) {
    const cred = await createOpenClient(payload)
    credentials.value = cred
    credVisible.value = true
    ElMessage.success('创建成功，请妥善保存密钥')
  } else {
    await updateOpenClient(editingId.value, payload)
    ElMessage.success('更新成功')
  }
  dialogVisible.value = false
  await load()
}

async function onResetKeys(row: OpenClientView) {
  await ElMessageBox.confirm(`确认重置客户端「${row.name}」的密钥？旧 Key 将立即失效。`, '提示', {
    type: 'warning',
  })
  const cred = await resetOpenClientKeys(row.id)
  credentials.value = cred
  credVisible.value = true
  ElMessage.success('密钥已重置')
}

async function onRemove(row: OpenClientView) {
  await ElMessageBox.confirm(`确认删除客户端 ${row.name}？`, '提示', { type: 'warning' })
  await removeOpenClient(row.id)
  ElMessage.success('已删除')
  await load()
}

async function copyText(text: string) {
  try {
    await navigator.clipboard.writeText(text)
    ElMessage.success('已复制')
  } catch {
    ElMessage.warning('复制失败，请手动选择')
  }
}

onMounted(async () => {
  await Promise.all([load(), loadEndpoints()])
})
</script>

<template>
  <el-card shadow="never">
    <div class="toolbar">
      <el-input v-model="keyword" clearable placeholder="名称/AccessKey" style="width: 240px" @keyup.enter="onSearch" />
      <el-button v-permission="'open:client:query'" type="primary" @click="onSearch">查询</el-button>
      <el-button v-permission="'open:client:add'" type="success" @click="openCreate">新增</el-button>
    </div>
    <el-table v-loading="loading" :data="rows" stripe>
      <el-table-column prop="name" label="名称" min-width="140" />
      <el-table-column prop="accessKey" label="AccessKey" min-width="160" show-overflow-tooltip />
      <el-table-column label="日限额" width="100">
        <template #default="{ row }">{{ row.dailyLimit || '不限' }}</template>
      </el-table-column>
      <el-table-column label="QPS" width="80">
        <template #default="{ row }">{{ row.qpsLimit || '不限' }}</template>
      </el-table-column>
      <el-table-column label="今日已用" width="100">
        <template #default="{ row }">{{ row.todayUsed ?? 0 }}</template>
      </el-table-column>
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="row.status ? 'success' : 'info'">{{ row.status ? '启用' : '停用' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="220" fixed="right">
        <template #default="{ row }">
          <el-button v-permission="'open:client:edit'" link type="primary" @click="openEdit(row)">编辑</el-button>
          <el-button v-permission="'open:client:resetKey'" link type="warning" @click="onResetKeys(row)">重置密钥</el-button>
          <el-button v-permission="'open:client:remove'" link type="danger" @click="onRemove(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <div class="pager">
      <el-pagination
        v-model:current-page="page"
        v-model:page-size="size"
        background
        layout="total, sizes, prev, pager, next"
        :total="total"
        :page-sizes="[10, 20, 50]"
        @current-change="onPageChange"
        @size-change="onSizeChange"
      />
    </div>
  </el-card>

  <el-dialog v-model="dialogVisible" :title="editingId == null ? '新增客户端' : '编辑客户端'" width="640px">
    <el-form ref="formRef" :model="form" :rules="rules" label-width="110px">
      <el-form-item label="名称" prop="name">
        <el-input v-model="form.name" />
      </el-form-item>
      <el-form-item label="日调用限额">
        <el-input-number v-model="form.dailyLimit" :min="0" :controls="false" placeholder="0 或不填表示不限" style="width: 100%" />
      </el-form-item>
      <el-form-item label="QPS">
        <el-input-number v-model="form.qpsLimit" :min="0" :controls="false" placeholder="0 或不填表示不限" style="width: 100%" />
      </el-form-item>
      <el-form-item label="过期时间">
        <el-date-picker
          v-model="form.expireAt"
          type="datetime"
          value-format="YYYY-MM-DDTHH:mm:ss"
          placeholder="不填表示不过期"
          style="width: 100%"
        />
      </el-form-item>
      <el-form-item label="IP 白名单">
        <el-input
          v-model="form.ipText"
          type="textarea"
          :rows="3"
          placeholder="每行一个 IP；留空表示不限制"
        />
      </el-form-item>
      <el-form-item label="可访问接口">
        <el-select v-model="form.endpointIds" multiple filterable placeholder="请选择" style="width: 100%">
          <el-option
            v-for="ep in endpointOptions"
            :key="ep.id"
            :label="`${ep.name} (${ep.httpMethod} ${ep.pathPattern})`"
            :value="ep.id"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="备注">
        <el-input v-model="form.remark" />
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

  <el-dialog v-model="credVisible" title="请妥善保存密钥（仅显示一次）" width="560px">
    <template v-if="credentials">
      <p><strong>AccessKey：</strong>{{ credentials.accessKey }}
        <el-button link type="primary" @click="copyText(credentials.accessKey)">复制</el-button>
      </p>
      <p><strong>API Key（X-Api-Key）：</strong>{{ credentials.apiKey }}
        <el-button link type="primary" @click="copyText(credentials.apiKey)">复制</el-button>
      </p>
      <p><strong>AccessSecret（二期签名预留）：</strong>{{ credentials.accessSecret }}
        <el-button link type="primary" @click="copyText(credentials.accessSecret)">复制</el-button>
      </p>
      <el-alert type="warning" :closable="false" title="关闭后将无法再次查看明文 API Key / Secret，请立即保存。" />
    </template>
    <template #footer>
      <el-button type="primary" @click="credVisible = false">已保存</el-button>
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
