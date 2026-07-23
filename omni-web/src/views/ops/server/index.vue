<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { fetchServerRuntime } from '@/api/ops/server'
import type { ServerRuntimeView } from '@/types/api'

const loading = ref(false)
const data = ref<ServerRuntimeView | null>(null)
const propKeyword = ref('')
const envKeyword = ref('')

function formatBytes(bytes?: number | null): string {
  if (bytes == null || bytes < 0) return '-'
  if (bytes === 0) return '0 B'
  const units = ['B', 'KB', 'MB', 'GB', 'TB']
  const i = Math.min(Math.floor(Math.log(bytes) / Math.log(1024)), units.length - 1)
  const value = bytes / 1024 ** i
  return `${value.toFixed(i === 0 ? 0 : 2)} ${units[i]}`
}

function formatUptime(ms?: number): string {
  if (ms == null) return '-'
  const sec = Math.floor(ms / 1000)
  const d = Math.floor(sec / 86400)
  const h = Math.floor((sec % 86400) / 3600)
  const m = Math.floor((sec % 3600) / 60)
  const s = sec % 60
  return `${d}天 ${h}时 ${m}分 ${s}秒`
}

function formatPercent(used?: number, max?: number): string {
  if (used == null || max == null || max <= 0) return '-'
  return `${((used / max) * 100).toFixed(1)}%`
}

function toRows(map?: Record<string, string>, keyword = '') {
  const entries = Object.entries(map || {})
  const kw = keyword.trim().toLowerCase()
  return entries
    .filter(([k, v]) => !kw || k.toLowerCase().includes(kw) || (v || '').toLowerCase().includes(kw))
    .map(([key, value]) => ({ key, value }))
}

const propRows = computed(() => toRows(data.value?.systemProperties, propKeyword.value))
const envRows = computed(() => toRows(data.value?.environment, envKeyword.value))

