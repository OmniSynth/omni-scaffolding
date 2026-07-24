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
      // xfwd: 把浏览器真实 IP 写入 X-Forwarded-*，否则后端登录日志只会看到 127.0.0.1
      '/api': {
        target: 'http://127.0.0.1:8080',
        changeOrigin: true,
        xfwd: true,
      },
      '/uploads': {
        target: 'http://127.0.0.1:8080',
        changeOrigin: true,
        xfwd: true,
      },
      // Druid 监控页（运维菜单 iframe 同源代理）
      '/druid': {
        target: 'http://127.0.0.1:8080',
        changeOrigin: true,
        xfwd: true,
      },
    },
  },
})

