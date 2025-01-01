package com.project.plateposting.api.security.jwt.service;
import com.project.plateposting.api.member.service.MemberService;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.SignatureVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.Map;
import java.util.Optional;

import lombok.extern.slf4j.Slf4j;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Service
@Slf4j
@Getter
@RequiredArgsConstructor
public class JwtService {

    @Value("${jwt.key.secretKey}")
    private String secretKey;

    @Value("${jwt.expiration.access}")
    private Long accessTokenExpiration;

    @Value("${jwt.expiration.refresh}")
    private Long refreshTokenExpiration;

    @Value("${jwt.header.access}")
    private String accessTokenHeader;

    @Value("${jwt.header.refresh}")
    private String refreshTokenHeader;

    private static final String ACCESS_TOKEN_SUBJECT = "AccessToken";
    private static final String REFRESH_TOKEN_SUBJECT = "RefreshToken";
    private static final String EMAIL_CLAIM = "email";
    private static final String BEARER = "Bearer ";

    private MemberService memberService;

    public String createAccessToken(String email) {
        Date now = new Date();
        return JWT.create()
                .withSubject(ACCESS_TOKEN_SUBJECT)
                .withExpiresAt(new Date(now.getTime() + accessTokenExpiration))
                .withClaim(EMAIL_CLAIM, email)
                .sign(Algorithm.HMAC512(secretKey));
    }

    public String createRefreshToken() {
        Date now = new Date();
        return JWT.create()
                .withSubject(REFRESH_TOKEN_SUBJECT)
                .withExpiresAt(new Date(now.getTime() + refreshTokenExpiration))
                .sign(Algorithm.HMAC512(secretKey));
    }

    public Map<String, String> createAccessAndRefreshToken(String email) {
        String accessToken = createAccessToken(email);
        String refreshToken = createRefreshToken();

        updateRefreshToken(email, refreshToken);

        log.info("AccessToken, RefreshToken 발급을 완료하였습니다.");
        log.info("AccessToken : {}", accessToken);
        log.info("RefreshToken : {}", refreshToken);

        return Map.of(
                "accessToken", accessToken,
                "refreshToken", refreshToken
        );
    }

    public void updateRefreshToken(String email, String refreshToken) {
        memberService.updateRefreshToken(email, refreshToken);
    }

    public void setAccessTokenHeader(HttpServletResponse response, String accessToken) {
        response.setHeader(accessTokenHeader, BEARER + accessToken);
    }

    public void setRefreshTokenHeader(HttpServletResponse response, String refreshToken) {
        response.setHeader(refreshTokenHeader, BEARER + refreshToken);
    }

    public void setAccessAndRefreshTokenHeader(HttpServletResponse response, String accessToken, String refreshToken) {
        response.setStatus(HttpServletResponse.SC_OK);
        setAccessTokenHeader(response, accessToken);
        setRefreshTokenHeader(response, refreshToken);
    }


    public boolean isTokenValid(String token) {
        try {
            JWT.require(Algorithm.HMAC512(secretKey)).build().verify(token);
            return true;
        } catch (TokenExpiredException e) {
            log.error("토큰이 만료되었습니다: {}", e.getMessage());
            return false;
        } catch (SignatureVerificationException e) {
            log.error("토큰 서명 검증을 실패하였습니다: {}", e.getMessage());
            return false;
        } catch (Exception e) {
            log.error("유효하지 않은 토큰입니다: {}", e.getMessage());
            return false;
        }
    }

    public Optional<String> extractEmail(String accessToken) {
        try {
            return Optional.ofNullable(JWT.require(Algorithm.HMAC512(secretKey))
                    .build()
                    .verify(accessToken)
                    .getClaim(EMAIL_CLAIM)
                    .asString());
        } catch (Exception e) {
            log.error("액세스 토큰이 유효하지 않습니다");
            return Optional.empty();
        }
    }
}
