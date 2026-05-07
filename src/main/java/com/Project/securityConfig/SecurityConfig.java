package com.Project.securityConfig;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http

            // ENABLE CORS
            .cors(cors -> {})

            // Disable CSRF
            .csrf(csrf -> csrf.disable())

            // Authorization
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

            // OAuth Login
            .oauth2Login(oauth -> oauth
                .successHandler((request, response, authentication) -> {

                    response.sendRedirect(
                        "https://ai-drive-frontend.vercel.app/folders"
                    );
                })
            )

            // Logout
            .logout(logout -> logout
                .logoutSuccessUrl(
                    "https://ai-drive-frontend.vercel.app"
                )
                .permitAll()
            );

        return http.build();
    }

    // CORS CONFIG
    @Bean
    public WebMvcConfigurer corsConfigurer() {

        return new WebMvcConfigurer() {

            @Override
            public void addCorsMappings(CorsRegistry registry) {

                registry.addMapping("/**")
                        .allowedOrigins(
                            "https://ai-drive-frontend.vercel.app"
                        )
                        .allowedMethods("*")
                        .allowedHeaders("*")
                        .allowCredentials(true);
            }
        };
    }
}