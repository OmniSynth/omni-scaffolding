<script setup lang="ts">
import { computed, watch } from 'vue'
import { useDict } from '@/composables/useDict'
import DictStyleTag from '@/components/DictStyleTag.vue'

const props = withDefaults(
  defineProps<{
    /** 字典类型编码，如 sys_gender */
    typeCode: string
    /** 字典值 */
    value?: string | null
    /** 空值占位 */
    emptyText?: string
    size?: 'large' | 'default' | 'small'
  }>(),
  {
    emptyText: '-',
    size: 'small',
  },
)

const { options, labelOf, optionOf, load } = useDict(props.typeCode)

watch(
  () => props.typeCode,
  () => {
    void load()
  },
)

const label = computed(() => labelOf(props.value) || props.emptyText)
const cssClass = computed(() => optionOf(props.value)?.cssClass || '')
const showTag = computed(() => !!props.value && options.value.some((item) => item.value === props.value))
</script>

<template>
  <DictStyleTag v-if="showTag" :css-class="cssClass" :label="label" :size="size" />
  <span v-else>{{ label }}</span>
</template>
