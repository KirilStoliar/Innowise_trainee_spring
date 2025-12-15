package com.stoliar.admin;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@Component
public class AdminTokenManager {

    private final WebClient webClient;
    private final String authLoginUrl;
    private final String adminEmail;
    private final String adminPassword;
    private final AtomicReference<String> adminToken = new AtomicReference<>();

    public AdminTokenManager(WebClient webClient,
                             @Value("${gateway.auth.url:http://auth-service:8081}") String authServiceUrl,
                             @Value("${gateway-admin.email:admin@example.com}") String adminEmail,
                             @Value("${gateway-admin.password:admin123}") String adminPassword) {
        this.webClient = webClient;
        this.authLoginUrl = authServiceUrl + "/api/v1/auth/login";
        this.adminEmail = adminEmail;
        this.adminPassword = adminPassword;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void obtainAdminTokenOnStartup() {
        log.info("Attempting to obtain admin token from {}", authLoginUrl);
        try {
            Map<String, Object> resp = webClient.post()
                    .uri(authLoginUrl)
                    .bodyValue(Map.of("email", adminEmail, "password", adminPassword))
                    .retrieve()
                    .bodyToMono(Map.class)
                    .retryWhen(Retry.backoff(5, Duration.ofSeconds(2)).maxBackoff(Duration.ofSeconds(10)))
                    .block();

            if (resp != null) {
                // try to extract token under common structures:
                // 1) { "data": { "accessToken": "..." } }
                // 2) { "accessToken": "..." }
                String token = null;
                if (resp.containsKey("data")) {
                    Object data = resp.get("data");
                    if (data instanceof Map) {
                        token = (String) ((Map<?, ?>) data).get("accessToken");
                    }
                }
                if (token == null && resp.containsKey("accessToken")) {
                    token = (String) resp.get("accessToken");
                }
                if (token != null) {
                    adminToken.set(token);
                    log.info("Admin token obtained (length={})", token.length());
                } else {
                    log.warn("Admin token not found in response: {}", resp);
                }
            }
        } catch (Exception e) {
            log.warn("Failed to obtain admin token on startup: {}", e.getMessage());
        }
    }

    public String getAdminToken() {
        return adminToken.get();
    }

    public void setAdminToken(String token) {
        adminToken.set(token);
    }
}
