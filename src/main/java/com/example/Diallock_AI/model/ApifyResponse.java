package com.example.Diallock_AI.model;

import java.util.List;

import lombok.Data;

@Data
public class ApifyResponse {
    private String status;
    private int totalRequests;
    private int processedRequests;
    private List<CrawlResult> results;

    @Data
    public static class CrawlResult {
        private String title;
        private String url;
        private String content;
    }
}
