package com.example.jobapplicationtracker.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class GeminiService {

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent";

    private final HttpClient httpClient;

    public GeminiService() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    public List<String> generateInterviewQuestions(String companyName, String jobRole) {
        String prompt = String.format("Give me 3 likely interview questions for a %s position at %s. Return as a plain numbered list, nothing else.", jobRole, companyName);
        
        // Escape quotes to prevent JSON syntax errors
        prompt = prompt.replace("\"", "\\\"");

        String requestBody = String.format("{\"contents\": [{\"parts\": [{\"text\": \"%s\"}]}]}", prompt);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(GEMINI_API_URL + "?key=" + geminiApiKey))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 200) {
                return parseQuestionsFromResponse(response.body());
            } else {
                throw new RuntimeException("Failed to call Gemini API: " + response.body());
            }
        } catch (Exception e) {
            throw new RuntimeException("Error during Gemini API call", e);
        }
    }

    private List<String> parseQuestionsFromResponse(String responseBody) {
        // very basic JSON parsing without adding Jackson/Gson tree dependency manually since we just want the text
        // Looks for "text": "actual answer" inside the JSON response
        
        List<String> questions = new ArrayList<>();
        
        Matcher matcher = Pattern.compile("\"text\":\\s*\"([^\"]+)\"").matcher(responseBody);
        
        if (matcher.find()) {
            String textContent = matcher.group(1);
            // Handle escaped newlines from the JSON string response
            textContent = textContent.replace("\\n", "\n");
            
            // Split by lines and clean up the numbers
            String[] lines = textContent.split("\n");
            for (String line : lines) {
                line = line.trim();
                // Match lines starting with "1. ", "2. ", etc
                if (line.matches("^\\d+\\.\\s*.*")) {
                    questions.add(line.replaceFirst("^\\d+\\.\\s*", "").trim());
                }
            }
        }
        
        // Fallback in case the numbered list parsing fails or empty
        if (questions.isEmpty()) {
            questions.add("Could not generate specific questions at this time.");
        }
        
        return questions;
    }
}
