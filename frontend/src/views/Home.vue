<!-- src/views/Home.vue -->
<template>
  <el-card>
    <template #header>환영합니다 🎉</template>
    <p v-if="user">안녕하세요, <b>{{ user.nickname || user.email }}</b> 님!</p>
    <p v-else>사용자 정보를 불러오는 중...</p>
    <el-button type="primary" @click="$router.push('/profile')">프로필로 이동</el-button>
  </el-card>
</template>

<script setup>
import { onMounted, computed } from 'vue';
import { useUserStore } from '@/store/userStore';

const store = useUserStore();
const user = computed(() => store.user);

onMounted(() => {
  if (!store.user) store.fetchUser().catch(()=>{});
});
</script>
