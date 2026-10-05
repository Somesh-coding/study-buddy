package com.studybuddy.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class HuggingFaceService {
    private final RestClient client;
    private final ObjectMapper mapper;
    private final String token;
    private final String model;

    public HuggingFaceService(
            RestClient.Builder builder,
            ObjectMapper mapper,
            @Value("${hf.api.url}") String url,
            @Value("${hf.api.token}") String token,
            @Value("${hf.model}") String model) {
        this.client = builder.baseUrl(url).build();
        this.mapper = mapper;
        this.token = token;
        this.model = model;
    }

    public String chat(List<Map<String, String>> messages, double temperature, int maxTokens) {
        if (token == null || token.isBlank()) {
            return "AI is not configured yet. Add HF_TOKEN to the server environment, then try again.";
        }

        Map<String, Object> body = Map.of(
                "model", model,
                "messages", messages,
                "temperature", temperature,
                "max_tokens", maxTokens,
                "stream", false
        );

        String raw = client.post()
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .body(body)
                .retrieve()
                .body(String.class);

        try {
            JsonNode root = mapper.readTree(raw);
            JsonNode content = root.path("choices").path(0).path("message").path("content");
            if (content.isMissingNode()) throw new IllegalStateException("Unexpected AI response: " + raw);
            return content.asText();
        } catch (Exception ex) {
            throw new IllegalStateException("Could not read the AI response.", ex);
        }
    }

    public String studyChat(String question, String context, List<Map<String, String>> history) {
        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of("role", "system", "content", "You are Study Buddy, a patient study tutor. Use the supplied study material when available. Never invent facts from the material. If the answer is not supported by the material, say that clearly and then provide a general explanation only if useful. Explain simply, use headings and bullets, and do not be unnecessarily verbose."));
        if (context != null && !context.isBlank()) {
            messages.add(Map.of("role", "system", "content", "STUDY MATERIAL:\n" + context));
        }
        if (history != null) messages.addAll(history.stream().limit(8).toList());
        messages.add(Map.of("role", "user", "content", question));
        return chat(messages, 0.35, 700);
    }

    public String quiz(String topic, String context, int count, String difficulty) {
        String prompt = "Create exactly " + count + " multiple-choice questions about: " + topic + ". Difficulty: " + difficulty + ". " +
                "Return ONLY valid JSON with this shape: {\"questions\":[{\"question\":\"...\",\"options\":[\"A\",\"B\",\"C\",\"D\"],\"answer\":0,\"explanation\":\"...\"}]}. " +
                "answer is the zero-based index of the correct option. Use the supplied study material as the primary source. " +
                "Do not include markdown fences.";
        if (context != null && !context.isBlank()) prompt += "\nSTUDY MATERIAL:\n" + context;
        return chat(List.of(Map.of("role", "system", "content", "You generate accurate educational quizzes."), Map.of("role", "user", "content", prompt)), 0.45, 1800);
    }

    public String summarize(String context) {
        return chat(List.of(
                Map.of("role", "system", "content", "Summarize study material for a student. Use a short title, key ideas, important definitions, and a final 5-bullet revision checklist."),
                Map.of("role", "user", "content", context)
        ), 0.25, 900);
    }
}
