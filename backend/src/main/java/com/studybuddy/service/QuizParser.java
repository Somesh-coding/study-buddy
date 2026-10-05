package com.studybuddy.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.studybuddy.model.QuizResponse;
import org.springframework.stereotype.Service;

@Service
public class QuizParser {
    private final ObjectMapper mapper;
    public QuizParser(ObjectMapper mapper) { this.mapper = mapper; }

    public QuizResponse parse(String raw) {
        try {
            String json = raw.trim();
            if (json.startsWith("```")) json = json.replaceFirst("^```(?:json)?\\s*", "").replaceFirst("\\s*```$", "");
            int start = json.indexOf('{');
            int end = json.lastIndexOf('}');
            if (start >= 0 && end > start) json = json.substring(start, end + 1);
            return mapper.readValue(json, QuizResponse.class);
        } catch (Exception ex) {
            throw new IllegalStateException("The AI returned an invalid quiz. Please try again.", ex);
        }
    }
}
