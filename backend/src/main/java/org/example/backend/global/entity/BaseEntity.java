package org.example.backend.global.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter @Setter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;    // 엔티티 최초 저장 시 자동 입력

    @LastModifiedDate
    private LocalDateTime updatedAt;    // 엔티티 수정 시 자동 갱신

    // soft delete 플래그
    private boolean deleted = false;    // 실제 삭제 대신 플래그로 관리한다.

}