async function load() {
  loading.value = true
  try {
    data.value = await fetchServerRuntime()
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>

<template>
  <div v-loading="loading" class="server-page">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <div>
            <div class="title">系统详情</div>
            <el-text type="info" size="small">采集时间：{{ data?.collectedAt || '-' }}</el-text>
          </div>
          <el-button v-permission="'ops:server:query'" type="primary" @click="load">刷新</el-button>
        </div>
      </template>

      <el-tabs v-if="data">
        <el-tab-pane label="应用">
          <el-descriptions :column="2" border size="small">
            <el-descriptions-item label="应用名">{{ data.app.name }}</el-descriptions-item>
            <el-descriptions-item label="版本">{{ data.app.version }}</el-descriptions-item>
            <el-descriptions-item label="Active Profiles">
              {{ (data.app.activeProfiles || []).join(', ') || '-' }}
            </el-descriptions-item>
            <el-descriptions-item label="虚拟线程">
              {{ data.app.virtualThreadsEnabled ? '已启用' : '未启用' }}
            </el-descriptions-item>
            <el-descriptions-item label="启动时间">{{ data.app.startTime || '-' }}</el-descriptions-item>
            <el-descriptions-item label="运行时长">{{ formatUptime(data.app.uptimeMs) }}</el-descriptions-item>
            <el-descriptions-item label="时区">{{ data.app.userTimezone || '-' }}</el-descriptions-item>
            <el-descriptions-item label="文件编码">{{ data.app.fileEncoding || '-' }}</el-descriptions-item>
            <el-descriptions-item label="JAVA_HOME" :span="2">{{ data.app.javaHome || '-' }}</el-descriptions-item>
            <el-descriptions-item label="工作目录" :span="2">{{ data.app.userDir || '-' }}</el-descriptions-item>
          </el-descriptions>
        </el-tab-pane>

        <el-tab-pane label="JVM">
          <el-descriptions :column="2" border size="small">
            <el-descriptions-item label="Java 版本">{{ data.jvm.version }}</el-descriptions-item>
            <el-descriptions-item label="Vendor">{{ data.jvm.vendor }}</el-descriptions-item>
            <el-descriptions-item label="VM">{{ data.jvm.vmName || data.jvm.name }}</el-descriptions-item>
            <el-descriptions-item label="PID">{{ data.jvm.pid || '-' }}</el-descriptions-item>
            <el-descriptions-item label="CPU 核数">{{ data.jvm.availableProcessors ?? '-' }}</el-descriptions-item>
            <el-descriptions-item label="进程 CPU">
              {{ data.jvm.processCpuLoad != null ? `${data.jvm.processCpuLoad}%` : '-' }}
            </el-descriptions-item>
            <el-descriptions-item label="系统 CPU">
              {{ data.jvm.systemCpuLoad != null ? `${data.jvm.systemCpuLoad}%` : '-' }}
            </el-descriptions-item>
            <el-descriptions-item label="线程数">
              {{ data.jvm.threadCount ?? '-' }} / 峰值 {{ data.jvm.peakThreadCount ?? '-' }} / 守护
              {{ data.jvm.daemonThreadCount ?? '-' }}
            </el-descriptions-item>
            <el-descriptions-item label="已加载类">{{ data.jvm.loadedClassCount ?? '-' }}</el-descriptions-item>
            <el-descriptions-item label="已卸载类">{{ data.jvm.unloadedClassCount ?? '-' }}</el-descriptions-item>
            <el-descriptions-item label="Runtime" :span="2">{{ data.jvm.runtimeName || '-' }}</el-descriptions-item>
          </el-descriptions>
          <div class="section-title">JVM 启动参数</div>
          <el-input
            type="textarea"
            :rows="8"
            readonly
            :model-value="(data.jvm.inputArguments || []).join('\n') || '-'"
          />
        </el-tab-pane>

        <el-tab-pane label="内存">
          <el-descriptions :column="2" border size="small">
            <el-descriptions-item label="堆已用">
              {{ formatBytes(data.memory.heapUsed) }} / {{ formatBytes(data.memory.heapMax) }}
              （{{ formatPercent(data.memory.heapUsed, data.memory.heapMax) }}）
            </el-descriptions-item>
            <el-descriptions-item label="堆已提交">{{ formatBytes(data.memory.heapCommitted) }}</el-descriptions-item>
            <el-descriptions-item label="非堆已用">
              {{ formatBytes(data.memory.nonHeapUsed) }}
              <template v-if="data.memory.nonHeapMax && data.memory.nonHeapMax > 0">
                / {{ formatBytes(data.memory.nonHeapMax) }}
              </template>
            </el-descriptions-item>
            <el-descriptions-item label="非堆已提交">{{ formatBytes(data.memory.nonHeapCommitted) }}</el-descriptions-item>
            <el-descriptions-item label="Runtime free">{{ formatBytes(data.memory.freeMemory) }}</el-descriptions-item>
            <el-descriptions-item label="Runtime total">{{ formatBytes(data.memory.totalMemory) }}</el-descriptions-item>
            <el-descriptions-item label="Runtime max" :span="2">{{ formatBytes(data.memory.maxMemory) }}</el-descriptions-item>
          </el-descriptions>
          <el-progress
            class="mt"
            :percentage="
              data.memory.heapMax && data.memory.heapMax > 0
                ? Math.min(100, Math.round(((data.memory.heapUsed || 0) / data.memory.heapMax) * 100))
                : 0
            "
            :stroke-width="16"
            striped
          />
        </el-tab-pane>

        <el-tab-pane label="操作系统">
          <el-descriptions :column="2" border size="small">
            <el-descriptions-item label="主机名">{{ data.os.hostName || '-' }}</el-descriptions-item>
            <el-descriptions-item label="系统">{{ data.os.name }} {{ data.os.version }}</el-descriptions-item>
            <el-descriptions-item label="架构">{{ data.os.arch }}</el-descriptions-item>
            <el-descriptions-item label="CPU 核数">{{ data.os.availableProcessors ?? '-' }}</el-descriptions-item>
            <el-descriptions-item label="系统负载">{{ data.os.systemLoadAverage ?? '-' }}</el-descriptions-item>
            <el-descriptions-item label="物理内存">
              {{ formatBytes(data.os.freeMemorySize) }} free / {{ formatBytes(data.os.totalMemorySize) }}
            </el-descriptions-item>
          </el-descriptions>
          <div class="section-title">磁盘</div>
          <el-table :data="data.disks" stripe size="small">
            <el-table-column prop="path" label="路径" min-width="160" />
            <el-table-column label="总量" width="140">
              <template #default="{ row }">{{ formatBytes(row.totalSpace) }}</template>
            </el-table-column>
            <el-table-column label="可用" width="140">
              <template #default="{ row }">{{ formatBytes(row.usableSpace) }}</template>
            </el-table-column>
            <el-table-column label="空闲" width="140">
              <template #default="{ row }">{{ formatBytes(row.freeSpace) }}</template>
            </el-table-column>
            <el-table-column label="使用率" width="160">
              <template #default="{ row }">
                <el-progress
                  :percentage="
                    row.totalSpace
                      ? Math.min(100, Math.round(((row.totalSpace - row.usableSpace) / row.totalSpace) * 100))
                      : 0
                  "
                  :stroke-width="12"
                />
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <el-tab-pane label="中间件">
          <div class="section-title">数据源</div>
          <el-descriptions :column="2" border size="small">
            <el-descriptions-item label="可用">{{ data.dataSource.available ? '是' : '否' }}</el-descriptions-item>
            <el-descriptions-item label="连接池">{{ data.dataSource.poolType || '-' }}</el-descriptions-item>
            <el-descriptions-item label="JDBC URL" :span="2">{{ data.dataSource.jdbcUrl || '-' }}</el-descriptions-item>
            <el-descriptions-item label="用户名">{{ data.dataSource.username || '-' }}</el-descriptions-item>
            <el-descriptions-item label="驱动">{{ data.dataSource.driverClassName || '-' }}</el-descriptions-item>
            <el-descriptions-item label="最大连接">{{ data.dataSource.maximumPoolSize ?? '-' }}</el-descriptions-item>
            <el-descriptions-item label="最小空闲">{{ data.dataSource.minimumIdle ?? '-' }}</el-descriptions-item>
            <el-descriptions-item label="活跃 / 空闲 / 总计">
              {{ data.dataSource.activeConnections ?? '-' }} /
              {{ data.dataSource.idleConnections ?? '-' }} /
              {{ data.dataSource.totalConnections ?? '-' }}
            </el-descriptions-item>
            <el-descriptions-item label="等待线程">
              {{ data.dataSource.threadsAwaitingConnection ?? '-' }}
            </el-descriptions-item>
            <el-descriptions-item v-if="data.dataSource.message" label="说明" :span="2">
              {{ data.dataSource.message }}
            </el-descriptions-item>
          </el-descriptions>

          <div class="section-title">Redis</div>
          <el-descriptions :column="2" border size="small">
            <el-descriptions-item label="可用">{{ data.redis.available ? '是' : '否' }}</el-descriptions-item>
            <el-descriptions-item label="PING">{{ data.redis.pong || '-' }}</el-descriptions-item>
            <el-descriptions-item label="版本">{{ data.redis.redisVersion || '-' }}</el-descriptions-item>
            <el-descriptions-item label="DB Size">{{ data.redis.dbSize ?? '-' }}</el-descriptions-item>
            <el-descriptions-item label="内存">{{ data.redis.usedMemoryHuman || '-' }}</el-descriptions-item>
            <el-descriptions-item v-if="data.redis.message" label="说明">{{ data.redis.message }}</el-descriptions-item>
          </el-descriptions>
        </el-tab-pane>

        <el-tab-pane label="系统属性">
          <el-input v-model="propKeyword" clearable placeholder="过滤 Key/Value" class="filter" />
          <el-table :data="propRows" stripe height="520" size="small">
            <el-table-column prop="key" label="Key" min-width="260" show-overflow-tooltip />
            <el-table-column prop="value" label="Value" min-width="360" show-overflow-tooltip />
          </el-table>
        </el-tab-pane>

        <el-tab-pane label="环境变量">
          <el-alert
            type="info"
            :closable="false"
            show-icon
            title="含 password/secret/token 等关键字的值已脱敏"
            class="mb"
          />
          <el-input v-model="envKeyword" clearable placeholder="过滤 Key/Value" class="filter" />
          <el-table :data="envRows" stripe height="480" size="small">
            <el-table-column prop="key" label="Key" min-width="260" show-overflow-tooltip />
            <el-table-column prop="value" label="Value" min-width="360" show-overflow-tooltip />
          </el-table>
        </el-tab-pane>
      </el-tabs>
      <el-empty v-else description="暂无数据" />
    </el-card>
  </div>
</template>

<style scoped>
.server-page {
  min-height: 100%;
}
.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}
.title {
  font-weight: 600;
  color: #0f172a;
}
.section-title {
  margin: 16px 0 8px;
  font-weight: 600;
  color: #334155;
}
.mt {
  margin-top: 16px;
}
.mb {
  margin-bottom: 12px;
}
.filter {
  margin-bottom: 12px;
  max-width: 360px;
}
</style>
