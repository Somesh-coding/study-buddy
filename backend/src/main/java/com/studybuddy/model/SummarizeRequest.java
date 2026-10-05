package com.studybuddy.model;

import jakarta.validation.constraints.NotBlank;

public record SummarizeRequest(@NotBlank String context) {}
