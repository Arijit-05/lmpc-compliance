package com.sih.lmpc_compliance.service;

import com.sih.lmpc_compliance.dto.ComplianceResponse;
import com.sih.lmpc_compliance.dto.ScanRequest;
import com.sih.lmpc_compliance.entity.*;
import com.sih.lmpc_compliance.repository.ProductRepository;
import com.sih.lmpc_compliance.repository.ScanRepository;
import com.sih.lmpc_compliance.repository.ViolationRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class ComplianceService {

    private final ProductRepository productRepository;
    private final ScanRepository scanRepository;
    private final ViolationRepository violationRepository;

    private static final UUID DEMO_USER_ID =
            UUID.fromString("04245f90-000e-4771-b079-eaa8d7b1c6cc");

    public ComplianceService(
            ProductRepository productRepository,
            ScanRepository scanRepository,
            ViolationRepository violationRepository
    ) {
        this.productRepository = productRepository;
        this.scanRepository = scanRepository;
        this.violationRepository = violationRepository;
    }

    public ComplianceResponse analyze(ScanRequest request) {

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() ->
                        new RuntimeException("Product not found"));

        String text = request.getLabelText();

        if (text == null) {
            text = "";
        }

        text = text.toLowerCase();

        List<String> found = new ArrayList<>();
        List<String> missing = new ArrayList<>();
        List<String> violations = new ArrayList<>();

        // MRP
        boolean hasMrp =
                text.contains("mrp") ||
                        text.contains("maximum retail price");

        checkDeclaration(
                hasMrp,
                "MRP (Maximum Retail Price)",
                "Missing mandatory MRP declaration",
                found, missing, violations
        );

        // NET QUANTITY
        boolean hasNetQuantity =
                text.matches("(?s).*\\d+(\\.\\d+)?\\s*(g|kg|gm|gram|grams|ml|l|litre|liter|pcs|pieces).*");

        checkDeclaration(
                hasNetQuantity,
                "Net Quantity",
                "Missing mandatory net quantity declaration",
                found, missing, violations
        );

        // MANUFACTURER
        boolean hasManufacturer =
                text.contains("manufactured by") ||
                        text.contains("packed by") ||
                        text.contains("imported by");

        checkDeclaration(
                hasManufacturer,
                "Manufacturer / Packer / Importer",
                "Missing manufacturer, packer or importer declaration",
                found, missing, violations
        );

        // GENERIC NAME
        boolean hasGenericName =
                text.contains("product") ||
                        text.contains("shampoo") ||
                        text.contains("soap") ||
                        text.contains("biscuits") ||
                        text.contains("snacks") ||
                        text.contains("commodity");

        checkDeclaration(
                hasGenericName,
                "Generic Name",
                "Common or generic name of commodity not detected",
                found, missing, violations
        );

        // MANUFACTURING DATE
        boolean hasManufacturingDate =
                text.contains("mfg") ||
                        text.contains("manufactured") ||
                        text.contains("mfd") ||
                        text.contains("manufacturing date");

        checkDeclaration(
                hasManufacturingDate,
                "Month and Year of Manufacture",
                "Manufacturing month/year not detected",
                found, missing, violations
        );

        // CONSUMER CARE
        boolean hasConsumerCare =
                text.contains("consumer care") ||
                        text.contains("customer care") ||
                        text.contains("complaint") ||
                        text.contains("helpline");

        checkDeclaration(
                hasConsumerCare,
                "Consumer Care Details",
                "Consumer care/contact information not detected",
                found, missing, violations
        );

        // COUNTRY OF ORIGIN FOR IMPORTED PRODUCTS
        if (product.isImported()) {

            boolean hasCountryOfOrigin =
                    text.contains("country of origin") ||
                            text.contains("made in");

            checkDeclaration(
                    hasCountryOfOrigin,
                    "Country of Origin",
                    "Imported product is missing country of origin",
                    found, missing, violations
            );
        }

        int totalChecks = found.size() + missing.size();

        int score = totalChecks == 0
                ? 0
                : (found.size() * 100) / totalChecks;

        String status = missing.isEmpty()
                ? "COMPLIANT"
                : "NON_COMPLIANT";

        Scan scan = Scan.builder()
                .productId(product.getId())
                .uploadedBy(DEMO_USER_ID)
                .imagePath(request.getImagePath() != null ? request.getImagePath() : "uploads/no-image.jpg")
                .status(ScanStatus.completed)
                .build();

        scan = scanRepository.save(scan);

        for (String violationDescription : violations) {

            Violation violation = Violation.builder()
                    .scanId(scan.getId())
                    .ruleRef("Legal Metrology Rules, 2011")
                    .severity(ViolationSeverity.major)
                    .description(violationDescription)
                    .build();

            violationRepository.save(violation);
        }


        return new ComplianceResponse(
                status,
                score,
                found,
                missing,
                violations
        );
    }

    private void checkDeclaration(
            boolean present,
            String declaration,
            String violation,
            List<String> found,
            List<String> missing,
            List<String> violations
    ) {

        if (present) {
            found.add(declaration);
        } else {
            missing.add(declaration);
            violations.add(violation);
        }
    }
}