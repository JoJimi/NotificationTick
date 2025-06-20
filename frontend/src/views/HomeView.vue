<!-- src/views/HomeView.vue -->
<template>
  <div class="home">
    <!-- 로그인 전 -->
    <div v-if="!authState.user" class="login-prompt">
      <p>서비스를 이용하려면 로그인이 필요합니다.</p>
      <button @click="showOptions = true">로그인</button>
      <LoginButtons v-if="showOptions" />
    </div>
    <!-- 로그인 후 -->
    <div v-else class="welcome">
      <p>{{ authState.user.nickname }}님, 환영합니다!</p>
      <button @click="goProfile">내 정보 보기</button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import apiClient from '@/api.js'
import { authState, setAuthTokens, setUser, logout } from '@/auth.js'
import LoginButtons from '@/components/LoginButtons.vue'

const showOptions = ref(false)
const route       = useRoute()
const router      = useRouter()

onMounted(() => {
  const { accessToken, refreshToken } = route.query
  if (accessToken && refreshToken) {
    setAuthTokens(accessToken, refreshToken)
    apiClient.get('/api/users/me')
        .then(res => setUser(res.data))
        .catch(err => {
          console.error('유저 정보 조회 실패', err)
          logout()
        })
        .finally(() => {
          // URL 파라미터 제거
          router.replace({ path: '/', query: {} })
        })
  }
})

function goProfile() {
  router.push('/profile')
}
</script>

<style>
.home { text-align: center; margin-top: 100px; }
.login-prompt p { margin-bottom: 1em; }
</style>
