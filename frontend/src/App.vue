<!-- src/App.vue -->
<template>
  <header class="header">
    <h1 class="logo" @click="$router.push('/')">PredicTick</h1>
    <nav class="nav">
      <RouterLink to="/login">로그인</RouterLink>
      <RouterLink to="/profile">내 프로필</RouterLink>
      <RouterLink to="/stocks">종목</RouterLink>
      <RouterLink to="/stocks/ranking">랭킹</RouterLink>
      <RouterLink to="/notifications">알림</RouterLink>
      <NotificationBell />
    </nav>
  </header>
  <main class="main">
    <RouterView />
  </main>
</template>

<script setup>
import NotificationBell from '@/components/NotificationBell.vue';
</script>

<style scoped>
.header{display:flex;justify-content:space-between;align-items:center;padding:10px 16px;border-bottom:1px solid #ececec}
.logo{font-weight:800;cursor:pointer}
.nav{display:flex;gap:12px;align-items:center}
.main{max-width:960px;margin:20px auto;padding:0 12px}
</style>
