<!-- src/views/ProfileView.vue -->
<template>
  <div class="profile">
    <h2>내 정보</h2>
    <div v-if="authState.user">
      <label>
        닉네임:
        <input v-model="nickname" />
      </label>
      <button @click="updateNick">닉네임 수정</button>
    </div>
    <div v-else>
      <p>로그인이 필요합니다.</p>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import apiClient from '@/api.js'
import { authState, setUser } from '@/auth.js'

const nickname = ref(authState.user?.nickname || '')

function updateNick() {
  const newName = nickname.value.trim()
  if (!newName) return
  apiClient.put('/api/users/me', { nickname: newName })
      .then(res => {
        setUser(res.data)
        alert('닉네임이 수정되었습니다.')
      })
      .catch(err => console.error('닉네임 수정 실패', err))
}
</script>

<style>
.profile { max-width: 400px; margin: 20px auto; }
.profile label { display: block; margin-bottom: 0.5em; }
.profile input { padding: 4px 8px; font-size: 1em; }
</style>
