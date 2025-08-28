<!-- src/views/OAuthCallback.vue -->
<template><div>로그인 처리 중...</div></template>

<script setup>
import { onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useUserStore } from '@/store/userStore';

const route = useRoute();
const router = useRouter();
const store = useUserStore();

onMounted(async () => {
  // 백엔드가 camelCase로 줌: accessToken, refreshToken
  const accessToken = route.query.accessToken;
  const refreshToken = route.query.refreshToken;

  if (accessToken) {
    store.setTokens(String(accessToken), String(refreshToken || ''));
    // 홈으로 이동
    await store.fetchUser().catch(()=>{});
    router.replace('/');
  } else {
    router.replace('/login');
  }
});
</script>
