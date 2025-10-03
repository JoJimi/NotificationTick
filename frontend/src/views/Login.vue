<!-- src/views/Login.vue -->
<template>
  <div class="login-wrap">
    <h2>소셜 로그인</h2>
    <button class="btn" @click="go('google')">
      <img :src="googleLogo" alt="Google" /> <span>Google로 로그인</span>
    </button>
    <button class="btn kakao" @click="go('kakao')">
      <img :src="kakaoLogo" alt="Kakao" /> <span>Kakao로 로그인</span>
    </button>
  </div>
</template>

<script setup>
import googleLogo from '@/assets/google.png';
import kakaoLogo from '@/assets/kakao.png';

const go = (provider) => {
  const apiBase = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';
  window.location.href = `${apiBase}/oauth2/authorization/${provider}`;
};
</script>

<style scoped>
.login-wrap{max-width:360px;margin:40px auto;display:grid;gap:12px}
.btn{display:flex;align-items:center;gap:10px;border:1px solid #eaeaea;border-radius:10px;padding:12px;background:#fff;cursor:pointer;font-weight:600}
.btn:hover{background:#fafafa}
.btn.kakao{border-color:#f7e600;background:#fffbe6}
img{width:22px;height:22px;object-fit:contain}
</style>
