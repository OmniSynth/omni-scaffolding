<script setup lang="ts">
import { computed } from 'vue'

/** 经 Vite/网关同源代理到后端 /druid；使用 index.html 进入监控台 */
const druidUrl = computed(() => '/druid/index.html')
</script>

<template>
  <el-card shadow="never" class="druid-card" body-class="druid-body">
    <template #header>
      <div class="card-header">
        <div>
          <span class="title">Druid 监控</span>
          <el-text type="info" size="small" class="hint">
            内嵌官方控制台；默认账号见后端 DRUID_USERNAME / DRUID_PASSWORD（开发默认 admin / admin123）
          </el-text>
        </div>
        <el-button tag="a" :href="druidUrl" target="_blank" rel="noopener">新窗口打开</el-button>
      </div>
    </template>
    <iframe class="druid-frame" :src="druidUrl" title="Druid Monitor" />
  </el-card>
</template>

<style scoped>
.druid-card {
  height: calc(100vh - 140px);
  display: flex;
  flex-direction: column;
}
.druid-card :deep(.druid-body) {
  flex: 1;
  padding: 0;
  min-height: 0;
}
.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}
.title {
  font-weight: 600;
  margin-right: 12px;
}
.hint {
  display: inline;
}
.druid-frame {
  width: 100%;
  height: 100%;
  min-height: 640px;
  border: 0;
  background: #fff;
}
</style>
