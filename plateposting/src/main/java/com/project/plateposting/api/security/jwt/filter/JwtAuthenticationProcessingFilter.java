package com.project.plateposting.api.security.jwt.filter;

import com.project.plateposting.api.member.entity.Member;
import com.project.plateposting.api.security.jwt.service.JwtService;
import com.project.plateposting.api.member.repository.MemberRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.core.authority.mapping.NullAuthoritiesMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationProcessingFilter extends OncePerRequestFilter { // 클라이언트 요청에 1번 실행

    @Value("${jwt.header.access}")
    private String accessTokenHeader;

    @Value("${jwt.header.refresh}")
    private String refreshTokenHeader;

    private static final List<String> NO_CHECK_URLS = List.of(
            "/api/v1/login"
    );
    private static final String TOKEN_REISSUE_URL = "/api/v1/token/reissue";

    private final JwtService jwtService;
    private final MemberRepository memberRepository;

    private final GrantedAuthoritiesMapper authoritiesMapper = new NullAuthoritiesMapper();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String requestURI = request.getRequestURI();

        if (NO_CHECK_URLS.contains(requestURI)) {
            filterChain.doFilter(request, response);
            return;
        }

        if (requestURI.equals(TOKEN_REISSUE_URL)) {
            Optional<String> refreshToken = extractToken(request, refreshTokenHeader)
                    .filter(jwtService::isTokenValid);

            refreshToken.ifPresent(token -> reissueToken(response, token));
            filterChain.doFilter(request, response);
            return;
        }

        extractToken(request, accessTokenHeader)
                .filter(jwtService::isTokenValid)
                .flatMap(jwtService::extractEmail)
                .flatMap(memberRepository::findByEmail)
                .ifPresent(this::setAuthentication);

        filterChain.doFilter(request, response);
    }

    private void reissueToken(HttpServletResponse response, String refreshToken) {
        memberRepository.findByRefreshToken(refreshToken)
                .ifPresent(user -> {
                    String newAccessToken = jwtService.createAccessToken(user.getEmail());
                    String newRefreshToken = jwtService.createRefreshToken();

                    jwtService.updateRefreshToken(user.getEmail(), newRefreshToken);
                    jwtService.setAccessAndRefreshTokenHeader(response, newAccessToken, newRefreshToken);

                    log.info("AccessToken, RefreshToken 재발급을 완료하였습니다");
                    log.info("Access Token : {}", newAccessToken);
                    log.info("Refresh Token : {}", newRefreshToken);
                });
    }

    private Optional<String> extractToken(HttpServletRequest request, String headerName) {
        String bearerToken = request.getHeader(headerName);
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return Optional.of(bearerToken.substring(7));
        }
        return Optional.empty();
    }

    public void setAuthentication(Member member) {
        UserDetails userDetails = User.builder()
                .username(member.getEmail())
                .password(member.getPassword())
                .roles(member.getRole().name())
                .build();

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null, authoritiesMapper.mapAuthorities(userDetails.getAuthorities())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        Authentication authenticationLogCheck = SecurityContextHolder.getContext().getAuthentication();
        if (authenticationLogCheck != null) {
            log.info("현재 인증된 사용자: {}", authenticationLogCheck.getName());
        } else {
            log.info("인증 정보가 존재하지 않습니다.");
        }
    }
}
