// src/main.js
import { createApp } from 'vue';
import { createPinia } from 'pinia';
import router from './router';
import App from './App.vue';

// UI 프레임워크 (Element Plus)
import ElementPlus from 'element-plus';
import 'element-plus/dist/index.css';

createApp(App).use(createPinia()).use(router).use(ElementPlus).mount('#app');
