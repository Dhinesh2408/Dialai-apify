package com.example.Diallock_AI.controller;

import java.util.*;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.example.Diallock_AI.Jwt.JwtUtil;
import com.example.Diallock_AI.Repository.*;
import com.example.Diallock_AI.Service.*;
import com.example.Diallock_AI.model.*;
import com.opencsv.exceptions.CsvValidationException;

import jakarta.transaction.Transactional;

@RestController
@CrossOrigin(origins = "http://localhost:5173")
@RequestMapping("/api")
public class DiallockController {

    @Autowired private DiallockService service;
    @Autowired private CsvService csvservice;
    @Autowired private campaigntablerepo campaign;
    @Autowired private campaignleadsrepo campaignleadsRepository;
    @Autowired private SettingService settingService;
    @Autowired private EmailReaderService emailReaderService;
    @Autowired private ConversationRepository conversationRepo;
    @Autowired private AuthRepo userRepository;
    @Autowired private BCryptPasswordEncoder passwordEncoder;
    @Autowired private FollowUpSchedulerService followUpSchedulerService;
    @Autowired private GenAiService genAIService; // <-- Injected GenAIService

    // Signup
    @PostMapping("/signup")
    public ResponseEntity<String> signup(@RequestBody User user) {
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("User already exists");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);
        return ResponseEntity.status(HttpStatus.CREATED).body("User registered successfully");
    }

    // Login
    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody User user) {
        Optional<User> existingUser = userRepository.findByEmail(user.getEmail());
        if (existingUser.isPresent() && passwordEncoder.matches(user.getPassword(), existingUser.get().getPassword())) {
            String token = JwtUtil.generateToken(String.valueOf(existingUser.get().getId()));
            return ResponseEntity.ok(Map.of("message", "Login successful", "token", token));
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid credentials"));
    }

    @GetMapping("/{campaignid}/{id}/read")
    public ResponseEntity<?> readMailForLead(@PathVariable int campaignid, @PathVariable int id) {
        int userId = getCurrentUserId();
        emailReaderService.readEmailsForLead(campaignid, id, userId);
        return ResponseEntity.ok("Checked for replies.");
    }

    @GetMapping("/{campaignId}/{leadId}/conversation")
    public List<Map<String, Object>> getConversationDetails(@PathVariable int campaignId, @PathVariable int leadId) {
        int userId = getCurrentUserId();
        Campaignleads lead = campaignleadsRepository.findByCampaign_CampaignidAndLead_Id(campaignId, leadId, userId)
                .orElseThrow(() -> new RuntimeException("Campaign lead not found"));
        return conversationRepo.findByCampaignLeadOrderByCreatedAtAsc(lead).stream().map(convo -> {
            Map<String, Object> map = new HashMap<>();
            map.put("body", convo.getBody());
            map.put("direction", convo.getDirection());
            map.put("timestamp", convo.getCreatedAt());
            return map;
        }).toList();
    }

    @PostMapping("/addsetting")
    public ResponseEntity<Setting> addOrUpdateSetting(@RequestBody Setting setting) {
        int userId = getCurrentUserId();
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        Setting saved = settingService.saveSetting(setting, userOpt.get());
        return ResponseEntity.ok(saved);
    }

    @GetMapping("/getsetting")
    public ResponseEntity<Setting> getSetting() {
        int userId = getCurrentUserId();
        return settingService.getSettingByUserId(userId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/{campaignid}/{id}/body")
    public String displaybody(@PathVariable int campaignid, @PathVariable int id) {
        return campaignleadsRepository.findemailbodybyid(campaignid, id, getCurrentUserId());
    }

    @GetMapping("/{campaignid}/{id}/subject")
    public String displaysubject(@PathVariable int campaignid, @PathVariable int id) {
        return campaignleadsRepository.findemailsubjectbyid(campaignid, id, getCurrentUserId());
    }

    @PutMapping("/{campaignid}/{id}/subject")
    public ResponseEntity<String> updateEmailSubject(@PathVariable int campaignid, @PathVariable int id, @RequestBody String subject) {
        int updated = campaignleadsRepository.updateEmailSubject(campaignid, id, subject, getCurrentUserId());
        return updated > 0 ? ResponseEntity.ok("Subject updated successfully") :
                ResponseEntity.status(HttpStatus.NOT_FOUND).body("Email not found or update failed");
    }

    @PutMapping("/{campaignid}/{id}/body")
    public ResponseEntity<String> updateEmailBody(@PathVariable int campaignid, @PathVariable int id, @RequestBody String body) {
        int updated = campaignleadsRepository.updateEmailBody(campaignid, id, body, getCurrentUserId());
        return updated > 0 ? ResponseEntity.ok("Body updated successfully") :
                ResponseEntity.status(HttpStatus.NOT_FOUND).body("Email not found or update failed");
    }

    @PostMapping("/{campaignid}/{id}/send")
    @Transactional
    public String sendMail(@PathVariable int id, @PathVariable int campaignid, @RequestBody Map<String, String> payload) {
        service.sendMail(id, campaignid, payload.get("subject"), payload.get("content"), getCurrentUserId());
        campaignleadsRepository.clearSubjectAndBody(campaignid, id, getCurrentUserId());
        return "sent successfully";
    }

    @GetMapping("/campaigns")
    public List<CampaignModel> displayCampaign() {
        return campaign.displayCampaign(getCurrentUserId());
    }

    @GetMapping("/{campaignId}/leads")
    public List<DiallockComm> getLeadsByCampaign(@PathVariable Integer campaignId) {
        return campaignleadsRepository.findLeadsByCampaignId(campaignId, getCurrentUserId());
    }

    @GetMapping("/{campaignId}/{id}/research")
    public ResponseEntity<Map<String, String>> getResearch(@PathVariable Integer campaignId, @PathVariable Integer id) {
        String research = campaignleadsRepository.findResearchByCampaignId(campaignId, id, getCurrentUserId());
        return ResponseEntity.ok(Map.of("research", research));
    }

    @GetMapping("/{campaignId}/{id}/emailcontent")
    public ResponseEntity<Map<String, String>> getEmailcontent(@PathVariable Integer campaignId, @PathVariable Integer id) {
        Campaignleads lead = campaignleadsRepository.findByCampaign_CampaignidAndLead_Id(campaignId, id, getCurrentUserId())
                .orElseThrow(() -> new RuntimeException("Campaign lead not found"));
        return ResponseEntity.ok(Map.of("emailcontent", lead.getCombinedContent()));
    }

    @PostMapping("/campaign/start")
    public ResponseEntity<?> startCampaign(@RequestParam("campaignName") String campaignName,
                                           @RequestParam("prompt") String prompt,
                                           @RequestParam("file") MultipartFile file,
                                           @RequestParam String Followupplan) {
        int userId = getCurrentUserId();
        Optional<User> userOpt = userRepository.findById(userId);
        try {
            if (file.isEmpty()) return ResponseEntity.badRequest().body("CSV file is required.");

            List<DiallockModel> savedLeads = csvservice.saveUsersFromCSV(file, userOpt.get());
            if (savedLeads == null || savedLeads.isEmpty())
                return ResponseEntity.badRequest().body("No valid leads found in CSV.");

            FollowUpPlan plan = FollowUpPlan.valueOf(Followupplan.toUpperCase());

            List<Integer> leadIds = savedLeads.stream().map(DiallockModel::getId).collect(Collectors.toList());
            List<String> urls = savedLeads.stream().map(DiallockModel::getUrl).collect(Collectors.toList());

            service.startCampaign(new StartCampaignRequest(campaignName, leadIds, prompt, urls, plan), userId);
            return ResponseEntity.ok("Campaign started with " + savedLeads.size() + " leads.");
        } catch (CsvValidationException e) {
            return ResponseEntity.badRequest().body("CSV Validation Error: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Unexpected error: " + e.getMessage());
        }
    }

    @PostMapping("/upload")
    public ResponseEntity<String> uploadCSV(@RequestParam("file") MultipartFile file) {
        int userId = getCurrentUserId();
        Optional<User> userOpt = userRepository.findById(userId);
        if (file.isEmpty()) return ResponseEntity.badRequest().body("File is empty!");
        if (!Objects.requireNonNull(file.getContentType()).equals("text/csv"))
            return ResponseEntity.badRequest().body("Only CSV files allowed!");
        try {
            csvservice.saveUsersFromCSV(file, userOpt.get());
            return ResponseEntity.ok("CSV uploaded and data saved successfully.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/User")
    public ResponseEntity<List<DiallockModel>> displayAll() {
        return new ResponseEntity<>(service.displayAll(getCurrentUserId()), HttpStatus.OK);
    }

    @GetMapping("/User/search")
    public ResponseEntity<List<DiallockModel>> getById(@RequestParam String keyword) {
        return new ResponseEntity<>(service.getByKeyword(keyword, getCurrentUserId()), HttpStatus.OK);
    }

    @PostMapping("/add")
    public ResponseEntity<DiallockModel> addUser(@RequestBody DiallockModel usermodel) {
        int userId = getCurrentUserId();
        usermodel.setUser(new User());
        usermodel.getUser().setId(userId);
        return new ResponseEntity<>(service.addUser(usermodel), HttpStatus.FOUND);
    }

    @PutMapping("/User/{id}")
    public ResponseEntity<DiallockModel> updateUserdetails(@PathVariable int id, @RequestBody DiallockModel usermodel) {
        return service.updateUserdetails(id, usermodel);
    }

    @PatchMapping("/users/{id}")
    public ResponseEntity<ResponseEntity<DiallockModel>> updateUser(@PathVariable int id, @RequestBody Map<String, Object> updates) {
        ResponseEntity<DiallockModel> updatedUser = service.updateUser(id, updates);
        return updatedUser != null ? ResponseEntity.ok(updatedUser) : ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    @DeleteMapping("/User/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable int id) {
        return service.deleteUser(id, getCurrentUserId());
    }

    @GetMapping("/User/keyword")
    public List<DiallockModel> getStatus(@RequestParam("keyword") String keyword) {
        return service.getStatus(keyword, getCurrentUserId());
    }

    // ✅ From GenAIService
    @PostMapping("/leads/upload")
    public ResponseEntity<?> uploadLead() {
        return ResponseEntity.ok("Upload successful"); // Add logic here
    }

    @PostMapping("/leads/generate-email")
    public String generateEmail(@RequestParam String company, @RequestParam String website, @RequestParam String siteText) {
    	return genAIService.generateEmail(company, website, siteText);
    }

    // 🔐 Utility
    private int getCurrentUserId() {
        String userIdStr = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return Integer.parseInt(userIdStr);
    }
}
