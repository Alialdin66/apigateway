package com.example.apigateway.Filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.security.Key;

@Component
public class JwtAuthFilter implements GlobalFilter, Ordered {

    private final Key SECRET = Keys.hmacShaKeyFor(
            "ThisIsMyVerySecrnbbeieirgwrqhfhdbeheeiieiewwbwwbbdnndkeowpqjfqjeqfpetKeyForHS512ThatIsLongEnough123456"
                    .getBytes(StandardCharsets.UTF_8)
    );

   @Override
public Mono<Void> filter(ServerWebExchange exchange, org.springframework.cloud.gateway.filter.GatewayFilterChain chain) {
    String path = exchange.getRequest().getURI().getPath();

    // ✅ تخطّي التحقق لو رايح على /auth/**
    if (path.startsWith("/auth/")) {
        return chain.filter(exchange);
    }

    HttpHeaders headers = exchange.getRequest().getHeaders();
    String authHeader = headers.getFirst(HttpHeaders.AUTHORIZATION);

    if (authHeader != null && authHeader.startsWith("Bearer ")) {
        try {
            String token = authHeader.substring(7);
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(SECRET)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            String username = claims.get("username", String.class);
            String role = claims.get("role", String.class);
            String userId = String.valueOf(claims.get("id"));

            System.out.println("Gateway extracted claims: username=" + username + ", role=" + role + ", id=" + userId);

            exchange = exchange.mutate().request(
                    exchange.getRequest().mutate()
                            .header("x-user-id", userId)
                            .header("x-user-role", role)
                            .header("x-username", username)
                            .build()
            ).build();

        } catch (Exception e) {
            System.out.println("❌ Invalid JWT Token: " + e.getMessage());
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
    } else {
        System.out.println("❌ No Authorization header or not Bearer");
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
    }

    return chain.filter(exchange);
}
    @Override
    public int getOrder() {
        return -1; // Highest priority
    }
}
