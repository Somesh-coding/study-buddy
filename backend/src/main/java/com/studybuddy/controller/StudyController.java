package com.studybuddy.controller;

import com.studybuddy.model.*;
import com.studybuddy.service.*;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class StudyController {
    private final PdfService pdfService;
    private final ContextRanker ranker;
    private final HuggingFaceService ai;
    private final QuizParser quizParser;

    public StudyController(PdfService pdfService, ContextRanker ranker, HuggingFaceService ai, QuizParser quizParser) {
        this.pdfService = pdfService;
        this.ranker = ranker;
        this.ai = ai;
        this.quizParser = quizParser;
    }

    @GetMapping("/health")
    public Map<String, String> health() { return Map.of("status", "ok", "app", "study-buddy"); }

    @PostMapping(value = "/extract", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Map<String, String> extract(@RequestPart("file") MultipartFile file) throws IOException {
        return Map.of("title", file.getOriginalFilename() == null ? "Imported PDF" : file.getOriginalFilename(), "text", pdfService.extractText(file));
    }

    @PostMapping("/chat")
    public Map<String, String> chat(@Valid @RequestBody ChatRequest request) {
        String context = ranker.relevantContext(request.context(), request.question());
        List<Map<String, String>> history = request.history() == null ? List.of() : request.history().stream().map(m -> Map.of("role", m.role(), "content", m.content())).toList();
        return Map.of("answer", ai.studyChat(request.question(), context, history));
    }

    @PostMapping("/quiz")
    public QuizResponse quiz(@Valid @RequestBody QuizRequest request) {
        String context = ranker.relevantContext(request.context(), request.topic());
        return quizParser.parse(ai.quiz(request.topic(), context, request.count(), request.difficulty()));
    }

    @PostMapping("/summarize")
    public Map<String, String> summarize(@Valid @RequestBody SummarizeRequest request) {
        String context = request.context().length() > 12000 ? request.context().substring(0, 12000) : request.context();
        return Map.of("summary", ai.summarize(context));
    }

    @ExceptionHandler(Exception.class)
    public org.springframework.http.ResponseEntity<ApiResponse> handle(Exception ex) {
        return org.springframework.http.ResponseEntity.badRequest().body(new ApiResponse(ex.getMessage() == null ? "Something went wrong." : ex.getMessage()));
    }
}
