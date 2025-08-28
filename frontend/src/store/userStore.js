// src/store/userStore.js
import { defineStore } from 'pinia';
import { getUserProfile, updateUserProfile, refreshTokens, logoutApi } from '@/api';

export const useUserStore = defineStore('user', {
    state: () => ({
        accessToken: localStorage.getItem('access_token') || null,
        refreshToken: localStorage.getItem('refresh_token') || null,
        user: null,
    }),
    actions: {
        setTokens(at, rt) {
            this.accessToken = at || null;
            this.refreshToken = rt || null;
            if (at) localStorage.setItem('access_token', at); else localStorage.removeItem('access_token');
            if (rt) localStorage.setItem('refresh_token', rt); else localStorage.removeItem('refresh_token');
        },
        async fetchUser() {
            this.user = await getUserProfile();
            return this.user;
        },
        async updateNickname(nickname) {
            this.user = await updateUserProfile({ nickname });
            return this.user;
        },
        async tryRefresh() {
            if (!this.refreshToken) return false;
            const tokens = await refreshTokens(this.refreshToken);
            this.setTokens(tokens.accessToken, tokens.refreshToken);
            return true;
        },
        async logout() {
            try { await logoutApi(); } catch (_) {}
            this.setTokens(null, null);
            this.user = null;
        },
    },
});
