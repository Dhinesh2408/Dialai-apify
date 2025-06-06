package com.example.Diallock_AI.model;


import java.time.LocalDate;
import java.util.List;

import jakarta.persistence.Column;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class StartCampaignRequest {
    private String campaignName;
    private List<Integer> leadIds;
    private String prompt;
    private List<String> url;
    private followUpPlan followupplan;
    private LocalDate createdDate;

    public StartCampaignRequest(String campaignName, List<Integer> leadIds, String prompt,
                                List<String> url, followUpPlan followupplan) {
        this.campaignName = campaignName;
        this.leadIds = leadIds;
        this.prompt = prompt;
        this.url = url;
        this.followupplan = followupplan;
        this.createdDate = LocalDate.now(); // initialize here
    }
}
