package com.habittracker.api.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@Service
public class TurnstileService {

    private static final Logger log = LoggerFactory.getLogger(TurnstileService.class);

    private final WebClient webClient;
    private final String secretKey;
    private final String verifyUrl;

    public TurnstileService(WebClient.Builder webClientBuilder,
                           @Value("${cloudflare.turnstile.secretKey}") String secretKey,
                           @Value("${cloudflare.turnstile.verifyUrl}") String verifyUrl) {
        this.webClient = webClientBuilder.baseUrl(verifyUrl).build();
        this.secretKey = secretKey;
        this.verifyUrl = verifyUrl;
    }

    public Mono<Boolean> verifyTurnstile(String token) {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("secret", secretKey);
        formData.add("response", token);
        // Optionally add 'remoteip' parameter if needed, requires getting user's IP

        log.debug("Verifying Turnstile token...");
        return webClient.post()
                .uri(verifyUrl) // Use full URL if baseUrl wasn't set or if posting elsewhere
                .body(BodyInserters.fromFormData(formData))
                .retrieve()
                .bodyToMono(Map.class) // Expect a Map response from Cloudflare
                .map(response -> {
                    boolean success = Boolean.TRUE.equals(response.get("success"));
                    if (success) {
                        log.info("Turnstile verification successful.");
                    } else {
                        log.warn("Turnstile verification failed. Response: {}", response);
                        // Log error codes if present
                        Object errorCodes = response.get("error-codes");
                        if (errorCodes instanceof List) {
                            log.warn("Turnstile error codes: {}", errorCodes);
                        }
                    }
                    return success;
                })
                .onErrorResume(e -> {
                    log.error("Error calling Turnstile verification API: {}", e.getMessage());
                    return Mono.just(false); // Treat API errors as verification failure
                });
    }
}