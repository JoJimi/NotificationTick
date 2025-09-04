<!-- src/views/notification/NotificationSettings.vue -->
<template>
  <el-card>
    <template #header>
      <div class="header-row">
        <span>알림 설정</span>
      </div>
    </template>

    <div class="row">
      <div class="left">
        <div class="title">야간 금지</div>
        <div class="desc">매일 00:00 ~ 06:00 사이에는 SSE 연결을 중지하여 알림을 받지 않습니다.</div>
      </div>
      <div class="right">
        <el-switch v-model="nightDndEnabled" active-text="사용" inactive-text="해제" />
      </div>
    </div>

    <el-divider />

    <div class="row">
      <div class="left">
        <div class="title">휴가 모드</div>
        <div class="desc">선택한 날짜 범위 동안 알림 수신을 중지합니다.</div>
        <div class="picker">
          <el-date-picker
              v-model="vacationRange"
              type="daterange"
              range-separator="~"
              start-placeholder="시작일"
              end-placeholder="종료일"
              format="YYYY-MM-DD"
              value-format="YYYY-MM-DD"
              unlink-panels
              :disabled="!vacationEnabled"
          />
        </div>
      </div>
      <div class="right">
        <div class="right-col">
          <el-switch v-model="vacationEnabled" active-text="사용" inactive-text="해제" />
          <div class="gap"></div>
          <el-button size="small" @click="clearVacation" :disabled="!vacationEnabled">휴가 해제</el-button>
        </div>
      </div>
    </div>

    <el-alert
        v-if="pausedNow"
        class="mt"
        title="현재 알림이 일시 중지된 상태입니다."
        type="warning"
        show-icon
    />
  </el-card>
</template>

<script setup>
import { computed, watch } from 'vue';
import { storeToRefs } from 'pinia';
import { ElMessage } from 'element-plus';
import { useNotificationPrefsStore } from '@/store/notificationPrefsStore';

const prefs = useNotificationPrefsStore();
// storeToRefs로 writable ref 추출 (v-model에 바로 바인딩 가능)
const { nightDndEnabled, vacationEnabled, vacationStart, vacationEnd, pausedNow } = storeToRefs(prefs);

// 날짜 범위를 하나의 v-model로 묶기
const vacationRange = computed({
  get: () => (vacationStart.value && vacationEnd.value) ? [vacationStart.value, vacationEnd.value] : null,
  set: (arr) => {
    if (!arr || arr.length !== 2) {
      vacationStart.value = null;
      vacationEnd.value = null;
      return;
    }
    const [s, e] = arr;
    vacationStart.value = s;
    vacationEnd.value = e;
  }
});

watch(nightDndEnabled, (on) => {
  ElMessage.success(on ? '야간 금지 사용' : '야간 금지 해제');
});

watch(vacationEnabled, (on) => {
  if (!on) { vacationStart.value = null; vacationEnd.value = null; }
});

function clearVacation() {
  prefs.clearVacation();
  ElMessage.success('휴가 모드를 해제했습니다.');
}
</script>

<style scoped>
.header-row{display:flex;justify-content:space-between;align-items:center}
.row{display:flex;justify-content:space-between;align-items:flex-start;gap:12px;margin:12px 0}
.left{flex:1}
.right{min-width:220px;display:flex;justify-content:flex-end}
.right-col{display:flex;flex-direction:column;align-items:flex-end}
.title{font-weight:700}
.desc{color:#666;margin-top:4px}
.picker{margin-top:10px}
.gap{height:8px}
.mt{margin-top:12px}
</style>
