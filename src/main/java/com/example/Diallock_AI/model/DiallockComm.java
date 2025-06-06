package com.example.Diallock_AI.model;



import lombok.Data;

@Data
public class DiallockComm {
    private int id;
    private String name;
    private String email;
    private CampaignLeadStatus status;
    private String company;

    public DiallockComm() {}

    public DiallockComm(int id, String name, String email, CampaignLeadStatus status, String company) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.status = status;
        this.company = company;
    }
}
