<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { Bell } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  fetchNoticeInbox,
  fetchUnreadNoticeCount,
  fetchUnreadNotices,
  markAllNoticesRead,
  markNoticeRead,
} from '@/api/system/notice'
import type { NoticeView } from '@/types/api'

const unreadCount = ref(0)
const drawerVisible = ref(false)
const inbox = ref<NoticeView[]>([])
const inboxLoading = ref(false)

const queue = ref<NoticeView[]>([])
const dialogVisible = ref(false)
const acknowledging = ref(false)

const current = computed(() => queue.value[0] || null)

function typeLabel(type?: string): string {
  return type === 'ANNOUNCE' ? '公告' : '通知'
}

async function refreshCount() {
  try {
    const data = await fetchUnreadNoticeCount()
    unreadCount.value = Number(data?.count || 0)
  } catch {
    // ignore
  }
}

async function loadInbox() {
  inboxLoading.value = true
  try {
    inbox.value = await fetchNoticeInbox()
  } finally {
    inboxLoading.value = false
  }
}

async function openDrawer() {
  drawerVisible.value = true
  await loadInbox()
  await refreshCount()
}

function showNextDialog() {
  if (queue.value.length) {
    dialogVisible.value = true
  } else {
    dialogVisible.value = false
  }
}

async function bootstrapUnreadPrompt() {
  try {
    const list = await fetchUnreadNotices()
    queue.value = list || []
    unreadCount.value = queue.value.length
    showNextDialog()
  } catch {
    await refreshCount()
  }
}

async function acknowledgeCurrent() {
  if (!current.value || acknowledging.value) return
  acknowledging.value = true
  try {
    await markNoticeRead(current.value.id)
    queue.value = queue.value.slice(1)
    unreadCount.value = Math.max(0, unreadCount.value - 1)
    if (drawerVisible.value) {
      await loadInbox()
    }
    showNextDialog()
  } finally {
    acknowledging.value = false
  }
}

async function openInboxItem(item: NoticeView) {
  if (!item.readFlag) {
    await markNoticeRead(item.id)
    item.readFlag = true
    unreadCount.value = Math.max(0, unreadCount.value - 1)
    queue.value = queue.value.filter((n) => n.id !== item.id)
    if (!queue.value.length) {
      dialogVisible.value = false
    }
  }
  await ElMessageBox.alert(item.content, item.title, {
    confirmButtonText: '关闭',
  })
}

async function onReadAll() {
  await markAllNoticesRead()
  unreadCount.value = 0
  queue.value = []
  dialogVisible.value = false
  ElMessage.success('已全部标记为已读')
  if (drawerVisible.value) {
    await loadInbox()
  }
}

onMounted(() => {
  bootstrapUnreadPrompt()
})
</script>

<template>
  <div class="notice-entry">
    <el-badge :value="unreadCount" :hidden="unreadCount <= 0" :max="99">
      <el-button class="bell-btn" text @click="openDrawer">
        <el-icon :size="18"><Bell /></el-icon>
      </el-button>
    </el-badge>

    <el-drawer v-model="drawerVisible" title="通知公告" size="380px">
      <div class="drawer-toolbar">
        <el-button size="small" :disabled="unreadCount <= 0" @click="onReadAll">全部已读</el-button>
        <el-button size="small" @click="loadInbox">刷新</el-button>
      </div>
      <el-skeleton v-if="inboxLoading" :rows="6" animated />
      <el-empty v-else-if="!inbox.length" description="暂无公告" />
      <div v-else class="inbox-list">
        <button
          v-for="item in inbox"
          :key="item.id"
          type="button"
          class="inbox-item"
          :class="{ unread: !item.readFlag }"
          @click="openInboxItem(item)"
        >
          <div class="inbox-head">
            <el-tag size="small" :type="item.type === 'ANNOUNCE' ? 'warning' : 'info'">
              {{ typeLabel(item.type) }}
            </el-tag>
            <span class="inbox-time">{{ item.publishTime || '-' }}</span>
          </div>
          <div class="inbox-title">{{ item.title }}</div>
          <div class="inbox-content">{{ item.content }}</div>
        </button>
      </div>
    </el-drawer>

    <el-dialog
      v-model="dialogVisible"
      :title="current?.title || '通知公告'"
      width="520px"
      :close-on-click-modal="false"
      :close-on-press-escape="false"
      :show-close="false"
    >
      <div v-if="current" class="dialog-body">
        <div class="dialog-meta">
          <el-tag size="small" :type="current.type === 'ANNOUNCE' ? 'warning' : 'info'">
            {{ typeLabel(current.type) }}
          </el-tag>
          <span>{{ current.publishTime || '-' }}</span>
          <span v-if="queue.length > 1">剩余 {{ queue.length }} 条</span>
        </div>
        <div class="dialog-content">{{ current.content }}</div>
      </div>
      <template #footer>
        <el-button type="primary" :loading="acknowledging" @click="acknowledgeCurrent">我知道了</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.notice-entry {
  display: inline-flex;
  align-items: center;
  margin-right: 8px;
}
.bell-btn {
  padding: 8px;
  color: #475569;
}
.drawer-toolbar {
  display: flex;
  gap: 8px;
  margin-bottom: 12px;
}
.inbox-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.inbox-item {
  text-align: left;
  border: 1px solid #e2e8f0;
  background: #fff;
  border-radius: 8px;
  padding: 10px 12px;
  cursor: pointer;
}
.inbox-item:hover {
  border-color: #94a3b8;
}
.inbox-item.unread {
  border-color: #93c5fd;
  background: #eff6ff;
}
.inbox-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 6px;
  gap: 8px;
}
.inbox-time {
  color: #94a3b8;
  font-size: 12px;
}
.inbox-title {
  font-weight: 600;
  color: #0f172a;
  margin-bottom: 4px;
}
.inbox-content {
  color: #64748b;
  font-size: 13px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  white-space: pre-wrap;
}
.dialog-meta {
  display: flex;
  gap: 12px;
  align-items: center;
  color: #64748b;
  font-size: 13px;
  margin-bottom: 12px;
}
.dialog-content {
  white-space: pre-wrap;
  line-height: 1.6;
  color: #334155;
  max-height: 360px;
  overflow: auto;
}
</style>
