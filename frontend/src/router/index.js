// src/router/index.js
import { createRouter, createWebHistory } from 'vue-router';
import { useUserStore } from '@/store/userStore';

const routes = [
    { path: '/', component: () => import('@/views/Home.vue'), meta: { requiresAuth: true } },
    { path: '/login', component: () => import('@/views/Login.vue') },
    { path: '/oauth2/callback', component: () => import('@/views/OAuthCallback.vue') },
    { path: '/profile', component: () => import('@/views/Profile.vue'), meta: { requiresAuth: true } },
    { path: '/news/:symbol', component: () => import('@/views/news/NewsBySymbol.vue'), props: true },
    { path: '/stocks', component: () => import('@/views/stock/StockList.vue') },
    { path: '/stocks/ranking', component: () => import('@/views/stock/StockRanking.vue') },
    { path: '/stocks/:symbol', component: () => import('@/views/stock/StockDetail.vue'), props: true },
    { path: '/notifications', component: () => import('@/views/notification/Notifications.vue'), meta: { requiresAuth: true } },
    { path: '/portfolios/:portfolioId', component: () => import('@/views/portfolio/PortfolioDetails.vue'), meta: { requiresAuth: true }, props: true },
    { path: '/settings/notifications', component: () => import('@/views/notification/NotificationSettings.vue'), meta: { requiresAuth: true } },
    { path: '/:pathMatch(.*)*', redirect: '/login' },
];

const router = createRouter({
    history: createWebHistory(),
    routes,
});

// 보호 라우트 가드 (로그인 후 원래 페이지로 복귀 가능)
router.beforeEach((to) => {
    const store = useUserStore();
    if (to.meta.requiresAuth && !store.accessToken) {
        return { path: '/login', query: { redirect: to.fullPath } };
    }
});

export default router;
