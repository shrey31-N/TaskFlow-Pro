package com.taskflow.apigateway.config;

import com.taskflow.apigateway.filter.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http) throws Exception {

        http
                // ==========================================
                // CSRF
                // ==========================================
                .csrf(csrf -> csrf.disable())

                // ==========================================
                // STATELESS SESSION
                // ==========================================
                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                // ==========================================
                // AUTHORIZATION RULES
                // ==========================================
                .authorizeHttpRequests(auth -> auth

                        // ----------------------------------
                        // PUBLIC ENDPOINTS
                        // ----------------------------------

                        .requestMatchers(
                                "/api/users/register",
                                "/api/users/login",

                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**",

                                "/actuator/health",
                                "/actuator/info"
                        ).permitAll()

                        // ----------------------------------
                        // ADMIN ONLY
                        // ----------------------------------

                        .requestMatchers(
                                HttpMethod.DELETE,
                                "/api/tasks/**"
                        ).hasRole("ADMIN")

                        .requestMatchers(
                                HttpMethod.DELETE,
                                "/api/projects/**"
                        ).hasRole("ADMIN")

                        // ----------------------------------
                        // MEMBER + ADMIN
                        // ----------------------------------

                        .requestMatchers(
                                "/api/tasks/**"
                        ).hasAnyRole("MEMBER", "ADMIN")

                        .requestMatchers(
                                "/api/projects/**"
                        ).hasAnyRole("MEMBER", "ADMIN")

                        .requestMatchers(
                                "/api/notifications/**"
                        ).hasAnyRole("MEMBER", "ADMIN")

                        // ----------------------------------
                        // EVERYTHING ELSE
                        // ----------------------------------

                        .anyRequest().authenticated()
                );

        // ==========================================
        // JWT FILTER
        // ==========================================

        http.addFilterBefore(
                jwtAuthenticationFilter,
                UsernamePasswordAuthenticationFilter.class
        );

        return http.build();
    }
}