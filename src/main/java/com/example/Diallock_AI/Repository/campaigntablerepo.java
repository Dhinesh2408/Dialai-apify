package com.example.Diallock_AI.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.Diallock_AI.model.CampaignModel;
import com.example.Diallock_AI.model.Campaigntable;


@Repository
public interface campaigntablerepo extends JpaRepository<Campaigntable, Integer> {

	@Query("SELECT DISTINCT new com.example.Diallock_AI.model.CampaignModel(c.campaignid, c.campaignname) " +
		       "FROM Campaigntable c " +
		       "JOIN c.campaignLeads cl " +
		       "JOIN cl.lead l " +
		       "JOIN l.user u " +
		       "WHERE u.id = :userId")
		List<CampaignModel> displayCampaign(@Param("userId") int userId);



}
