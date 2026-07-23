<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { validateCron } from '@/api/system/job'

const model = defineModel<string>({ default: '0 0/1 * * * ?' })

const presets = [
  { label: '每分钟', value: '0 0/1 * * * ?' },
  { label: '每5分钟', value: '0 0/5 * * * ?' },
  { label: '每小时', value: '0 0 0/1 * * ?' },
  { label: '每天0点', value: '0 0 0 * * ?' },
  { label: '每天8点', value: '0 0 8 * * ?' },
  { label: '每周一9点', value: '0 0 9 ? * MON' },
]

const minuteMode = ref<'every' | 'interval' | 'specific'>('interval')
const minuteInterval = ref(1)
const minuteSpecific = ref<number[]>([0])

const hourMode = ref<'every' | 'specific'>('every')
const hourSpecific = ref<number[]>([0])

const dayMode = ref<'every' | 'specific'>('every')
const daySpecific = ref<number[]>([1])

const monthMode = ref<'every' | 'specific'>('every')
const monthSpecific = ref<number[]>([1])

const weekMode = ref<'ignore' | 'specific'>('ignore')
const weekSpecific = ref<string[]>(['MON'])

const previewLoading = ref(false)
const nextTimes = ref<string[]>([])
const validHint = ref('')

const minuteOptions = Array.from({ length: 60 }, (_, i) => i)
const hourOptions = Array.from({ length: 24 }, (_, i) => i)
const dayOptions = Array.from({ length: 31 }, (_, i) => i + 1)
const monthOptions = Array.from({ length: 12 }, (_, i) => i + 1)
const weekOptions = [
  { label: '周日', value: 'SUN' },
  { label: '周一', value: 'MON' },
  { label: '周二', value: 'TUE' },
  { label: '周三', value: 'WED' },
  { label: '周四', value: 'THU' },
  { label: '周五', value: 'FRI' },
  { label: '周六', value: 'SAT' },
]

const builtCron = computed(() => {
  const second = '0'
  let minute = '*'
  if (minuteMode.value === 'interval') {
    minute = `0/${Math.min(59, Math.max(1, minuteInterval.value))}`
  } else if (minuteMode.value === 'specific') {
    minute = (minuteSpecific.value.length ? [...minuteSpecific.value].sort((a, b) => a - b) : [0]).join(',')
  }

  let hour = '*'
  if (hourMode.value === 'specific') {
    hour = (hourSpecific.value.length ? [...hourSpecific.value].sort((a, b) => a - b) : [0]).join(',')
  }

  let day = '*'
  let week = '?'
  if (weekMode.value === 'specific') {
    day = '?'
    week = (weekSpecific.value.length ? weekSpecific.value : ['MON']).join(',')
  } else if (dayMode.value === 'specific') {
    day = (daySpecific.value.length ? [...daySpecific.value].sort((a, b) => a - b) : [1]).join(',')
    week = '?'
  }

  let month = '*'
  if (monthMode.value === 'specific') {
    month = (monthSpecific.value.length ? [...monthSpecific.value].sort((a, b) => a - b) : [1]).join(',')
  }

  return `${second} ${minute} ${hour} ${day} ${month} ${week}`
})

function applyBuilt() {
  model.value = builtCron.value
}

function applyPreset(value: string) {
  model.value = value
}

async function preview() {
  if (!model.value?.trim()) {
    ElMessage.warning('请先填写 Cron 表达式')
    return
  }
  previewLoading.value = true
  try {
    const res = await validateCron(model.value.trim())
    validHint.value = res.valid ? '表达式有效' : res.message || '表达式无效'
    nextTimes.value = res.nextFireTimes || []
    if (!res.valid) {
      ElMessage.warning(res.message || 'Cron 无效')
    }
  } finally {
    previewLoading.value = false
  }
}

watch(
  () => model.value,
  () => {
    nextTimes.value = []
    validHint.value = ''
  },
)
</script>

