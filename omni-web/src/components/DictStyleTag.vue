<script setup lang="ts">
import { computed } from 'vue'
import { dictStyleInline, resolveDictStyle } from '@/constants/dictStyles'

const props = withDefaults(
  defineProps<{
    cssClass?: string | null
    /** 展示文案 */
    label?: string
    size?: 'large' | 'default' | 'small'
  }>(),
  {
    label: '',
    size: 'small',
  },
)

const style = computed(() => dictStyleInline(props.cssClass))
const meta = computed(() => resolveDictStyle(props.cssClass))
const text = computed(() => props.label || meta.value.label)
</script>

<template>
  <span
    class="dict-style-tag"
    :class="[`is-${size}`]"
    :style="style"
    :title="meta.label"
  >
    {{ text }}
  </span>
</template>

<style scoped>
.dict-style-tag {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  box-sizing: border-box;
  max-width: 100%;
  padding: 0 8px;
  border: 1px solid;
  border-radius: 4px;
  font-weight: 500;
  line-height: 1;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  vertical-align: middle;
}
.dict-style-tag.is-small {
  height: 20px;
  font-size: 12px;
}
.dict-style-tag.is-default {
  height: 24px;
  font-size: 12px;
  padding: 0 9px;
}
.dict-style-tag.is-large {
  height: 28px;
  font-size: 13px;
  padding: 0 11px;
}
</style>
