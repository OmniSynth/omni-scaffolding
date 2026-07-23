<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Close } from '@element-plus/icons-vue'
import { useTagsViewStore, type TagView } from '@/stores/tagsView'

const route = useRoute()
const router = useRouter()
const tagsStore = useTagsViewStore()

const menuVisible = ref(false)
const menuStyle = reactive({ left: '0px', top: '0px' })
const selected = ref<TagView | null>(null)

const tags = computed(() => tagsStore.visited)
const activePath = computed(() => route.path)

watch(
  () => route.fullPath,
  () => {
    tagsStore.addView(route)
    menuVisible.value = false
  },
  { immediate: true },
)

function isActive(tag: TagView) {
  return tag.path === activePath.value
}

async function openTag(tag: TagView) {
  if (tag.path === route.path) {
    return
  }
  await router.push(tag.fullPath || tag.path)
}

async function closeTag(tag: TagView, event?: Event) {
  event?.stopPropagation()
  if (tag.affix) {
    return
  }
  const wasActive = isActive(tag)
  tagsStore.delView(tag.path)
  if (!wasActive) {
    return
  }
  const latest = tagsStore.visited[tagsStore.visited.length - 1]
  await router.push(latest?.fullPath || latest?.path || '/home')
}

function openMenu(tag: TagView, e: MouseEvent) {
  e.preventDefault()
  selected.value = tag
  menuStyle.left = `${e.clientX}px`
  menuStyle.top = `${e.clientY}px`
  menuVisible.value = true
}

function closeMenu() {
  menuVisible.value = false
}

async function onRefresh() {
  const tag = selected.value
  closeMenu()
  if (!tag) {
    return
  }
  if (tag.path !== route.path) {
    await router.push(tag.fullPath || tag.path)
    await nextTick()
  }
  tagsStore.refreshView(
    tag.path,
    typeof route.name === 'string' ? route.name : tag.name,
  )
}

async function onClose() {
  const tag = selected.value
  closeMenu()
  if (tag) {
    await closeTag(tag)
  }
}

async function onCloseOthers() {
  const tag = selected.value
  closeMenu()
  if (!tag) {
    return
  }
  tagsStore.delOthers(tag.path)
  if (tag.path !== route.path) {
    await router.push(tag.fullPath || tag.path)
  }
}

async function onCloseAll() {
  closeMenu()
  tagsStore.delAll()
  if (route.path !== '/home') {
    await router.push('/home')
  }
}

onMounted(() => {
  document.addEventListener('click', closeMenu)
})

onBeforeUnmount(() => {
  document.removeEventListener('click', closeMenu)
})
</script>

<template>
  <div class="tags-view">
    <div class="tags-scroll">
      <div
        v-for="tag in tags"
        :key="tag.path"
        class="tag-item"
        :class="{ active: isActive(tag) }"
        @click="openTag(tag)"
        @contextmenu="openMenu(tag, $event)"
      >
        <span class="dot" />
        <span class="title">{{ tag.title }}</span>
        <el-icon v-if="!tag.affix" class="close" @click="closeTag(tag, $event)">
          <Close />
        </el-icon>
      </div>
    </div>

    <ul v-show="menuVisible" class="context-menu" :style="menuStyle">
      <li @click="onRefresh">刷新</li>
      <li :class="{ disabled: selected?.affix }" @click="!selected?.affix && onClose()">关闭</li>
      <li @click="onCloseOthers">关闭其他</li>
      <li @click="onCloseAll">关闭全部</li>
    </ul>
  </div>
</template>

<style scoped>
.tags-view {
  position: relative;
  background: #fff;
  border-bottom: 1px solid #e2e8f0;
  padding: 6px 12px;
}
.tags-scroll {
  display: flex;
  flex-wrap: nowrap;
  gap: 6px;
  overflow-x: auto;
}
.tag-item {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  height: 28px;
  padding: 0 10px;
  border: 1px solid #e2e8f0;
  border-radius: 4px;
  background: #f8fafc;
  color: #475569;
  font-size: 12px;
  cursor: pointer;
  white-space: nowrap;
  user-select: none;
}
.tag-item:hover {
  background: #f1f5f9;
}
.tag-item.active {
  background: #eff6ff;
  border-color: #93c5fd;
  color: #1d4ed8;
}
.dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #94a3b8;
  flex-shrink: 0;
}
.tag-item.active .dot {
  background: #2563eb;
}
.close {
  font-size: 12px;
  border-radius: 50%;
  padding: 1px;
}
.close:hover {
  background: #cbd5e1;
  color: #0f172a;
}
.context-menu {
  position: fixed;
  z-index: 3000;
  margin: 0;
  padding: 4px 0;
  list-style: none;
  min-width: 120px;
  background: #fff;
  border: 1px solid #e2e8f0;
  border-radius: 6px;
  box-shadow: 0 8px 24px rgba(15, 23, 42, 0.12);
}
.context-menu li {
  padding: 8px 14px;
  font-size: 13px;
  color: #334155;
  cursor: pointer;
}
.context-menu li:hover {
  background: #f1f5f9;
}
.context-menu li.disabled {
  color: #94a3b8;
  cursor: not-allowed;
}
.context-menu li.disabled:hover {
  background: transparent;
}
</style>
