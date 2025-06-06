package com.example.Diallock_AI.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.Diallock_AI.model.Campaignleads;
import com.example.Diallock_AI.model.DiallockComm;



@Repository
public interface campaignleadsrepo extends JpaRepository<Campaignleads, Integer> {

    @Query("SELECT new com.example.Diallock_AI.model.DiallockComm(c.lead.id, c.lead.name, c.lead.email, c.status, c.lead.company) "
            + "FROM Campaignleads c WHERE c.campaign.campaignid = :campaignId AND c.lead.user.id = :userId")
    List<DiallockComm> findLeadsByCampaignId(@Param("campaignId") Integer campaignId, @Param("userId") int userId);

    @Query("SELECT cl.research FROM Campaignleads cl WHERE cl.lead.id = :id AND cl.campaign.campaignid= :campaignId AND cl.lead.user.id = :userId")
    String findResearchByCampaignId(@Param("campaignId") Integer campaignId, @Param("id") Integer id, @Param("userId") int userId);

    @Query("SELECT cl.body FROM Campaignleads cl WHERE cl.lead.id= :id AND cl.campaign.campaignid= :campaignId AND cl.lead.user.id = :userId")
    String findemailbodybyid(@Param("campaignId") Integer campaignId, @Param("id") Integer id, @Param("userId") int userId);

    @Query("SELECT cl.subject FROM Campaignleads cl WHERE cl.lead.id= :id AND cl.campaign.campaignid= :campaignId AND cl.lead.user.id = :userId")
    String findemailsubjectbyid(@Param("campaignId") Integer campaignId, @Param("id") Integer id, @Param("userId") int userId);

    @Modifying
    @Query("UPDATE Campaignleads cl SET cl.subject = :subject WHERE cl.lead.id= :id AND cl.campaign.campaignid= :campaignid AND cl.lead.user.id = :userId")
    int updateEmailSubject(@Param("campaignid") int campaignid,
                           @Param("id") int id,
                           @Param("subject") String subject,
                           @Param("userId") int userId);

    @Modifying
    @Query("UPDATE Campaignleads cl SET cl.body = :body WHERE cl.lead.id= :id AND cl.campaign.campaignid= :campaignid AND cl.lead.user.id = :userId")
    int updateEmailBody(@Param("campaignid") int campaignid,
                        @Param("id") int id,
                        @Param("body") String body,
                        @Param("userId") int userId);

    @Query("SELECT cl FROM Campaignleads cl WHERE cl.lead.id= :id AND cl.campaign.campaignid= :campaignid AND cl.lead.user.id = :userId")
    Optional<Campaignleads> findByCampaign_CampaignidAndLead_Id(@Param("campaignid") int campaignid,
            @Param("id") int id,@Param("userId") int userId);
    
    @Modifying
    @Query("UPDATE Campaignleads cl SET cl.subject = '', cl.body = '' WHERE cl.lead.id= :id AND cl.campaign.campaignid= :campaignId AND cl.lead.user.id = :userId")
    void clearSubjectAndBody(@Param("campaignId") int campaignid, @Param("id") int id, @Param("userId") int userId);
}