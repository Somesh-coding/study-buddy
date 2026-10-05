package com.studybuddy.model;

import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record ChatRequest(
        @NotBlank String question,
        String context,
        List<ChatMessage> history
) {}
