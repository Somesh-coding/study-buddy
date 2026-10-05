package com.studybuddy.service;

import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ContextRanker {
    private static final int CHUNK_SIZE = 1800;
    private static final int MAX_CONTEXT = 12000;

    public String relevantContext(String document, String query) {
        if (document == null || document.isBlank()) return "";
        if (document.length() <= MAX_CONTEXT) return document;

        List<String> chunks = chunk(document);
        Set<String> terms = Arrays.stream(query.toLowerCase(Locale.ROOT).split("\\W+"))
                .filter(s -> s.length() > 2)
                .collect(Collectors.toSet());

        return chunks.stream()
                .map(c -> Map.entry(c, score(c, terms)))
                .sorted((a, b) -> Integer.compare(b.getValue(), a.getValue()))
                .limit(6)
                .map(Map.Entry::getKey)
                .collect(Collectors.joining("\n\n--- SOURCE CHUNK ---\n\n"));
    }

    private int score(String chunk, Set<String> terms) {
        String lower = chunk.toLowerCase(Locale.ROOT);
        int score = 0;
        for (String term : terms) {
            int from = 0;
            while ((from = lower.indexOf(term, from)) >= 0) {
                score++;
                from += term.length();
            }
        }
        return score;
    }

    private List<String> chunk(String text) {
        List<String> result = new ArrayList<>();
        for (int i = 0; i < text.length(); i += CHUNK_SIZE) {
            result.add(text.substring(i, Math.min(i + CHUNK_SIZE, text.length())));
        }
        return result;
    }
}
