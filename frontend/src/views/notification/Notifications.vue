<!-- src/views/notification/Notifications.vue -->
<template>
  <el-card>
    <template #header>
      <div class="header-row">
        <span>내 알림</span>
        <div class="actions">
          <el-button size="small" @click="refresh">새로고침</el-button>
          <el-button size="small" text @click="onReadAll">모두읽음</el-button>
        </div>
      </div>
    </template>

    <el-table
        :data="rows"
        v-loading="loading"
        empty-text="알림이 없습니다."
        style="width: 100%"
        header-cell-class-name="table-header"
        @row-click="open"
    >
      <el-table-column prop="message" label="내용" min-width="420">
        <template #default="{ row }">
          <div :class="{ unread: !row.isRead }">{{ row.message }}</div>
        </template>
      </el-table-column>
      <el-table-column prop="type" label="유형" width="140" />
      <el-table-column prop="createdAt" label="시각" width="170">
        <template #default="{ row }">{{ fmt(row.createdAt) }}</template>
      </el-table-column>
      <el-table-column label="" width="120">
        <template #default="{ row }">
          <el-button size="small" @click.stop="open(row)">열기</el-button>
          <el-button size="small" text @click.stop="onRead(row)" :disabled="row.isRead">읽음</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pager">
      <el-pagination
          background
          layout="prev, pager, next, sizes, total"
          :page-sizes="[10,20,50]"
          :page-size="size"
          :current-page="page"
          :total="totalElements"
          @size-change="onSizeChange"
          @current-change="onPageChange"
      />
    </div>
  </el-card>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import dayjs from 'dayjs';
import { useNotificationStore } from '@/store/notificationStore';

const route = useRoute();
const router = useRouter();
const store = useNotificationStore();

const loading = ref(false);
const page = ref(Number(route.query.page || 1));
const size = ref(Number(route.query.size || 10));

const rows = computed(() => store.items);
const totalElements = ref(0); // 서버 totalElements 반영

function fmt(t) {
  return t ? dayjs(t).format('YYYY-MM-DD HH:mm') : '-';
}

async function load() {
  loading.value = true;
  try {
    const res = await store.loadPage({ page: page.value, size: size.value });
    totalElements.value = res.totalElements ?? store.items.length;
  } catch {
    ElMessage.error('알림을 불러오지 못했습니다.');
  } finally {
    loading.value = false;
  }
}

function refresh() { load(); }

function syncQuery() {
  router.replace({ path: '/notifications', query: { page: String(page.value), size: String(size.value) } });
}

function onPageChange(p) { page.value = p; syncQuery(); }
function onSizeChange(s) { size.value = s; page.value = 1; syncQuery(); }

async function onRead(row) {
  try {
    await store.readOne(row.id);
    ElMessage.success('읽음 처리되었습니다.');
  } catch {
    ElMessage.error('처리에 실패했습니다.');
  }
}

async function onReadAll() {
  try {
    await store.readAll();
    ElMessage.success('모든 알림을 읽음 처리했습니다.');
  } catch {
    ElMessage.error('처리에 실패했습니다.');
  }
}

function open(row) {
  // 알림 타입 별 라우팅 커스터마이즈 포인트
  // 예: if (row.type === 'NEWS_EVENT') router.push(`/news/${symbol}`)
  // 여기서는 목록 유지
}

watch(() => route.query, () => {
  page.value = Number(route.query.page || 1);
  size.value = Number(route.query.size || 10);
  load();
});

onMounted(load);
</script>

<style scoped>
.header-row{display:flex;justify-content:space-between;align-items:center}
.actions{display:flex;gap:8px;align-items:center}
.pager{display:flex;justify-content:flex-end;margin-top:12px}
.table-header{font-weight:700}
.unread{font-weight:700}
</style>
