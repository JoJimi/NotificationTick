package org.example.backend.domain.portfolio.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.backend.domain.transaction.entity.Transaction;
import org.example.backend.domain.user.entity.User;
import org.example.backend.global.entity.BaseEntity;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "portfolio")
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Portfolio extends BaseEntity {     /** 포트폴리오 **/

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 소유자 (NOT NULL) **/
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** 포트올리오 이름 (NOT NULL) **/
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    /** 포트폴리오 설명 (TEXT) **/
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @OneToMany(mappedBy = "portfolio", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Transaction> transactions = new HashSet<>();

}
