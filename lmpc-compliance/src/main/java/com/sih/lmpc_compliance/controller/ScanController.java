package com.sih.lmpc_compliance.controller;

import com.sih.lmpc_compliance.dto.ComplianceResponse;
import com.sih.lmpc_compliance.dto.ScanRequest;
import com.sih.lmpc_compliance.entity.Scan;
import com.sih.lmpc_compliance.entity.Violation;
import com.sih.lmpc_compliance.repository.ScanRepository;
import com.sih.lmpc_compliance.repository.ViolationRepository;
import com.sih.lmpc_compliance.service.ComplianceService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/scans")
public class ScanController {
    private final ComplianceService complianceService;
    private final ScanRepository scanRepository;
    private final ViolationRepository violationRepository;

    public ScanController(
            ComplianceService complianceService,
            ScanRepository scanRepository,
            ViolationRepository violationRepository
    ) {
        this.complianceService = complianceService;
        this.scanRepository = scanRepository;
        this.violationRepository = violationRepository;
    }


    // ANALYZE PRODUCT LABEL
    @PostMapping("/analyze")
    public ComplianceResponse analyze(@RequestBody ScanRequest request) {
        return complianceService.analyze(request);
    }


    // GET ALL SCANS
    @GetMapping
    public List<Scan> getAllScans() {
        return scanRepository.findAll();
    }

    // GET SCANS FOR A PRODUCT
    @GetMapping("/product/{productId}")
    public List<Scan> getScansByProduct(@PathVariable UUID productId) {
        return scanRepository.findByProductId(productId);
    }

    // GET VIOLATIONS FOR A SCAN
    @GetMapping("/{scanId}/violations")
    public List<Violation> getViolations(@PathVariable UUID scanId) {
        return violationRepository.findByScanId(scanId);
    }
}