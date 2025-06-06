package com.example.Diallock_AI.Service;

import java.io.IOException;

import org.apache.http.HttpException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.example.Diallock_AI.Repository.SettingRepo;
import com.example.Diallock_AI.model.Setting;
import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;

@Service
public class GenAiService {
	
	@Autowired
	private SettingRepo setting;
	
	@Value("${google.genai.api-key}")
	private String API_KEY;
	
	public String generateResponseFromContent(String websiteContent) {
	    Client client = new Client.Builder().apiKey(API_KEY).build();
	    
	    try {
	    	StringBuilder prompt = new StringBuilder();
	    	prompt.append("Summarize the documents provided. The summary should be detailed with the following things to be considered:\n")
	    	      .append("- Identify and summarize the company goals, mission, or vision.\n")
	    	      .append("- Summarize in depth about their products and services offered.\n")
	    	      .append("- Summarize the news and updates of the company such as funding, expansion, etc.\n")
	    	      .append("- Find contact details such as email, phone number, LinkedIn page, Twitter, Instagram on the website.\n\n")
	    	      .append("Give the results in a detailed format.\n\n")
	    	      .append(websiteContent); 

	    	String finalPrompt = prompt.toString();
	        GenerateContentResponse response = client.models.generateContent("gemini-2.0-flash-001", finalPrompt, null);
	        
	        return response.text();
	        
	    } catch (IOException | HttpException e) {
	        return "Error: " + e.getMessage();
	    }
	}
	
	public String generateResponse(String query,String prompt1,String name,int id) {
		Client client=new Client.Builder().apiKey(API_KEY).build();
		try {
			Setting s=setting.findByUserId(id).orElseThrow(() -> new RuntimeException("Campaign lead not found"));
			String prompt = "Write a personalized Sales outreach Cold email to the lead "+ name +"for our product.\n" +
					"You should understand the given information about the company of the lead/prospect and figure out how our product\n" +
					"will help them with their business with help of informations provided just like a sales development representative.\n" +
					"Find a pain point and provide viable solution to the recipient empathetically.\n\n" +
					"Use only the information from the lead/company summary provided at the end of this prompt. Follow these rules and guides:\n\n" +
					"Subject Line should be Action-oriented with max 8 words.\n" +
					"Opening of the email will be Personalized greeting if contact name exists, else neutral.\n" +
					"Clearly state why you're reaching out.\n" +
					"The painpoints we are solving for a company are " + s.getUsp() + ".\n" +
					"From the adressed painpoints as provided by Use 1-2 differentiators.\n" +
					"To stay Relevance Connect to their products/industry if data exists.\n" +
					"Mention notable clients/metrics if available to validate our social presence.\n" +
					"At the end of email include a simple, low-pressure Call to action with aim to set up a call, demo, discussion.\n" +
					"Email closing and Signature data is given as \n" + 
					s.getFullName() + "\n"+
					s.getJobTitle() + "\n"+
					s.getCompanyName() + ".\n\n" +
					"Use EXACT product names and metrics from the source.\n" +
					"Prioritize their most repeated phrases from 'Content Patterns'.\n" +
					"Keep to 2 concise paragraphs with maximum 70 words total.\n" +
					"If there is no product with the lead try relating with company mission.\n" +
					"If no metrics try Highlight unique features instead.\n" +
					"Do not use jargon language.\n\n" +
					"While generating the email make sure that it does not contain any words that will be marked as spam, example spam words are\n" +
					"'automation, Bots, money, income, free, best, bonus, risk, win, limited time, warranty'.\n" +
					"The best subject line will be something personalized from the information about the lead.\n\n" +
					"The information about our product to be sold via cold email: " + prompt1 + "\n\n" +
					"Here is the detailed summary about the Lead and their company: " + query + ";";



			GenerateContentResponse response=client.models.generateContent("gemini-2.0-flash-001", prompt, null);
			return response.text();
			
		}catch(IOException |HttpException e){
			return e.getMessage();
		}
		
	}

	public String generateEmail(String company, String website, String siteText) {
		Client client = new Client.Builder().apiKey(API_KEY).build();
		try {
			String prompt = "Based on the company's website and content, generate a cold email for " + company +
			                ".\nWebsite: " + website + "\nContent: " + siteText;

			GenerateContentResponse response = client.models.generateContent("gemini-2.0-flash-001", prompt, null);
			return response.text();
		} catch (IOException | HttpException e) {
			return "Error: " + e.getMessage();
		}
	}

}
