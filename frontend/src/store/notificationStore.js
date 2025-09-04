// src/store/notificationStore.js
import { defineStore } from 'pinia';
import { ref, computed, watch } from 'vue';
import dayjs from 'dayjs';
import { fetchNotifications, markNotificationRead, markAllNotificationsRead } from '@/api/notifications';
import { useUserStore } from '@/store/userStore';

export const useNotificationStore = defineStore('notification', () => {
    const userStore = useUserStore();

    // --- 상태 ---
    const items = ref([]);           // 알림 목록 (최신 우선)
    const ids = new Set();           // 중복 방지용 ID Set (string key)
    const unreadCount = computed(() => items.value.filter(n => !n.isRead).length);

    // SSE 연결 관리
    const connecting = ref(false);
    const connected = ref(false);
    const lastEventId = ref(null);
    let abortController = null;
    let reconnectTimer = null;
    let retryMs = 3000;              // 기본 재연결 간격
    const MAX_RETRY_MS = 30000;
    const MAX_ITEMS = 500;           // 메모리 보호: 최대 보관 수
    let listenersAttached = false;   // 전역 리스너 중복 방지

    // --- 유틸 ---
    function keyOf(n) { return String(n.id); }

    function sortByTimeDesc() {
        items.value.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt));
    }

    // 목록에 안전하게 삽입/업데이트
    function upsertNotification(n) {
        if (!n || n.id == null) return;
        const key = keyOf(n);
        const idx = items.value.findIndex(x => keyOf(x) === key);

        if (idx >= 0) {
            items.value[idx] = { ...items.value[idx], ...n };
        } else {
            // createdAt 포맷 보정(서버가 문자열로 보낼 때)
            if (n.createdAt) n.createdAt = dayjs(n.createdAt).toISOString();
            items.value.unshift(n);
            ids.add(key);
            // 메모리 보호
            if (items.value.length > MAX_ITEMS) {
                const removed = items.value.splice(MAX_ITEMS);
                removed.forEach(x => ids.delete(keyOf(x)));
            }
        }
        sortByTimeDesc();
    }

    // --- API 연동 ---
    async function loadPage({ page = 1, size = 10 } = {}) {
        const pageData = await fetchNotifications({ page, size });
        const list = (pageData.content || []).map(n => ({
            ...n,
            ...(n.createdAt ? { createdAt: dayjs(n.createdAt).toISOString() } : {}),
        })).sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt));

        if (page === 1) {
            items.value = [];
            ids.clear();
        }
        for (const n of list) upsertNotification(n);
        return pageData;
    }

    async function readOne(id) {
        await markNotificationRead(id);
        const idx = items.value.findIndex(n => keyOf(n) === String(id));
        if (idx >= 0) items.value[idx] = { ...items.value[idx], isRead: true };
    }

    async function readAll() {
        await markAllNotificationsRead();
        items.value = items.value.map(n => ({ ...n, isRead: true }));
    }

    // --- SSE 파서 ---
    function parseSseChunk(buffer, onEvent) {
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

    // --- SSE 연결 ---
    async function connect() {
        if (connected.value || connecting.value) return;
        if (!userStore.accessToken) return;

        connecting.value = true;
        abortController = new AbortController();

        const doConnect = async (isRetry = false) => {
            clearTimeout(reconnectTimer);
            try {
                const res = await fetch(
                    `${import.meta.env.VITE_API_BASE_URL}/api/notifications/stream?t=${Date.now()}`,
                    {
                        method: 'GET',
                        headers: {
                            Accept: 'text/event-stream',
                            Authorization: `Bearer ${userStore.accessToken}`,
                            'Cache-Control': 'no-cache',
                            Pragma: 'no-cache',
                            ...(lastEventId.value ? { 'Last-Event-ID': String(lastEventId.value) } : {}),
                        },
                        cache: 'no-store',
                        mode: 'cors',
                        signal: abortController.signal,
                    }
                );

                // 401 → 토큰 리프레시 후 1회 재시도
                if (res.status === 401) {
                    const refreshed = await userStore.tryRefresh().catch(() => false);
                    if (refreshed) return doConnect(true);
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

                    const cut = buf.lastIndexOf('\n\n');
                    if (cut !== -1) {
                        const chunk = buf.slice(0, cut + 2);
                        buf = buf.slice(cut + 2);
                        parseSseChunk(chunk, ({ event, data, id }) => {
                            if (id) lastEventId.value = id;
                            if (event === 'init') return;
                            if (event === 'notification') {
                                try {
                                    const payload = JSON.parse(data);
                                    upsertNotification(payload);
                                } catch {
                                    /* ignore non-json */
                                }
                            }
                        });
                    }
                }

                // 정상 종료 → 재연결
                if (connected.value) {
                    connected.value = false;
                    scheduleReconnect();
                }
            } catch (e) {
                if (abortController?.signal?.aborted) return;
                connected.value = false;
                connecting.value = false;
                scheduleReconnect();
            }
        };

        const scheduleReconnect = () => {
            clearTimeout(reconnectTimer);
            const jitter = Math.floor(Math.random() * 1000); // 0~1s
            reconnectTimer = setTimeout(() => {
                if (userStore.accessToken) {
                    connecting.value = true;
                    doConnect(true);
                    retryMs = Math.min(retryMs * 2, MAX_RETRY_MS);
                }
            }, retryMs + jitter);
        };

        // 전역 리스너(가시성/네트워크 상태) 1회 등록
        if (!listenersAttached) {
            listenersAttached = true;
            window.addEventListener('visibilitychange', handleVisibility);
            window.addEventListener('online', handleOnline);
            window.addEventListener('offline', handleOffline);
        }

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

    // 가시성/네트워크 이벤트 핸들러
    function handleVisibility() {
        if (document.visibilityState === 'hidden') disconnect();
        else connect();
    }
    function handleOnline() { connect(); }
    function handleOffline() { disconnect(); }

    // 토큰 변화를 감지하여 자동 연결/해제 + 로그아웃 시 클리어
    watch(
        () => userStore.accessToken,
        (at, prev) => {
            if (at) connect();
            else {
                disconnect();
                if (prev) {
                    items.value = [];
                    ids.clear();
                    lastEventId.value = null;
                }
            }
        },
        { immediate: true }
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
