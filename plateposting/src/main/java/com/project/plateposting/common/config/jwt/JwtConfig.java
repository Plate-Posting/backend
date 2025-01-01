package com.project.plateposting.common.config.jwt;
import com.project.plateposting.api.member.repository.MemberRepository;
import com.project.plateposting.api.security.jwt.filter.JwtAuthenticationProcessingFilter;
import com.project.plateposting.api.security.jwt.service.JwtService;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class JwtConfig {

    private final JwtService jwtService;
    private final MemberRepository memberRepository;

    @Bean
    public JwtAuthenticationProcessingFilter jwtAuthenticationProcessingFilter() {
        return new JwtAuthenticationProcessingFilter(jwtService, memberRepository);
    }
}
