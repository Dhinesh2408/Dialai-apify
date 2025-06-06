package com.example.Diallock_AI.model;

import java.util.Map;

import lombok.Data;

@Data
public class ApifyRequest {
    private String startUrl;
    private int maxRequestsPerCrawl;
    private Map<String, Object> options;

    public ApifyRequest(String startUrl, int maxRequestsPerCrawl) {
        this.startUrl = startUrl;
        this.maxRequestsPerCrawl = maxRequestsPerCrawl;
        this.options = Map.of(
            "pageFunction", "async ({ $, request, response }) => { return { title: $('title').text() }; }",
            "pseudoUrls", new String[] {startUrl + ".*"}
        );
    }
}
