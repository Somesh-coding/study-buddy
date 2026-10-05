package com.studybuddy.model;

import java.util.List;

public record QuizQuestion(
        String question,
        List<String> options,
        int answer,
        String explanation
) {}
