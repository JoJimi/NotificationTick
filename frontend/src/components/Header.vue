<!-- src/components/Header.vue -->
<template>
  <header class="header">
    <h1 class="logo">PredicTick</h1>
    <div class="user-area">
      <template v-if="authState.user">
        <span class="nickname" @click="goProfile">
          {{ authState.user.nickname }}
        </span>
        <button @click="handleLogout">로그아웃</button>
      </template>
    </div>
  </header>
</template>

<script setup>
import { useRouter } from 'vue-router'
import apiClient from '@/api.js'
import { authState, logout } from '@/auth.js'

const router = useRouter()
function goProfile() {
  router.push('/profile')
}
function handleLogout() {
  apiClient.delete('/api/auth/logout')
      .finally(() => {
        logout()
        router.push('/')
      })
}
</script>

<style>
.header {
  display: flex; justify-content: space-between; align-items: center;
  padding: 10px 20px; border-bottom: 1px solid #ccc;
}
.logo { font-size: 1.5em; font-weight: bold; }
.user-area { font-size: 1em; }
.nickname { cursor: pointer; margin-right: 10px; }
</style>
