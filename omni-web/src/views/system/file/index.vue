<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox, type UploadRequestOptions } from 'element-plus'
import { listFiles, removeFile, uploadFile, type FileView } from '@/api/system/file'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const canUpload = computed(() => userStore.hasPermission('system:file:upload'))
const canRemove = computed(() => userStore.hasPermission('system:file:remove'))

const loading = ref(false)
const rows = ref<FileView[]>([])
const keyword = ref('')
const bizType = ref('')
const page = ref(1)
const size = ref(10)
const total = ref(0)
const uploading = ref(false)

const query = reactive({
  contentTypePrefix: '',
})

const bizTypeLabel: Record<string, string> = {
  avatar: '头像',
  common: '通用',
}

const storageTypeLabel: Record<string, string> = {
  LOCAL: '本地',
  MINIO: 'MinIO',
  OSS: '对象存储',
}

function formatBizType(value?: string) {
  if (!value) return '-'
  return bizTypeLabel[value] || value
}

function formatStorageType(value?: string) {
  if (!value) return '-'
  return storageTypeLabel[value.toUpperCase()] || value
}

async function load() {
  loading.value = true
  try {
    const result = await listFiles({
      keyword: keyword.value || undefined,
      bizType: bizType.value || undefined,
      contentTypePrefix: query.contentTypePrefix || undefined,
      page: page.value,
      size: size.value,
    })
    rows.value = result.records
    total.value = result.total
  } finally {
    loading.value = false
  }
}

async function onUpload(options: UploadRequestOptions) {
  uploading.value = true
  try {
    await uploadFile(options.file as File, bizType.value || 'common')
    ElMessage.success('上传成功')
    await load()
  } finally {
    uploading.value = false
  }
}

async function onRemove(row: FileView) {
  await ElMessageBox.confirm(`确认删除文件「${row.originalName}」？`, '提示', { type: 'warning' })
  await removeFile(row.id)
  ElMessage.success('已删除')
  await load()
}

function formatSize(bytes?: number) {
  if (bytes == null) return '-'
  if (bytes < 1024) return `${bytes} B`
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`
  return `${(bytes / 1024 / 1024).toFixed(2)} MB`
}

function isImage(contentType?: string) {
  return !!contentType && contentType.startsWith('image/')
}

onMounted(load)
</script>

<template>
  <div class="page">
    <el-card shadow="never">
      <div class="toolbar">
        <el-input v-model="keyword" clearable placeholder="文件名" style="width: 200px" @keyup.enter="load" />
        <el-select v-model="bizType" clearable placeholder="业务类型" style="width: 140px">
          <el-option label="头像" value="avatar" />
          <el-option label="通用" value="common" />
        </el-select>
        <el-select v-model="query.contentTypePrefix" clearable placeholder="类型" style="width: 140px">
          <el-option label="图片" value="image/" />
          <el-option label="全部" value="" />
        </el-select>
        <el-button type="primary" @click="load">查询</el-button>
        <el-upload
          v-if="canUpload"
          :show-file-list="false"
          :http-request="onUpload"
          accept="image/*,.pdf,.doc,.docx,.xls,.xlsx,.zip"
        >
          <el-button type="success" :loading="uploading">上传</el-button>
        </el-upload>
      </div>

      <el-table v-loading="loading" :data="rows" stripe>
        <el-table-column label="预览" width="90">
          <template #default="{ row }">
            <el-image
              v-if="isImage(row.contentType) && row.previewUrl"
              :src="row.previewUrl"
              :preview-src-list="[row.previewUrl]"
              preview-teleported
              fit="cover"
              class="thumb"
            />
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="originalName" label="文件名" min-width="180" show-overflow-tooltip />
        <el-table-column label="业务类型" width="110">
          <template #default="{ row }">{{ formatBizType(row.bizType) }}</template>
        </el-table-column>
        <el-table-column label="存储" width="100">
          <template #default="{ row }">{{ formatStorageType(row.storageType) }}</template>
        </el-table-column>
        <el-table-column prop="contentType" label="MIME" min-width="140" show-overflow-tooltip />
        <el-table-column label="大小" width="100">
          <template #default="{ row }">{{ formatSize(row.sizeBytes) }}</template>
        </el-table-column>
        <el-table-column prop="createdAt" label="上传时间" width="180" />
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button v-if="canRemove" link type="danger" @click="onRemove(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pager">
        <el-pagination
          v-model:current-page="page"
          v-model:page-size="size"
          layout="total, prev, pager, next"
          :total="total"
          @current-change="load"
          @size-change="load"
        />
      </div>
    </el-card>
  </div>
</template>

<style scoped>
.page {
  padding: 0;
}
.toolbar {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  margin-bottom: 16px;
}
.pager {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
.thumb {
  width: 48px;
  height: 48px;
  border-radius: 4px;
}
</style>
