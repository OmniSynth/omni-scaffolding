import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { fileURLToPath, URL } from 'node:url'

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url)),
    },
  },
  server: {
    // 允许局域网通过本机 IP 访问（如 http://192.168.x.x:5173）
    host: true,
    port: 5173,
    proxy: {
      // 用 127.0.0.1，避免 Windows 上 localhost 解析到 ::1 导致代理失败
      '/api': {
        target: 'http://127.0.0.1:8080',
        changeOrigin: true,
      },
      '/uploads': {
        target: 'http://127.0.0.1:8080',
        changeOrigin: true,
      },
      // Druid 监控页（运维菜单 iframe 同源代理）
      '/druid': {
        target: 'http://127.0.0.1:8080',
        changeOrigin: true,
      },
    },
  },
})

