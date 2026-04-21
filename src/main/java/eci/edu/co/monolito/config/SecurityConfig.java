package eci.edu.co.monolito.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.JwtValidators;

// ✅ Solo estas 3 importaciones nuevas
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.List;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource())) // ✅ línea nueva
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/v1/auth/**").permitAll()
                        .requestMatchers("/api/v1/posts/**").permitAll()
                        .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**").permitAll()
                        .anyRequest().authenticated()
                );

        if (Boolean.parseBoolean(System.getProperty("app.security.auth0.enabled", System.getenv().getOrDefault("AUTH0_ENABLED", "false")))) {
            http.oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> {}));
        }

        return http.build();
    }

    // ✅ Bean nuevo, lo demás intacto
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(List.of("*"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public JwtDecoder jwtDecoder(
            @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri:}") String issuerUri,
            @Value("${spring.security.oauth2.resourceserver.jwt.jwk-set-uri:}") String jwkSetUri,
            @Value("${app.security.auth0.audience:}") String audience
    ) {
        if (!Boolean.parseBoolean(System.getProperty("app.security.auth0.enabled", System.getenv().getOrDefault("AUTH0_ENABLED", "false")))) {
            return token -> { throw new org.springframework.security.oauth2.jwt.JwtException("Auth0 is disabled"); };
        }
        NimbusJwtDecoder jwtDecoder;
        if (issuerUri != null && !issuerUri.isBlank()) {
            jwtDecoder = NimbusJwtDecoder.withIssuerLocation(issuerUri).build();
            OAuth2TokenValidator<Jwt> withIssuer = JwtValidators.createDefaultWithIssuer(issuerUri);
            OAuth2TokenValidator<Jwt> audienceValidator = new DelegatingOAuth2TokenValidator<>(withIssuer, new eci.edu.co.monolito.config.AudienceValidator(audience));
            jwtDecoder.setJwtValidator(audienceValidator);
            return jwtDecoder;
        }
        if (jwkSetUri != null && !jwkSetUri.isBlank()) {
            jwtDecoder = NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build();
            OAuth2TokenValidator<Jwt> withIssuer = JwtValidators.createDefault();
            OAuth2TokenValidator<Jwt> audienceValidator = new DelegatingOAuth2TokenValidator<>(withIssuer, new eci.edu.co.monolito.config.AudienceValidator(audience));
            jwtDecoder.setJwtValidator(audienceValidator);
            return jwtDecoder;
        }
        throw new IllegalStateException("No issuer-uri or jwk-set-uri configured for JWT decoding");
    }
}

