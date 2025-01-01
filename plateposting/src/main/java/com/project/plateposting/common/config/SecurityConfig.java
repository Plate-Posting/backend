package com.project.plateposting.common.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.plateposting.api.member.repository.MemberRepository;
import com.project.plateposting.api.security.jwt.service.JwtService;
import com.project.plateposting.api.security.login.custom.filter.CustomJsonUsernamePasswordAuthenticationFilter;
import com.project.plateposting.api.security.login.custom.handler.LoginFailureHandler;
import com.project.plateposting.api.security.login.custom.handler.LoginSuccessHandler;
import com.project.plateposting.api.security.login.custom.service.LoginService;
import com.project.plateposting.common.config.jwt.JwtConfig;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.filter.OrderedFormContentFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.Arrays;
import java.util.Collections;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    @Value("${jwt.header.access}")
    private String accessTokenHeader;

    @Value("${jwt.header.refresh}")
    private String refreshTokenHeader;

    private final LoginService loginService;
    private final JwtService jwtService;
    private final MemberRepository memberRepository;
    private final ObjectMapper objectMapper;
    private final JwtConfig jwtConfig;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, OrderedFormContentFilter formContentFilter) throws Exception {
        http
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())
                .csrf(csrf -> csrf.disable())
                .cors(corsCustomizer -> corsCustomizer.configurationSource(new CorsConfigurationSource() {
                    @Override
                    public CorsConfiguration getCorsConfiguration(HttpServletRequest request) {
                        CorsConfiguration config = new CorsConfiguration();
                        config.setAllowedOrigins(Arrays.asList("http://localhost:8080"));
                        config.setAllowedMethods(Collections.singletonList("*"));
                        config.setAllowCredentials(true);
                        config.setAllowedHeaders(Collections.singletonList("*"));
                        config.setMaxAge(3600L); //1시간
                        config.addExposedHeader(accessTokenHeader);
                        config.addExposedHeader(refreshTokenHeader);
                        return config;
                    }
                }))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/api/v1/login", "/api/v1/member/register").permitAll()
                        .anyRequest().authenticated()
                )
                .exceptionHandling(exceptionHandling -> exceptionHandling
                        .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
                );

        http.addFilterAfter(customJsonUsernamePasswordAuthenticationFilter(), LogoutFilter.class);
        http.addFilterBefore(jwtConfig.jwtAuthenticationProcessingFilter(), CustomJsonUsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CustomJsonUsernamePasswordAuthenticationFilter customJsonUsernamePasswordAuthenticationFilter() {
        CustomJsonUsernamePasswordAuthenticationFilter customJsonUsernamePasswordAuthenticationFilter
                = new CustomJsonUsernamePasswordAuthenticationFilter(objectMapper);
        customJsonUsernamePasswordAuthenticationFilter.setAuthenticationManager(authenticationManager());
        customJsonUsernamePasswordAuthenticationFilter.setAuthenticationSuccessHandler(loginSuccessHandler());
        customJsonUsernamePasswordAuthenticationFilter.setAuthenticationFailureHandler(loginFailureHandler());
        return customJsonUsernamePasswordAuthenticationFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        DelegatingPasswordEncoder delegatingPasswordEncoder = (DelegatingPasswordEncoder)PasswordEncoderFactories.createDelegatingPasswordEncoder();
        delegatingPasswordEncoder.setDefaultPasswordEncoderForMatches(new BCryptPasswordEncoder());

        return delegatingPasswordEncoder;
    }

    @Bean
    public AuthenticationManager authenticationManager() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setPasswordEncoder(passwordEncoder());
        provider.setUserDetailsService(loginService);
        return new ProviderManager(provider);
    }

    @Bean
    public LoginSuccessHandler loginSuccessHandler() {
        return new LoginSuccessHandler(jwtService, memberRepository);
    }

    @Bean
    public LoginFailureHandler loginFailureHandler() {
        return new LoginFailureHandler();
    }
}
