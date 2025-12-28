package com.stoliar.filter;

import com.stoliar.admin.AdminTokenManager;
import lombok.RequiredArgsConstructor;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class AdminAuthHeaderGatewayFilter implements GatewayFilter, Ordered {

    private final AdminTokenManager adminTokenManager;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();

        if (path != null && path.equals("/api/v1/auth/register")) {
            String token = adminTokenManager.getAdminToken();
            if (token == null || token.isBlank()) {
                exchange.getResponse().setStatusCode(HttpStatus.SERVICE_UNAVAILABLE);
                return exchange.getResponse().setComplete();
            }
            ServerHttpRequest mutated = exchange.getRequest().mutate()
                    .header("Authorization", "Bearer " + token)
                    .header("X-Service-Name", "api-gateway")
                    .build();
            return chain.filter(exchange.mutate().request(mutated).build());
        }

        // остальные запросы — просто пропускаем
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}
