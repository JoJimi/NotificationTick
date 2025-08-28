// src/api/index.js
import axios from 'axios';
import { useUserStore } from '@/store/userStore';

const api = axios.create({
    baseURL: import.meta.env.VITE_API_BASE_URL, // http://localhost:8080
});

// 요청 인터셉터: Authorization 추가
api.interceptors.request.use((config) => {
    const store = useUserStore();
    if (store.accessToken) {
        config.headers.Authorization = `Bearer ${store.accessToken}`;
    }
    return config;
});

// 응답 인터셉터: 401 시 자동 리프레시 1회 시도
let refreshing = false;
let queue = [];
api.interceptors.response.use(
    (res) => res,
    async (error) => {
        const { response, config } = error;
        const store = useUserStore();

        if (response && response.status === 401 && !config._retry) {
            if (refreshing) {
                // 진행 중이면 큐에 쌓았다가 완료 후 재시도
                return new Promise((resolve, reject) => {
                    queue.push({ resolve, reject });
                }).then((token) => {
                    config.headers.Authorization = `Bearer ${token}`;
                    return api(config);
                });
            }

            config._retry = true;
            refreshing = true;
            try {
                const ok = await store.tryRefresh(); // /api/auth/refresh 호출
                refreshing = false;
                queue.forEach(({ resolve }) => resolve(store.accessToken));
                queue = [];

                if (ok) {
                    config.headers.Authorization = `Bearer ${store.accessToken}`;
                    return api(config);
                }
            } catch (e) {
                refreshing = false;
                queue.forEach(({ reject }) => reject(e));
                queue = [];
                await store.logout();
            }
        }
        return Promise.reject(error);
    }
);

// ===== API 함수들 =====
export async function getUserProfile() {
    const { data } = await api.get('/api/users/me');
    return data;
}

export async function updateUserProfile(body) {
    const { data } = await api.put('/api/users/me', body);
    return data;
}

export async function refreshTokens(refreshToken) {
    const { data } = await axios.post(`${import.meta.env.VITE_API_BASE_URL}/api/auth/refresh`, { refreshToken });
    return data; // { accessToken, refreshToken }
}

export async function logoutApi() {
    // 백엔드 /api/auth/logout은 @AuthenticationPrincipal 필요 → AccessToken이 있어야 호출됨
    await api.delete('/api/auth/logout');
}

export default api;
