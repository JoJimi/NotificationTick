import { defineConfig } from 'vite';
import vue from '@vitejs/plugin-vue';

export default defineConfig({
  plugins: [vue()],
  server: {
    port: 3000, // 개발 모드에서 뜨는 포트 (원하는 숫자로 변경 가능)
    proxy: {
      // "/api/*" 요청을 모두 "http://localhost:8080/api/*"로 포워딩
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/api/, '/api'),
      },
    },
  },
});