<template>
  <div class="cron-editor">
    <div class="presets">
      <el-tag
        v-for="p in presets"
        :key="p.value"
        class="preset"
        effect="plain"
        style="cursor: pointer"
        @click="applyPreset(p.value)"
      >
        {{ p.label }}
      </el-tag>
    </div>

    <el-form label-width="70px" size="small" class="builder">
      <el-form-item label="分钟">
        <el-radio-group v-model="minuteMode">
          <el-radio value="every">每分钟</el-radio>
          <el-radio value="interval">间隔</el-radio>
          <el-radio value="specific">指定</el-radio>
        </el-radio-group>
        <el-input-number
          v-if="minuteMode === 'interval'"
          v-model="minuteInterval"
          :min="1"
          :max="59"
          class="ml"
        />
        <el-select
          v-if="minuteMode === 'specific'"
          v-model="minuteSpecific"
          multiple
          collapse-tags
          class="ml wide"
        >
          <el-option v-for="m in minuteOptions" :key="m" :label="String(m)" :value="m" />
        </el-select>
      </el-form-item>
      <el-form-item label="小时">
        <el-radio-group v-model="hourMode">
          <el-radio value="every">每小时</el-radio>
          <el-radio value="specific">指定</el-radio>
        </el-radio-group>
        <el-select
          v-if="hourMode === 'specific'"
          v-model="hourSpecific"
          multiple
          collapse-tags
          class="ml wide"
        >
          <el-option v-for="h in hourOptions" :key="h" :label="String(h)" :value="h" />
        </el-select>
      </el-form-item>
      <el-form-item label="日">
        <el-radio-group v-model="dayMode" :disabled="weekMode === 'specific'">
          <el-radio value="every">每天</el-radio>
          <el-radio value="specific">指定</el-radio>
        </el-radio-group>
        <el-select
          v-if="dayMode === 'specific' && weekMode !== 'specific'"
          v-model="daySpecific"
          multiple
          collapse-tags
          class="ml wide"
        >
          <el-option v-for="d in dayOptions" :key="d" :label="String(d)" :value="d" />
        </el-select>
      </el-form-item>
      <el-form-item label="月">
        <el-radio-group v-model="monthMode">
          <el-radio value="every">每月</el-radio>
          <el-radio value="specific">指定</el-radio>
        </el-radio-group>
        <el-select
          v-if="monthMode === 'specific'"
          v-model="monthSpecific"
          multiple
          collapse-tags
          class="ml wide"
        >
          <el-option v-for="m in monthOptions" :key="m" :label="String(m)" :value="m" />
        </el-select>
      </el-form-item>
      <el-form-item label="周">
        <el-radio-group v-model="weekMode">
          <el-radio value="ignore">不指定</el-radio>
          <el-radio value="specific">指定</el-radio>
        </el-radio-group>
        <el-select
          v-if="weekMode === 'specific'"
          v-model="weekSpecific"
          multiple
          collapse-tags
          class="ml wide"
        >
          <el-option v-for="w in weekOptions" :key="w.value" :label="w.label" :value="w.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="生成">
        <el-text type="info" size="small">{{ builtCron }}</el-text>
        <el-button class="ml" type="primary" link @click="applyBuilt">应用到表达式</el-button>
      </el-form-item>
    </el-form>

    <div class="expr-row">
      <el-input v-model="model" placeholder="Quartz Cron，如 0 0/5 * * * ?" />
      <el-button :loading="previewLoading" @click="preview">预览下次执行</el-button>
    </div>
    <el-text v-if="validHint" :type="nextTimes.length ? 'success' : 'danger'" size="small">{{ validHint }}</el-text>
    <ul v-if="nextTimes.length" class="next-list">
      <li v-for="t in nextTimes" :key="t">{{ t }}</li>
    </ul>
  </div>
</template>

<style scoped>
.cron-editor {
  width: 100%;
}
.presets {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 12px;
}
.builder {
  margin-bottom: 8px;
  padding: 8px 12px;
  background: var(--el-fill-color-lighter);
  border-radius: 6px;
}
.ml {
  margin-left: 8px;
}
.wide {
  min-width: 220px;
}
.expr-row {
  display: flex;
  gap: 8px;
  margin-top: 8px;
}
.next-list {
  margin: 8px 0 0;
  padding-left: 18px;
  color: var(--el-text-color-secondary);
  font-size: 12px;
}
</style>
