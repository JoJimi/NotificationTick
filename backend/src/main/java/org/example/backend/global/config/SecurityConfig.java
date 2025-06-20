package org.example.backend.global.config;

import lombok.RequiredArgsConstructor;
import org.example.backend.global.jwt.JwtAuthenticationFilter;
import org.example.backend.global.jwt.handler.*;
import org.example.backend.global.jwt.oauth2.*;
import org.example.backend.type.RoleType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.security.config.annotation.web.configurers.HttpBasicConfigurer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;

import java.util.*;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
// Spring Security 설정 전체를 담당 (CORS, CSRF, 세션관리, 필터체인 등)
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final OAuth2AuthenticationSuccessHandler oAuth2LoginSuccessHandler;
    private final CustomOAuth2UserService oauth2Service;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final CustomOidcUserService oidcService;

    private static final String[] AUTH_ALLOWLIST = {
            "/swagger-ui/**",
            "/v3/**",
            "/oauth2/**",
            "/auth/**"
    };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(CsrfConfigurer::disable)
                .httpBasic(HttpBasicConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(AUTH_ALLOWLIST).permitAll()
                        .requestMatchers("/admin/**").hasAuthority(String.valueOf(RoleType.ROLE_ADMIN.name()))
                        .requestMatchers("/api/**").hasAuthority(String.valueOf(RoleType.ROLE_USER.name())))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(oauth2Service)
                                .oidcUserService(oidcService)
                        )
                        .successHandler(oAuth2LoginSuccessHandler)
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(
                        x -> {
                            x.authenticationEntryPoint(jwtAuthenticationEntryPoint);
                            x.accessDeniedHandler(jwtAccessDeniedHandler);
                        })
                .build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOriginPatterns(List.of("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }

    /**
     * [**트러불 슈팅**]
     * static으로 설정 안하면 SecurityConfig와 CustomOAuth2UserService가 순환이 발생함
     * SecurityConfig → CustomOAuth2UserService → defaultOAuth2UserService() → SecurityConfig
     * --------------------------------------------------------------------------------------------------
     * Spring은 static @Bean 메서드는 @Configuration 클래스의 인스턴스 생성 없이도 바로 호출해서 빈으로 등록합니다.
     * 따라서 순환을 피할 수 있다.
     * --------------------------------------------------------------------------------------------------
     * 다른 방식으로는 별도의 클래스로 분리해서 작성한다. (@Configuration)
     */
    @Bean
    public static DefaultOAuth2UserService defaultOAuth2UserService() {
        return new DefaultOAuth2UserService();
    }
}
