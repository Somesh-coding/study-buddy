package com.studybuddy.model;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record QuizRequest(
        @NotBlank String topic,
        String context,
        @Min(3) @Max(15) int count,
        String difficulty
) {}
