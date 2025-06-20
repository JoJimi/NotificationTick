// src/main.js
import { createApp } from 'vue'
import App        from '@/App.vue'
import router     from '@/router'
import apiClient  from '@/api.js'  // axios 인터셉터 설정

createApp(App)
    .use(router)
    .mount('#app')
