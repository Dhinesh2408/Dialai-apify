package com.example.Diallock_AI.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

import lombok.Data;

@Data
@Entity
@Table(name="campaignleads")
public class Campaignleads {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    @JoinColumn(name = "leadid", referencedColumnName = "id", nullable = false)
    private DiallockModel lead; 
    
    @ManyToOne
    @JoinColumn(name = "campaignid", referencedColumnName = "campaignid", nullable = false)
    private Campaigntable campaign;
    
    private String subject;

    @Column(columnDefinition = "TEXT")
    private String body;

    @Transient
    public String getCombinedContent() {
        return "Subject: " + subject + "\n\n" + body;
    }

    @Lob
    private String research;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private CampaignLeadStatus status;
}
