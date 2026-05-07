package com.Project.securityConfig;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
            // Disable CSRF for frontend API calls
            .csrf(csrf -> csrf.disable())

            // Authorization rules
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                        "/",
                        "/error",
                        "/oauth2/**",
                        "/login**"
                ).permitAll()
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .anyRequest().authenticated()
            )

            // Google OAuth Login
            .oauth2Login(oauth -> oauth
                .successHandler((request, response, authentication) -> {
                    response.sendRedirect("https://ai-drive-frontend.vercel.app");
                })
            )
        
            // Logout
            .logout(logout -> logout
                .logoutSuccessUrl("https://ai-drive-frontend.vercel.app/folders")
                .permitAll()
            );

        return http.build();
    }
}