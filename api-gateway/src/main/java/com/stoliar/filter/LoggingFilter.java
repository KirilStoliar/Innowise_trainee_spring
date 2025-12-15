package com.stoliar.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class LoggingFilter implements GatewayFilter {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        ServerHttpRequest request = exchange.getRequest();

        String method = request.getMethod() != null ? request.getMethod().name() : "UNKNOWN";
        String path = request.getURI().getRawPath();

        log.info("➡️ {} {}", method, path);

        return chain.filter(exchange)
                .doOnSuccess(aVoid ->
                        log.info("⬅️ Response sent for {} {}", method, path)
                );
    }
}
