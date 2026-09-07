package com.sih.lmpc_compliance.controller;

import com.sih.lmpc_compliance.service.OcrService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

@RestController
@RequestMapping("/api/ocr")
public class OcrController {

    private final OcrService ocrService;

    public OcrController(OcrService ocrService) {
        this.ocrService = ocrService;
    }

    @GetMapping("/extract")
    public Map<String, String> extractText(@RequestParam String imagePath) {
        String extractedText = ocrService.extractText(imagePath);
        return Map.of("imagePath", imagePath, "extractedText", extractedText);
    }
}