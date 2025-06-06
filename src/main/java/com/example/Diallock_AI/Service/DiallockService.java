package com.example.Diallock_AI.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.Diallock_AI.Repository.ConversationRepository;
import com.example.Diallock_AI.Repository.DiallockRepo;
import com.example.Diallock_AI.Repository.FollowUpRepository;
import com.example.Diallock_AI.Repository.SettingRepo;
import com.example.Diallock_AI.Repository.campaignleadsrepo;
import com.example.Diallock_AI.Repository.campaigntablerepo;
import com.example.Diallock_AI.model.CampaignLeadStatus;
import com.example.Diallock_AI.model.CampaignStatus;
import com.example.Diallock_AI.model.Campaignleads;
import com.example.Diallock_AI.model.Campaigntable;
import com.example.Diallock_AI.model.Conversation;
import com.example.Diallock_AI.model.DiallockModel;
import com.example.Diallock_AI.model.Direction;
import com.example.Diallock_AI.model.FollowUp;
import com.example.Diallock_AI.model.Setting;
import com.example.Diallock_AI.model.StartCampaignRequest;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import reactor.core.publisher.Mono;

@Service
public class DiallockService {
	
	@Autowired
    private DiallockRepo userRepository;
	@Autowired
	private CsvService csvservice;
	@Autowired
	private campaignleadsrepo leadrepo;
	@Autowired
	private campaigntablerepo tablerepo;
	
	@Autowired
	private  ApifiService firecrawl;
	
	@Autowired
	private GenAiService aiservice;
	
	
	
	@Autowired
	private ConversationRepository conversationRepo;
	@Autowired
    private UserService user;
	@Autowired
	private FollowUpRepository followUpRepository;
	@Autowired
	private SettingRepo settingrepo;


	public void sendMail(@RequestParam int id, @RequestParam int campaignid,
            @RequestBody String subject, @RequestBody String body, @RequestParam int userId) {
			
				Campaignleads campaignLead = leadrepo
				.findByCampaign_CampaignidAndLead_Id(campaignid, id, userId)
				.orElseThrow(() -> new RuntimeException("Campaign lead not found"));
				
				Setting s = settingrepo.findByUserId(userId)
				.orElseThrow(() -> new RuntimeException("Email setting not found"));
				
				String to = campaignLead.getLead().getEmail();
				
				try {
				JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
				mailSender.setHost(s.getSmtpserver()); // e.g. smtp.gmail.com
				mailSender.setPort(587);
				mailSender.setUsername(s.getUsername());
				mailSender.setPassword(s.getPassword());
				
				Properties props = mailSender.getJavaMailProperties();
				props.put("mail.smtp.auth", "true");
				props.put("mail.smtp.starttls.enable", "true");
				props.put("mail.transport.protocol", "smtp");
				
				MimeMessage mimeMessage = mailSender.createMimeMessage();
				MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
				
				helper.setTo(to);
				helper.setSubject(subject);
				helper.setText(body); // true = HTML body
				
				String msgId = "<CID-" + campaignid + "-LID-" + id + "@diallock.ai>";
				mimeMessage.setHeader("Message-ID", msgId);
				
				mailSender.send(mimeMessage); // ✅ FIXED: use dynamic mailSender
				
				Date emailDate = mimeMessage.getSentDate();
				ZonedDateTime emailZonedDateTime = (emailDate != null)
				       ? emailDate.toInstant().atZone(ZoneId.of("Asia/Kolkata"))
				       : ZonedDateTime.now(ZoneId.of("Asia/Kolkata"));
				LocalDateTime emailLocalDateTime = emailZonedDateTime.toLocalDateTime();
			
				Conversation sentMessage = new Conversation();
				sentMessage.setCampaignLead(campaignLead);
				sentMessage.setBody(body);
				sentMessage.setFromEmail(s.getUsername());
				sentMessage.setToEmail(to);
				sentMessage.setMessageId(msgId);
				sentMessage.setCreatedAt(emailLocalDateTime);
				sentMessage.setDirection(Direction.SENT);
				sentMessage.setHasReplied(false);
				
				conversationRepo.save(sentMessage);
				
				} catch (MessagingException e) {
			        throw new RuntimeException("Failed to send email", e);
			    }
			}


	
	public String startCampaign(StartCampaignRequest request, int userId) {
		Campaigntable campaign = new Campaigntable();
		campaign.setCampaignname(request.getCampaignName());
		campaign.setStatus(CampaignStatus.Active);
		campaign.setPrompt(request.getPrompt());
		campaign.setFollowUpPlan(request.getFollowupplan());
		campaign.setStartDate(LocalDate.now());

		tablerepo.save(campaign);

		List<LocalDate> followUpDates = FollowUpScheduler.getFollowUpDates(LocalDate.now(), request.getFollowupplan());
		int maxFollowUps = switch (request.getFollowupplan()) {
			case ACCELERATED_15 -> 6;
			case BALANCED_21, EXTENDED_30 -> 7;
			default -> 1;
		};

		for (int i = 0; i < request.getLeadIds().size(); i++) {
			Integer leadId = request.getLeadIds().get(i);
			String leadUrl = request.getUrl().get(i);
			DiallockModel lead = userRepository.findById(leadId).orElse(null);
			if (lead == null) continue;

			String name = lead.getName();
			String scrapedContent = firecrawl.crawlUrl(leadUrl).block();
			String summary = aiservice.generateResponseFromContent(scrapedContent);
			String emailContent = aiservice.generateResponse(summary, request.getPrompt(), name, userId);

			String subject = "", body = "";
			if (emailContent.startsWith("Subject:")) {
				int subjectEnd = emailContent.indexOf("\n\n");
				if (subjectEnd != -1) {
					subject = emailContent.substring(8, subjectEnd).trim();
					body = emailContent.substring(subjectEnd + 2).trim();
				} else {
					subject = emailContent.substring(8).trim();
				}
			}

			Campaignleads campaignLead = new Campaignleads();
			campaignLead.setLead(lead);
			campaignLead.setCampaign(campaign);
			campaignLead.setBody(body);
			campaignLead.setSubject(subject);
			campaignLead.setResearch(summary);
			campaignLead.setStatus(CampaignLeadStatus.New);
			leadrepo.save(campaignLead);

			for (LocalDate date : followUpDates) {
				FollowUp followUp = new FollowUp();
				followUp.setCampaign(campaign);
				followUp.setFollowUpDate(date);
				followUp.setFollowUpCount(0);
				followUp.setLead(campaignLead);
				followUp.setMaxFollowUps(maxFollowUps);
				followUpRepository.save(followUp);
			}
		}

		return "Campaign '" + request.getCampaignName() + "' started successfully!";
	}

