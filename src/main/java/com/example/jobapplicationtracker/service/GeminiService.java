package com.example.jobapplicationtracker.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Service
public class GeminiService {

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    private static final String GEMINI_API_URL ="https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash-latest:generateContent";

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public GeminiService() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(15))
                .build();
        this.objectMapper = new ObjectMapper();
    }

    public List<String> generateInterviewQuestions(String companyName, String jobRole) {
        String prompt = String.format(
            "Give me exactly 3 interview questions for a %s position at %s. " +
            "Return ONLY a numbered list like this format:\n1. question\n2. question\n3. question",
            jobRole, companyName
        );

        String requestBody = buildRequestBody(prompt);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(GEMINI_API_URL + "?key=" + geminiApiKey))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request,
                HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                return parseQuestions(response.body());
            } else {
                System.err.println("Gemini API error: " + response.body());
                throw new RuntimeException("Gemini API returned status: " + response.statusCode());
            }
        } catch (Exception e) {
            System.err.println("Gemini call failed: " + e.getMessage());
            throw new RuntimeException("Error calling Gemini API", e);
        }
    }

    private String buildRequestBody(String prompt) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            // Build JSON safely using Jackson — no manual string escaping
            com.fasterxml.jackson.databind.node.ObjectNode root = mapper.createObjectNode();
            com.fasterxml.jackson.databind.node.ArrayNode contents = mapper.createArrayNode();
            com.fasterxml.jackson.databind.node.ObjectNode content = mapper.createObjectNode();
            com.fasterxml.jackson.databind.node.ArrayNode parts = mapper.createArrayNode();
            com.fasterxml.jackson.databind.node.ObjectNode part = mapper.createObjectNode();
            part.put("text", prompt);
            parts.add(part);
            content.set("parts", parts);
            contents.add(content);
            root.set("contents", contents);
            return mapper.writeValueAsString(root);
        } catch (Exception e) {
            throw new RuntimeException("Failed to build request body", e);
        }
    }

    private List<String> parseQuestions(String responseBody) {
        List<String> questions = new ArrayList<>();
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            // Navigate: candidates[0].content.parts[0].text
            String text = root
                .path("candidates").get(0)
                .path("content")
                .path("parts").get(0)
                .path("text")
                .asText("");

            System.out.println("Gemini raw text: " + text);

            // Split by lines and extract numbered items
            String[] lines = text.split("\n");
            for (String line : lines) {
                line = line.trim()
                           .replaceAll("\\*+", "") // remove bold asterisks
                           .trim();
                if (line.matches("^\\d+[.)\\s].*")) {
                    String question = line.replaceFirst("^\\d+[.)\\s]+", "").trim();
                    if (!question.isEmpty()) {
                        questions.add(question);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to parse Gemini response: " + e.getMessage());
        }

        if (questions.isEmpty()) {
            questions.add("What experience do you have relevant to this role?");
            questions.add("How do you approach problem-solving in a team environment?");
            questions.add("Where do you see yourself growing in this position?");
        }

        return questions;
    }
}