package com.studybuddy.service;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
public class PdfService {
    public String extractText(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) throw new IllegalArgumentException("Please choose a PDF file.");
        if (!"application/pdf".equalsIgnoreCase(file.getContentType()) && !file.getOriginalFilename().toLowerCase().endsWith(".pdf")) {
            throw new IllegalArgumentException("Only PDF files are supported.");
        }
        try (var document = Loader.loadPDF(file.getBytes())) {
            return new PDFTextStripper().getText(document).trim();
        }
    }
}
