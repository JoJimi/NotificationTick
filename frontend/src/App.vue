<template>
  <div style="text-align: center; margin-top: 4rem;">
    <h1>Vue + Vite 연동 테스트</h1>
    <button @click="callBackend">백엔드에게 인사 보내기</button>
    <p v-if="message" style="margin-top: 1rem; font-weight: bold;">
      {{ message }}
    </p>
  </div>
</template>

<script setup>
import { ref } from 'vue';

const message = ref('');

async function callBackend() {
  try {
    // proxy가 설정되어 있으므로, 도메인이나 포트를 적지 않고도 호출 가능
    const res = await fetch('/api/hello');
    if (!res.ok) throw new Error(`서버 응답 에러: ${res.status}`);
    const text = await res.text();
    message.value = text; // "Hello from Spring Backend!" 기대
  } catch (err) {
    console.error(err);
    message.value = 'Error: ' + err.message;
  }
}
</script>

<style>
button {
  padding: 0.5rem 1rem;
  border: none;
  background-color: #4f46e5;
  color: white;
  border-radius: 0.375rem;
  cursor: pointer;
}
button:hover {
  background-color: #4338ca;
}
</style>