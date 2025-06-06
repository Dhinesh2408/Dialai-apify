package com.example.Diallock_AI.Service;

import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class ReactiveRateLimiter {

    private final int MAX_REQUESTS_PER_MIN = 3;
    private final AtomicInteger requestCount = new AtomicInteger(0);
    private Instant windowStart = Instant.now();

    public synchronized Mono<Void> checkRateLimit() {
        Instant now = Instant.now();

        if (now.isAfter(windowStart.plusSeconds(60))) {
            windowStart = now;
            requestCount.set(0);
        }

        if (requestCount.get() < MAX_REQUESTS_PER_MIN) {
            requestCount.incrementAndGet();
            return Mono.empty();
        }

        long delayMillis = 60000 - Duration.between(windowStart, now).toMillis();
        return Mono.delay(Duration.ofMillis(delayMillis)).then(Mono.empty());
    }
}
