package com.assignment3.project.security;

import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationEntryPoint;
import org.springframework.security.oauth2.server.resource.web.access.BearerTokenAccessDeniedHandler;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.security.web.SecurityFilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@AllArgsConstructor
public class SecurityConfig {

    private final JwtConfig jwtConfig;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource())) // Добавь это!
                .authorizeHttpRequests(authorizeRequests -> authorizeRequests
                        .requestMatchers("/health", "/").permitAll()
                        .requestMatchers("/api/v1/auth/**", "/images/**", "/avatars/**", "/docs/**", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/projects", "/api/v1/projects/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/events", "/api/v1/events/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/users").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/categories", "/api/v1/categories/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/donations/count", "/api/v1/donations/month-sum", "/api/v1/donations/last-year-sum").permitAll()
                        .anyRequest().authenticated())
                .sessionManagement(sessionManagement -> sessionManagement
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt
                                .jwtAuthenticationConverter(jwtAuthenticationConverter())
                        )
                        .bearerTokenResolver(new BearerTokenResolver() {
                            @Override
                            public String resolve(HttpServletRequest request) {
                                String path = request.getRequestURI();
                                String method = request.getMethod();

                                if ("GET".equals(method) && (
                                    path.equals("/api/v1/projects") || path.startsWith("/api/v1/projects/") ||
                                    path.equals("/api/v1/events") || path.startsWith("/api/v1/events/") ||
                                    path.equals("/api/v1/users") || path.startsWith("/api/v1/users/") ||
                                    path.equals("/api/v1/categories") || path.startsWith("/api/v1/categories/") ||
                                    path.equals("/api/v1/donations/count") ||
                                    path.equals("/api/v1/donations/month-sum") ||
                                    path.equals("/api/v1/donations/last-year-sum")
                                )) {
                                    String authHeader = request.getHeader("Authorization");
                                    if (authHeader != null && authHeader.startsWith("Bearer ") && authHeader.length() > 7) {
                                        String token = authHeader.substring(7);
                                        if (token != null && !token.isEmpty() && !token.equals("null")) {
                                            return token;
                                        }
                                    }
                                    return null;
                                }

                                String authHeader = request.getHeader("Authorization");
                                if (authHeader != null && authHeader.startsWith("Bearer ") && authHeader.length() > 7) {
                                    return authHeader.substring(7);
                                }
                                return null;
                            }
                        })
                )
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint((request, response, authException) -> {
                            String path = request.getRequestURI();
                            String method = request.getMethod();

                            if ("GET".equals(method) && (
                                path.equals("/api/v1/projects") || path.startsWith("/api/v1/projects/") ||
                                path.equals("/api/v1/events") || path.startsWith("/api/v1/events/") ||
                                path.equals("/api/v1/users") || path.startsWith("/api/v1/users/") ||
                                path.equals("/api/v1/categories") || path.startsWith("/api/v1/categories/") ||
                                path.equals("/api/v1/donations/count") ||
                                path.equals("/api/v1/donations/month-sum") ||
                                path.equals("/api/v1/donations/last-year-sum")
                            )) {
                                response.setStatus(HttpServletResponse.SC_OK);
                                return;
                            }

                            new BearerTokenAuthenticationEntryPoint().commence(request, response, authException);
                        })
                        .accessDeniedHandler(new BearerTokenAccessDeniedHandler())
                )
                .build();
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        return NimbusJwtDecoder.withSecretKey(jwtConfig.getSecretKey()).build();
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    AuthenticationProvider authenticationProvider(com.assignment3.project.services.UserService userService) {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authManager(AuthenticationProvider authenticationProvider) {
        return new ProviderManager(authenticationProvider);
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of(
                "http://localhost:5173",
                "http://localhost:3000",
                "https://azharfund.netlify.app"
        ));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }


    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setPrincipalClaimName("email");
        return converter;
    }

}
