package com.stoliar.filter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class InternalTokenGatewayFilter implements GatewayFilter {

    @Value("${API_GATEWAY_INTERNAL_TOKEN}")
    private String internalToken;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        ServerHttpRequest request = exchange.getRequest().mutate()
            .header("Authorization", "Bearer " + internalToken)
            .header("X-Service-Name", "api-gateway")
            .build();

        return chain.filter(exchange.mutate().request(request).build());
    }
}
