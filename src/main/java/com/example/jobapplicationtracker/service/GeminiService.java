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

    @Value("${gemini.api.model:gemini-2.5-flash}")
    private String geminiModel;

    private static final String GEMINI_API_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/%s:generateContent";

    private static final List<String> FALLBACK_QUESTIONS = List.of(
        "Tell me about yourself and your background in this field.",
        "How do you approach problem-solving when you face a technical challenge?",
        "Where do you see yourself growing in this role over the next year?"
    );

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public GeminiService() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(15))
                .build();
        this.objectMapper = new ObjectMapper();
    }

    public List<String> generateInterviewQuestions(String companyName, String jobRole) {
        if (geminiApiKey == null || geminiApiKey.isBlank()) {
            System.err.println("Gemini API key is missing. Returning fallback questions.");
            return FALLBACK_QUESTIONS;
        }

        String prompt = String.format(
            "Give me exactly 3 interview questions for a %s position at %s. " +
            "Return ONLY a numbered list like this format:\n1. question\n2. question\n3. question",
            jobRole, companyName
        );

        try {
            String requestBody = buildRequestBody(prompt);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(String.format(GEMINI_API_URL, geminiModel) + "?key=" + geminiApiKey))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request,
                HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                List<String> parsed = parseQuestions(response.body());
                return parsed.isEmpty() ? FALLBACK_QUESTIONS : parsed;
            } else if (response.statusCode() == 429) {
                System.err.println("Gemini API quota exceeded (429). Returning fallback questions.");
                return FALLBACK_QUESTIONS;
            } else {
                String apiError = extractApiError(response.body());
                System.err.println("Gemini API returned status " + response.statusCode() + ": " + apiError);
                return FALLBACK_QUESTIONS;
            }
        } catch (Exception e) {
            System.err.println("Gemini call failed: " + e.getMessage());
            return FALLBACK_QUESTIONS;
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

            // Navigate: candidates[0] → content → parts[0] → text
            JsonNode firstCandidate = root.path("candidates").path(0);
            JsonNode firstPart = firstCandidate.path("content").path("parts").path(0);

            if (firstCandidate.isMissingNode() || firstPart.isMissingNode()) {
                System.err.println("Gemini response missing candidates/parts. Returning fallback.");
                return FALLBACK_QUESTIONS;
            }

            String text = firstPart.path("text").asText("");

            System.out.println("Gemini raw text: " + text);

            if (text.isBlank()) {
                return FALLBACK_QUESTIONS;
            }

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
            return FALLBACK_QUESTIONS;
        }

        return questions.isEmpty() ? FALLBACK_QUESTIONS : questions;
    }

    private String extractApiError(String responseBody) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            String message = root.path("error").path("message").asText("");
            if (!message.isBlank()) {
                return message;
            }
        } catch (Exception ignored) {
            // Fall through to the generic message below if the body is not valid JSON.
        }
        return "Unknown Gemini API error";
    }
}
