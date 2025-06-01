package org.example.backend.global.entity;


import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
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

    @CreatedBy
    private String createdBy;           // 데이터를 생성한 주체, Spring Security와 연동하여 자동 주입

    @LastModifiedBy
    private String lastModifiedBy;      // 마지막으로 수정한 주체, 자동 주입

    // soft delete 플래그
    private boolean deleted = false;    // 실제 삭제 대신 플래그로 관리한다.

}
