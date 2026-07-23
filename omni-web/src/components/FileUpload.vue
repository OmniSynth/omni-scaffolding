<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { ElMessage, type UploadRequestOptions } from 'element-plus'
import { uploadFile } from '@/api/system/file'

const props = withDefaults(
  defineProps<{
    modelValue?: number | null
    previewUrl?: string | null
    bizType?: string
    accept?: string
    tip?: string
    disabled?: boolean
  }>(),
  {
    modelValue: null,
    previewUrl: null,
    bizType: 'common',
    accept: 'image/png,image/jpeg,image/gif,image/webp',
    tip: '支持图片上传',
    disabled: false,
  },
)

const emit = defineEmits<{
  'update:modelValue': [value: number | null]
  'update:previewUrl': [value: string | null]
  uploaded: [payload: { id: number; previewUrl?: string }]
}>()

const localPreview = ref(props.previewUrl || '')
const uploading = ref(false)

watch(
  () => props.previewUrl,
  (v) => {
    localPreview.value = v || ''
  },
)

const showImage = computed(() => !!localPreview.value)

async function onUpload(options: UploadRequestOptions) {
  if (props.disabled) {
    return
  }
  uploading.value = true
  try {
    const result = await uploadFile(options.file as File, props.bizType)
    emit('update:modelValue', result.id)
    emit('update:previewUrl', result.previewUrl || null)
    localPreview.value = result.previewUrl || ''
    emit('uploaded', { id: result.id, previewUrl: result.previewUrl })
    ElMessage.success('上传成功')
  } finally {
    uploading.value = false
  }
}

function clear() {
  emit('update:modelValue', null)
  emit('update:previewUrl', null)
  localPreview.value = ''
}
</script>

<template>
  <div class="file-upload">
    <el-avatar v-if="showImage" :size="64" :src="localPreview" />
    <el-upload
      :show-file-list="false"
      :accept="accept"
      :disabled="disabled || uploading"
      :http-request="onUpload"
    >
      <el-button :loading="uploading" :disabled="disabled">上传</el-button>
    </el-upload>
    <el-button v-if="modelValue" link type="danger" :disabled="disabled" @click="clear">清除</el-button>
    <div v-if="tip" class="tip">{{ tip }}</div>
  </div>
</template>

<style scoped>
.file-upload {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}
.tip {
  width: 100%;
  color: var(--el-text-color-secondary);
  font-size: 12px;
}
</style>
