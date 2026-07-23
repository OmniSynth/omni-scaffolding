<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowDown, HomeFilled, SwitchButton, User } from '@element-plus/icons-vue'
import { ElMessageBox } from 'element-plus'
import { getConfigValue } from '@/api/system/config'
import { ConfigKeys } from '@/constants/configKeys'
import { useUserStore } from '@/stores/user'
import { useTagsViewStore } from '@/stores/tagsView'
import { toSidebarItems } from '@/utils/menu'
import { resolveMenuIcon } from '@/utils/icons'
import TagsView from '@/components/TagsView.vue'
import NoticePrompt from '@/components/NoticePrompt.vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const tagsStore = useTagsViewStore()

const watermarkEnabled = ref(false)

const activeMenu = computed(() => route.path)
const sidebar = computed(() => toSidebarItems(userStore.menus))
const avatarLetter = computed(() => (userStore.displayName || userStore.username || '?').slice(0, 1))
const watermarkContent = computed(() => {
  const name = userStore.displayName || userStore.username || 'Omni'
  return [name, userStore.username && userStore.username !== name ? userStore.username : ''].filter(Boolean)
})

async function loadWatermarkFlag() {
  try {
    const raw = await getConfigValue(ConfigKeys.UI_WATERMARK)
    watermarkEnabled.value = String(raw ?? '').trim().toLowerCase() === 'true'
  } catch {
    watermarkEnabled.value = false
  }
}

async function onCommand(command: string) {
  if (command === 'profile') {
    await router.push('/profile')
    return
  }
  if (command === 'logout') {
    await ElMessageBox.confirm('确认退出登录？', '提示', { type: 'warning' })
    tagsStore.reset()
    await userStore.logout()
    await router.replace('/login')
  }
}

onMounted(loadWatermarkFlag)
</script>

<template>
  <el-watermark
    class="watermark-root"
    :content="watermarkEnabled ? watermarkContent : ''"
    :font="{ color: 'rgba(15, 23, 42, 0.12)', fontSize: 14 }"
    :gap="[140, 120]"
    :z-index="9"
  >
    <el-container class="layout">
      <el-aside width="220px" class="aside">
        <div class="brand">Omni Admin</div>
        <el-menu :default-active="activeMenu" router background-color="#0f172a" text-color="#cbd5e1" active-text-color="#fff">
          <el-menu-item index="/home">
            <el-icon><HomeFilled /></el-icon>
            <span>首页</span>
          </el-menu-item>
          <template v-for="item in sidebar" :key="item.id">
            <el-sub-menu v-if="item.children?.length" :index="`dir-${item.id}`">
              <template #title>
                <el-icon v-if="resolveMenuIcon(item.icon)">
                  <component :is="resolveMenuIcon(item.icon)" />
                </el-icon>
                <span>{{ item.title }}</span>
              </template>
              <el-menu-item v-for="child in item.children" :key="child.id" :index="child.path">
                <el-icon v-if="resolveMenuIcon(child.icon)">
                  <component :is="resolveMenuIcon(child.icon)" />
                </el-icon>
                <span>{{ child.title }}</span>
              </el-menu-item>
            </el-sub-menu>
            <el-menu-item v-else-if="item.path" :index="item.path">
              <el-icon v-if="resolveMenuIcon(item.icon)">
                <component :is="resolveMenuIcon(item.icon)" />
              </el-icon>
              <span>{{ item.title }}</span>
            </el-menu-item>
          </template>
        </el-menu>
      </el-aside>
      <el-container>
        <el-header class="header">
          <div class="header-title">{{ route.meta.title || '控制台' }}</div>
          <div class="header-right">
            <NoticePrompt />
            <el-dropdown trigger="click" @command="onCommand">
              <div class="user-entry">
                <el-avatar :size="32" :src="userStore.avatar || undefined">{{ avatarLetter }}</el-avatar>
                <span class="username">{{ userStore.displayName || userStore.username }}</span>
                <el-icon class="caret"><ArrowDown /></el-icon>
              </div>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="profile" :icon="User">个人中心</el-dropdown-item>
                  <el-dropdown-item divided command="logout" :icon="SwitchButton">退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </el-header>
        <TagsView />
        <el-main class="main">
          <router-view v-slot="{ Component, route: viewRoute }">
            <keep-alive :exclude="tagsStore.excludeNames">
              <component :is="Component" :key="tagsStore.viewKey(viewRoute.path)" />
            </keep-alive>
          </router-view>
        </el-main>
      </el-container>
    </el-container>
  </el-watermark>
</template>


<style scoped>
.watermark-root {
  display: block;
  min-height: 100vh;
}
.layout {
  min-height: 100vh;
}
.aside {
  background: #0f172a;
}
.brand {
  height: 56px;
  display: flex;
  align-items: center;
  padding: 0 20px;
  color: #fff;
  font-weight: 700;
  letter-spacing: 0.04em;
  border-bottom: 1px solid rgba(148, 163, 184, 0.2);
}
.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #fff;
  border-bottom: 1px solid #e2e8f0;
}
.header-title {
  font-size: 16px;
  font-weight: 600;
  color: #0f172a;
}
.header-right {
  display: flex;
  align-items: center;
}
.user-entry {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 8px;
  outline: none;
}
.user-entry:hover {
  background: #f1f5f9;
}
.username {
  color: #334155;
  font-size: 14px;
  max-width: 140px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.caret {
  color: #94a3b8;
  font-size: 12px;
}
.main {
  background: #f1f5f9;
}
.el-menu {
  border-right: none;
}
</style>
