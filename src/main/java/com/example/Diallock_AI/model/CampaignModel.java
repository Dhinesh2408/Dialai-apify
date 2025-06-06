package com.example.Diallock_AI.model;

import lombok.Data;

@Data
public class CampaignModel {
    private int campaignid;
    private String campaignname;

    public CampaignModel(int campaignid,String campaignname) {
        this.campaignid = campaignid;
        this.campaignname = campaignname;
    }
}
