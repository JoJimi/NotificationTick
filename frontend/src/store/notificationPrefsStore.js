// src/store/notificationPrefsStore.js
import { defineStore } from 'pinia';
import { ref, computed, watch } from 'vue';
import dayjs from 'dayjs';

const LS_KEY = 'notification_prefs_v1';

export const useNotificationPrefsStore = defineStore('notificationPrefs', () => {
    // --- 상태 ---
    const nightDndEnabled = ref(true);   // 야간 금지 기본 ON (원하면 false로 바꿔도 됨)
    const vacationEnabled = ref(false);
    const vacationStart = ref(null);     // 'YYYY-MM-DD'
    const vacationEnd = ref(null);       // 'YYYY-MM-DD'

    // 반응형 시계(분 단위 업데이트): 06:00 경계/날짜 변경에 즉시 반응
    const now = ref(dayjs());
    setInterval(() => { now.value = dayjs(); }, 60 * 1000);

    // --- 영속화 ---
    function load() {
        try {
            const saved = JSON.parse(localStorage.getItem(LS_KEY));
            if (saved) {
                nightDndEnabled.value = !!saved.nightDndEnabled;
                vacationEnabled.value = !!saved.vacationEnabled;
                vacationStart.value = saved.vacationStart ?? null;
                vacationEnd.value = saved.vacationEnd ?? null;
            }
        } catch {}
    }

    function persist() {
        localStorage.setItem(LS_KEY, JSON.stringify({
            nightDndEnabled: nightDndEnabled.value,
            vacationEnabled: vacationEnabled.value,
            vacationStart: vacationStart.value,
            vacationEnd: vacationEnd.value,
        }));
    }

    watch([nightDndEnabled, vacationEnabled, vacationStart, vacationEnd], persist, { deep: true });

    // --- 계산/판정 ---
    const isInNightDndNow = computed(() => {
        if (!nightDndEnabled.value) return false;
        const h = now.value.hour();
        // 00:00 <= t < 06:00
        return h >= 0 && h < 6;
    });

    function isInVacationNow(ts = now.value) {
        if (!vacationEnabled.value) return false;
        if (!vacationStart.value || !vacationEnd.value) return false;
        const s = dayjs(vacationStart.value, 'YYYY-MM-DD').startOf('day');
        const e = dayjs(vacationEnd.value, 'YYYY-MM-DD').endOf('day');
        return (ts.isAfter(s) && ts.isBefore(e)) || ts.isSame(s) || ts.isSame(e);
    }

    const pausedNow = computed(() => isInNightDndNow.value || isInVacationNow());

    function clearVacation() {
        vacationEnabled.value = false;
        vacationStart.value = null;
        vacationEnd.value = null;
        persist();
    }

    load();

    return {
        // state
        nightDndEnabled, vacationEnabled, vacationStart, vacationEnd,
        // computed & fn
        isInNightDndNow, isInVacationNow, pausedNow,
        // actions
        clearVacation,
    };
});
