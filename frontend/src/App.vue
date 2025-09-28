<!-- src/App.vue -->
<template>
  <header class="header">
    <h1 class="logo" @click="$router.push('/')">PredicTick</h1>
    <nav class="nav">
      <NavButton to="/login" size="small">로그인</NavButton>
      <NavButton to="/profile" size="small" type="primary">내 프로필</NavButton>
      <NavButton to="/stocks" size="small">종목</NavButton>
      <NavButton to="/stocks/ranking" size="small">랭킹</NavButton>
      <NavButton to="/notifications" size="small">알림</NavButton>
      <NotificationBell />
    </nav>
  </header>
  <main class="main">
    <RouterView />
  </main>
</template>

<script setup>
import NotificationBell from '@/components/NotificationBell.vue';
import NavButton from '@/components/NavButton.vue';
</script>

<style scoped>
.header{display:flex;justify-content:space-between;align-items:center;padding:10px 16px;border-bottom:1px solid #ececec;background:#fff;position:sticky;top:0;z-index:10}
.logo{font-weight:800;cursor:pointer}
.nav{display:flex;gap:8px;align-items:center}
.main{max-width:960px;margin:20px auto;padding:0 12px}
</style>
