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
import org.example.backend.global.jwt.redis.TokenService;
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
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtTokenProvider jwtTokenProvider;
    private final CustomUserDetailsService customUserDetailsService;
    private final ObjectMapper objectMapper;
    private final TokenService tokenService;

    private static final String AUTHORIZATION_HEADER = HttpHeaders.AUTHORIZATION;
    private static final String BEARER_PREFIX = "Bearer ";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = resolveToken(request);

        if (StringUtils.hasText(token)) {
            try {
                if (jwtTokenProvider.validateAccessToken(token)) {
                    String userId = jwtTokenProvider.getSubject(token);

                    // fam 블랙리스트(세션군 차단) 체크 — 재사용 탐지 후 AT도 차단
                    String fam = jwtTokenProvider.getFamilyId(token);
                    if (tokenService.isFamilyBlacklisted(fam)) {
                        sendErrorResponse(response, ErrorCode.UNAUTHORIZED);
                        return;
                    }

                    if (userId != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                        UserDetails userDetails = customUserDetailsService.loadUserByUsername(userId);
                        UsernamePasswordAuthenticationToken authentication =
                                new UsernamePasswordAuthenticationToken(
                                        userDetails, null, userDetails.getAuthorities()
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

    private String resolveToken(HttpServletRequest request) {
        String bearer = request.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.hasText(bearer) && bearer.startsWith(BEARER_PREFIX)) {
            return bearer.substring(BEARER_PREFIX.length());
        }
        String fromQuery = request.getParameter("access_token");
        if (!StringUtils.hasText(fromQuery)) {
            fromQuery = request.getParameter("token");
        }
        if (StringUtils.hasText(fromQuery)) {
            return fromQuery.trim();
        }
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
