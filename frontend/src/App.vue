<!-- src/App.vue -->
<template>
  <Header />
  <main>
    <router-view />
  </main>
</template>

<script setup>
import Header from '@/components/Header.vue'
</script>

<style>
main { padding: 20px; }
</style>
