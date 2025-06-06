package com.example.Diallock_AI.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.Diallock_AI.Repository.DiallockRepo;
import com.example.Diallock_AI.model.DiallockModel;
import com.example.Diallock_AI.model.User;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvValidationException;

@Service
public class CsvService {
	
	@Autowired
    private DiallockRepo userRepository;
	public List<DiallockModel> saveUsersFromCSV(MultipartFile file,User userId) throws CsvValidationException {
    	final Logger logger = LoggerFactory.getLogger(DiallockService.class);
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()));
             CSVReader csvReader = new CSVReaderBuilder(reader).build()) { 
        	String[] headers = csvReader.readNext(); // Read header row
        	String[] expectedHeaders = {"name", "email", "phoneno", "company", "companysize", "status", "country", "url"};
        	String[] normalizedHeaders = Arrays.stream(headers)
        	    .map(h -> h.trim().toLowerCase())
        	    .toArray(String[]::new);

        	System.out.println("Received headers: " + Arrays.toString(normalizedHeaders));

        	if (!Arrays.equals(normalizedHeaders, expectedHeaders)) {
        	    throw new CsvValidationException("Invalid CSV headers. Expected: " 
        	        + Arrays.toString(expectedHeaders) + ", Found: " 
        	        + Arrays.toString(normalizedHeaders));
        	}
            String[] nextRecord;
            List<DiallockModel> users = new ArrayList<>();

            while ((nextRecord = csvReader.readNext()) != null) {
                if (nextRecord.length < 8) continue; 
                
                DiallockModel user = new DiallockModel();
                
                user.setName(nextRecord[0] == null ? "" : nextRecord[0].trim());
                user.setEmail(nextRecord[1] == null ? "" : nextRecord[1].trim());
                user.setPhoneno(nextRecord[2] == null ? "" : nextRecord[2].trim());
                user.setCompany(nextRecord[3] == null ? "" : nextRecord[3].trim());
                user.setCompanysize(nextRecord[4] == null ? "" : nextRecord[4].trim());
                user.setStatus(nextRecord[5] == null ? "" : nextRecord[5].trim());
                user.setCountry(nextRecord[6] == null ? "" : nextRecord[6].trim());
                user.setUrl(nextRecord[7] == null ? "" : nextRecord[7].trim());
                user.setUser(userId);

                
                users.add(user);
                
            }

            
            if (!users.isEmpty()) {
                userRepository.saveAll(users);
                logger.info("Saved {} leads successfully", users.size());
            }
            return users;
         

        } catch (IOException e) {
        	logger.error("CSV processing failed: {}", e.getMessage(), e);
        	throw new CsvValidationException("File read error");
        }
    }
}
