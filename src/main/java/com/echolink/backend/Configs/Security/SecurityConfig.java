package com.echolink.backend.Configs.Security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
public class SecurityConfig {
    private final CustomAuthenticationProvider authenticationProvider;
    private final JWTFilter jwtFilter;

    public SecurityConfig(CustomAuthenticationProvider authenticationProvider, JWTFilter jwtFilter) {
        this.authenticationProvider = authenticationProvider;
        this.jwtFilter = jwtFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http.csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .authorizeRequests(auth -> auth
                        .requestMatchers("/auth/login", "/auth/register-start", "/auth/verify-email", "/error",
                                "/auth/complete-profile", "/auth/token/refresh", "/ws/**",
                                "/*.js",
                                "/*.css",
                                "/*.html",
                                "/*.ico",
                                "/*.png", "/*.jpg", "/*.svg",
                                "/static/**",
                                "/assets/**")
                        .permitAll().requestMatchers(HttpMethod.OPTIONS, "/**")
                        .permitAll().anyRequest()
                        .authenticated())
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtFilter,
                        UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.addAllowedOrigin("https://echolink-frontend.vercel.app");
        config.addAllowedOrigin("https://echolink-bp73.onrender.com");
        config.addAllowedOriginPattern("https://*.vercel.app");
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
