package com.example.Diallock_AI.model;

import java.time.LocalDate;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Table(name="campaigntable")
@NoArgsConstructor
public class Campaigntable {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int campaignid;
    
    private String campaignname;

    @Column(columnDefinition = "TEXT")
    private String prompt;
    
    @Enumerated(EnumType.STRING) 
    private CampaignStatus status;
    
    @OneToMany(mappedBy = "campaign", cascade = CascadeType.ALL)
    @JsonIgnore  
    private List<Campaignleads> campaignLeads;
    
    private LocalDate startDate;
    
    @Enumerated(EnumType.STRING)
    private FollowUpPlan followUpPlan;

    public Campaigntable(String campaignName, String prompt, CampaignStatus status, FollowUpPlan followupplan) {
        this.campaignname = campaignName;
        this.status = status;
        this.prompt = prompt;
        this.followUpPlan = followupplan;
        this.startDate = LocalDate.now();
    }
}
