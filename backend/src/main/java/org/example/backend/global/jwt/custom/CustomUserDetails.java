package org.example.backend.global.jwt.custom;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.example.backend.domain.user.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.*;

@Getter
@RequiredArgsConstructor
// Spring Security가 인증 정보를 참조할 때 사용하는 UserDetails 구현체
public class CustomUserDetails implements UserDetails {

    private final User user;

    @Override
    public String getUsername() {     //유저 이메일 가져오기
        return user.getEmail();
    }

    @Override
    public String getPassword() {
        return null;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority(user.getRole().name()));
    }

}
