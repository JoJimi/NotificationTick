<!-- src/components/LoginButtons.vue -->
<template>
  <div class="login-options">
    <a :href="googleAuthUrl" class="oauth-btn google">
      <img :src="googleLogo" alt="Google 로그인" class="icon" /> Google 로그인
    </a>
    <a :href="kakaoAuthUrl" class="oauth-btn kakao">
      <img :src="kakaoLogo" alt="Kakao 로그인" class="icon" /> Kakao 로그인
    </a>
  </div>
</template>

<script setup>
import googleLogo from '@/assets/google.png'
import kakaoLogo  from '@/assets/kakao.png'

const API_BASE    = 'http://localhost:8080'
const redirectUri = window.location.origin

const googleAuthUrl = `${API_BASE}/oauth2/authorization/google?redirect_uri=${encodeURIComponent(redirectUri)}`
const kakaoAuthUrl  = `${API_BASE}/oauth2/authorization/kakao?redirect_uri=${encodeURIComponent(redirectUri)}`
</script>

<style>
.login-options {
  display: flex; justify-content: center; gap: 1em; margin-top: 1em;
}
.oauth-btn {
  display: inline-flex; align-items: center;
  padding: 0.5em 1em; border: 1px solid #ccc; border-radius: 4px;
  text-decoration: none; color: inherit; font-size: 0.9em;
}
.oauth-btn .icon { width: 20px; height: 20px; margin-right: 8px; }
.google { /* 스타일 추가 */ }
.kakao { /* 스타일 추가 */ }
</style>
