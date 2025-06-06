package com.example.Diallock_AI.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

   @Value("${apify.api.token}")
    private String apifyApiToken;


    @Bean
    WebClient apifyWebClient(WebClient.Builder builder) {
        return builder
            .baseUrl("https://api.apify.com/v2")
            .defaultHeader("Authorization", "Bearer " + apifyApiToken)
            .build();
    }
}
