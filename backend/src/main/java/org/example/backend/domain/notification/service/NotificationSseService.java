package org.example.backend.domain.notification.service;

import lombok.extern.slf4j.Slf4j;
import org.example.backend.domain.notification.dto.response.NotificationResponse;
import org.example.backend.domain.notification.entity.Notification;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

@Slf4j
@Service
public class NotificationSseService {

    // 사용자별 SSE 연결 목록
    private final Map<Long, CopyOnWriteArrayList<SseEmitter>> clients = new ConcurrentHashMap<>();

    // 기본 타임아웃 (여기선 30분)
    private static final long SSE_TIMEOUT_MS = Duration.ofMinutes(30).toMillis();

    public SseEmitter subscribe(Long userId) {
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT_MS);
        clients.computeIfAbsent(userId, k -> new CopyOnWriteArrayList<>()).add(emitter);

        // 연결 종료/타임아웃 시 정리
        emitter.onCompletion(() -> removeEmitter(userId, emitter));
        emitter.onTimeout(()    -> removeEmitter(userId, emitter));
        emitter.onError(e -> removeEmitter(userId, emitter));

        // 초기 헬스체크 이벤트(옵션)
        try {
            emitter.send(SseEmitter.event()
                    .name("init")
                    .data("connected")
                    .id(String.valueOf(System.currentTimeMillis()))
                    .reconnectTime(3000));
        } catch (IOException ignored) { /* 연결 직후 끊긴 경우 무시 */ }

        return emitter;
    }

    private void removeEmitter(Long userId, SseEmitter emitter) {
        List<SseEmitter> list = clients.get(userId);
        if (list != null) list.remove(emitter);
    }

    /** 도메인 엔티티 기반 푸시 */
    public void push(Long userId, Notification notification) {
        push(userId, NotificationResponse.fromEntity(notification));
    }

    /** DTO 기반 푸시 */
    public void push(Long userId, NotificationResponse payload) {
        List<SseEmitter> userEmitters = clients.get(userId);
        if (userEmitters == null || userEmitters.isEmpty()) return;

        for (SseEmitter emitter : userEmitters) {
            try {
                emitter.send(SseEmitter.event()
                        .name("notification")
                        .data(payload, MediaType.APPLICATION_JSON)
                        .id(String.valueOf(payload.id()))
                        .reconnectTime(3000));
            } catch (Exception e) {
                // 전송 실패 시 연결 제거
                removeEmitter(userId, emitter);
            }
        }
    }
}