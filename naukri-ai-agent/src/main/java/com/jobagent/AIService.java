package com.jobagent;

import okhttp3.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;

public class AIService {
    private static final String API_KEY = "YOUR_API_KEY_HERE"; // Replace with your OpenAI / Gemini API key
    private static final OkHttpClient client = new OkHttpClient();
    private static final ObjectMapper mapper = new ObjectMapper();

    public static int evaluateMatch(String jobTitle, String company, String jobDesc, String profileJson) {
        // If API Key is not set yet, perform local heuristic match scoring based on resume skills
        if (API_KEY.equals("YOUR_API_KEY_HERE")) {
            return performLocalHeuristicScore(jobTitle, jobDesc);
        }

        try {
            String prompt = String.format(
                "Compare this user profile: %s with this Job: Title: %s, Company: %s, Description: %s. " +
                "Evaluate skill match for Java, Selenium, API testing, Karate, REST Assured, BDD, and AI tools. " +
                "Respond ONLY with a valid JSON containing 'match_score' (0 to 100) and 'summary'.",
                profileJson, jobTitle, company, jobDesc
            );

            MediaType mediaType = MediaType.parse("application/json");
            String jsonPayload = String.format(
                "{\"model\":\"gpt-4o-mini\",\"messages\":[{\"role\":\"user\",\"content\":\"%s\"}]}",
                prompt.replace("\"", "\\\"").replace("\n", " ")
            );

            RequestBody body = RequestBody.create(jsonPayload, mediaType);
            Request request = new Request.Builder()
                    .url("https://api.openai.com/v1/chat/completions")
                    .post(body)
                    .addHeader("Authorization", "Bearer " + API_KEY)
                    .addHeader("Content-Type", "application/json")
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful() && response.body() != null) {
                    JsonNode root = mapper.readTree(response.body().string());
                    String content = root.path("choices").get(0).path("message").path("content").asText();
                    JsonNode resultNode = mapper.readTree(content);
                    return resultNode.path("match_score").asInt(0);
                }
            }
        } catch (IOException e) {
            System.err.println("AI Service Error: " + e.getMessage());
        }

        return performLocalHeuristicScore(jobTitle, jobDesc);
    }

    private static int performLocalHeuristicScore(String jobTitle, String jobDesc) {
        int score = 50; // base score
        String text = (jobTitle + " " + jobDesc).toLowerCase();

        if (text.contains("cypress") || text.contains("tosca")) return 30; // lower priority for tools outside core resume
        if (text.contains("automation") || text.contains("tester")) score += 20;
        if (text.contains("java")) score += 10;
        if (text.contains("selenium")) score += 10;
        if (text.contains("api") || text.contains("karate") || text.contains("rest assured")) score += 10;

        return Math.min(score, 100);
    }
}