	public List<DiallockModel> displayAll(int userId) {
        return userRepository.findByUser_Id(userId); 
    }

	public DiallockModel addUser(DiallockModel usermodel) {
        return userRepository.save(usermodel);
    }
	
	public ResponseEntity<DiallockModel> updateUser(@PathVariable int id, @RequestBody Map<String, Object> updates) {
	    return userRepository.findById(id).map(existingUser -> {
	        updates.forEach((key, value) -> {
	            switch (key) {
	                case "name": existingUser.setName((String) value); break;
	                case "email": existingUser.setEmail((String) value); break;
	                case "company": existingUser.setCompany((String) value); break;
	                case "phoneno": existingUser.setPhoneno((String) value); break;
	                case "companysize": existingUser.setCompanysize((String) value); break;
	                case "status": existingUser.setStatus((String) value); break;
	                case "country": existingUser.setCountry((String) value); break;
	                case "url": existingUser.setUrl((String) value); break;
	            }
	        });
	        DiallockModel updatedUser = userRepository.save(existingUser);
	        return ResponseEntity.ok(updatedUser);
	    }).orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
	}

	public ResponseEntity<String> deleteUser(int id, int userId) { // Added userId
        return userRepository.findById(id).map(existingUser -> {
            if (existingUser.getUser().getId() != userId) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You are not authorized to delete this resource."); //check ownership
            }
            userRepository.deleteById(id);
            return ResponseEntity.ok("Deleted successfully");
        }).orElseGet(() -> ResponseEntity.notFound().build());

    }

	public List<DiallockModel> getByKeyword(String keyword, Integer userId) {
        return userRepository.findByKeyword(keyword, userId);
    }

	public ResponseEntity<DiallockModel> updateUserdetails(int id, DiallockModel usermodel) {
		return userRepository.findById(id).map(existingUser -> {
	        existingUser.setName(usermodel.getName());
	        existingUser.setEmail(usermodel.getEmail());
	        existingUser.setCompany(usermodel.getCompany());
	        existingUser.setPhoneno(usermodel.getPhoneno());
	        existingUser.setCompanysize(usermodel.getCompanysize());
	        existingUser.setStatus(usermodel.getStatus());
	        existingUser.setCountry(usermodel.getCountry());
	        existingUser.setUrl(usermodel.getUrl());
	        DiallockModel updatedUser = userRepository.save(existingUser);
	        return ResponseEntity.ok(updatedUser);
	    }).orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
	}

	public List<DiallockModel> getStatus(String keyword, Integer userId) {
        return userRepository.findStatus(keyword, userId);
    }

	


}
