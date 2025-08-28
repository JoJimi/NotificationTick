<!-- src/views/Profile.vue -->
<template>
  <el-card>
    <template #header>내 프로필</template>
    <div v-if="user">
      <p><b>ID:</b> {{ user.id }}</p>
      <p><b>Email:</b> {{ user.email }}</p>
      <p><b>LoginType:</b> {{ user.loginType }}</p>
      <p><b>Role:</b> {{ user.role }}</p>

      <el-form @submit.prevent>
        <el-form-item label="닉네임">
          <el-input v-model="nickname" />
        </el-form-item>
        <el-button type="primary" @click="save" :loading="saving">저장</el-button>
      </el-form>
    </div>
    <div v-else>불러오는 중...</div>
  </el-card>
</template>

<script setup>
import { onMounted, ref, computed } from 'vue';
import { useUserStore } from '@/store/userStore';
import { ElMessage } from 'element-plus';

const store = useUserStore();
const user = computed(() => store.user);
const nickname = ref('');
const saving = ref(false);

onMounted(async () => {
  if (!store.user) await store.fetchUser().catch(()=>{});
  nickname.value = store.user?.nickname || '';
});

const save = async () => {
  saving.value = true;
  try {
    await store.updateNickname(nickname.value);
    ElMessage.success('닉네임이 업데이트되었습니다.');
  } catch (e) {
    ElMessage.error('업데이트 실패');
  } finally {
    saving.value = false;
  }
};
</script>
