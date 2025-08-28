// src/router/index.js
import { createRouter, createWebHistory } from 'vue-router';
import { useUserStore } from '@/store/userStore';

const routes = [
    { path: '/', component: () => import('@/views/Home.vue'), meta: { requiresAuth: true } },
    { path: '/login', component: () => import('@/views/Login.vue') },
    { path: '/oauth2/callback', component: () => import('@/views/OAuthCallback.vue') },
    { path: '/profile', component: () => import('@/views/Profile.vue'), meta: { requiresAuth: true } },
    { path: '/stocks', component: () => import('@/views/stock/StockList.vue') },
    { path: '/stocks/ranking', component: () => import('@/views/stock/StockRanking.vue') },
    { path: '/stocks/:symbol', component: () => import('@/views/stock/StockDetail.vue'), props: true },
    { path: '/:pathMatch(.*)*', redirect: '/login' },
];

const router = createRouter({
    history: createWebHistory(),
    routes,
});

// 간단한 보호 라우트 가드
router.beforeEach((to) => {
    const store = useUserStore();
    if (to.meta.requiresAuth && !store.accessToken) {
        return '/login';
    }
});

export default router;
