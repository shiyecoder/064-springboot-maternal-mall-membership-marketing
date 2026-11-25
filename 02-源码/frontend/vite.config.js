import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'path'

// 端口固定 5172（通道3），host 显式绑 127.0.0.1，避免只监听 IPv6 导致脚本访问失败
export default defineConfig({
  plugins: [vue()],
  resolve: { alias: { '@': path.resolve(__dirname, 'src') } },
  server: {
    host: '127.0.0.1',
    port: 5172,
    strictPort: true,
    proxy: {
      '/api': { target: 'http://127.0.0.1:8082', changeOrigin: true },
      '/uploads': { target: 'http://127.0.0.1:8082', changeOrigin: true }
    }
  },
  build: { outDir: 'dist', chunkSizeWarningLimit: 1600 }
})
