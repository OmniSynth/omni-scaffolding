<script setup lang="ts">
import { computed, ref } from 'vue'
import { MENU_ICON_OPTIONS, resolveMenuIcon } from '@/utils/icons'

const model = defineModel<string>({ default: '' })

const visible = ref(false)
const keyword = ref('')

const filtered = computed(() => {
  const q = keyword.value.trim().toLowerCase()
  if (!q) return MENU_ICON_OPTIONS
  return MENU_ICON_OPTIONS.filter((name) => name.toLowerCase().includes(q))
})

const currentIcon = computed(() => resolveMenuIcon(model.value))

function pick(name: string) {
  model.value = name
  visible.value = false
}

function clear() {
  model.value = ''
  visible.value = false
}
</script>

<template>
  <el-popover v-model:visible="visible" placement="bottom-start" :width="360" trigger="click">
    <template #reference>
      <el-button class="trigger">
        <el-icon v-if="currentIcon" :size="18">
          <component :is="currentIcon" />
        </el-icon>
        <span>{{ model || '选择图标' }}</span>
      </el-button>
    </template>
    <div class="picker">
      <div class="picker-toolbar">
        <el-input v-model="keyword" clearable placeholder="搜索图标名" size="small" />
        <el-button size="small" link type="danger" @click="clear">清空</el-button>
      </div>
      <div class="icon-grid">
        <button
          v-for="name in filtered"
          :key="name"
          type="button"
          class="icon-cell"
          :class="{ active: model === name }"
          :title="name"
          @click="pick(name)"
        >
          <el-icon :size="18">
            <component :is="resolveMenuIcon(name)" />
          </el-icon>
        </button>
      </div>
    </div>
  </el-popover>
</template>

<style scoped>
.trigger {
  width: 100%;
  justify-content: flex-start;
  gap: 8px;
}
.picker-toolbar {
  display: flex;
  gap: 8px;
  margin-bottom: 10px;
}
.icon-grid {
  display: grid;
  grid-template-columns: repeat(8, 1fr);
  gap: 6px;
  max-height: 240px;
  overflow: auto;
}
.icon-cell {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 34px;
  border: 1px solid #e2e8f0;
  border-radius: 6px;
  background: #fff;
  cursor: pointer;
  color: #334155;
  padding: 0;
}
.icon-cell:hover,
.icon-cell.active {
  border-color: #409eff;
  color: #409eff;
  background: #ecf5ff;
}
</style>
