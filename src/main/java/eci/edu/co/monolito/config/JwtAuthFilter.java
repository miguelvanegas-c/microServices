package eci.edu.co.monolito.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@AllArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtDecoder jwtDecoder;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        if (HttpMethod.OPTIONS.matches(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        if (!requiresTokenValidation(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = extractBearerToken(request.getHeader(HttpHeaders.AUTHORIZATION));
        if (token == null) {
            rejectUnauthorized(response, "Missing Bearer token in Authorization header");
            return;
        }

        try {
            Jwt jwt = jwtDecoder.decode(token);
            Authentication authentication = new UsernamePasswordAuthenticationToken(jwt, null, List.of());
            SecurityContextHolder.getContext().setAuthentication(authentication);
            filterChain.doFilter(request, response);
        } catch (JwtException ex) {
            SecurityContextHolder.clearContext();
            rejectUnauthorized(response, "Invalid or expired token: " + ex.getMessage());
        }
    }

    private boolean requiresTokenValidation(HttpServletRequest request) {
        String method = request.getMethod();
        String path = request.getRequestURI();

        // Permitir todas las rutas de WebSocket y STOMP sin validación de token
        if (path.equals("/ws") || path.startsWith("/ws/") ||
            path.startsWith("/topic/") || path.startsWith("/app/")) {
            return false;
        }

        // Permitir GET público para obtener posts
        if (HttpMethod.GET.matches(method) && "/api/v1/posts".equals(path)) {
            return false;
        }

        return true;
    }

    private String extractBearerToken(String authorizationHeader) {
        if (authorizationHeader == null || authorizationHeader.isBlank()) {
            return null;
        }
        if (!authorizationHeader.startsWith("Bearer ")) {
            return null;
        }
        return authorizationHeader.substring(7).trim();
    }

    private void rejectUnauthorized(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("{\"error\":\"unauthorized\",\"message\":\"" + escapeJson(message) + "\"}");
    }

    private String escapeJson(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", " ")
                .replace("\r", " ");
    }
}
