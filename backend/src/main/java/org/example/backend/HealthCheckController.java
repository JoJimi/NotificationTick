package org.example.backend;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@Tag(name = "HealthCheck", description = "애플리케이션 상태 확인용 API")
public class HealthCheckController {

    @Operation(summary = "헬스 체크", description = "서버가 정상 동작 중인지 확인합니다.")
    @GetMapping("/v1/health")
    public ResponseEntity<String> health(){
        return ResponseEntity.ok("OK");
    }
}
