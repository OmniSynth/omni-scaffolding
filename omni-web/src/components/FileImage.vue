<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { getFilePreviewUrl } from '@/api/system/file'

const props = withDefaults(
  defineProps<{
    fileId?: number | null
    src?: string | null
    size?: number
    letter?: string
    previewable?: boolean
  }>(),
  {
    fileId: null,
    src: null,
    size: 36,
    letter: '?',
    previewable: false,
  },
)

const resolvedSrc = ref(props.src || '')

watch(
  () => [props.fileId, props.src] as const,
  async ([id, src]) => {
    if (src) {
      resolvedSrc.value = src
      return
    }
    if (!id) {
      resolvedSrc.value = ''
      return
    }
    try {
      const preview = await getFilePreviewUrl(id)
      resolvedSrc.value = preview.url
    } catch {
      resolvedSrc.value = ''
    }
  },
  { immediate: true },
)

const displayLetter = computed(() => (props.letter || '?').slice(0, 1))
</script>

<template>
  <el-image
    v-if="previewable && resolvedSrc"
    :src="resolvedSrc"
    :preview-src-list="[resolvedSrc]"
    preview-teleported
    fit="cover"
    :style="{ width: `${size}px`, height: `${size}px`, borderRadius: '50%' }"
  />
  <el-avatar v-else :size="size" :src="resolvedSrc || undefined">{{ displayLetter }}</el-avatar>
</template>
