<!-- src/components/NotificationBell.vue -->
<template>
  <el-dropdown trigger="click" @visible-change="onVisible">
    <span class="bell">
      <el-badge :value="unreadCount" :hidden="unreadCount === 0" class="badge">
        <el-icon><Bell /></el-icon>
      </el-badge>
    </span>
    <template #dropdown>
      <el-dropdown-menu class="dropdown">
        <div class="head">
          <span>알림</span>
          <div class="head-actions">
            <el-button size="small" text @click.stop="goAll">전체보기</el-button>
            <el-button size="small" text @click.stop="readAll">모두읽음</el-button>
          </div>
        </div>

        <el-dropdown-item v-if="preview.length === 0" disabled>
          <div class="empty">알림이 없습니다.</div>
        </el-dropdown-item>

        <template v-for="n in preview" :key="n.id">
          <el-dropdown-item @click="openOne(n)">
            <div class="item" :class="{ unread: !n.isRead }">
              <div class="msg">{{ n.message }}</div>
              <div class="meta">{{ formatTime(n.createdAt) }}</div>
            </div>
          </el-dropdown-item>
        </template>
      </el-dropdown-menu>
    </template>
  </el-dropdown>
</template>

<script setup>
import { computed } from 'vue';
import dayjs from 'dayjs';
import { ElMessage } from 'element-plus';
import { Bell } from '@element-plus/icons-vue';
import { useNotificationStore } from '@/store/notificationStore';
import { useRouter } from 'vue-router';

const router = useRouter();
const store = useNotificationStore();

const unreadCount = computed(() => store.unreadCount);
const preview = computed(() => store.items.slice(0, 10));

function onVisible(show) {
  if (show && store.items.length === 0) {
    // 첫 오픈 시 1페이지 프리페치
    store.loadPage({ page: 1, size: 10 }).catch(() => {});
  }
}

function formatTime(t) {
  return t ? dayjs(t).format('YYYY-MM-DD HH:mm') : '-';
}

async function readAll() {
  try {
    await store.readAll();
    ElMessage.success('모든 알림을 읽음 처리했습니다.');
  } catch {
    ElMessage.error('읽음 처리에 실패했습니다.');
  }
}

async function openOne(n) {
  try {
    if (!n.isRead) await store.readOne(n.id);
    // 알림 타입에 따라 라우팅 로직을 확장할 수 있습니다.
    // 예: NEWS_EVENT → 관련 종목 상세/뉴스로 이동 등
    router.push('/notifications');
  } catch {
    ElMessage.error('처리에 실패했습니다.');
  }
}

function goAll() {
  router.push('/notifications');
}
</script>

<style scoped>
.bell { cursor: pointer; display: inline-flex; align-items: center; }
.badge :deep(.el-badge__content){ transform: translate(8px, -8px); }
.dropdown { width: 360px; padding: 8px; }
.head{display:flex;justify-content:space-between;align-items:center;padding:6px 10px 8px 10px;border-bottom:1px solid #eee;margin-bottom:6px}
.head-actions{display:flex;gap:6px}
.item{display:flex;flex-direction:column;gap:4px;max-width:100%}
.item.unread .msg{font-weight:700}
.msg{white-space:nowrap;overflow:hidden;text-overflow:ellipsis}
.meta{font-size:12px;color:#888}
.empty{padding:10px 6px;color:#888}
</style>
