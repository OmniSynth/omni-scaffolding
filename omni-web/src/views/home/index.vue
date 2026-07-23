<script setup lang="ts">
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
</script>

<template>
  <div class="home">
    <el-card shadow="never">
      <h2>欢迎，{{ userStore.nickname || userStore.username }}</h2>
      <p class="desc">
        数据范围：{{ userStore.dataScope || '-' }}；左侧菜单来自角色菜单权限，按钮受 v-permission 控制。
      </p>
    </el-card>

    <el-row :gutter="16" class="cards">
      <el-col :span="12">
        <el-card shadow="never" header="角色">
          <el-tag v-for="role in userStore.roles" :key="role" class="tag">{{ role }}</el-tag>
          <el-empty v-if="!userStore.roles.length" description="无角色" :image-size="60" />
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="never" header="权限（节选）">
          <el-tag v-for="perm in userStore.permissions.slice(0, 20)" :key="perm" type="info" class="tag">{{ perm }}</el-tag>
          <el-text v-if="userStore.permissions.length > 20" type="info">…共 {{ userStore.permissions.length }} 项</el-text>
          <el-empty v-if="!userStore.permissions.length" description="无权限" :image-size="60" />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<style scoped>
.home h2 {
  margin: 0 0 8px;
  color: #0f172a;
}
.desc {
  margin: 0;
  color: #64748b;
}
.cards {
  margin-top: 16px;
}
.tag {
  margin: 0 8px 8px 0;
}
</style>
