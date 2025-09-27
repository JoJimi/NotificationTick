package org.example.backend.global.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.backend.global.exception.ErrorCode;
import org.example.backend.global.exception.ErrorResponse;
import org.example.backend.global.jwt.custom.CustomUserDetailsService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
@Component
// 들어오는 요청의 헤더에서 JWT를 추출해 검증 후 SecurityContext에 인증 객체 설정
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtTokenProvider jwtTokenProvider;
    private final CustomUserDetailsService customUserDetailsService;
    private final ObjectMapper objectMapper;

    private static final String AUTHORIZATION_HEADER = HttpHeaders.AUTHORIZATION;
    private static final String BEARER_PREFIX = "Bearer ";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // 0) Preflight는 그대로 통과
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        // 1) HTTP 헤더에서 토큰 꺼내기(헤더 → 쿼리파라미터 → 쿠키)
        String token = resolveToken(request);

        if (StringUtils.hasText(token)) {
            try {
                // 2) 토큰 유효성 검사 (만료, 서명, 타입 등)
                if (jwtTokenProvider.validateAccessToken(token)) {
                    // 3) 토큰에서 userId 추출
                    String userId = jwtTokenProvider.getSubject(token);

                    // 4) 아직 인증 정보가 없다면 SecurityContext 에 등록
                    if (userId != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                        UserDetails userDetails = customUserDetailsService.loadUserByUsername(userId);

                        UsernamePasswordAuthenticationToken authentication =
                                new UsernamePasswordAuthenticationToken(
                                        userDetails,
                                        null,                                    // credentials: 토큰을 직접 저장할 필요 없음
                                        userDetails.getAuthorities()                        // DB에 저장된 권한 그대로 사용
                                );

                        SecurityContextHolder.getContext().setAuthentication(authentication);
                    }
                }
            } catch (ExpiredJwtException e) {
                sendErrorResponse(response, ErrorCode.UNAUTHORIZED);
                log.warn("Expired JWT token: {}", e.getMessage());
                return;
            } catch (JwtException e) {
                sendErrorResponse(response, ErrorCode.UNAUTHORIZED);
                log.warn("Invalid JWT token: {}", e.getMessage());
                return;
            } catch (Exception e) {
                sendErrorResponse(response, ErrorCode.INTERNAL_SERVER_ERROR);
                log.error("Authentication filter error", e);
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private void sendErrorResponse(HttpServletResponse response, ErrorCode errorCode) throws IOException {
        response.setStatus(errorCode.getHttpStatus().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        ErrorResponse body = ErrorResponse.builder()
                .errorCode(errorCode.getCode())
                .message(errorCode.getMessage())
                .build();

        objectMapper.writeValue(response.getWriter(), body);
    }

    /**
     * 토큰 추출 우선순위:
     * 1) Authorization: Bearer xxx
     * 2) ?access_token=xxx (또는 ?token=xxx)
     * 3) 쿠키 access_token (또는 Authorization=Bearer xxx)
     */
    private String resolveToken(HttpServletRequest request) {
        // 1) Authorization 헤더
        String bearer = request.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.hasText(bearer) && bearer.startsWith(BEARER_PREFIX)) {
            return bearer.substring(BEARER_PREFIX.length());
        }

        // 2) Query String (?access_token= / ?token=)
        String fromQuery = request.getParameter("access_token");
        if (!StringUtils.hasText(fromQuery)) {
            fromQuery = request.getParameter("token");
        }
        if (StringUtils.hasText(fromQuery)) {
            return fromQuery.trim();
        }

        // 3) Cookie
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie c : cookies) {
                if ("access_token".equals(c.getName()) && StringUtils.hasText(c.getValue())) {
                    return c.getValue().trim();
                }
                if ("Authorization".equalsIgnoreCase(c.getName())) {
                    String v = c.getValue();
                    if (StringUtils.hasText(v) && v.startsWith(BEARER_PREFIX)) {
                        return v.substring(BEARER_PREFIX.length()).trim();
                    }
                }
            }
        }

        return null;
    }
}
