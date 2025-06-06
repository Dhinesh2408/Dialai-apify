package com.example.Diallock_AI.Service;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.example.Diallock_AI.Repository.ConversationRepository;
import com.example.Diallock_AI.Repository.FollowUpRepository;
import com.example.Diallock_AI.Repository.SettingRepo;
import com.example.Diallock_AI.Repository.campaignleadsrepo;
import com.example.Diallock_AI.model.Conversation;
import com.example.Diallock_AI.model.Direction;
import com.example.Diallock_AI.model.FollowUp;
import com.example.Diallock_AI.model.Setting;
import com.example.Diallock_AI.model.User;

import jakarta.mail.internet.MimeMessage;

@Service
public class FollowUpSchedulerService {

    @Autowired
    private FollowUpRepository followUpRepo;

    @Autowired
    private ConversationRepository conversationRepo;
    	
	@Autowired
	private campaignleadsrepo leadrepo;
	@Autowired
	private SettingRepo settingrepo;
	

    

    @Scheduled(cron = "0 0 10 * * ?")
    public void processFollowUps() {
        LocalDate today = LocalDate.now();
        List<FollowUp> todaysFollowUps = followUpRepo.findByFollowUpDate(today);
        
        

        for (FollowUp followUp : todaysFollowUps) {
            int leadId = followUp.getLead().getId();
            User user = followUp.getLead().getLead().getUser(); // Navigate to user from follow-up
            int userId = user.getId();

            Optional<Setting> settingOpt = settingrepo.findByUserId(userId);
            if (settingOpt.isEmpty()) {
                System.err.println("No SMTP setting found for user ID: " + userId);
                continue;
            }
            Setting s = settingOpt.get();

            // 1. Check if user has replied to any of the conversations
            boolean hasReplied = conversationRepo.existsByCampaignLeadIdAndDirection(leadId, Direction.RECEIVED);
            if (hasReplied) {
                continue; // Skip follow-up if lead has replied
            }

            // 2. Compose follow-up content
            String subject = "Following up on our last conversation";
            String body = "Hi, just checking in again. Let me know if you're interested or have any questions.";
            String to = followUp.getLead().getLead().getEmail();
			

            try {
            	JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
            	mailSender.setHost(s.getSmtpserver());
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
				helper.setText(body);
				
				// Custom Message-ID for threading
				String msgId = "<FID-" + followUp.getId() + "-CID-" + followUp.getLead().getId() +"-LID"+ followUp.getLead().getLead().getId() +"@diallock.ai>";
				mimeMessage.setHeader("Message-ID", msgId);
				
				// Send email
				mailSender.send(mimeMessage);
				
				// Sent date fallback logic
				Date emailDate = mimeMessage.getSentDate();
				ZonedDateTime emailZonedDateTime = (emailDate != null)
				   ? emailDate.toInstant().atZone(ZoneId.of("Asia/Kolkata"))
				   : ZonedDateTime.now(ZoneId.of("Asia/Kolkata"));
				
				LocalDateTime emailLocalDateTime = emailZonedDateTime.toLocalDateTime();
				
				// Save conversation with follow-up tracking
				Conversation sentMessage = new Conversation();
				sentMessage.setCampaignLead(followUp.getLead());
				sentMessage.setBody(body);
				sentMessage.setFromEmail(s.getUsername()); // Can be config-based
				sentMessage.setToEmail(to);
				sentMessage.setMessageId(msgId);
				sentMessage.setCreatedAt(emailLocalDateTime);
				sentMessage.setDirection(Direction.SENT);
				sentMessage.setHasReplied(false);
				
				conversationRepo.save(sentMessage);
            } catch (Exception e) {
                System.err.println("Failed to send follow-up to lead ID: " + leadId);
                e.printStackTrace();
            }
        }
    }
}
