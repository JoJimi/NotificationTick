// src/store/notificationStore.js
import { defineStore } from 'pinia';
import { ref, computed, watch } from 'vue';
import dayjs from 'dayjs';
import { fetchNotifications, markNotificationRead, markAllNotificationsRead } from '@/api/notifications';
import { useUserStore } from '@/store/userStore';

export const useNotificationStore = defineStore('notification', () => {
    const userStore = useUserStore();

    // 상태
    const items = ref([]);           // 알림 목록 (최신 우선)
    const ids = new Set();           // 중복 방지용 ID Set
    const unreadCount = computed(() => items.value.filter(n => !n.isRead).length);

    // SSE 연결 관리
    const connecting = ref(false);
    const connected = ref(false);
    const lastEventId = ref(null);
    let abortController = null;
    let reconnectTimer = null;
    let retryMs = 3000;              // 기본 재연결 간격(서버가 retry 제공 안하면 이 값 사용)
    const MAX_RETRY_MS = 30000;

    // 유틸: 목록에 안전하게 삽입
    function upsertNotification(n) {
        if (!n || !n.id) return;
        if (ids.has(n.id)) {
            // 부분 업데이트: 읽음 상태/메시지 등 동기화
            const idx = items.value.findIndex(x => x.id === n.id);
            if (idx >= 0) items.value[idx] = { ...items.value[idx], ...n };
            return;
        }
        ids.add(n.id);
        items.value.unshift(n);
    }

    // 최초 페이지 로딩
    async function loadPage({ page = 1, size = 10 } = {}) {
        const pageData = await fetchNotifications({ page, size });
        // 최신순 보장: 서버 정렬 + 클라에서 한 번 더 정렬
        const list = (pageData.content || []).sort((a, b) =>
            new Date(b.createdAt) - new Date(a.createdAt)
        );
        if (page === 1) {
            items.value = [];
            ids.clear();
        }
        for (const n of list) upsertNotification(n);
        return pageData;
    }

    // 단건 읽음
    async function readOne(id) {
        await markNotificationRead(id);
        const idx = items.value.findIndex(n => n.id === id);
        if (idx >= 0) items.value[idx] = { ...items.value[idx], isRead: true };
    }

    // 전체 읽음
    async function readAll() {
        await markAllNotificationsRead();
        items.value = items.value.map(n => ({ ...n, isRead: true }));
    }

    // SSE 메시지 파서 (event-stream 규격 단순 파싱)
    function parseSseChunk(buffer, onEvent) {
        // 이벤트는 \n\n 로 구분
        const events = buffer.split(/\n\n+/);
        for (const raw of events) {
            if (!raw.trim()) continue;
            let eventName = 'message';
            let dataLines = [];
            let id = null;
            for (const line of raw.split(/\n/)) {
                if (!line || line.startsWith(':')) continue;
                if (line.startsWith('event:')) eventName = line.slice(6).trim();
                else if (line.startsWith('data:')) dataLines.push(line.slice(5).trim());
                else if (line.startsWith('id:')) id = line.slice(3).trim();
                else if (line.startsWith('retry:')) {
                    const v = Number(line.slice(6).trim());
                    if (!Number.isNaN(v)) retryMs = Math.min(Math.max(v, 1000), MAX_RETRY_MS);
                }
            }
            const dataRaw = dataLines.join('\n');
            onEvent({ event: eventName, data: dataRaw, id });
        }
    }

    // SSE 연결
    async function connect() {
        if (connected.value || connecting.value) return;
        if (!userStore.accessToken) return;

        connecting.value = true;
        abortController = new AbortController();

        const doConnect = async (isRetry = false) => {
            clearTimeout(reconnectTimer);
            try {
                const res = await fetch(
                    `${import.meta.env.VITE_API_BASE_URL}/api/notifications/stream`,
                    {
                        method: 'GET',
                        headers: {
                            Accept: 'text/event-stream',
                            Authorization: `Bearer ${userStore.accessToken}`,
                            ...(lastEventId.value ? { 'Last-Event-ID': String(lastEventId.value) } : {}),
                        },
                        signal: abortController.signal,
                    }
                );

                // 401 → 토큰 리프레시 후 1회 재시도
                if (res.status === 401) {
                    const refreshed = await userStore.tryRefresh().catch(() => false);
                    if (refreshed) return doConnect(true);
                    // 리프레시 실패: 연결 중단
                    throw new Error('Unauthorized');
                }

                if (!res.ok || !res.body) {
                    throw new Error(`SSE failed: ${res.status}`);
                }

                connected.value = true;
                connecting.value = false;

                const reader = res.body.getReader();
                const decoder = new TextDecoder('utf-8');
                let buf = '';

                while (true) {
                    const { value, done } = await reader.read();
                    if (done) break;
                    buf += decoder.decode(value, { stream: true });
                    // 파싱은 덩어리마다 수행
                    const cut = buf.lastIndexOf('\n\n');
                    if (cut !== -1) {
                        const chunk = buf.slice(0, cut + 2);
                        buf = buf.slice(cut + 2);
                        parseSseChunk(chunk, ({ event, data, id }) => {
                            if (id) lastEventId.value = id;
                            if (event === 'init') return; // 헬스 체크
                            if (event === 'notification') {
                                try {
                                    const payload = JSON.parse(data);
                                    // createdAt 포맷 보정(문자열로 들어올 때)
                                    if (payload?.createdAt) {
                                        payload.createdAt = dayjs(payload.createdAt).toISOString();
                                    }
                                    upsertNotification(payload);
                                } catch {
                                    // JSON 아님 → 무시
                                }
                            }
                        });
                    }
                }

                // 정상 종료(타임아웃 포함) → 재연결
                if (connected.value) {
                    connected.value = false;
                    scheduleReconnect();
                }
            } catch (e) {
                // abort 가 아닌 경우에만 재연결
                if (abortController?.signal?.aborted) return;
                connected.value = false;
                connecting.value = false;
                scheduleReconnect();
            }
        };

        const scheduleReconnect = () => {
            clearTimeout(reconnectTimer);
            reconnectTimer = setTimeout(() => {
                // 토큰이 있으면 재연결
                if (userStore.accessToken) {
                    connecting.value = true;
                    doConnect(true);
                    // 지수 백오프
                    retryMs = Math.min(retryMs * 2, MAX_RETRY_MS);
                }
            }, retryMs);
        };

        // 최초 연결
        retryMs = 3000;
        await doConnect(false);
    }

    function disconnect() {
        clearTimeout(reconnectTimer);
        reconnectTimer = null;
        if (abortController) {
            abortController.abort();
            abortController = null;
        }
        connecting.value = false;
        connected.value = false;
    }

    // 토큰 변화를 감지하여 자동 연결/해제
    watch(
        () => userStore.accessToken,
        (at) => {
            if (at) connect();
            else disconnect();
        },
        { immediate: true }
    );

    // 로그아웃 시 목록 초기화(선택)
    watch(
        () => userStore.accessToken,
        (at, prev) => {
            if (!at && prev) {
                items.value = [];
                ids.clear();
                lastEventId.value = null;
            }
        }
    );

    return {
        // state
        items,
        unreadCount,
        connected,
        connecting,
        // actions
        loadPage,
        readOne,
        readAll,
        connect,
        disconnect,
    };
});
