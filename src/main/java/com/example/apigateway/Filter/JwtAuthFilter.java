
// package com.example.apigateway.Filter;

// import io.jsonwebtoken.Claims;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.http.HttpHeaders;
// import org.springframework.http.server.reactive.ServerHttpRequest;
// import org.springframework.stereotype.Component;
// import org.springframework.web.server.ServerWebExchange;
// import org.springframework.web.server.WebFilter;
// import org.springframework.web.server.WebFilterChain;

// import com.example.apigateway.Service.JwtService;

// import reactor.core.publisher.Mono;

// @Component
// public class JwtAuthFilter implements WebFilter {

//     @Autowired
//     private JwtService jwtService;

//     @Override
//     public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
//         ServerHttpRequest request = exchange.getRequest();
//         String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

//         if (authHeader != null && authHeader.startsWith("Bearer ")) {
//             String token = authHeader.substring(7);
//             Claims claims = jwtService.extractClaims(token);
//             String role = claims.get("role", String.class);
//             String username = claims.getSubject();

//             // Forward custom headers
//             ServerHttpRequest modifiedRequest = exchange.getRequest()
//                     .mutate()
//                     .header("x-role", role)
//                     .header("x-username", username)
//                     .build();

//             return chain.filter(exchange.mutate().request(modifiedRequest).build());
//         }

//         return chain.filter(exchange); // No token, let it go (or return unauthorized)
//     }
// }
// package com.example.apigateway.Filter;

// import com.example.apigateway.Service.JwtService;
// import io.jsonwebtoken.Claims;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.http.HttpHeaders;
// import org.springframework.http.HttpStatus;
// import org.springframework.http.server.reactive.ServerHttpRequest;
// import org.springframework.stereotype.Component;
// import org.springframework.web.server.ServerWebExchange;
// import org.springframework.web.server.WebFilter;
// import org.springframework.web.server.WebFilterChain;
// import reactor.core.publisher.Mono;

// @Component
// public class JwtAuthFilter implements WebFilter {

//     @Autowired
//     private JwtService jwtService;

//     @Override
//     public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
//         ServerHttpRequest request = exchange.getRequest();
//         String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

//         if (authHeader != null && authHeader.startsWith("Bearer ")) {
//             String token = authHeader.substring(7);

//             try {
//                 Claims claims = jwtService.extractClaims(token);
//                 String role = claims.get("role", String.class);
//                 String username = claims.getSubject();

//                 ServerHttpRequest modifiedRequest = request.mutate()
//                         .header("x-role", role)
//                         .header("x-username", username)
//                         .build();

//                 return chain.filter(exchange.mutate().request(modifiedRequest).build());

//             } catch (Exception e) {
//                 // JWT غير صالح أو منتهي
//                 exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
//                 return exchange.getResponse().setComplete();
//             }
//         }

//         // لو مطلوب منع الوصول بدون توكن، أرجع 401 هنا
//         // exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
//         // return exchange.getResponse().setComplete();

//         return chain.filter(exchange); // أو اسمح بالمرور حسب الحالة
//     }
// }
package com.example.apigateway.Filter;

import com.example.apigateway.Service.JwtService;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Component
public class JwtAuthFilter implements WebFilter {

    @Autowired
    private JwtService jwtService;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);

            try {
                Claims claims = jwtService.extractClaims(token);
                String role = claims.get("role", String.class);
                String username = claims.getSubject();

                // إضافة التوكن والـ role و الـ username إلى الهيدر
                ServerHttpRequest modifiedRequest = request.mutate()
                        .header("x-role", role)
                        .header("x-username", username)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)  // إضافة التوكن في الهيدر
                        .build();

                return chain.filter(exchange.mutate().request(modifiedRequest).build());

            } catch (Exception e) {
                // في حال كان التوكن غير صالح أو انتهت صلاحيته
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }
        }

        // في حال لم يكن هناك توكن في الهيدر
        return chain.filter(exchange); // أو يمكنك إرجاع 401 Unauthorized إذا كان التوكن مطلوب
    }
}
