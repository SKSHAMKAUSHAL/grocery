package com.example.product.config;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

/**
 * Security configuration class for enabling Firebase authentication in a Spring Boot application.
 * It configures HTTP security rules and integrates a custom filter to handle Firebase token validation.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.GET, "/api/products", "/api/products/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/products").hasAuthority("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/products/**").hasAuthority("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/products/**").hasAuthority("ADMIN")
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                .anyRequest().authenticated()
            )
            .addFilterBefore(firebaseAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public FirebaseAuthenticationFilter firebaseAuthenticationFilter() {
        return new FirebaseAuthenticationFilter();
    }

    /**
     * Custom filter to validate Firebase JWT tokens in incoming HTTP requests.
     * It sets authentication in the security context if the token is valid and checks for admin role.
     */
    public static class FirebaseAuthenticationFilter extends OncePerRequestFilter {
        @Override
        protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
                throws ServletException, IOException {
            String header = request.getHeader("Authorization");
            // Skip token validation for permitAll endpoints (GET /api/products, /api/products/**)
            if (!requiresAuthentication(request)) {
                chain.doFilter(request, response);
                return;
            }
            if (header == null || !header.startsWith("Bearer ")) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.getWriter().write("{\"error\": \"Authentication token required\"}");
                return;
            }
            String token = header.substring(7);
            try {
                FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(token);
                String role = (String) decodedToken.getClaims().getOrDefault("role", "USER");
                SimpleGrantedAuthority authority = role.equals("ADMIN")
                    ? new SimpleGrantedAuthority("ADMIN")
                    : new SimpleGrantedAuthority("USER");
                SecurityContextHolder.getContext().setAuthentication(
                    new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                        decodedToken.getUid(), null, Collections.singletonList(authority)
                    )
                );
                chain.doFilter(request, response);
            } catch (FirebaseAuthException e) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.getWriter().write("{\"error\": \"Invalid token\"}");
                return;
            }
        }

        private boolean requiresAuthentication(HttpServletRequest request) {
            String path = request.getRequestURI();
            String method = request.getMethod();
            // Explicitly allow GET requests to /api/products and /api/products/** without authentication
            if (path.startsWith("/api/products") && "GET".equals(method)) {
                return false;
            }
            // Require authentication for POST, PUT, DELETE on /api/products/**
            return (path.startsWith("/api/products") &&
                    ("POST".equals(method) || "PUT".equals(method) || "DELETE".equals(method)));
        }
    }
}