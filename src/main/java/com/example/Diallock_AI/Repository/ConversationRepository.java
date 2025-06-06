package com.example.Diallock_AI.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.Diallock_AI.model.Campaignleads;
import com.example.Diallock_AI.model.Conversation;
import com.example.Diallock_AI.model.Direction;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Long> {
    List<Conversation> findByCampaignLead(Campaignleads campaignLead);

    List<Conversation> findByCampaignLeadOrderByCreatedAtAsc(Campaignleads lead);

    boolean existsByMessageId(String messageId);

    @Query("SELECT c FROM Conversation c WHERE c.direction = 'SENT' AND c.hasReplied = false AND c.campaignLead.lead.user.id = :userId")
    List<Conversation> findPendingFollowUps(@Param("userId") int userId);

    
    boolean existsByCampaignLeadIdAndDirection(int campaignLeadId, Direction direction);
    

}