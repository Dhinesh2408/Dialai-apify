package com.example.Diallock_AI.model;

import java.time.LocalDate;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "followup_schedule")
@Data
@NoArgsConstructor
public class FollowUp {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private LocalDate followUpDate;

    @Enumerated(EnumType.STRING)
    private CampaignLeadStatus status; 
    
    @Enumerated(EnumType.STRING)
    private FollowUpStatus followUpStatus = FollowUpStatus.PENDING;

    private int followUpCount = 0;

    private int maxFollowUps;

    @ManyToOne(optional = false)
    @JoinColumn(name = "lead_id", nullable = false)
    private Campaignleads lead;

    @ManyToOne(optional = false)
    @JoinColumn(name = "campaign_id", nullable = false)
    private Campaigntable campaign;
}
