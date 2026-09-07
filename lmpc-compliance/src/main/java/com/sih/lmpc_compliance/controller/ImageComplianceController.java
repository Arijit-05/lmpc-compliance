package com.sih.lmpc_compliance.controller;

import com.sih.lmpc_compliance.dto.ComplianceResponse;
import com.sih.lmpc_compliance.dto.ImageComplianceRequest;
import com.sih.lmpc_compliance.dto.ScanRequest;
import com.sih.lmpc_compliance.entity.Scan;
import com.sih.lmpc_compliance.entity.Violation;
import com.sih.lmpc_compliance.repository.ScanRepository;
import com.sih.lmpc_compliance.repository.ViolationRepository;
import com.sih.lmpc_compliance.service.ComplianceService;
import com.sih.lmpc_compliance.service.FileStorageService;
import com.sih.lmpc_compliance.service.OcrService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/compliance")
@CrossOrigin
public class ImageComplianceController {

    private final OcrService ocrService;
    private final ComplianceService complianceService;
    private final FileStorageService fileStorageService;
    private final ScanRepository scanRepository;
    private final ViolationRepository violationRepository;

    public ImageComplianceController(
            OcrService ocrService,
            ComplianceService complianceService,
            FileStorageService fileStorageService,
            ScanRepository scanRepository,
            ViolationRepository violationRepository
    ) {
        this.ocrService = ocrService;
        this.complianceService = complianceService;
        this.fileStorageService = fileStorageService;
        this.scanRepository = scanRepository;
        this.violationRepository = violationRepository;
    }

    @PostMapping("/analyze-image")
    public ResponseEntity<?> analyzeImage(
            @RequestBody ImageComplianceRequest request
    ) {

        String extractedText =
                ocrService.extractText(request.getImagePath());

        ScanRequest scanRequest = new ScanRequest();
        scanRequest.setProductId(request.getProductId());
        scanRequest.setImagePath(request.getImagePath());
        scanRequest.setLabelText(extractedText);

        ComplianceResponse complianceResponse =
                complianceService.analyze(scanRequest);

        return ResponseEntity.ok(
                new ImageComplianceResponse(
                        extractedText,
                        complianceResponse
                )
        );
    }

    @PostMapping("/upload-and-analyze")
    public ResponseEntity<?> uploadAndAnalyze(
            @RequestParam("file") MultipartFile file,
            @RequestParam("productId") UUID productId
    ) {

        try {
            String imagePath = fileStorageService.saveFile(file);

            String extractedText = ocrService.extractText(imagePath);

            ScanRequest scanRequest = new ScanRequest();
            scanRequest.setProductId(productId);
            scanRequest.setImagePath(imagePath);
            scanRequest.setLabelText(extractedText);

            ComplianceResponse complianceResponse =
                    complianceService.analyze(scanRequest);

            Scan scan = scanRepository.findByImagePath(imagePath)
                    .orElseThrow(() -> new RuntimeException("Scan not found"));

            List<Violation> violations = violationRepository.findByScanId(scan.getId());

            return ResponseEntity.ok(
                    new ComprehensiveComplianceReport(
                            scan.getId(),
                            productId,
                            imagePath,
                            extractedText,
                            complianceResponse,
                            violations
                    )
            );

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(new ErrorResponse("Processing failed: " + e.getMessage()));
        }
    }

    @GetMapping("/report/{scanId}")
    public ResponseEntity<?> getComplianceReport(@PathVariable UUID scanId) {
        try {
            Scan scan = scanRepository.findById(scanId)
                    .orElseThrow(() -> new RuntimeException("Scan not found"));

            List<Violation> violations = violationRepository.findByScanId(scanId);

            String extractedText = "";
            try {
                extractedText = ocrService.extractText(scan.getImagePath());
            } catch (Exception e) {
                extractedText = "OCR extraction failed for saved image";
            }

            ComplianceResponse complianceResponse = new ComplianceResponse(
                    scan.getStatus().name(),
                    calculateComplianceScore(violations),
                    List.of(),
                    List.of(),
                    violations.stream().map(Violation::getDescription).toList()
            );

            return ResponseEntity.ok(
                    new ComprehensiveComplianceReport(
                            scan.getId(),
                            scan.getProductId(),
                            scan.getImagePath(),
                            extractedText,
                            complianceResponse,
                            violations
                    )
            );
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(new ErrorResponse("Report generation failed: " + e.getMessage()));
        }
    }

    private int calculateComplianceScore(List<Violation> violations) {
        if (violations.isEmpty()) return 100;
        int totalChecks = 7;
        int passedChecks = totalChecks - violations.size();
        return (passedChecks * 100) / totalChecks;
    }

    public static class ImageComplianceResponse {
        private String extractedText;
        private ComplianceResponse compliance;

        public ImageComplianceResponse(String extractedText, ComplianceResponse compliance) {
            this.extractedText = extractedText;
            this.compliance = compliance;
        }

        public String getExtractedText() {
            return extractedText;
        }

        public ComplianceResponse getCompliance() {
            return compliance;
        }
    }

    public static class ComprehensiveComplianceReport {
        private UUID scanId;
        private UUID productId;
        private String imagePath;
        private String extractedText;
        private ComplianceResponse compliance;
        private List<Violation> violations;

        public ComprehensiveComplianceReport(
                UUID scanId,
                UUID productId,
                String imagePath,
                String extractedText,
                ComplianceResponse compliance,
                List<Violation> violations
        ) {
            this.scanId = scanId;
            this.productId = productId;
            this.imagePath = imagePath;
            this.extractedText = extractedText;
            this.compliance = compliance;
            this.violations = violations;
        }

        public UUID getScanId() {
            return scanId;
        }

        public UUID getProductId() {
            return productId;
        }

        public String getImagePath() {
            return imagePath;
        }

        public String getExtractedText() {
            return extractedText;
        }

        public ComplianceResponse getCompliance() {
            return compliance;
        }

        public List<Violation> getViolations() {
            return violations;
        }
    }

    public static class ErrorResponse {
        private String error;

        public ErrorResponse(String error) {
            this.error = error;
        }

        public String getError() {
            return error;
        }
    }
}