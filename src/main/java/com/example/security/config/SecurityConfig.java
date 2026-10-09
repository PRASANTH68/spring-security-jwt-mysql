package com.example.security.config;

import com.example.security.security.JwtAuthenticationFilter;

import lombok.RequiredArgsConstructor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;

import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;

import org.springframework.security.core.userdetails.UserDetailsService;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    private final UserDetailsService userDetailsService;


    // ==========================================
    // Password Encoder
    // ==========================================

    @Bean
    public PasswordEncoder passwordEncoder() {

        return new BCryptPasswordEncoder();
    }


    // ==========================================
    // Authentication Provider
    // ==========================================

    @Bean
    public DaoAuthenticationProvider authenticationProvider(
            PasswordEncoder passwordEncoder) {

        /*
         * Create provider using default constructor.
         */
        DaoAuthenticationProvider provider =
                new DaoAuthenticationProvider();

        /*
         * Configure UserDetailsService separately.
         */
        provider.setUserDetailsService(userDetailsService);

        /*
         * Configure BCrypt password encoder.
         */
        provider.setPasswordEncoder(passwordEncoder);

        return provider;
    }


    // ==========================================
    // Authentication Manager
    // ==========================================

    @Bean
    public AuthenticationManager authenticationManager(
            DaoAuthenticationProvider authenticationProvider) {

        return new ProviderManager(authenticationProvider);
    }


    // ==========================================
    // Security Filter Chain
    // ==========================================

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            DaoAuthenticationProvider authenticationProvider)
            throws Exception {

        http

                // REST API does not use CSRF tokens
                .csrf(csrf -> csrf.disable())

                // JWT authentication is stateless
                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                // Authentication provider
                .authenticationProvider(
                        authenticationProvider
                )

                // Authorization rules
                .authorizeHttpRequests(auth -> auth

                        // Authentication APIs
                        .requestMatchers(
                                "/api/auth/**"
                        )
                        .permitAll()

                        // Admin APIs
                        .requestMatchers(
                                "/api/admin/**"
                        )
                        .hasRole("ADMIN")

                        // User + Admin APIs
                        .requestMatchers(
                                "/api/user/**"
                        )
                        .hasAnyRole(
                                "USER",
                                "ADMIN"
                        )

                        // User profile
                        .requestMatchers(
                                "/api/users/profile"
                        )
                        .authenticated()

                        // Get all users - ADMIN only
                        .requestMatchers(
                                "/api/users"
                        )
                        .hasRole("ADMIN")

                        // Any other request
                        .anyRequest()
                        .authenticated()
                )

                // Exception handling
                .exceptionHandling(exception ->
                        exception

                                .authenticationEntryPoint(
                                        authenticationEntryPoint()
                                )

                                .accessDeniedHandler(
                                        accessDeniedHandler()
                                )
                )

                // JWT filter
                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }


    // ==========================================
    // 401 Unauthorized
    // ==========================================

    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint() {

        return (request, response, exception) -> {

            response.setStatus(401);

            response.setContentType(
                    "application/json"
            );

            response.getWriter().write(
                    """
                    {
                      "status": 401,
                      "error": "Unauthorized",
                      "message": "Authentication is required"
                    }
                    """
            );
        };
    }


    // ==========================================
    // 403 Forbidden
    // ==========================================

    @Bean
    public AccessDeniedHandler accessDeniedHandler() {

        return (request, response, exception) -> {

            response.setStatus(403);

            response.setContentType(
                    "application/json"
            );

            response.getWriter().write(
                    """
                    {
                      "status": 403,
                      "error": "Forbidden",
                      "message": "You do not have permission to access this resource"
                    }
                    """
            );
        };
    }
}