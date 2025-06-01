package org.example.backend.domain.user.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.backend.domain.notification.entity.Notification;
import org.example.backend.domain.portfolio.entity.Portfolio;
import org.example.backend.domain.watch_list.entity.WatchList;
import org.example.backend.global.entity.BaseEntity;
import org.example.backend.type.LoginType;
import org.example.backend.type.RoleType;

import java.util.*;

@Entity
@Table(name = "users",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_user_login_type_email",
                        columnNames = {"login_type", "email"}),
                @UniqueConstraint(name = "uq_user_login_type_provider",
                        columnNames = {"login_type", "provider_id"})
        })
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class User extends BaseEntity {      /** 사용자 **/

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 로컬 로그인인 경우에만 사용되는 이메일/아이디
    @Column(name = "email", length = 100)
    private String email;

    // LOCAL 로그인 시에만 저장할 비밀번호(암호화된 해시). 소셜 로그인 시 null 가능
    @Column(name = "password", length = 100)
    private String password;

    // KAKAO, GOOGLE 로그인 시 프로바이더에서 내려준 고유 식별자(ID)
    @Column(name = "provider_id", length = 100)
    private String providerId;

    // 닉네임
    @Column(name = "nickname", length = 50)
    private String nickname;

    // 로그인 유형(NOT NULL, LOCAL or KAKAO or GOOGLE)
    @Enumerated(EnumType.STRING)
    @Column(name = "login_type", nullable = false, length = 20)
    private LoginType loginType;

    // 역할 유형 (NOT NULL, ROLE_USER or ROLE_ADMIN)
    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "role_type", nullable = false, length = 20)
    private RoleType role = RoleType.ROLE_USER;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Notification> notifications = new HashSet<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Portfolio> portfolios = new HashSet<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<WatchList> watchList = new HashSet<>();

}
