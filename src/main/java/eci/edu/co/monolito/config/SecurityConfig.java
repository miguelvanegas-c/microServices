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

@Configuration
public class SecurityConfig {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/v1/auth/**").permitAll()
                        // user registration now requires a valid Auth0 access token
                        .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**").permitAll()
                        .anyRequest().authenticated()
                )

                ;

        // Only enable oauth2 resource server when Auth0 is enabled via properties
        if (Boolean.parseBoolean(System.getProperty("app.security.auth0.enabled", System.getenv().getOrDefault("AUTH0_ENABLED", "false")))) {
            http.oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> {}));
        }

        return http.build();
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
        // if Auth0 is disabled via env/property return a dummy JwtDecoder to allow app startup
        if (!Boolean.parseBoolean(System.getProperty("app.security.auth0.enabled", System.getenv().getOrDefault("AUTH0_ENABLED", "false")))) {
            return token -> { throw new org.springframework.security.oauth2.jwt.JwtException("Auth0 is disabled"); };
        }
        NimbusJwtDecoder jwtDecoder;
        if (issuerUri != null && !issuerUri.isBlank()) {
            jwtDecoder = NimbusJwtDecoder.withIssuerLocation(issuerUri).build();
            // default issuer validator
            OAuth2TokenValidator<Jwt> withIssuer = JwtValidators.createDefaultWithIssuer(issuerUri);
            // combine with audience validator
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

