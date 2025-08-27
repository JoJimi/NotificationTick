package org.example.backend.domain.notification.controller;

import lombok.RequiredArgsConstructor;
import org.example.backend.domain.notification.service.NotificationSseService;
import org.example.backend.global.jwt.custom.CustomUserDetails;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationSseService sseService;

    /** SSE 구독: 클라이언트는 GET으로 연결 유지 */
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream(@AuthenticationPrincipal CustomUserDetails principal) {
        Long userId = principal.getUser().getId();
        return sseService.subscribe(userId);
    }
}
