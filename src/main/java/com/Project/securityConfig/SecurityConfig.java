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
            // 🔹 Disable CSRF for dev (important for POST from React)
            .csrf(csrf -> csrf.disable())

            // 🔹 Authorization rules
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                        "/", 
                        "/error",
                        "/oauth2/**",
                        "/login**"
                ).permitAll()
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll() // CORS preflight
                .anyRequest().authenticated()
            )

            // 🔹 OAuth Login + Redirect to React
            .oauth2Login(oauth -> oauth
            	    .successHandler((request, response, authentication) -> {
            	        response.sendRedirect("https://your-frontend.vercel.app/folders");
            	    })
            	)

            	.logout(logout -> logout
            	    .logoutSuccessUrl("https://your-frontend.vercel.app")
            	    .permitAll()
            	);

        return http.build();
    }

    // 🔹 CORS Config (React ↔ Backend)
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins("http://localhost:5173")
                        .allowedMethods("*")
                        .allowCredentials(true);
            }
        };
    }
}