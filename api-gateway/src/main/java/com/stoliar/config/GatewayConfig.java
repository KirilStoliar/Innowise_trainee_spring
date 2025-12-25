package com.stoliar.config;

import com.stoliar.filter.AdminAuthHeaderGatewayFilter;
import com.stoliar.filter.InternalTokenGatewayFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

    @Value("${gateway.auth.url:http://auth-service:8081}")
    private String authServiceUrl;

    @Value("${gateway.user.url:http://user-service:8080}")
    private String userServiceUrl;

    @Value("${gateway.order.url:http://order-service:8082}")
    private String orderServiceUrl;

    @Value("${gateway.payment.url:http://payment-service:8084}")
    private String paymentServiceUrl;

    // adminAuthFilter используется только для /api/v1/auth/register чтобы добавить admin token
    @Bean
    public RouteLocator routes(RouteLocatorBuilder builder,
                               InternalTokenGatewayFilter internalTokenFilter,
                               AdminAuthHeaderGatewayFilter adminAuthFilter) {
        return builder.routes()
                // ================= AUTH — PUBLIC + JWT =================
                .route("auth-public", r -> r.path("/api/v1/auth/login", "/api/v1/auth/register",
                                "/api/v1/auth/validate", "/api/v1/auth/refresh")
                        .filters(f -> f.filter(adminAuthFilter)) // только register
                        .uri(authServiceUrl))

                // ================= AUTH — INTERNAL =================
                .route("auth-internal", r -> r.path("/api/v1/auth/internal/**")
                        .filters(f -> f.filter(internalTokenFilter))
                        .uri(authServiceUrl))

                // ================= USER =================
                .route("user-service", r -> r.path("/api/v1/users/**")
                        .uri(userServiceUrl))

                // ================= ORDER =================
                .route("order-service", r -> r.path("/api/v1/orders/**",
                                "/api/v1/items/**", "/api/v1/order-items/**")
                        .uri(orderServiceUrl))

                // ================= PAYMENT =================
                .route("payment-service", r -> r.path("/api/v1/payments/**")
                        .uri(paymentServiceUrl))

                // root redirect to docs on auth-service (or just to /)
                .route("root-redirect", r -> r.path("/")
                        .filters(f -> f.redirect(302, "/"))
                        .uri("no://op"))

                .build();
    }
}
