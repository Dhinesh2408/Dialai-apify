package com.example.Diallock_AI.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@Service
public class ApifiService {

    private final WebClient webClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${apify.token}")
    private String apifyToken;

    public ApifiService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("https://api.apify.com").build();
    }

    public Mono<String> crawlUrl(String url) {
        Map<String, Object> requestBody = Map.of(
                "startUrls", List.of(Map.of("url", url)),
                "maxPagesPerCrawl", 3,
                "maxDepth", 1
        );

        String crawlUrl = "/v2/acts/apify~website-content-crawler/run-sync-get-dataset-items?token=" + apifyToken;

        return webClient.post()
                .uri(crawlUrl)
                .header("Content-Type", "application/json")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .flatMap(this::extractTextWithRetry);
    }

    private Mono<String> extractTextWithRetry(String responseBody) {
        return tryExtractText(responseBody, 5);
    }

    private Mono<String> tryExtractText(String body, int attemptsLeft) {
        try {
            JsonNode arrayNode = objectMapper.readTree(body);
            if (arrayNode.isArray() && arrayNode.size() > 0 && arrayNode.get(0).has("text")) {
                String text = arrayNode.get(0).get("text").asText();
                return Mono.justOrEmpty(text);
            } else if (attemptsLeft > 0) {
                // Retry with exponential backoff
                return Mono.delay(Duration.ofSeconds((long) Math.pow(2, 5 - attemptsLeft)))
                        .flatMap(ignore -> tryExtractText(body, attemptsLeft - 1));
            } else {
                return Mono.error(new RuntimeException("Text not found after retries."));
            }
        } catch (Exception e) {
            return Mono.error(new RuntimeException("Error parsing Apify response", e));
        }
    }
}
